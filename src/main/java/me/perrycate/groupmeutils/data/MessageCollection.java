package me.perrycate.groupmeutils.data;

/**
 * Represents a group of messages, for example the most recent 20 messages
 * in a group.
 */
public class MessageCollection {

    private Message[] messages;
    private int count; // this is technically unnecessary since we could just
                       // use messages.length, but it's better to match what
                       // what we're getting from GroupMe's API.

    public MessageCollection() {
    }

    public MessageCollection(Message[] messages) {
        this.messages = messages;
        count = messages.length;
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
