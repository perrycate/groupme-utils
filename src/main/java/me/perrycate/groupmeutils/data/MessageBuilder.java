package me.perrycate.groupmeutils.data;

import java.time.Instant;

/**
 * Constructs a Message using the builder pattern
 */
public class MessageBuilder {

    private String id;
    // Id used by client, groupme discards duplicate messages with the same guid
    private String sourceGuid;
    private Instant createdAt;
    private String userId;
    private String groupId;

    // Name and avatarUrl are of the user _at the time that this message was
    // posted_
    private String name;
    private String avatarUrl;

    private String text;
    private boolean system; // Haven't worked out what this one is yet
    private String[] favoritedBy; // Array of userIds of people who liked this
    private Attachment[] attachments;

    public MessageBuilder() {
        super();
        system = true; // all the Groupme API docs have this set to true, though it
                       // doesn't actually say what it's for.

        // Everything else we leave null and set with setters below
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSourceGuid(String sourceGuid) {
        this.sourceGuid = sourceGuid;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public void setFavoritedBy(String[] favoritedBy) {
        this.favoritedBy = favoritedBy;
    }

    public void setAttachments(Attachment[] attachments) {
        this.attachments = attachments;
    }

    public Message createMessage() {
        return new Message(id, sourceGuid, createdAt, userId, groupId, name,
                avatarUrl, text, system, favoritedBy, attachments);
    }

}
