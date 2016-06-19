package me.perrycate.groupmeutils.deserializers;

import java.lang.reflect.Type;
import java.time.Instant;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import me.perrycate.groupmeutils.data.Group;
import me.perrycate.groupmeutils.data.GroupBuilder;

public class GroupDeserializer extends GroupmeDeserializer
        implements JsonDeserializer<Group> {

    public Group deserialize(JsonElement jsonElement, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {

        JsonObject json = getResponse(jsonElement).getAsJsonObject();
        GroupBuilder g = new GroupBuilder();

        g.setId(json.get("id").getAsString());
        g.setName(json.get("name").getAsString());
        g.setType(json.get("type").getAsString());
        g.setDescription(json.get("description").getAsString());
        g.setImageUrl(json.get("image_url").getAsString());
        g.setCreatorUserId(json.get("creator_user_id").getAsString());
        long createTime = json.get("created_at").getAsLong();
        g.setCreatedAt(Instant.ofEpochSecond(createTime));
        long updateTime = json.get("updated_at").getAsLong();
        g.setUpdatedAt(Instant.ofEpochSecond(updateTime));
        g.setShareUrl(json.get("shareUrl").getAsString());

        //TODO this will be reworked later, see note in Group Class.
        JsonObject messages = json.get("messages").getAsJsonObject();
        g.setMessageCount(messages.get("count").getAsInt());
        g.setLastMessageId(messages.get("last_message_id").getAsString());
        long lastCreate = messages.get("last_message_created_at").getAsLong();
        g.setLastMessageCreatedAt(Instant.ofEpochSecond(lastCreate));

        return g.createGroup();
    }

}
