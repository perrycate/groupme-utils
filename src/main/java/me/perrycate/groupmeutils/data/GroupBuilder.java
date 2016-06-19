package me.perrycate.groupmeutils.data;

import java.time.Instant;

/**
 * Constructs a Group using the builder pattern
 */
public class GroupBuilder {

    private String id;
    private String name;
    private String type; // TODO this should be an enum, once I figure out how many types there are.
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
    private Instant lastMessageCreatedAt; // Is this ever different from updatedAt?

    public Group createGroup() {
        return new Group(id, name, type, description, imageUrl,
                creatorUserId, createdAt, updatedAt, shareUrl, messageCount,
                lastMessageId, lastMessageCreatedAt);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCreatorUserId(String creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public void setLastMessageCreatedAt(Instant lastMessageCreatedAt) {
        this.lastMessageCreatedAt = lastMessageCreatedAt;
    }

}
