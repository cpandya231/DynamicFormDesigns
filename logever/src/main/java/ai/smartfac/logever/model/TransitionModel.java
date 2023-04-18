package ai.smartfac.logever.model;

public class TransitionModel {
    public String fromState;
    public String toState;

    public TransitionModel() {
    }

    public TransitionModel(String fromState, String toState) {
        this.fromState = fromState;
        this.toState = toState;
    }

    public String getFromState() {
        return fromState;
    }

    public void setFromState(String fromState) {
        this.fromState = fromState;
    }

    public String getToState() {
        return toState;
    }

    public void setToState(String toState) {
        this.toState = toState;
    }
}
