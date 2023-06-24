package ai.smartfac.logever.model;

import java.util.ArrayList;
import java.util.Map;

public class LogEntry {

    private int id;
    private String state;
    private boolean endState;
    private Map<String,String> data;
    private ArrayList<GridLogEntry> gridData;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public boolean isEndState() {
        return endState;
    }

    public void setEndState(boolean endState) {
        this.endState = endState;
    }

    public ArrayList<GridLogEntry> getGridData() {
        return gridData;
    }

    public void setGridData(ArrayList<GridLogEntry> gridData) {
        this.gridData = gridData;
    }
}
