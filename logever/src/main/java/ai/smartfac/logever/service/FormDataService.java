package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Form;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FormDataService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void insertInto(Form form, Map<String, String> values) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> connection.prepareStatement(form.makeInsertValuesStmt(values), new String[]{"id"}), keyHolder);
        var insertedId = keyHolder.getKey().intValue();
        values.put("log_entry_id", insertedId + "");
        jdbcTemplate.execute(form.makeInsertMetadataValuesStmt(values));
        values.remove("log_entry_id");
        values.put("id", insertedId + "");
        if (values.get("endState").equalsIgnoreCase("true") && form.getType().equalsIgnoreCase("master")) {
            jdbcTemplate.execute(form.makeInsertMasterValuesStmt(values));
        }
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

    public List<DataQuery> getAllFor(Form form, int entryId, boolean filterByUsername, boolean filterByDepartment) {
        Table table = new Table();
        table.setName(form.getName());
        String selectCols = "id," + form.getColumns() + ",state,log_create_dt,created_by,log_update_dt,updated_by";
        String selectStmt = "SELECT " + selectCols + " from " + table.getName() + "";

        if (entryId != -1) {
            selectStmt += " WHERE id=" + entryId;
        } else if (filterByUsername) {
            selectStmt += " WHERE created_by=  '" + SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString() + "'";
        }


        return jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet, selectCols.split(",")));
    }

    public List<DataQuery> getAllForWithAssignments(Form form, int entryId, boolean filterByUsername, boolean filterByDepartment) {
        Table table = new Table();
        table.setName(form.getName());
        String selectCols = "id," + form.getColumns() + ",state,log_create_dt,created_by,log_update_dt,updated_by,assigned_user,assigned_role,assigned_dept";
        String selectStmt = "SELECT " + selectCols + " from " + table.getName() + "";

        if (entryId != -1) {
            selectStmt += " WHERE id=" + entryId;
        } else if (filterByUsername) {
            selectStmt += " WHERE created_by=  '" + SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString() + "'";
        }


        return jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet, selectCols.split(",")));
    }

    public List<DataQuery> getAllForUser(Form form, User user) {
        Table table = new Table();
        table.setName(form.getName());
        Table metaDataTable = new Table();
        metaDataTable.setName(form.getMetadataTableName());
        String selectCols = "id," + form.getColumns() + ",state,log_create_dt,created_by,log_update_dt,updated_by";
        String selectStmt = "SELECT " + selectCols + " from " + table.getName() + " WHERE id in (SELECT log_entry_id from "+metaDataTable.getName()+
                " WHERE created_by='"+user.getUsername()+"' OR ";
        String roleClause = "(1=0 OR assigned_role='' OR "+user.getRoles().stream().map(role-> {
            return "FIND_IN_SET('"+role.getId()+"',assigned_role) > 0";
        }).collect(Collectors.joining("OR"))+")";
        String deptClause = "(1=0 OR assigned_dept='' OR FIND_IN_SET('"+user.getDepartment().getId()+"',assigned_dept)>0))";
        selectStmt = selectStmt + "("+ roleClause + " AND "+deptClause+")";
        System.out.println(selectStmt);
        //select selectCols from table.getName() where id in (select id from table.getMetadata() where created_by
        return jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet, selectCols.split(",")));
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
