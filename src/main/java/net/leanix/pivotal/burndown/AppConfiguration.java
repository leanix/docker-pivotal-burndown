package net.leanix.pivotal.burndown;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.leanix.dropkit.DropkitConfiguration;

/**
 * Configuration.
 *
 */
public class AppConfiguration extends DropkitConfiguration {

    @JsonProperty("pivotalapikey")
    private String pivotalApiKey;

    @JsonProperty("pivotalprojectid")
    private String pivotalProjectId;

    public String getPivotalUrl() {
        return "https://www.pivotaltracker.com/services/v5/";
    }

    public String getPivotalApiKey() {
        return pivotalApiKey;
    }

    public void setPivotalApiKey(String pivotalApiKey) {
        this.pivotalApiKey = pivotalApiKey;
    }

    public String getPivotalProjectId() {
        return pivotalProjectId;
    }

    public void setPivotalProjectId(String pivotalProjectId) {
        this.pivotalProjectId = pivotalProjectId;
    }
}
