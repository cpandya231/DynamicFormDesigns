package com.swiftiq.logever.service;

import com.swiftiq.logever.model.DataQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EquipmentDataService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${master.equipment.table}")
    private String equipmentMaster;

    public Map<String,Object> fetchDataFor(int equipmentId) {

        String updateDoFetchQuery = "UPDATE "+equipmentMaster+" SET do_fetch = true, last_fetched_value = 'ERR' where id = '"+equipmentId+"'";
        jdbcTemplate.update(updateDoFetchQuery);

        try {
            System.out.println("Waiting for Machine Data Reader to get data from the Machine");
            Thread.sleep(3000);
            System.out.println("Waiting for Machine Data Reader to get data from the Machine");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String selectCols = "last_fetched_value";
        String selectStmt = "SELECT " + selectCols + " from " + equipmentMaster + " WHERE id = '"+equipmentId+"'";
        String[] sMCArray = Arrays.stream(selectCols.split(",")).map(col->col.split("\\.")[1]).collect(Collectors.joining(",")).split(",");
        DataQuery resultOne = new DataQuery(jdbcTemplate.queryForList(selectStmt),12);
        return resultOne.getData().get(0);

    }
}
