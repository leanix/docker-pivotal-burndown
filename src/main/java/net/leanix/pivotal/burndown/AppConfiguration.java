package net.leanix.pivotal.burndown;

import net.leanix.dropkit.DropkitConfiguration;

/**
 * Configuration.
 *
 */
public class AppConfiguration extends DropkitConfiguration {

    public String getPivotalUrl() {
        return "https://www.pivotaltracker.com/services/v5/";
    }

    public String getPivotalApiKey() {
        return System.getProperty("pivotal.apiKey");
    }

    public String getPivotalProjectId() {
        return System.getProperty("pivotal.projectId");
    }

    public String getGeckoboardApiKey() {
        return System.getProperty("geckoboard.apiKey");
    }

    public String getGeckoboardWidgetKey() {
        return System.getProperty("geckoboard.widgetKey");
    }

    public String getTargetPath() {
        return System.getProperty("imageTargetPath");
    }

    public String getIteration() {
        return System.getProperty("iteration");
    }
}
