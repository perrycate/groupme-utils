package me.perrycate.groupmeutils.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.perrycate.groupmeutils.data.Message;
import me.perrycate.groupmeutils.data.GroupMessages;
import me.perrycate.groupmeutils.deserializers.MessageDeserializer;
import me.perrycate.groupmeutils.deserializers.GroupMessagesDeserializer;

/**
 * Interacts with the GroupMe Api, returning serialized objects from the data
 * package.
 */
public class Client {
    private static final String BASE_URL = "https://api.groupme.com/v3";
    private static final String CHARSET = "UTF-8";

    private final String apiToken;
    private final Gson gson; // used for deserializing things

    public Client(String apiToken) {
        this.apiToken = apiToken;

        // Register deserializers
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Message.class,
                new MessageDeserializer());
        gsonBuilder.registerTypeAdapter(GroupMessages.class,
                new GroupMessagesDeserializer());
        gson = gsonBuilder.create();
    }

    /**
     * Returns a group of the 100 most recent messages in group with groupId
     * after the message with afterId.
     */
    public GroupMessages getMessagesAfter(String groupId, String afterId) {
        String target = "/groups/" + groupId + "/messages";

        // get request url
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("after_id", afterId);
        URL url = createUrl(target, params);

        // Make request
        InputStream resultStream = makeGETRequest(url);

        // Deserialize returned JSON into a MessageCollection
        Reader reader = new InputStreamReader(resultStream);
        return gson.fromJson(reader, GroupMessages.class);
    }

    /**
     * Builds a valid request url to target with the given params as GET
     * parameters. Target MUST start with a "/".
     */
    private URL createUrl(String target, HashMap<String, String> params) {
        // Set up base url
        String urlString = BASE_URL + target;

        // Add parameters to url, if any
        int length = params.size();
        urlString += "?";
        String[] keys = params.keySet().toArray(new String[0]);
        for (int i = 0; i < length; i++) {
            urlString += keys[i] + "=" + params.get(keys[i]) + "&";
        }

        // Always include auth token
        urlString += "token=" + apiToken;

        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            System.err.println("FATAL: failed to create a request url.");
            System.err.println("       URL string: " + urlString);
            System.err.println(e);
            System.exit(1);
        }

        return url;
    }

    /**
     * Convenience method for if we need to make a get request and have no
     * parameters other than authentication
     */
    private URL createUrl(String target) {

        HashMap<String, String> empty = new HashMap<String, String>();
        return createUrl(target, empty);

    }

    public InputStream makeGETRequest(URL url) {
        InputStream stream = null;

        try {
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");

            connection.connect();
            stream = connection.getInputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return stream;
    }

}
