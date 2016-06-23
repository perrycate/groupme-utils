package me.perrycate.groupmeutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public Dump(Client groupmeClient, Group group) {
        this.groupme = groupmeClient;
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
    public void dumpFromTop(File outputFile) {
        // Get each message starting from bottom, write each chunk of 100
        // messages to an individual file, then concatenate them.

        GroupMessages messages = groupme.getMessagesBefore(groupId,
                lastMessageId);

        // Create directory to store temp files in
        String dirname = "temp-" + Math.round(Math.random() * 1000000000);
        File tempdir = new File(dirname);
        tempdir.mkdir();

        // Write each group of 100 messages from the server to a sepparate
        // file in tempdir
        int totalMessages = messages.getCount();
        int fileCount = 0;
        for (int i = 0; i < totalMessages; i += Client.MAX_MESSAGES) {
            fileCount++;
            messages = groupme.getMessagesBefore(groupId, lastMessageId);
            File newFile = new File(tempdir + "/" + fileCount + ".txt");
            PrintWriter out = getPrinter(newFile);

            int length = messages.getMessages().length;
            for (int j = length - 1; j >= 0; j--) {
                print(messages.getMessage(j), out);
            }
            lastMessageId = messages.getMessage(length - 1).getId();
            out.close();
        }

        // Concatenate each mini-file into a single log
        try {
            FileOutputStream output = new FileOutputStream(outputFile);
            for (int i = fileCount; i > 0; i--) {
                Path file = Paths.get(dirname + "/" + i + ".txt");
                output.write(Files.readAllBytes(file));
                // Delete mini-file after using
                Files.delete(file);
            }
            output.close();
        } catch (FileNotFoundException e) {
            System.err.println("FATAL: Could not open output file " +
                    outputFile + " for writing.");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Delete temporary directory
        try {
            Files.delete(Paths.get(dirname));
        } catch (IOException e) {
            System.out.println("WARNING: Failed to delete temporary directory");
        }

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
