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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.perrycate.groupmeutils.data.Message;
import me.perrycate.groupmeutils.data.MessageCollection;
import me.perrycate.groupmeutils.deserializers.MessageDeserializer;
import me.perrycate.groupmeutils.deserializers.MessageCollectionDeserializer;

/**
 * Interacts with the GroupMe Api, returning serialized objects from the data
 * package.
 */
public class Client {
    private static final String BASE_URL = "https://api.groupme.com/v3";

    private final String apiToken;
    private final Gson gson; // used for deserializing things

    public Client(String apiToken) {
        this.apiToken = apiToken;

        // Register deserializers
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Message.class,
                new MessageDeserializer());
        gsonBuilder.registerTypeAdapter(MessageCollection.class,
                new MessageCollectionDeserializer());
        gson = gsonBuilder.create();
    }

    /**
     * Returns a group of the 100 most recent messages in group with groupId
     * after the message with afterId.
     */
    public MessageCollection getMessagesAfter(String groupId, String afterId) {

        // Create URL
        String urlString = BASE_URL + "/groups/" + groupId + "/messages";
        try {
            URL url = new URL(urlString);

            // Add request parameters
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("after_id", afterId);

            // Make request
            InputStream resultStream = makeRequest(url, params);

            // Deserialize returned JSON into a MessageCollection
            Reader reader = new InputStreamReader(resultStream);
            return gson.fromJson(reader, MessageCollection.class);
        } catch (MalformedURLException e) {
            System.out.println("ERROR: failed to create a request url for"
                    + " group id " + groupId + " when attempting to fetch"
                    + " messages.");
            return null;
        }
    }

    public InputStream makeRequest(URL url, Map<String, String> params) {
        InputStream stream = null;

        try {
            URLConnection connection = url.openConnection();

            // Always add auth token to request
            connection.addRequestProperty("token", apiToken);

            // Add params to request
            for (String key : params.keySet()) {
                connection.addRequestProperty(key, params.get(key));
            }

            connection.connect();
            stream = connection.getInputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return stream;
    }

}
