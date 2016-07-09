package me.perrycate.groupmeutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
        // We do this to avoid attempting to store the entire group in memory
        // while still not duplicating any network calls.

        // Create temporary directory to store chunks
        String dirname = "temp-" + Math.round(Math.random() * 1000000000);
        File tempdir = new File(dirname);
        tempdir.mkdir();

        // Dump groupme to a series of mini-files, aka chunks
        List<File> chunks = dumpToChunks(tempdir.toPath());

        // Concatenate each chunk into a single log
        try {
            FileOutputStream output = new FileOutputStream(outputFile);
            for (int i = chunks.size() - 1; i >= 0; i--) {
                Path file = chunks.get(i).toPath();
                output.write(Files.readAllBytes(file));
                // Delete chunk after reading
                //Files.delete(file);
            }
            output.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Delete temporary directory. Comment this out for debugging help.
        try {
            Files.delete(tempdir.toPath());
        } catch (IOException e) {
            System.out.println(
                    "WARNING: Failed to delete temporary directory!"
                            + " Maybe not all messages were written to the log?");
        }
        //*/

    }

    /**
     * Dumps the group to a specified outputFolder. Messages will be dumped to a
     * series of .txt files, each of length 100 with the most recent message at
     * the bottom. The .txt files are numbered, so the most recent 100 messages
     * in the group are in 1.txt, the ones immediately preceding those are in
     * 2.txt, and so for. Otherwise, return a list of Path objects pointing to
     * the chunks created, in the order that they were created. (list[0] was the
     *  first chunk created, and so forth.)
     */
    public List<File> dumpToChunks(Path outputFolder) {

        ArrayList<File> chunksWritten = new ArrayList<File>();
        //TODO: BUG: The first chunk will be missing the latest message to the
        // group. This is because the getMessagesBefore excludes lastMessageId.  
        // Best fix I can think of is making a separate getMessages method
        // that just gets the most recent messages, and using that here (just in
        // this one line) instead of getMessagesBefore
        GroupMessages messages = groupme.getMessagesBefore(groupId,
                lastMessageId);

        // Write each group of 100 messages from the server to a separate
        // file in outputFolder
        int totalMessages = messages.getCount();
        for (int i = 0; i < totalMessages; i += Client.MAX_MESSAGES) {
            messages = groupme.getMessagesBefore(groupId, lastMessageId);
            File newFile = new File(outputFolder + "/" + chunksWritten.size() +
                    ".txt");
            writeChunk(messages, newFile);
            chunksWritten.add(newFile);
        }

        return chunksWritten;

    }

    /**
     * Writes messages to specified chunk. (A chunk is just a short text file
     * containing 100 or less messages in text format.) 
     */
    private void writeChunk(GroupMessages messages, File chunk) {
        PrintWriter out = getPrinter(chunk);

        int length = messages.getMessages().length;
        for (int j = length - 1; j >= 0; j--) {
            print(messages.getMessage(j), out);
        }
        lastMessageId = messages.getMessage(length - 1).getId();
        out.close();
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
