package ai.smartfac.logever.model;

import java.util.ArrayList;

public class FormTemplate {
    private String formName;
    private ArrayList<ArrayList<Control>> controls;

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public ArrayList<ArrayList<Control>> getControls() {
        return controls;
    }

    public void setControls(ArrayList<ArrayList<Control>> controls) {
        this.controls = controls;
    }
}
