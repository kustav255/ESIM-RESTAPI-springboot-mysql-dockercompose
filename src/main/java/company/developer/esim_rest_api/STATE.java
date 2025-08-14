package company.developer.esim_rest_api;

public enum STATE {
    AVAILABLE("available"),
    INUSE("in-use"),
    INACTIVE("inactive");

    private String state;

    STATE(String state){
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
