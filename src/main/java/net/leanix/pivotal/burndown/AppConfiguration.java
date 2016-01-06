package net.leanix.pivotal.burndown;

/**
 * Configuration.
 *
 */
public class AppConfiguration {

    public static String getPivotalUrl() {
        return "https://www.pivotaltracker.com/services/v5/";
    }

    public static String getPivotalApiKey() {
        return System.getProperty("pivotal.apiKey");
    }

    public static String getPivotalProjectId() {
        return System.getProperty("pivotal.projectId");
    }

    public static String getGeckoboardApiKey() {
        return System.getProperty("geckoboard.apiKey");
    }

    public static String getGeckoboardWidgetKey() {
        return System.getProperty("geckoboard.widgetKey");
    }

    public static String getTargetPath() {
        return System.getProperty("imageTargetPath");
    }

    public static String getIteration() {
        return System.getProperty("iteration");
    }

    public static String getDisplayType() {
        String displayType = System.getProperty("displayType");

        switch (displayType) {
            case "both":
                return "both";
            case "burndown":
                return "burndown";
            case "accepted_points":
                return "accepted_points";
            default:
                return "burndown";
        }
    }
}
