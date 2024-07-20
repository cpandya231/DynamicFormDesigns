package ai.smartfac.logever.model;

import java.util.ArrayList;
import java.util.List;

public class GridDataQuery {
    public String grid;
    public String gridLabel;
    public String columns;
    public String labels;
    public List<DataQuery> data;

    public GridDataQuery(String grid, String gridLabel, List<DataQuery> data, String columns, String labels) {
        this.grid = grid;
        this.gridLabel = gridLabel;
        this.data = data;
        this.columns = columns;
        this.labels = labels;
    }

    public String getGridLabel() {
        return gridLabel;
    }

    public void setGridLabel(String gridLabel) {
        this.gridLabel = gridLabel;
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

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }
}
