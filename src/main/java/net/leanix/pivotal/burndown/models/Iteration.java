package net.leanix.pivotal.burndown.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

/**
 *
 * @author berndschoenbach
 */
public class Iteration {

    private int number;

    @JsonProperty(value = "project_id")
    private int projectId;
    private int length;

    @JsonProperty(value = "team_strength")
    private int teamStrength;

    private DateTime start;
    private DateTime finish;
    private String kind;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getTeamStrength() {
        return teamStrength;
    }

    public void setTeamStrength(int teamStrength) {
        this.teamStrength = teamStrength;
    }

    public DateTime getStart() {
        return start;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    public DateTime getFinish() {
        return finish;
    }

    public void setFinish(DateTime finish) {
        this.finish = finish;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}
