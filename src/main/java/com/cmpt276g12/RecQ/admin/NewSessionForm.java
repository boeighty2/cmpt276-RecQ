package com.cmpt276g12.RecQ.admin;

/** No scheduling fields: staff controls the live open/close state manually. */
public class NewSessionForm {
    private String gym = "";
    private String sport = "";
    private String courtCount = "4";
    public String getGym() { return gym; }
    public void setGym(String gym) { this.gym = gym; }
    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }
    public String getCourtCount() { return courtCount; }
    public void setCourtCount(String courtCount) { this.courtCount = courtCount; }
}
