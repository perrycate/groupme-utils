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
import me.perrycate.groupmeutils.data.deserializers.GroupArrayDeserializer;
import me.perrycate.groupmeutils.data.deserializers.GroupDeserializer;
import me.perrycate.groupmeutils.data.deserializers.GroupMessagesDeserializer;
import me.perrycate.groupmeutils.data.deserializers.MessageDeserializer;
import me.perrycate.groupmeutils.data.Group;
import me.perrycate.groupmeutils.data.GroupMessages;

/**
 * Contains useful methods for interacting with the GroupMe Api that return
 * serialized objects from the groupmeutils.data package.
 */
public class Client {
    // HTTP related constants
    private static final String BASE_URL = "https://api.groupme.com/v3";
    private static final String CHARSET = "UTF-8";

    // GroupMe Api Constants
    /** The maximum number of messages that can be retrieved from a group in a
     * single network request. */
    public static final int MAX_MESSAGES = 100;

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
        gsonBuilder.registerTypeAdapter(Group.class, new GroupDeserializer());
        gsonBuilder.registerTypeAdapter(Group[].class,
                new GroupArrayDeserializer());

        gson = gsonBuilder.create();
    }

    /**
     * Returns an array of Groups that the user is in.
     */
    public Group[] getGroups() {
        String target = "/groups";

        InputStream resultStream = makeGETRequest(createUrl(target));

        Reader reader = new InputStreamReader(resultStream);

        return gson.fromJson(reader, Group[].class);
    }

    /**
     * Returns a GroupMessages object containing the 100 messages immediately
     * following the message with id afterId, ordered by createdAt ascending.
     * (The oldest message will be at index 0)
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

    // TODO getMessage

    /**
     * Returns a GroupMessages object containing the 100 messages immediately
     * preceding the message with id beforeId, ordered by createdAt descending. 
     * (The newest message message will be at index 0)
     */
    public GroupMessages getMessagesBefore(String groupId, String beforeId) {
        String target = "/groups/" + groupId + "/messages";

        // get request url
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("before_id", beforeId);
        params.put("limit", "" + MAX_MESSAGES);
        URL url = createUrl(target, params);

        // Make request
        InputStream resultStream = makeGETRequest(url);

        // Deserialize returned JSON into a MessageCollection
        Reader reader = new InputStreamReader(resultStream);
        return gson.fromJson(reader, GroupMessages.class);
    }

    /**
     * Returns a group matching the given id
     */
    public Group getGroup(String id) {
        String target = "/groups/" + id;

        // Get request url
        HashMap<String, String> params = new HashMap<String, String>();
        URL url = createUrl(target, params);

        // Make request
        InputStream resultStream = makeGETRequest(url);

        // Deserialize returned JSON into a Group
        Reader reader = new InputStreamReader(resultStream);
        return gson.fromJson(reader, Group.class);
    }

    /**
     * Likes the given message. Returns true if successful, false otherwise.
     */
    public boolean likeMessage(Message m) {
        String messageid = m.getId();
        String groupId = m.getGroupId();

        URL url = createUrl("/messages/" + groupId + "/" + messageid + "/like");
        InputStream result = makePOSTRequest(url);

        // Probably a better way to check for success/failure than this.
        if (result != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Unlikes the given message. Returns true if successful, false otherwise.
     */
    public boolean unlikeMessage(Message m) {
        String messageid = m.getId();
        String groupId = m.getGroupId();

        URL url = createUrl(
                "/messages/" + groupId + "/" + messageid + "/unlike");
        InputStream result = makePOSTRequest(url);

        // Probably a better way to check for success/failure than this.
        if (result != null) {
            return true;
        } else {
            return false;
        }
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

    /**
     * TODO Maybe Split these into 2 parts: One that makes request and returns
     * the connection so we can check for errors using HTTP codes, another that
     * uses the first and just returns an inputStream if that's all we need.
     */
    public InputStream makeGETRequest(URL url) {
        InputStream stream = null;

        try {
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");

            connection.connect();
            stream = connection.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return stream;
    }

    public InputStream makePOSTRequest(URL url) {
        InputStream stream = null;

        try {
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("POST");

            connection.connect();
            stream = connection.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return stream;
    }

}
