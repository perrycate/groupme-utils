package me.perrycate.groupmeutils.data;

import java.io.Serializable;

/**
 * Represents a group of messages from a group
 *
 * Note: The GroupMe API only sends messages in groups of up to 100, so
 * getCount() (the number of messages total in a group) will usually be much
 * larger than the number of messages contained in a GroupMessages object.
 */
public class GroupMessages implements Serializable {

    private static final long serialVersionUID = 6584230480555998937L;

    /**
     * The maximum number of messages that GroupMe will send a GroupMessages
     * object with.
     */
    public static final int MAX_MESSAGES = 100;

    private Message[] messages;
    private int count; // Total number of messages in the group.

    public GroupMessages() {
    }

    public GroupMessages(int count, Message[] messages) {
        this.messages = messages;
        this.count = count;
    }

    /**
     * Returns a list of the messages this GroupMessages object was created
     * with. What these messages are depends on what created this object.
     */
    public Message[] getMessages() {
        return messages;
    }

    /**
     * Returns the message at index index.
     */
    public Message getMessage(int index) {
        return messages[index];
    }

    /**
     * WARNING: Following the pattern set by groupme's API, this returns the
     * total number of messages in the group this GroupMessage's messages are
     * from. However, groupme only sends messages in groups of 100, so
     * this.getCount() is often very different from this.getMessages().length.
     */
    public int getCount() {
        return count;
    }
}
