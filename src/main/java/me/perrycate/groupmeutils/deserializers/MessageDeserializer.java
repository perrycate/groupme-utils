package me.perrycate.groupmeutils.deserializers;

import me.perrycate.groupmeutils.data.*;

import java.lang.reflect.Type;
import java.time.Instant;

import com.google.gson.*;

/**
 * Deserializes a JsonElement into a Message Object
 */
public class MessageDeserializer extends GroupmeDeserializer
        implements JsonDeserializer<Message> {

    public Message deserialize(JsonElement jsonElement, Type typeOfT,
            JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject json = getResponse(jsonElement).getAsJsonObject();
        MessageBuilder m = new MessageBuilder();

        // Set primitive properties
        m.setId(json.get("id").getAsString());
        m.setSourceGuid(json.get("source_guid").getAsString());
        long createTime = json.get("created_at").getAsLong();
        m.setCreatedAt(Instant.ofEpochSecond(createTime));
        m.setUserId(json.get("user_id").getAsString());
        m.setGroupId(json.get("group_id").getAsString());
        m.setName(json.get("name").getAsString());
        if (!json.get("avatar_url").isJsonNull()) { // null if user is using default avatar
            m.setAvatarUrl(json.get("avatar_url").getAsString());
        }
        if (!json.get("text").isJsonNull()) { // could happen if only image sent
            m.setText(json.get("text").getAsString());
        }
        m.setSystem(json.get("system").getAsBoolean());

        // Set list of ids of people who liked this message 
        JsonArray favoritesAsJson = json.get("favorited_by").getAsJsonArray();
        int length = favoritesAsJson.size();
        String[] favorites = new String[length];
        for (int i = 0; i < length; i++) {
            favorites[i] = favoritesAsJson.get(i).getAsString();
        }

        // For now, we ignore any attachments. TODO implement attachments

        return m.createMessage();
    }

}