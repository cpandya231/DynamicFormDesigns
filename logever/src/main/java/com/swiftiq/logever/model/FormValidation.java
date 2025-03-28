package com.swiftiq.logever.model;

public class FormValidation {

    private String preIngestCode;
    private String postIngestCode;

    public FormValidation(String preIngestCode, String postIngestCode) {
        this.preIngestCode = preIngestCode;
        this.postIngestCode = postIngestCode;
    }

    public String getPreIngestCode() {
        return preIngestCode;
    }

    public void setPreIngestCode(String preIngestCode) {
        this.preIngestCode = preIngestCode;
    }

    public String getPostIngestCode() {
        return postIngestCode;
    }

    public void setPostIngestCode(String postIngestCode) {
        this.postIngestCode = postIngestCode;
    }
}
