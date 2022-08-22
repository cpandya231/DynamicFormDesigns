package ai.smartfac.logever.model;

import java.util.ArrayList;

public class Component {
    private String label;
    private String key;
    private String type;
    private String defaultValue;
    private boolean persistent;
    private Validate validate;
    private String inputType;
    private boolean unique;

    private ArrayList<ArrayList<Row>> rows;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefaultValue() {
        if(defaultValue==null) {
            defaultValue = "";
        }
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public Validate getValidate() {
        return validate;
    }

    public void setValidate(Validate validate) {
        this.validate = validate;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public ArrayList<ArrayList<Row>> getRows() {
        return rows;
    }

    public void setRows(ArrayList<ArrayList<Row>> rows) {
        this.rows = rows;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }
}
