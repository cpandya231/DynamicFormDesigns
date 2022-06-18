package ai.smartfac.logever.model;

import java.util.Map;

public class LogEntry {

    private String state;
    private Map<String,String> data;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
