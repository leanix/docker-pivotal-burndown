package net.leanix.pivotal.burndown;

/**
 * Main entry point.
 *
 *
 */
public class App {

    public static void main(String[] args) throws Exception {
        BusinessLogic businessLogic = new BusinessLogic();
        businessLogic.calculateBurndown();
    }
}
