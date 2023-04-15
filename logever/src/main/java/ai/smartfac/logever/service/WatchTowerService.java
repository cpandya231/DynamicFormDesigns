package ai.smartfac.logever.service;

import ai.smartfac.logever.entity.Report;
import ai.smartfac.logever.model.DataQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class WatchTowerService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public Iterable<DataQuery> getQueryResults(String query) {
        String selectCols = query.split("from")[0].split("  ")[1];
        String finalCols = Arrays.stream(selectCols.split(",")).map(col-> {
            int len = col.split(" as ").length;
            return col.split(" as ")[len-1];
        }).collect(Collectors.joining(","));
        System.out.println(finalCols);
        return jdbcTemplate.query(query,
                (resultSet, rowNum) -> new DataQuery(resultSet, finalCols.split(",")));
    }

    public Iterable<DataQuery> getTableDetails() {
        String query = "select table_name as tbl,group_concat(column_name) as columns from information_schema.columns where table_schema='logever' group by table_name";
        String selectCols = "tbl,columns";
        return jdbcTemplate.query(query,
                (resultSet, rowNum) -> new DataQuery(resultSet, selectCols.split(",")));
    }
}
