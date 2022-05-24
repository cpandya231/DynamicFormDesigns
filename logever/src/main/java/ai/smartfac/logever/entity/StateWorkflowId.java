package ai.smartfac.logever.entity;

public class StateWorkflowId {

    private String name;

    private Workflow workflow;

    // default constructor

    public StateWorkflowId(String state, Workflow workflow) {
        this.name = state;
        this.workflow = workflow;
    }

    @Override
    public boolean equals(Object obj) {
        StateWorkflowId check = (StateWorkflowId) obj;
        if(check.workflow.getId() == this.workflow.getId() && check.name.equals(this.name)) {
            return true;
        } else {
            return false;
        }
    }
}
