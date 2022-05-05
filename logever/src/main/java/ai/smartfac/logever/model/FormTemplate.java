package ai.smartfac.logever.model;

import java.util.ArrayList;

public class FormTemplate {
    private String formName;
    private ArrayList<Component> components;

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public ArrayList<Component> getComponents() {
        return components;
    }

    public void setComponents(ArrayList<Component> components) {
        this.components = components;
    }

}
