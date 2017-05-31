package me.perrycate.groupmeutils.data;

import java.time.Instant;

/**
 * Immutable class that represents a groupme group
 */
public final class Group {

    private String id;
    private String name;
    private String type; // TODO this should be an enum, once I figure out how
    // many types there are.
    private String description;
    private String imageUrl;
    private String creatorUserId;
    private Instant createdAt;
    private Instant updatedAt; // not 100% certain what this is for yet.
    // TODO private Member members;
    private String shareUrl; // Huh, I didn't even realise this was a thing.

    // TODO these properties are actually inside a nested object under
    // "messages", however they are not contained in the MessageGroup class.
    // For now I'm just including them here, however further thought may be
    // required. Also, there are several other bits of info not being used here,
    // for example a preview object.
    private int messageCount;
    private String lastMessageId;
    private Instant lastMessageCreatedAt; // Is this ever different from
    // updatedAt?
    private Member[] members;

    public Group(String id, String name, String type, String description,
            String imageUrl, String creatorUserId, Instant createdAt,
            Instant updatedAt, String shareUrl, int messageCount,
            String lastMessageId, Instant lastMessageCreatedAt, Member[] members) {
        super();
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.imageUrl = imageUrl;
        this.creatorUserId = creatorUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.shareUrl = shareUrl;
        this.messageCount = messageCount;
        this.lastMessageId = lastMessageId;
        this.lastMessageCreatedAt = lastMessageCreatedAt;
        this.members = members;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCreatorUserId() {
        return creatorUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public String getLastMessageId() {
        return lastMessageId;
    }

    public Instant getLastMessageCreatedAt() {
        return lastMessageCreatedAt;
    }

    public Member[] getMembers() {
        return members;
    }

}
