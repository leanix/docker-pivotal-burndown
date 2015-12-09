package net.leanix.pivotal.burndown.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author berndschoenbach
 */
public class ApiClient {

    private final Map<String, Client> hostMap = new HashMap<>();
    private final Map<String, String> defaultHeaderMap = new HashMap<>();
    private String basePath;

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getBasePath() {
        return basePath;
    }

    public void addDefaultHeader(String key, String value) {
        defaultHeaderMap.put(key, value);
    }

    /**
     * Invokes POST,PUT, DELETE and GET calls to the Confluence REST API.
     *
     * @param path the resource path in the confluence API
     * @param method the method for the POST, PUT, DELETE or GET
     * @param username the username to authenticate the request
     * @param password the password to authenticate the request
     * @param body the data body to be transmitted with the request
     *
     * @return the response of the REST API
     *
     * @throws ApiException
     */
    private ClientResponse invokeApi(String path, String method, Object body) throws ApiException {
        String host = this.basePath;
        Client client = getClient(host);

        client.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, false);

        WebResource resource = client.resource(host + path);
        WebResource.Builder builder = resource.getRequestBuilder();

        ClientResponse response = null;

        switch (method) {
            case "GET":
                response = (ClientResponse) builder.get(ClientResponse.class);
                break;
            case "POST":
                response = (ClientResponse) builder.type("application/json").post(ClientResponse.class, serialize(body));
                break;
            case "DELETE":
                response = (ClientResponse) builder.type("application/json").delete(ClientResponse.class);
                break;
            case "PUT":
                response = (ClientResponse) builder.type("application/json").put(ClientResponse.class, serialize(body));
                break;
            default:
                throw new ApiException(501, "invalid method type " + method);
        }

        if (response == null) {
            throw new ApiException(500, "Bad Request");
        }

        if (response.getStatus() == 401) {
            throw new ApiException(401, "Invalid credetials given");
        } else if (response.getStatus() == 404) {
            throw new ApiException(404, "Target path was invalid.");
        }

        return response;
    }

    /**
     * Invokes GET calls to the Confluence REST API.
     *
     * @param path the resource path in the confluence API
     *
     * @return the response of the REST API
     *
     * @throws ApiException
     */
    public ClientResponse invokeApiGetCall(String path) throws ApiException {
        return invokeApi(path, "GET", null);
    }

    /**
     * Retrieves a jersey API client.
     *
     * @param host the URL to the host to retrieve the client for.
     *
     * @return the API client
     */
    public Client getClient(String host) {
        if (!hostMap.containsKey(host)) {
            Client client = Client.create();
            client.addFilter(new LoggingFilter());
            hostMap.put(host, client);
        }

        return hostMap.get(host);
    }

    /**
     * Serializes the given object to create a JSON string.
     *
     * @param obj the object to serialize
     *
     * @return the object as serialized string
     *
     * @throws ApiException
     */
    public static String serialize(Object obj) throws ApiException {
        try {
            if (obj != null) {
                return JsonUtil.getJsonMapper().writeValueAsString(obj);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    /**
     * Creates data objects from the given JSON string.
     *
     * @param json the JSON string returned from the result
     * @param containerType the type of the container
     * @param cls the class for the object
     *
     * @throws ApiException
     * @return the created objects, or multiple created objects
     */
    public static Object deserialize(String json, String containerType, Class cls) throws ApiException {
        try {
            if ("List".equals(containerType) || "Array".equals(containerType)) {
                JavaType typeInfo = JsonUtil.getJsonMapper().getTypeFactory().constructCollectionType(List.class, cls);
                List response = (List<?>) JsonUtil.getJsonMapper().readValue(json, typeInfo);
                return response;
            } else if ("ArrayWithoutKey".equals(containerType)) {
                ArrayList<Object> myObjects = JsonUtil.getJsonMapper().readValue(json, new TypeReference<ArrayList<Object>>() {
                });
                System.out.println("objects");
                System.out.println(myObjects);
                return myObjects;

            } else if (String.class.equals(cls)) {
                if (json != null && json.startsWith("\"") && json.endsWith("\"") && json.length() > 1) {
                    return json.substring(1, json.length() - 2);
                } else {
                    return json;
                }
            } else {
                return JsonUtil.getJsonMapper().readValue(json, cls);
            }
        } catch (IOException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

}
