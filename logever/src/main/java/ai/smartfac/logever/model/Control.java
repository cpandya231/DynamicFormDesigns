package ai.smartfac.logever.model;

public class Control {
    private String label;
    private String type;
    private boolean isRequired;
    private String key;
    private String selectValues;
    private String placeholder;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSelectValues() {
        return selectValues;
    }

    public void setSelectValues(String selectValues) {
        this.selectValues = selectValues;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }
}
