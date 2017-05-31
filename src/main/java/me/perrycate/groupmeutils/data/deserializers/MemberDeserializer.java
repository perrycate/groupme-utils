package me.perrycate.groupmeutils.data.deserializers;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import me.perrycate.groupmeutils.data.Member;
import me.perrycate.groupmeutils.data.MemberBuilder;

public class MemberDeserializer extends GroupmeDeserializer
implements JsonDeserializer<Member> {

    @Override
    public Member deserialize(JsonElement jsonElement, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {

        JsonObject json = getResponse(jsonElement).getAsJsonObject();
        MemberBuilder m = new MemberBuilder();

        m.setUserId(json.get("user_id").getAsString());
        m.setNickname(json.get("nickname").getAsString());
        if (!json.get("image_url").isJsonNull()) {
            m.setImageUrl(json.get("image_url").getAsString());
        }
        m.setId(json.get("id").getAsString());
        m.setMuted(json.get("muted").getAsBoolean());
        m.setAutokicked(json.get("autokicked").getAsBoolean());

        return m.createMember();
    }

}
