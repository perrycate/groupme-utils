package me.perrycate.groupmeutils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import me.perrycate.groupmeutils.api.GroupMe;
import me.perrycate.groupmeutils.data.Group;
import me.perrycate.groupmeutils.data.GroupMessages;
import me.perrycate.groupmeutils.data.Message;

/**
 * Dumps a groupme group to a text file. Ignores images for now.
 */
public class Dumper {
    private GroupMe groupme;
    private String groupId;
    private Group group;

    private static String ENCODING = "UTF-8";
    private static int LINE_WIDTH = 72; // Used for progress bars

    public Dumper(GroupMe groupmeClient, Group group) {
        this.groupme = groupmeClient;
        this.group = group;
        this.groupId = group.getId();
    }

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);

        // User auth
        System.out.println("Please enter your GroupMe API Token: ");
        String token = s.nextLine();
        GroupMe api = new GroupMe(token);

        // Group Selection
        System.out.println("Fetching groups list...");
        Group[] groups = api.getGroups();
        for (int i = 0; i < groups.length; i++) {
            System.out.format("[%d] %s\n", i, groups[i].getName());
        }
        System.out.print("Please select a group to dump: ");
        Group selectedGroup = groups[s.nextInt()];
        System.out.println("Selected \"" + selectedGroup.getName() + "\".");

        // Initialize dumper
        Dumper dumper = new Dumper(api, selectedGroup);

        // Skip newline
        s.nextLine();

        // Get file to use, determine if we're writing a new one or appending
        System.out.print("Enter a file name: ");
        File fileToWrite = new File(s.nextLine());
        if (fileToWrite.isFile()) {
            // Append if applicable
            System.out.println("File already exists. Append to this file? [Y/n]");
            String response = s.nextLine();

            if (response.toLowerCase().trim().startsWith("n")) {
                System.out.println("Fine then. Exiting...");
                s.close();
                return;
            }

            // Append
            long startTime = System.currentTimeMillis();
            // int messagesAdded = dumper.append(fileToWrite);
            dumper.append(fileToWrite);
            long elapsedTime = System.currentTimeMillis() - startTime;

            // Exit
            System.out.format("Completed in %d seconds. Goodbye!",
                    (int) (elapsedTime / 1000 + .5));

        } else {
            // Dump otherwise.
            System.out.println("Create new file " + fileToWrite + "? [Y/n]");
            String response = s.nextLine();

            if (response.toLowerCase().trim().startsWith("n")) {
                System.out.println("Fine then. Exiting...");
                s.close();
                return;
            }
            // Append
            long startTime = System.currentTimeMillis();
            int messagesDumped = dumper.dump(fileToWrite);
            long elapsedTime = System.currentTimeMillis() - startTime;

            // Exit
            System.out.format("Dumped %d messages in %.1f seconds. Goodbye!",
                    messagesDumped, (double) (elapsedTime / 1000));

        }
        s.close();
    }

    /**
     * Dumps the group to output in order, with most recent messages appearing
     * at the bottom.
     */
    public int dump(File outputFile) {
        // Get each message starting from bottom, then concatenate them.
        // We use hold messages in groups of 100 in ChunkStorage to avoid
        // attempting to store the entire group in memory while still not
        // duplicating any network calls.

        // TODO check that outputFile does not already exist, and if it does
        // prompt user to confirm they want to overwrite it. (Then actually
        // overwrite it, currently we just append.)
        // Actually, that'd be a good way to check to see whether we should use
        // dump() or append(), if we did that check elsewhere. We'd have to have
        // better error handling if the file doesn't match the group, the file
        // is ill-formated (or just plain not a log file), etc, in case the user
        // just accidentally specifies appending at some random-ass file we know
        // nothing about.

        // TODO could just use Message[] instead of GroupMessages, looks nicer
        // is all

        // Get each message in groups of GroupMessages.MAX_MESSAGES. Store in
        // chunks to conserve memory.
        ChunkStorage storage = new ChunkStorage();
        int totalMessages = group.getMessageCount();
        String lastMessageId = group.getLastMessageId();
        GroupMessages messages;

        ProgressBar bar = new ProgressBar(System.out, LINE_WIDTH,
                totalMessages / GroupMessages.MAX_MESSAGES);

        for (int i = 0; i < totalMessages; i += GroupMessages.MAX_MESSAGES) {
            messages = groupme.getMessagesBefore(groupId, lastMessageId);
            writeChunk(messages.getMessages(), storage);
            lastMessageId = messages
                    .getMessage(messages.getMessages().length - 1).getId();
            bar.update();
        }

        // Concatenate each chunk into a single log
        try (
                OutputStream o = Files.newOutputStream(outputFile.toPath(),
                        StandardOpenOption.APPEND, StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE);

                BufferedOutputStream output = new BufferedOutputStream(o);) {

            while (storage.size() != 0) {
                output.write(storage.removeFirst());
            }

            storage.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return totalMessages;
    }
    // */

    /**
     * Scans the groupme for any new messages ocurring after the last message in
     * inputFile, and appends new messages to the bottom.
     *
     * This method assumes that inputFile is the result of a previous
     * dumpFromTop or appendFromTop call, and thus maintains the same output
     * format.
     *
     */
    public int append(File sourceFile) {

        // TODO we could in fact share more code with dump if we only
        // made note of the last message in the group, but then got messages in
        // the same order as dump() (bottom-up) and just stop when we reach the
        // message Id we made note of. The downside is that if people add to
        // the chat while append() is running, these messages won't be added.
        // On the other hand though, this would eliminate the concern of the
        // message count being innacurate for the same reason, so maybe that's
        // a good thing?

        FileReader r;
        try {
            r = new FileReader(sourceFile);
        } catch (FileNotFoundException e) {
            System.err.println("FATAL: Could not find file " + sourceFile + "!");
            return -1;
        }

        // Find the last message in the file. Ideally there is a faster way to
        // do this other than reading through it line by line from the top, but
        // I have not found it yet.
        // TODO there certainly must be, especially now that we don't have to
        // copy things into new file, we just need to get the last line in file.
        String lastLine = "";
        String tmp = "";
        try (BufferedReader reader = new BufferedReader(r);) {
            tmp = reader.readLine();

            while (tmp != null) {
                lastLine = tmp;
                tmp = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO if no id found, should tell that to the user.

        // get ID of last message in file
        String firstMessageId = readIdFromLine(lastLine);

        // Append new messages to group, storing messages in file chunks to
        // avoid keeping them all in memory.
        ChunkStorage storage = new ChunkStorage();
        GroupMessages messages = groupme.getMessagesAfter(groupId,
                firstMessageId);

        int nrOfMessages = messages.getMessages().length;

        while (messages.getMessages().length > 0) {

            // Have to reverse message order since getMessagesAfter's order is
            // opposite to getMessagesBefore. Inconvenient, but I'm trying to
            // keep it consistent with GroupMe's API for now, for better or
            // worse.
            List<Message> m = Arrays.asList(messages.getMessages());

            // WARNING/TODO/NOTE: fixing the "GroupMessages not being
            // defensively
            // copied bug" will require change here, since the current code is
            // working around messages being reversed.
            Collections.reverse(m);
            writeChunk(m.toArray(new Message[0]), storage);

            // Remember, we reversed the array
            firstMessageId = messages.getMessage(0).getId();
            // Different from dump(): this gets Messages AFTER messageId
            messages = groupme.getMessagesAfter(groupId, firstMessageId);
        }

        // Concatenate each chunk into a single log
        try (
                OutputStream o = Files.newOutputStream(sourceFile.toPath(),
                        StandardOpenOption.APPEND, StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE);

                BufferedOutputStream output = new BufferedOutputStream(o);) {

            while (storage.size() != 0) {
                // Different from dump(): this removes from the first, not last
                output.write(storage.removeLast());
            }

            storage.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return nrOfMessages;

    }
    // */

    /**
     * Writes messages to specified chunk. (A chunk is just a short text file
     * containing 100 or less messages in text format.)
     */
    private void writeChunk(Message[] messages, ChunkStorage storage) {

        int length = messages.length;

        try (ByteArrayOutputStream chunk = new ByteArrayOutputStream()) {
            // concatenate each message into a series of bytes in a chunk
            for (int i = length - 1; i >= 0; i--) {
                Message message = messages[i];
                String text = format(message) + '\n';
                byte[] textInBytes = text.getBytes(Charset.forName(ENCODING));
                chunk.write(textInBytes);
            }

            // Write chunk to storage
            storage.addFirst(chunk.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
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
