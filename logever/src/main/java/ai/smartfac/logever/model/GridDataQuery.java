package ai.smartfac.logever.model;

import java.util.ArrayList;
import java.util.List;

public class GridDataQuery {
    public String grid;
    public String columns;
    public List<DataQuery> data;

    public GridDataQuery(String grid, List<DataQuery> data, String columns) {
        this.grid = grid;
        this.data = data;
        this.columns = columns;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public String getGrid() {
        return grid;
    }

    public void setGrid(String grid) {
        this.grid = grid;
    }

    public List<DataQuery> getData() {
        return data;
    }

    public void setData(List<DataQuery> data) {
        this.data = data;
    }
}
