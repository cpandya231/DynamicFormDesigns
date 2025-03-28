package com.swiftiq.logever.model;

import java.sql.Timestamp;

public class FormSettings {

    private String owners;

    private FormValidation validation;

    private String transactionColumns;

    public FormSettings(String transactionColumns, String owners, FormValidation validation) {
        this.owners = owners;
        this.validation = validation;
        this.transactionColumns = transactionColumns;
    }

    public String getOwners() {
        return owners;
    }

    public void setOwners(String owners) {
        this.owners = owners;
    }

    public FormValidation getValidation() {
        return validation;
    }

    public void setValidation(FormValidation validation) {
        this.validation = validation;
    }

    public String getTransactionColumns() {
        return transactionColumns;
    }

    public void setTransactionColumns(String transactionColumns) {
        this.transactionColumns = transactionColumns;
    }
}
