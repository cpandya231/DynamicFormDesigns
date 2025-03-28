package com.swiftiq.logever.model;

public class FormReviewRequest {
        Integer formId;
        String reviewer;
        String comments;
        String process;
        boolean approved;

        public FormReviewRequest() {
        }

        public FormReviewRequest(Integer formId, String reviewer, String comments, boolean approved, String process) {
            this.formId = formId;
            this.reviewer = reviewer;
            this.comments = comments;
            this.approved = approved;
            this.process = process;
        }

        public Integer getFormId() {
            return formId;
        }

        public void setFormId(Integer formId) {
            this.formId = formId;
        }

        public String getReviewer() {
            return reviewer;
        }

        public void setReviewer(String reviewer) {
            this.reviewer = reviewer;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }

        public boolean isApproved() {
            return approved;
        }

        public void setApproved(boolean approved) {
            this.approved = approved;
        }

        public String getProcess() {
            return process;
        }

        public void setProcess(String process) {
            this.process = process;
        }

}
