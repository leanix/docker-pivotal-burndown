package net.leanix.pivotal.burndown;

/**
 * Main entry point.
 *
 *
 */
public class App {

    public static void main(String[] args) throws Exception {
        AppConfiguration configuration = new AppConfiguration();

        BusinessLogic businessLogic = new BusinessLogic(configuration);
        businessLogic.calculateBurndown();
    }
}
