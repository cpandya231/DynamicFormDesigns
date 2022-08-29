package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.model.DataQuery;
import ai.smartfac.logever.model.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MasterFormDataService {

    @Autowired
    JdbcTemplate jdbcTemplate;


    public List<DataQuery> getAllFor(Form form, String column, String columnValue) {
        Table table = new Table();
        table.setName(form.getMasterTableName());
        String selectCols = "id," + form.getColumns();
        String selectStmt = "SELECT " + selectCols + " from " + table.getName() + "";

        if (null != column && column.length() > 0) {
            selectStmt += " WHERE " + column + " = '" + columnValue + "'";
        }

        return jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet, selectCols.split(",")));
    }


}
