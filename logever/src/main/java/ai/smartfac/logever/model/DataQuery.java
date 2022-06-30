package ai.smartfac.logever.model;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DataQuery {

    private Map<String,String> data = new HashMap<>();

    public DataQuery(ResultSet resultSet, String[] columns) {
        Arrays.stream(columns).forEach(column-> {
            try {
                this.data.put(column,resultSet.getString(column));
            } catch (Exception ex) {
                System.out.println("Exception occurred : "+ex.getMessage());
            }
        });
    }
    public DataQuery(Map<String, String> data) {
        this.data = data;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
