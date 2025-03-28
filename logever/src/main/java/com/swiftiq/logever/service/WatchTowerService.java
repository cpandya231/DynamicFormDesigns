package com.swiftiq.logever.service;

import com.swiftiq.logever.model.DataQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class WatchTowerService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public DataQuery getQueryResults(String query) {
        String selectCols = query.split("from")[0].split("  ")[1];
        String finalCols = Arrays.stream(selectCols.split(",")).map(col-> {
            int len = col.split(" as ").length;
            return col.split(" as ")[len-1];
        }).collect(Collectors.joining(","));
        System.out.println(finalCols);
        String[] sMCArray = Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
        return new DataQuery(jdbcTemplate.queryForList(query),12);
//        return jdbcTemplate.query(query,
//                (resultSet, rowNum) -> new DataQuery(resultSet, finalCols.split(",")));
    }

    public DataQuery getTableDetails() {
        String query = "select table_name as tbl,group_concat(column_name) as columns from information_schema.columns where table_schema='logever' group by table_name";
        String selectCols = "tbl,columns";
        String[] sMCArray = Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
        return new DataQuery(jdbcTemplate.queryForList(query),12);
//        return jdbcTemplate.query(query,
//                (resultSet, rowNum) -> new DataQuery(resultSet, selectCols.split(",")));
    }
}
