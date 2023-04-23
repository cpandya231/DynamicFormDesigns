package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Department;
import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.entity.Role;
import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.model.DataQuery;
import ai.smartfac.logever.model.Table;
import ai.smartfac.logever.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    public int insertInto(Form form, Map<String, String> values) {
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
        return insertedId;
    }

    public void update(Form form, Map<String, String> values) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> connection.prepareStatement(form.makeUpdateStmt(values), new String[]{"id"}), keyHolder);
        values.put("created_by", values.get("updated_by"));
        jdbcTemplate.execute(form.makeInsertMetadataValuesStmt(values));
        values.remove("log_entry_id");
        if (values.get("endState").equalsIgnoreCase("true") && form.getType().equalsIgnoreCase("master")) {
            jdbcTemplate.execute(form.makeUpdateMasterStmt(values));
        }
    }

    public List<DataQuery> getAllPendingFor(Form form, User user) {
        String userRoles = user.getRoles().stream().map(r->r.getId()+"").collect(Collectors.joining(","));
        Integer userDept = user.getDepartment().getId();

        Table table = new Table();
        table.setName(form.getName());
        Table metaTable = new Table();
        metaTable.setName(form.getMetadataTableName());
        String selectCols = "l.id," + Arrays.stream(form.getColumns().split(",")).map(s->"l."+s).collect(Collectors.joining(",")) + ",l.state,l.log_create_dt,l.created_by,l.log_update_dt,l.updated_by";
        String selectStmt = "SELECT " + selectCols + " from " + table.getName() + " l inner join pending_entry p on l.id=p.entry_id where (p.assigned_role in ("+userRoles
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
        String selectCols = "l.id," + Arrays.stream(form.getColumns().split(",")).map(s->"l."+s).collect(Collectors.joining(",")) + ",l.state,l.log_create_dt,l.created_by,l.log_update_dt,l.updated_by";
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
