package me.perrycate.groupmeutils.deserializers;

import me.perrycate.groupmeutils.data.*;

import java.lang.reflect.Type;

import com.google.gson.*;

/**
 * Deserializes a JsonElement into a MessageCollection Object
 */
public class MessageCollectionDeserializer
        implements JsonDeserializer<MessageCollection> {

    public MessageCollection deserialize(JsonElement jsonElement, Type typeOfT,
            JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject json = jsonElement.getAsJsonObject();
        int count = json.get("count").getAsInt();
        Message[] messages = new Message[count];
        JsonArray messagesJson = json.get("messages").getAsJsonArray();

        // Deserialize each message, add to array
        for (int i = 0; i < count; i++) {
            JsonElement elem = messagesJson.get(i);
            // leave actual deserialization to MessageDeserializer
            messages[i] = context.deserialize(elem, Message.class);
        }

        return new MessageCollection(messages);
    }

}