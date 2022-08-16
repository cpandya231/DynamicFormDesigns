package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Form;
import ai.smartfac.logever.model.DataQuery;
import ai.smartfac.logever.model.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Service
public class MasterFormDataService {

    @Autowired
    JdbcTemplate jdbcTemplate;


    public Map<String, List<DataQuery>> getAllFor(Form form, String groupBy) {
        Table table = new Table();
        table.setName(form.getName());
        String selectCols = "id," + form.getColumns() + ",state,log_create_dt,created_by,log_update_dt,updated_by";
        String selectStmt = "SELECT " + selectCols + " from " + table.getName() + "";


        var result = jdbcTemplate.query(selectStmt,
                (resultSet, rowNum) -> new DataQuery(resultSet, selectCols.split(",")));

        return result.stream().collect(groupingBy(post -> post.getData().get(groupBy)));
    }


}
