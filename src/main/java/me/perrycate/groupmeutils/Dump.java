package me.perrycate.groupmeutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import me.perrycate.groupmeutils.api.Client;
import me.perrycate.groupmeutils.data.GroupMessages;
import me.perrycate.groupmeutils.data.Message;

/**
 * Dumps a groupme group to a text file. Ignores images for now.
 */
public class Dump {
    private Client groupme;
    private String groupId;
    private PrintWriter output; // File to dump text to
    private File outputFolder; // Directory to save images to

    public Dump(String token, String id, File file,
            File folder) {
        groupme = new Client(token);
        this.groupId = id;
        this.outputFolder = folder;

        try {
            this.output = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            // This should never happen
            System.err.println("FATAL: Failed to open file " + file.getName()
                    + " for writing.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
    }

    /**
     * Dumps the groupme to output starting at the specified lastMessageId. The
     * messages will appear in the file in reverse order, with most recent at
     * the top.
     */
    public void dumpFromBottom(String lastMessageId) {
        GroupMessages messages;
        while (true) {
            messages = groupme.getMessagesBefore(groupId, lastMessageId);
            int length = messages.getMessages().length;
            int i;
            for (i = 0; i < length; i++) {
                print(messages.getMessage(i));
            }
            lastMessageId = messages.getMessage(i - 1).getId();
        }
    }

    public void print(Message message) {
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
