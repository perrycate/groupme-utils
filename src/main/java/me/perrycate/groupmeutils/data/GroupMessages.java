package me.perrycate.groupmeutils.data;

/**
 * Represents a group of messages from a group
 * 
 * Note: The GroupMe API only sends messages in groups of up to 100, so
 * getCount() (the number of messages total in a group) will usually be much
 * larger than the number of messages contained in a GroupMessages object.
 */
public class GroupMessages {

    private Message[] messages;
    private int count; // Total number of messages in the group.

    public GroupMessages() {
    }

    public GroupMessages(int count, Message[] messages) {
        this.messages = messages;
        this.count = count;
    }

    public Message[] getMessages() {
        return messages;
    }

    public Message getMessage(int index) {
        return messages[index];
    }

    public int getCount() {
        return count;
    }

    public void setMessages(Message[] messages) {
        this.messages = messages;
    }

}
