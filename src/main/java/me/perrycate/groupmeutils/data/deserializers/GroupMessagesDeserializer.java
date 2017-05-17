package me.perrycate.groupmeutils.data.deserializers;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import me.perrycate.groupmeutils.data.GroupMessages;
import me.perrycate.groupmeutils.data.Message;

/**
 * Deserializes a JsonElement into a MessageCollection Object
 */
public class GroupMessagesDeserializer extends GroupmeDeserializer
        implements JsonDeserializer<GroupMessages> {

    public GroupMessages deserialize(JsonElement jsonElement, Type typeOfT,
            JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject response = getResponse(jsonElement).getAsJsonObject();

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
