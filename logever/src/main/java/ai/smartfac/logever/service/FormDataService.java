package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.entity.State;
import ai.smartfac.logever.model.DataQuery;
import ai.smartfac.logever.model.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FormDataService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void insertInto(Form form, Map<String, String> values) {
        jdbcTemplate.execute(form.makeInsertValuesStmt(values));
    }

    public void update(Form form, Map<String, String> values) {
        jdbcTemplate.execute(form.makeUpdateStmt(values));
    }

    public List<DataQuery> getAllFor(Form form, List<State> states, int entryId) {
        Table table = new Table();
        table.setName(form.getName());
        String selectCols = "id," + form.getColumns() + ",state,log_create_dt,created_by,log_update_dt,updated_by";
        String selectStmt = "SELECT " + selectCols + " from " + table.getName() + " WHERE state in (" +
                String.join(",", states.stream().map(f -> "'" + f.getName() + "'").collect(Collectors.toList())) + ")";

        if (entryId != -1) {
            selectStmt += " AND id=" + entryId;
        }
        return jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet, selectCols.split(",")));
    }
}
