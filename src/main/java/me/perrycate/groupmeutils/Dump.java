package me.perrycate.groupmeutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import me.perrycate.groupmeutils.api.Client;
import me.perrycate.groupmeutils.data.Group;
import me.perrycate.groupmeutils.data.GroupMessages;
import me.perrycate.groupmeutils.data.Message;

/**
 * Dumps a groupme group to a text file. Ignores images for now.
 */
public class Dump {
    private Client groupme;
    private String groupId;
    private String lastMessageId;
    private Group group;

    public Dump(String token, Group group) {
        groupme = new Client(token);
        this.group = group;
        this.groupId = group.getId();
        this.lastMessageId = group.getLastMessageId();
    }

    /**
     * Dumps the group to output starting at the specified lastMessageId. The
     * messages will appear in the file in reverse order, with most recent at
     * the top.
     */
    public void dumpFromBottom(File output) {
        PrintWriter out = getPrinter(output);
        GroupMessages messages;
        messages = groupme.getMessagesBefore(groupId, lastMessageId);

        int totalMessages = messages.getCount();
        for (int i = 0; i < totalMessages; i += Client.MAX_MESSAGES) {
            messages = groupme.getMessagesBefore(groupId, lastMessageId);
            int length = messages.getMessages().length;
            int j;
            for (j = 0; j < length; j++) {
                print(messages.getMessage(j), out);
            }
            lastMessageId = messages.getMessage(j - 1).getId();
        }
        out.close();
    }

    /**
     * Dumps the group to output in order, with most recent messages appearing
     * at the bottom.
     */
    public void dumpFromTop(File output) {
        // Get each message starting from bottom, write each chunk of 100
        // messages to an individual file, then concatenate them.

        PrintWriter out = getPrinter(output);
        GroupMessages messages = groupme.getMessagesBefore(groupId,
                lastMessageId);

        // Create temporary output directory

        int totalMessages = messages.getCount();
    }

    private PrintWriter getPrinter(File file) {

        try {
            return new PrintWriter(file);
        } catch (FileNotFoundException e) {
            // This should never happen
            System.err.println("FATAL: Failed to open file " + file.getName()
                    + " for writing.");
            System.exit(1);
            return null;
        }
    }

    private void print(Message message, PrintWriter output) {
        if (message.getText() == null) {
            output.println(message.getId() + " | "
                    + message.getCreatedAt() + " | "
                    + message.getName() + " posted a picture: "
                    + "//NOT IMPLEMENTED YET, SORRY"); // TODO
        } else {
            output.println(message.getId() + " | "
                    + message.getCreatedAt() + " | "
                    + message.getName() + ": "
                    + message.getText());
        }
    }

}
