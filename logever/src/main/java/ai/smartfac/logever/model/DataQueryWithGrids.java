package ai.smartfac.logever.model;

import java.util.ArrayList;
import java.util.List;

public class DataQueryWithGrids {
    public List<DataQuery> data;
    public ArrayList<GridDataQuery> grids;

    public DataQueryWithGrids() {
    }

    public DataQueryWithGrids(List<DataQuery> data, ArrayList<GridDataQuery> grids) {
        this.data = data;
        this.grids = grids;
    }

    public List<DataQuery> getData() {
        return data;
    }

    public void setData(List<DataQuery> data) {
        this.data = data;
    }

    public ArrayList<GridDataQuery> getGrids() {
        return grids;
    }

    public void setGrids(ArrayList<GridDataQuery> grids) {
        this.grids = grids;
    }
}
