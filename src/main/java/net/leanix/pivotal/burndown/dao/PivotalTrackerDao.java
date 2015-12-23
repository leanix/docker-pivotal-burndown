package net.leanix.pivotal.burndown.dao;

import com.sun.jersey.api.client.ClientResponse;
import java.util.List;
import net.leanix.pivotal.burndown.AppConfiguration;
import net.leanix.pivotal.burndown.api.ApiClient;
import net.leanix.pivotal.burndown.api.ApiException;
import net.leanix.pivotal.burndown.models.Iteration;
import net.leanix.pivotal.burndown.models.IterationHistory;

/**
 *
 * @author berndschoenbach
 */
public class PivotalTrackerDao {

    /**
     * Retrieves the current iteration from Pivotal Tracker.
     *
     * @param projectId the project ID to find the current iteration for
     *
     * @return the found iteration
     *
     * @throws ApiException
     */
    public static Iteration getCurrentIteration(String projectId) throws ApiException {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("https://www.pivotaltracker.com/services/v5/");
        apiClient.addDefaultHeader("X-TrackerToken", AppConfiguration.getPivotalApiKey());

        ClientResponse iterationResponse = apiClient.invokeApiGetCall("projects/" + projectId + "/iterations?scope=current");
        List<Iteration> iterations;
        iterations = (List<Iteration>) ApiClient.deserialize((String) iterationResponse.getEntity(String.class), "Array", Iteration.class);

        Iteration iteration = iterations.get(0);

        return iteration;
    }

    /**
     * Retrieves the iteration with the given numer
     *
     * @param iterationNumber the iteration number
     * @param projectId the project ID to find the iteration for
     *
     * @return the found iteration
     * @throws ApiException
     */
    public static Iteration getIterationByNumber(String iterationNumber, String projectId) throws ApiException, Exception {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("https://www.pivotaltracker.com/services/v5/");
        apiClient.addDefaultHeader("X-TrackerToken", AppConfiguration.getPivotalApiKey());

        ClientResponse iterationResponse = apiClient.invokeApiGetCall("projects/" + projectId + "/iterations?limit=1&offset=" + (Integer.valueOf(iterationNumber) - 1));
        List<Iteration> iterations;
        iterations = (List<Iteration>) ApiClient.deserialize((String) iterationResponse.getEntity(String.class), "Array", Iteration.class);

        if (iterations != null && iterations.size() > 0) {
            Iteration iteration = iterations.get(0);

            return iteration;
        }

        throw new Exception("The iteration with the given number: '" + iterationNumber + "' could not be found.");
    }

    /**
     * Gets the history for the given iteration in the given project.
     *
     * @param projectId the project which contains the iteration
     * @param iteration the iteration to retrieve the history for
     *
     * @return the iteration history with the relevant data
     *
     * @throws ApiException
     */
    public static IterationHistory getIterationHistory(String projectId, Iteration iteration) throws ApiException {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(AppConfiguration.getPivotalUrl());
        apiClient.addDefaultHeader("X-TrackerToken", AppConfiguration.getPivotalApiKey());

        ClientResponse historyResponse = apiClient.invokeApiGetCall("projects/" + projectId + "/history/iterations/" + iteration.getNumber() + "/days");
        IterationHistory history = historyResponse.getEntity(IterationHistory.class);

        return history;
    }
}
