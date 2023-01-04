package ai.smartfac.logever.service;

import ai.smartfac.logever.model.DataQuery;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EquipmentDataService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${master.equipment.table}")
    private String equipmentMaster;

    public Map<String,String> fetchDataFor(int equipmentId) {

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

        return jdbcTemplate.query(selectStmt,
            (resultSet, rowNum) -> new DataQuery(resultSet, selectCols.split(","))).get(0).getData();

    }
}
