package me.perrycate.groupmeutils.deserializers;

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
     * process of being deserialized. This method takes a jsonElement and 
     * returns it such that in either case the deserializers are dealing with
     * the same thing.
     */
    public JsonObject getResponse(JsonElement jsonElement) {
        JsonObject json = jsonElement.getAsJsonObject();

        if (json.has("response")) {
            return json.get("response").getAsJsonObject();
        } else {
            return json;
        }

    }
}
