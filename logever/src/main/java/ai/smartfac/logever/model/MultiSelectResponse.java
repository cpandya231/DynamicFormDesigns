package ai.smartfac.logever.model;

import java.util.ArrayList;

public class MultiSelectResponse {
    ArrayList<String> all;
    ArrayList<String> selected;

    public MultiSelectResponse(ArrayList<String> all, ArrayList<String> selected) {
        this.all = all;
        this.selected = selected;
    }

    public ArrayList<String> getAll() {
        return all;
    }

    public void setAll(ArrayList<String> all) {
        this.all = all;
    }

    public ArrayList<String> getSelected() {
        return selected;
    }

    public void setSelected(ArrayList<String> selected) {
        this.selected = selected;
    }
}
