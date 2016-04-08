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
        return (System.getenv("PIVOTAL_API_KEY") == null) ? "" : System.getenv("PIVOTAL_API_KEY");
    }

    public static String getPivotalProjectId() {
        return (System.getenv("PIVOTAL_PROJECT_ID") == null) ? "" : System.getenv("PIVOTAL_PROJECT_ID");
    }

    public static String getGeckoboardApiKey() {
        return (System.getenv("GECKOBOARD_API_KEY") == null) ? "" : System.getenv("GECKOBOARD_API_KEY");
    }

    public static String getGeckoboardWidgetKey() {
        return (System.getenv("GECKOBOARD_WIDGET_KEY") == null) ? "" : System.getenv("GECKOBOARD_WIDGET_KEY");
    }

    public static String getTargetPath() {
        return (System.getenv("IMAGE_TARGET_PATH") == null) ? "" : System.getenv("IMAGE_TARGET_PATH");
    }

    public static String getIteration() {
        return (System.getenv("ITERATION") == null) ? "" : System.getenv("ITERATION");
    }

    public static String getDisplayType() {
        String displayType = System.getenv("DISPLAY_TYPE");

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
