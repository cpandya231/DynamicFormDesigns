package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.*;
import ai.smartfac.logever.model.*;
import ai.smartfac.logever.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FormDataService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    PendingEntryService pendingEntryService;

    @Transactional
    public void bulkInsert(User user, Form form, ArrayList<Map<String,String>> records) {
        records.forEach(record-> {
            insertIntoWithPendingEntries(user,form,record);
        });
    }

    @Transactional
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
            jdbcTemplate.execute(form.makeInsertMasterValuesStmt(values));
        }
        if(!values.get("endState").equalsIgnoreCase("true")) {
            State nextState = form.getWorkflow().getStates().stream().filter(st->st.getLabel().equals(values.get("state"))).findFirst().get();
            List<PendingEntry> pendingEntries = new ArrayList<>();
            nextState.getRoles().forEach(r->{
                nextState.getDepartments().forEach(d-> {
                    if(d.getName().equalsIgnoreCase("Initiator Department")) {
                        departmentService.getAllUnder(user.getDepartment()).forEach(aD-> {
                            pendingEntries.add(new PendingEntry(form.getId(),insertedId,null,r.getId(),aD.getId()));
                        });
                    } else {
                        departmentService.getAllUnder(d).forEach(aD-> {
                            pendingEntries.add(new PendingEntry(form.getId(),insertedId,null,r.getId(),aD.getId()));
                        });
                    }
                });
            });

            pendingEntryService.saveAll(pendingEntries);
        }
        return insertedId;
    }

    @Transactional
    public Entry insertInto(Form form, Map<String, String> values) {
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
        return new Entry(insertedId,insertedHistoryId);
    }

    @Transactional
    public void insertInto(Form form, ArrayList<GridLogEntry> gridValues, Entry entry) {
        if(form.getGrids().size() > 0) {
            gridValues.forEach(gridValue-> {
                gridValue.getData().forEach(values-> {
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

    @Transactional
    public Entry update(Form form, Map<String, String> values) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        System.out.println(keyHolder.getKey());
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
        }
        return new Entry(insertedId,insertedHistoryId);
    }

    @Transactional
    public void update(Form form, ArrayList<GridLogEntry> gridValues, Entry entry) {
        if(form.getGrids().size() > 0) {
            gridValues.forEach(gridValue-> {
                gridValue.getData().forEach(values-> {
                    values.put("log_entry_id", entry.getEntryId()+"");
                    values.put("history_log_entry_id",entry.getHistoryEntryId()+"");
                    KeyHolder keyHolder = new GeneratedKeyHolder();
                    var insertedId = 0;
                    jdbcTemplate.update(
                            connection -> connection.prepareStatement(form.makeGridDeleteValuesStmt(gridValue.getName(), values)));
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

    public List<DataQuery> getAllPendingFor(Form form, User user) {
        String userRoles = user.getRoles().stream().map(r->r.getId()+"").collect(Collectors.joining(","));
        Integer userDept = user.getDepartment().getId();

        Table table = new Table();
        table.setName(form.getName());
        Table metaTable = new Table();
        metaTable.setName(form.getMetadataTableName());
        String gridCols = form.getGrids().stream().flatMap(f->f.stream().map(grid->grid.getKey())).collect(Collectors.joining(","));
        String selectCols = "l.id," + Arrays.stream(form.getColumns().split(",")).filter(c-> Arrays.stream(gridCols.split(",")).filter(gc->gc.equalsIgnoreCase(c)).count() == 0).map(s->"l."+s).collect(Collectors.joining(",")) + ",l.state,l.log_create_dt,l.created_by,l.log_update_dt,l.updated_by";
        String selectStmt = "SELECT " + selectCols + " from " + table.getName() + " l inner join pending_entry p on l.id=p.entry_id and p.form_id='"+form.getId()+"' where (p.assigned_role in ("+userRoles
                +") and p.assigned_department="+userDept+") or p.assigned_user = '"+user.getUsername()+"'";
        System.out.println(selectStmt);

        return jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet,  Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",")));

    }

    public List<DataQuery> getAllFor(Form form, int entryId, boolean filterByUsername, boolean filterByDepartment) {
        Table table = new Table();
        table.setName(form.getName());
        Table metaTable = new Table();
        metaTable.setName(form.getMetadataTableName());
        String gridCols = form.getGrids().stream().flatMap(f->f.stream().map(grid->grid.getKey())).collect(Collectors.joining(","));
        String selectCols = "distinct l.id," + Arrays.stream(form.getColumns().split(",")).filter(c-> Arrays.stream(gridCols.split(",")).filter(gc->gc.equalsIgnoreCase(c)).count() == 0).map(s->"l."+s).collect(Collectors.joining(",")) + ",l.state,l.log_create_dt,l.created_by,l.log_update_dt,l.updated_by";
        String selectStmt = "SELECT " + selectCols + " from " + table.getName() + " l inner join "+metaTable.getName()+" h on l.id=h.log_entry_id";

        if (entryId != -1) {
            selectStmt += " WHERE id=" + entryId;
        } else if (filterByUsername) {
            selectStmt += " WHERE h.created_by=  '" + SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString() + "'";
        }

        System.out.println(selectStmt);

        return jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet, Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",")));
    }

    public List<DataQuery> getLogEntryMetadata(Form form, int entryId) {
        Table table = new Table();
        table.setName(form.getMetadataTableName());
        String selectCols = "id," + form.getColumns() + ",log_entry_id,state,log_create_dt,created_by,comment";
        String selectStmt = "SELECT " + selectCols + " from " + table.getName() + " WHERE log_entry_id=" + entryId + " ORDER BY log_create_dt desc";
        return jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet, selectCols.split(",")));
    }

    public ArrayList<GridDataQuery> getGridsFor(Form form, int logEntryId) {
        Table table = new Table();
        ArrayList<GridDataQuery> grids = new ArrayList<>();
        if(form.getGrids().size() > 0) {
            form.getGrids().stream().forEach(gridCtrl -> {
                table.setGridTableName(form.getName()+" "+gridCtrl.get(0).getKey());
                String columns = gridCtrl.get(0).getControls().stream().map(ctrl -> ctrl.getKey()).collect(Collectors.joining(","));
                String select = "id,log_entry_id," + columns;
                String gridSelectStmt = "SELECT " + select + " from " + table.getName() + " where log_entry_id ='"+logEntryId+"'";

                List<DataQuery> gridDataQuery = jdbcTemplate.query(gridSelectStmt,
                        (resultSet, rowNum) -> new DataQuery(resultSet, select.split(",")));
                grids.add(new GridDataQuery(gridCtrl.get(0).getKey(), gridDataQuery, select));
            });
        }
        return grids;
    }

    public void saveLogEntryMetadata(Form form, Map<String, String> metaDataValues) {

        jdbcTemplate.execute(form.makeInsertMetadataValuesStmt(metaDataValues));

    }

    public List<DataQuery> getAllFor(Form form, Map<String,String> filter) {
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

        return jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet, finalColumns.split(",")));
    }
}
