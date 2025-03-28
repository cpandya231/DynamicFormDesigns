package com.swiftiq.logever.service;

import com.swiftiq.logever.main.entity.PendingEntry;
import com.swiftiq.logever.builder.entity.State;
import com.swiftiq.logever.kraftos.entity.User;
import com.swiftiq.logever.model.*;
import com.swiftiq.logever.builder.entity.Form;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FormDataService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    PendingEntryService pendingEntryService;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    FormService formService;

    @org.springframework.transaction.annotation.Transactional(transactionManager = "logeverTransactionManager")
    public void bulkInsert(User user, Form form, ArrayList<Map<String,String>> records) throws Exception{
        records.forEach(record-> {
            insertIntoWithPendingEntries(user,form,record);
        });
    }

    @org.springframework.transaction.annotation.Transactional(transactionManager = "logeverTransactionManager")
    public int insertIntoWithPendingEntries(User user, Form form, Map<String, String> values) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> connection.prepareStatement(form.makeInsertValuesStmt(values), new String[]{"id"}), keyHolder);
        var insertedId = keyHolder.getKey().intValue();
        values.put("log_entry_id", insertedId + "");
        jdbcTemplate.execute(form.makeInsertMetadataValuesStmt(values));
        values.remove("log_entry_id");
        values.put("id", insertedId + "");
        if (values.get("endState").equalsIgnoreCase("true") && form.getType()!=null && form.getType().equalsIgnoreCase("master")) {
            System.out.println("Executing master inserts");
            jdbcTemplate.execute(form.makeInsertMasterValuesStmt(values));
        }
        if(!values.get("endState").equalsIgnoreCase("true")) {
            State nextState = form.getWorkflow().getStates().stream().filter(st-> st.getName().equals(values.get("state"))).findFirst().get();
            List<PendingEntry> pendingEntries = new ArrayList<>();
            formService.getRolesForState(nextState).forEach(r->{
                formService.getDepartmentsForState(nextState).forEach(d-> {
                    if(d.getName().equalsIgnoreCase("Initiator Department")) {
                        departmentService.getAllUnder(user.getDepartment()).forEach(aD-> {
                            pendingEntries.add(new PendingEntry(form.getId(),insertedId,null,r.getId(),aD.getId(),values.get("created_by"),aD.getHod()));
                        });
                    } else {
                        departmentService.getAllUnder(d).forEach(aD-> {
                            pendingEntries.add(new PendingEntry(form.getId(),insertedId,null,r.getId(),aD.getId()
                                    ,values.get("created_by"), aD.getHod()));
                        });
                    }
                });
            });

            pendingEntryService.saveAll(pendingEntries);
        }
        return insertedId;
    }

    @org.springframework.transaction.annotation.Transactional(transactionManager = "logeverTransactionManager")
    public Entry insertInto(Form form, Map<String, String> values, MultipartFile[] files) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> connection.prepareStatement(form.makeInsertValuesStmt(values), new String[]{"id"}), keyHolder);
        var insertedId = keyHolder.getKey().intValue();
        values.put("log_entry_id", insertedId + "");
//        jdbcTemplate.execute(form.makeInsertMetadataValuesStmt(values));
        jdbcTemplate.update(
                connection -> connection.prepareStatement(form.makeInsertMetadataValuesStmt(values), new String[]{"id"}), keyHolder);
        var insertedHistoryId = keyHolder.getKey().intValue();
        values.remove("log_entry_id");
        values.put("id", insertedId + "");
        if (values.get("endState").equalsIgnoreCase("true") && form.getType()!=null && form.getType().equalsIgnoreCase("master")) {
            jdbcTemplate.execute(form.makeInsertMasterValuesStmt(values));
        }
        String tableName = "f_"+String.join("_",form.getName().trim().toLowerCase().split(" "))+"_lgs";
        String upadteLogStatsStmt = "INSERT into log_stats (form_id, state, count) SELECT "+form.getId()+", state, count(*) from "+tableName+" group by state ON " +
                "DUPLICATE KEY UPDATE count = VALUES(count)";
        jdbcTemplate.execute(upadteLogStatsStmt);

        System.out.println(upadteLogStatsStmt);

        if(files!=null)
            Arrays.asList(files).stream().forEach(file-> {
                fileStorageService.save(file, form.getId()+"/"+insertedId+"_"+insertedHistoryId+"/"+file.getOriginalFilename());
            });

        return new Entry(insertedId,insertedHistoryId);
    }

    @org.springframework.transaction.annotation.Transactional(transactionManager = "logeverTransactionManager")
    public void insertInto(Form form, ArrayList<GridLogEntry> gridValues, Entry entry) {
        if(form.getGrids().size() > 0) {
            gridValues.forEach(gridValue-> {
                if(gridValue.getData()!=null) gridValue.getData().forEach(values-> {
                    values.put("log_entry_id", entry.getEntryId()+"");
                    KeyHolder keyHolder = new GeneratedKeyHolder();
                    jdbcTemplate.update(
                            connection -> connection.prepareStatement(form.makeGridInsertValuesStmt(gridValue.getName(),values), new String[]{"id"}), keyHolder);
                    var insertedId = keyHolder.getKey().intValue();
                    values.put("grid_entry_id", insertedId + "");
                    values.put("history_log_entry_id",entry.getHistoryEntryId()+"");
                    jdbcTemplate.execute(form.makeGridInsertMetadataValuesStmt(gridValue.getName(),values));
                });
            });
        }
    }

    @org.springframework.transaction.annotation.Transactional(transactionManager = "logeverTransactionManager")
    public Entry update(Form form, Map<String, String> values, MultipartFile[] files) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> connection.prepareStatement(form.makeUpdateStmt(values), new String[]{"id"}), keyHolder);
        var insertedId = Integer.parseInt(values.get("id"));
        values.put("created_by", values.get("updated_by"));

        jdbcTemplate.update(
                connection -> connection.prepareStatement(form.makeInsertMetadataValuesStmt(values), new String[]{"id"}), keyHolder);
        var insertedHistoryId = keyHolder.getKey().intValue();
        values.remove("log_entry_id");
        if (values.get("endState").equalsIgnoreCase("true") && form.getType().equalsIgnoreCase("master")) {
            jdbcTemplate.execute(form.makeUpdateMasterStmt(values));
        } else if(values.get("endState").equalsIgnoreCase("false") && form.getType()!=null && form.getType().equalsIgnoreCase("master")) {
            jdbcTemplate.execute(form.makeDeleteMasterDataStmt(values));
        }

        if(files!=null)
            Arrays.asList(files).stream().forEach(file-> {
                fileStorageService.save(file, form.getId()+"/"+insertedId+"_"+insertedHistoryId+"/"+file.getOriginalFilename());
            });


        return new Entry(insertedId,insertedHistoryId);
    }

    @org.springframework.transaction.annotation.Transactional(transactionManager = "logeverTransactionManager")
    public void update(Form form, ArrayList<GridLogEntry> gridValues, Entry entry) {
        if(form.getGrids().size() > 0) {
            gridValues.forEach(gridValue-> {
                jdbcTemplate.update(
                        connection -> connection.prepareStatement(form.makeGridDeleteValuesStmt(gridValue.getName(), entry.getEntryId())));
                gridValue.getData().forEach(values-> {
                    values.put("log_entry_id", entry.getEntryId()+"");
                    values.put("history_log_entry_id",entry.getHistoryEntryId()+"");
                    KeyHolder keyHolder = new GeneratedKeyHolder();
                    var insertedId = 0;
                    jdbcTemplate.update(
                            connection -> connection.prepareStatement(form.makeGridInsertValuesStmt(gridValue.getName(), values), new String[]{"id"}), keyHolder);
                    insertedId = keyHolder.getKey().intValue();

                    values.put("grid_entry_id", insertedId + "");
                    values.put("history_log_id",entry.getHistoryEntryId()+"");
                    jdbcTemplate.execute(form.makeGridInsertMetadataValuesStmt(gridValue.getName(),values));
                });
            });
        }
    }

    public List<HashMap> getAllPendingFor(Form form, User user) {
        String userRoles = user.getRoles().stream().map(r->r.getId()+"").collect(Collectors.joining(","));
        Integer userDept = user.getDepartment().getId();

        Table table = new Table();
        table.setName(form.getName());
        Table metaTable = new Table();
        metaTable.setName(form.getMetadataTableName());
        String gridCols = form.getGrids().stream().flatMap(f->f.stream().map(grid->grid.getKey())).collect(Collectors.joining(","));
        String selectCols = "l.id," + Arrays.stream(form.getColumns().split(",")).filter(c-> Arrays.stream(gridCols.split(",")).filter(gc->gc.equalsIgnoreCase(c)).count() == 0).map(s->"l."+s).collect(Collectors.joining(",")) + ",l.state,l.log_create_dt,l.created_by,l.log_update_dt,l.updated_by";
        String[] sCArray = Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
        String selectStmt = "SELECT " + selectCols + " from " + table.getName() + " l inner join kraftos.notification p on l.id=p.subPrimaryId and p.primaryId='"+form.getId()+"' where (p.role in ("+userRoles
                +") and p.department="+userDept+" and p.createdBy <> '"+ user.getUsername() +"') or p.user = '"+user.getUsername()+"'";
        System.out.println(selectStmt);

        return jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> {
                    HashMap<String, String> data = new HashMap<>();
                    Arrays.stream(sCArray).forEach(col -> {
                        try {
                            data.put(col, resultSet.getString(col));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                    return data;
                });
    }

    public DataQuery getAllFor(Form form, int entryId, boolean filterByUsername, boolean filterByDepartment,
                                     int pageNumber, int pageSize, List<Filter> filters) {
        Table table = new Table();
        table.setName(form.getName());
        Table metaTable = new Table();
        metaTable.setName(form.getMetadataTableName());
        String gridCols = form.getGrids().stream().flatMap(f->f.stream().map(grid->grid.getKey())).collect(Collectors.joining(","));

        if (entryId != -1) {
            String selectCols = "distinct l.id," + Arrays.stream(form.getColumns().split(",")).filter(c-> Arrays.stream(gridCols.split(",")).filter(gc->gc.equalsIgnoreCase(c)).count() == 0).map(s->"l."+s).collect(Collectors.joining(",")) + ",l.state,l.log_create_dt,l.created_by,l.log_update_dt,l.updated_by";
//            String[] sCArray = Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
            String selectStmt = "SELECT "+selectCols+" FROM (SELECT " + selectCols + " from " + table.getName() + " limit "+pageSize+" offset "+pageNumber*pageSize+") l inner join "+metaTable.getName()+" h on l.id=h.log_entry_id";
            selectStmt += " WHERE id=" + entryId;
            System.out.println(selectStmt);
            return new DataQuery(jdbcTemplate.queryForList(selectStmt), 12);
        }
        else if (filterByUsername) {
            filters.add(new Filter("1", "1"));
            String selectCols = "l.id," + Arrays.stream(form.getColumns().split(",")).filter(c-> Arrays.stream(gridCols.split(",")).filter(gc->gc.equalsIgnoreCase(c)).count() == 0).map(s->"l."+s).collect(Collectors.joining(",")) + ",l.state,l.log_create_dt,l.created_by,l.log_update_dt,l.updated_by";
            String selectStmt = "SELECT "+selectCols+" FROM "+table.getName() +" l where l.id in (SELECT distinct log_entry_id from "+metaTable.getName()+" h where "+filters.stream().map(f->"lower("+f.getId()+") like '%"+f.getValue().toLowerCase()+"%'").collect(Collectors.joining(" AND ")) + " AND h.created_by = '"+SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()+"') order by l.id desc limit "+pageSize+" offset "+pageNumber*pageSize;
            System.out.println(selectStmt);

            String countQuery = "SELECT count(distinct h.log_entry_id) from "+metaTable.getName()+" h WHERE "+filters.stream().map(f->"lower("+f.getId()+") like '%"+f.getValue().toLowerCase()+"%'").collect(Collectors.joining(" AND "))+" AND h.created_by = '"+SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()+"'";
            int totalRows = jdbcTemplate.queryForObject(countQuery, Integer.class);

            return new DataQuery(jdbcTemplate.queryForList(selectStmt), totalRows);
//            return new DataQuery(jdbcTemplate.query(selectStmt,
//                    (resultSet, rowNum) -> {
//                        HashMap<String, String> data = new HashMap<>();
//                        Arrays.stream(sCArray).forEach(col -> {
//                            try {
//                                data.put(col, resultSet.getString(col));
//                            } catch (Exception ex) {
//                                ex.printStackTrace();
//                            }
//                        });
//                        return data;
//                    }),totalRows);
//            new DataQuery(resultSet, Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",")))
        }
        else {
            filters.add(new Filter("1", "1"));
            String selectCols = "l.id," + Arrays.stream(form.getColumns().split(",")).filter(c-> Arrays.stream(gridCols.split(",")).filter(gc->gc.equalsIgnoreCase(c)).count() == 0).map(s->"l."+s).collect(Collectors.joining(",")) + ",l.state,l.log_create_dt,l.created_by,l.log_update_dt,l.updated_by";
//            String[] sCArray = Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
            String selectStmt = "SELECT " + selectCols + " from " + table.getName() + " l where "+filters.stream().map(f->"lower("+f.getId()+") like '%"+f.getValue().toLowerCase()+"%'").collect(Collectors.joining(" AND ")) + " limit "+pageSize+" offset "+pageNumber*pageSize;
            System.out.println(selectStmt);
            String countQuery = "SELECT count(*) from "+table.getName()+" where "+filters.stream().map(f->"lower("+f.getId()+") like '%"+f.getValue().toLowerCase()+"%'").collect(Collectors.joining(" AND "));
            int totalRows = jdbcTemplate.queryForObject(countQuery, Integer.class);

            return new DataQuery(jdbcTemplate.queryForList(selectStmt), totalRows);
//            new DataQuery(resultSet, Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(","))
        }

    }

    public DataQuery getLogEntryMetadata(Form form, int entryId) {
        Table table = new Table();
        table.setName(form.getMetadataTableName());
        String gridColumns = form.getGrids().stream().map(grid->grid.get(0).getKey()).collect(Collectors.joining(","));
        String queryColumns = Arrays.stream(form.getColumns().split(",")).filter(col-> Arrays.stream(gridColumns.split(",")).filter(c->c.equalsIgnoreCase(col)).count()==0).collect(Collectors.joining(","));
        System.out.println(gridColumns);
        System.out.println(queryColumns);
        String selectCols = "id," + queryColumns + ",log_entry_id,state,log_create_dt,created_by,comment";
//        String[] sCArray = Arrays.stream(selectCols.split(",")).collect(Collectors.joining(",")).split(",");
        String selectStmt = "SELECT " + selectCols + " from " + table.getName() + " WHERE log_entry_id=" + entryId + " ORDER BY log_create_dt desc";
        return new DataQuery(jdbcTemplate.queryForList(selectStmt),12);
//        return jdbcTemplate.query(selectStmt,
//                (resultSet, rowNum) -> new DataQuery(resultSet, selectCols.split(",")));
    }

    public ArrayList<GridDataQuery> getLogEntryGridMetadata(Form form, int entryId) {
        Table table = new Table();
        Table auditTable = new Table();
        ArrayList<GridDataQuery> grids = new ArrayList<>();
        if(form.getGrids().size() > 0) {
            form.getGrids().stream().forEach(gridCtrl -> {
                table.setName(form.getMetadataTableName());
                auditTable.setGridAuditTableName(form.getName()+" "+gridCtrl.get(0).getKey());
                String columns = gridCtrl.get(0).getControls().stream().map(ctrl -> ctrl.getKey()).collect(Collectors.joining(","));
                String lbls = gridCtrl.get(0).getControls().stream().map(ctrl -> ctrl.getLabel()).collect(Collectors.joining(","));
                String select = "id,grid_entry_id,history_log_entry_id,log_create_dt," + columns;
                String labels = "ID,Grid Entry ID,History Log Entry ID,Log Create Date," + lbls;
                String gridSelectStmt = "SELECT " + select + " from " + auditTable.getName() + " where history_log_entry_id in (SELECT id from "+table.getName()+" where log_entry_id ='"+entryId+"')";
                System.out.println(gridSelectStmt);
                DataQuery gridDataQuery = new DataQuery(jdbcTemplate.queryForList(gridSelectStmt),12);

//                        jdbcTemplate.query(gridSelectStmt,
//                        (resultSet, rowNum) -> new DataQuery(resultSet, select.split(",")));
                grids.add(new GridDataQuery(gridCtrl.get(0).getKey(), gridCtrl.get(0).getLabel(), gridDataQuery, select, labels));
            });
        }
        return grids;
    }

    public ArrayList<GridDataQuery> getGridsFor(Form form, int logEntryId) {
        Table table = new Table();
        ArrayList<GridDataQuery> grids = new ArrayList<>();
        if(form.getGrids().size() > 0) {
            form.getGrids().stream().forEach(gridCtrl -> {
                table.setGridTableName(form.getName()+" "+gridCtrl.get(0).getKey());
                String columns = gridCtrl.get(0).getControls().stream().map(ctrl -> ctrl.getKey()).collect(Collectors.joining(","));
                String labels = gridCtrl.get(0).getControls().stream().map(ctrl -> ctrl.getLabel()).collect(Collectors.joining(","));
                String select = "id,log_entry_id," + columns;
                String gridSelectStmt = "SELECT " + select + " from " + table.getName() + " where log_entry_id ='"+logEntryId+"'";
                DataQuery gridDataQuery = new DataQuery(jdbcTemplate.queryForList(gridSelectStmt),12);
//                List<DataQuery> gridDataQuery = jdbcTemplate.query(gridSelectStmt,
//                        (resultSet, rowNum) -> new DataQuery(resultSet, select.split(",")));
                grids.add(new GridDataQuery(gridCtrl.get(0).getKey(), gridCtrl.get(0).getLabel(), gridDataQuery, select, "ID,Log Entry ID,"+labels));
            });
        }
        return grids;
    }

    public void saveLogEntryMetadata(Form form, Map<String, String> metaDataValues) {

        jdbcTemplate.execute(form.makeInsertMetadataValuesStmt(metaDataValues));

    }

    public DataQuery getAllFor(Form form, Map<String,String> filter) {
        Table table = new Table();
        table.setName(form.getName());
        String selectCols = "id," + form.getColumns();
        var finalColumns = selectCols;
        String selectStmt = "SELECT " + selectCols + " from " + table.getName() + "";

        ArrayList<String> wheres = new ArrayList<>();

        filter.forEach((col,val)-> {
            wheres.add(col +"="+"'"+val+"'");
        });

        selectStmt += " WHERE 1=1 "+wheres.stream().reduce("",(con1,con2)->con1+" AND "+con2);
        return new DataQuery(jdbcTemplate.queryForList(selectStmt),12);
//        return jdbcTemplate.query(selectStmt,
//                (resultSet, rowNum) -> new DataQuery(resultSet, finalColumns.split(",")));
    }

//    public void updateEntryState(Form masterForm, String masterTableEntryIdColumn, String idColumnValue, String stateColumn, String stateValue) {
//        var result = getAllFor(masterForm, masterTableEntryIdColumn, idColumnValue, stateColumn);
//        if (result.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                    String.format("No data found for Column %s value %s in master table %s", masterTableEntryIdColumn, idColumnValue, masterForm.getName()));
//        }
//        var entryInProgress =
//                result.stream().filter(dataQuery -> stateValue.equalsIgnoreCase(dataQuery.getData().get(stateColumn))).findFirst();
//
//        if (entryInProgress.isPresent()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                    String.format("Same value present for Master Data entry."));
//        }
//        Map<String, String> masterValues = new HashMap<>();
//        masterValues.put(masterTableEntryIdColumn, idColumnValue);
//        masterValues.put(stateColumn, stateValue);
//        jdbcTemplate.execute(masterForm.makeUpdateMasterEntryStateStmtWithId(masterValues, masterTableEntryIdColumn));
//    }

}
