package me.perrycate.groupmeutils.deserializers;

import me.perrycate.groupmeutils.data.*;

import java.lang.reflect.Type;

import com.google.gson.*;

/**
 * Deserializes a JsonElement into a MessageCollection Object
 */
public class GroupMessagesDeserializer
        implements JsonDeserializer<GroupMessages> {

    public GroupMessages deserialize(JsonElement jsonElement, Type typeOfT,
            JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject json = jsonElement.getAsJsonObject();
        JsonObject response = json.get("response").getAsJsonObject();

        JsonArray messagesJson = response.get("messages").getAsJsonArray();
        int count = response.get("count").getAsInt();
        int numMessages = messagesJson.size(); // different from count!
        Message[] messages = new Message[numMessages];

        // Deserialize each message, add to array
        for (int i = 0; i < numMessages; i++) {
            JsonElement elem = messagesJson.get(i);
            // leave actual deserialization to MessageDeserializer
            messages[i] = context.deserialize(elem, Message.class);
        }

        return new GroupMessages(count, messages);
    }

}