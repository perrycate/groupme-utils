package me.perrycate.groupmeutils.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Contains some helpful functions to make GET and POST requests to a url.
 */
public class HTTP {

    /*
     * TODO Maybe Split these into 2 parts: One that makes request and returns
     * the connection so we can check for errors using HTTP codes, another that
     * uses the first and just returns an inputStream if that's all we need.
     */

    /**
     * Sends an HTTP GET request to the given url.
     *
     * @param url
     * @return InputStream containing a stream of the response.
     */
    public static InputStream makeGETRequest(URL url) {
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

    /**
     * Sends an HTTP POST request to the given url. Does not support adding
     * request bodies at this time.
     *
     * @param url
     * @return InputStream containing a stream of the response.
     */
    public static InputStream makePOSTRequest(URL url) {
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
