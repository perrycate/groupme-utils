package me.perrycate.groupmeutils.api;

import java.util.Iterator;

import me.perrycate.groupmeutils.data.Group;
import me.perrycate.groupmeutils.data.GroupMessages;
import me.perrycate.groupmeutils.data.Message;
import me.perrycate.groupmeutils.util.ByteConverter;
import me.perrycate.groupmeutils.util.ChunkStorage;

/**
 * Retrieves all messages from a given group and makes them accessible as an
 * iterator.
 *
 * WARNING: High Overhead! Due to limitations in GroupMe's publicly accessible
 * API (retrieving messages must be done from the most recent messages up), this
 * class must get all messages (in 100 message chunks!) at once on construction.
 */
public class MessageIterator implements Iterator<Message> {

    // Stores messages once retrieved from the server
    private ChunkStorage storage;

    // Stores the currently-in-use chunk of 100 messages
    private Message[] currentMessages;

    // number of messages left in currentMessages
    private int remainingMessages;

    public MessageIterator(GroupMe api, String groupID) {
        storage = getAllMessages(api, groupID);
        currentMessages = loadMessages();
        remainingMessages = currentMessages.length;
    }

    @Override
    public boolean hasNext() {
        // relies on the assumption that next() will automatically load the next
        // set of messages whenever index reaches currentMessages.length;
        return remainingMessages > 0;
    }

    @Override
    public Message next() {
        Message current = currentMessages[remainingMessages - 1];
        remainingMessages--;
        if (remainingMessages == 0) {
            currentMessages = loadMessages();
            remainingMessages = currentMessages.length;
        }
        return current;
    }

    /*
     * Get each message starting from bottom.
     *
     * @return a ChunkStorage object containing all messages belonging to the
     * given group, in chunks of GroupMessages.MAX_MESSAGES.
     *
     */
    private ChunkStorage getAllMessages(GroupMe api, String groupID) {
        // We use hold messages in groups of 100 in ChunkStorage to avoid
        // attempting to store the entire group in memory while still not
        // duplicating any network calls.

        Group group = api.getGroup(groupID);
        int totalMessages = group.getMessageCount();
        String lastMessageId = group.getLastMessageId();

        ChunkStorage storage = new ChunkStorage();

        // Dump all messages to chunk storage
        GroupMessages messages;
        for (int i = 0; i < totalMessages; i += GroupMessages.MAX_MESSAGES) {
            messages = api.getMessagesBefore(groupID, lastMessageId);
            byte[] data = ByteConverter.objectToBytes(messages);
            storage.addFirst(data);
            lastMessageId = messages
                    .getMessage(messages.getMessages().length - 1).getId();
        }

        return storage;

    }

    // Pops the next set of messages out of storage. If no messages are
    // avilable, returns an empty array.
    private Message[] loadMessages() {
        if (storage.isEmpty()) {
            return new Message[0];
        }

        Object o = ByteConverter.bytesToObject(storage.removeFirst());
        GroupMessages gm = (GroupMessages) o;
        return gm.getMessages();
    }
}
