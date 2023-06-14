package ai.smartfac.logever.model;

public class ColumnConstraints {
    private boolean required;
    private boolean unique;
    private boolean defaults;
    private String defaultValue;

    public ColumnConstraints(boolean required, boolean unique, boolean defaults, String defaultValue) {
        this.required = false;
        this.unique = unique;
        this.defaults = defaults;
        this.defaultValue = defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isDefaults() {
        return defaults;
    }

    public void setDefaults(boolean defaults) {
        this.defaults = defaults;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String toString() {
        String constraints = "";
        if(required) {
            constraints += " NOT NULL ";
        }
        if(unique) {
            constraints += " UNIQUE ";
        }
        if(defaults) {
            if(defaultValue.contains("AUTO_INCREMENT")) {
                constraints += " AUTO_INCREMENT ";
            } else {
                constraints += " DEFAULT " + defaultValue + " ";
            }
        }
        return constraints;
    }
}
