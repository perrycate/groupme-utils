package me.perrycate.groupmeutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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

        // Get temporary directory to store chunks
        Path tempdir;
        try {
            tempdir = Files.createTempDirectory("groupme-dump-");
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }

        // Dump groupme to a series of mini-files, aka chunks
        List<File> chunks = dumpToChunks(tempdir);

        // Concatenate each chunk into a single log
        try {
            FileOutputStream output = new FileOutputStream(outputFile);
            for (int i = chunks.size() - 1; i >= 0; i--) {
                Path file = chunks.get(i).toPath();
                output.write(Files.readAllBytes(file));
                // Delete chunk after reading
                Files.delete(file);
            }
            output.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Delete temporary directory. Comment this out for debugging help.
        try {
            Files.delete(tempdir);
        } catch (IOException e) {
            System.err.println(e);
            System.out.println(
                    "WARNING: Failed to delete temporary directory!"
                            + " Maybe not all messages were written to the log?");
        }
        //*/

    }

    /**
     * Scans the groupme for any new messages ocurring after the last message
     * in inputFile, and appends new messages to the bottom.
     * 
     * This method assumes that inputFile is the result of a previous
     * dumpFromTop or appendFromTop call, and thus maintains the same output
     * format.
     * 
     * Returns the number of lines appended, or -1 if inputFile could not be 
     * opened.
     *
     * TODO this method breaks if inputFile and outputFile are the same
     */
    public int appendFromTop(File inputFile, File outputFile) {

        PrintWriter output = getPrinter(outputFile);
        FileReader r;
        try {
            r = new FileReader(inputFile);
        } catch (FileNotFoundException e) {
            //TODO throw exception instead
            return -1;
        }
        BufferedReader reader = new BufferedReader(r);

        // Find the last message in the file. Ideally there is a faster way to
        // do this other than reading through it line by line from the top, but
        // I have not found it yet.
        String lastLine = "";
        String tmp = "";
        try { // TODO Almost certainly a better way to structure this
            tmp = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (tmp != null) {
            lastLine = tmp;
            output.println(lastLine); // Copy existing messages into new file
            try {
                tmp = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // get ID of last message in file
        String lastIdInFile = readIdFromLine(lastLine);

        // Append new messages to group
        // TODO we shouldn't assume that it's not a giant group of messages,
        // that is, we should dump these to chunks as in the regular dumpFromTop
        // method. First though, we need more robust dumpToChunk methods.
        GroupMessages messageGroup;
        messageGroup = groupme.getMessagesAfter(groupId, lastIdInFile);
        int diffLines = 0;
        while (messageGroup.getMessages().length > 0) {
            Message[] messages = messageGroup.getMessages();

            for (int j = 0; j < messages.length; j++) {
                print(messages[j], output);
                diffLines++;
            }

            lastIdInFile = messages[messages.length - 1].getId();

            messageGroup = groupme.getMessagesAfter(groupId, lastIdInFile);
        }
        output.close();

        return diffLines;
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

    /**
     * Returns a PrintWriter to the specified file
     */
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

    /**
     * Returns message in text format
     */
    private String format(Message message) {
        if (message.getText() == null) {
            return message.getId() + " | "
                    + message.getCreatedAt() + " | "
                    + message.getName() + " posted a picture: "
                    + "//NOT IMPLEMENTED YET, SORRY"; // TODO
        } else {
            return message.getId() + " | "
                    + message.getCreatedAt() + " | "
                    + message.getName() + ": "
                    + message.getText();
        }
    }

    /**
     * Prints message to a printwriter in text format.
     */
    private void print(Message message, PrintWriter output) {
        output.println(format(message));
    }

    /**
     * Parses out the message id from a given line. Assumes that line has been
     * extracted from a dump from one of this class's dump methods.
     */
    private String readIdFromLine(String line) {
        line = line.trim();
        // In the future we may use asterisks to denote that user liked a
        // message, might as well start planning for it now.
        if (line.startsWith("*")) {
            line = line.substring(1);
        }
        String[] divided = line.split("\\|");
        return divided[0].trim();
    }

}
