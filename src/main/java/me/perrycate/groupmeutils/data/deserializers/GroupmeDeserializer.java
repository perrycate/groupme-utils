package me.perrycate.groupmeutils.data.deserializers;

import com.google.gson.JsonObject;

import com.google.gson.JsonElement;

/**
 * Superclass for Deserializers that need to process something coming from
 * groupme.
 */
public class GroupmeDeserializer {

    /**
     * If jsonElement has a response property, return whatever's inside.
     * Otherwise, return jsonElement.
     * 
     * Deserializers will sometimes recieve JSON directly from the groupme
     * server, (and thus the actual body is the value of a "response" element),
     * and sometimes as a call from a halfway-parsed other json object in the
     * process of being deserialized. JSON directly from groupme's
     * servers includes an "envelope" with some extra metadata like status code.
     * This strips that information so we're working with the same JSON object
     * regardless of source.
     */
    public JsonElement getResponse(JsonElement jsonElement) {
        JsonObject json = jsonElement.getAsJsonObject();
        if (json.has("response")) {
            return json.get("response");
        } else {
            return jsonElement;
        }

    }

    /**
     * Returns the HTTP code associated with a response from the groupme servers 
     */
    private int getErrorCode(JsonElement json) {
        JsonObject envelope = json.getAsJsonObject();
        JsonObject metadata = envelope.get("meta").getAsJsonObject();
        return metadata.get("code").getAsInt();
    }
}
