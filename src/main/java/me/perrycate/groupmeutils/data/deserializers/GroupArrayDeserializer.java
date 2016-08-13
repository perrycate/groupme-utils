package me.perrycate.groupmeutils.data.deserializers;

import java.lang.reflect.Type;
import java.time.Instant;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import me.perrycate.groupmeutils.data.Group;
import me.perrycate.groupmeutils.data.GroupBuilder;

/**
 * Deserializes a JSON array of groups into a Java Array of Groups.
 */
public class GroupArrayDeserializer extends GroupmeDeserializer
        implements JsonDeserializer<Group[]> {

    public Group[] deserialize(JsonElement jsonElement, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {

        JsonArray response = getResponse(jsonElement).getAsJsonArray();

        // Deserialize one group at a time, add to new groups array.
        int numGroups = response.size();
        Group[] groups = new Group[numGroups];
        for (int i = 0; i < numGroups; i++) {
            JsonElement elem = response.get(i);
            groups[i] = context.deserialize(elem, Group.class);
        }

        return groups;
    }

}
