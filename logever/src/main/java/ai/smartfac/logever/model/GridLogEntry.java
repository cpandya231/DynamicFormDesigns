package ai.smartfac.logever.model;

import java.util.ArrayList;
import java.util.Map;

public class GridLogEntry {
    private int id;
    private String name;
    private int logEntryId;
    private ArrayList<Map<String,String>> data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLogEntryId() {
        return logEntryId;
    }

    public void setLogEntryId(int logEntryId) {
        this.logEntryId = logEntryId;
    }

    public ArrayList<Map<String, String>> getData() {
        return data;
    }

    public void setData(ArrayList<Map<String, String>> data) {
        this.data = data;
    }
}
