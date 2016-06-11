package me.perrycate.groupmeutils.data;

import java.time.Instant;

/**
 * Immutable class that represents a single message posted to a group.
 */
public final class Message {

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

    public Message(String id, String sourceGuid, Instant createdAt,
            String userId, String groupId, String name, String avatarUrl,
            String text, boolean system, String[] favoritedBy,
            Attachment[] attachments) {
        super();
        this.id = id;
        this.sourceGuid = sourceGuid;
        this.createdAt = createdAt;
        this.userId = userId;
        this.groupId = groupId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.text = text;
        this.system = system;
        this.favoritedBy = favoritedBy;
        this.attachments = attachments;
    }

    public String getId() {
        return id;
    }

    public String getSourceGuid() {
        return sourceGuid;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getText() {
        return text;
    }

    public boolean isSystem() {
        return system;
    }

    public String[] getFavoritedBy() {
        return favoritedBy;
    }

    public Attachment[] getAttachments() {
        return attachments;
    }

}
