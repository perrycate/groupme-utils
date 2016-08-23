package me.perrycate.groupmeutils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.perrycate.groupmeutils.api.Client;
import me.perrycate.groupmeutils.data.Group;
import me.perrycate.groupmeutils.data.GroupMessages;
import me.perrycate.groupmeutils.data.Message;

/**
 * Dumps a groupme group to a text file. Ignores images for now.
 */
public class Dumper {
    private Client groupme;
    private String groupId;
    private Group group;

    // TODO let client change this
    private static String ENCODING = "UTF-8";

    public Dumper(Client groupmeClient, Group group) {
        this.groupme = groupmeClient;
        this.group = group;
        this.groupId = group.getId();
    }

    /**
     * Dumps the group to output in order, with most recent messages appearing
     * at the bottom.
     */
    public void dump(Path outputFile) {
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

        // TODO get first message

        // TODO could return int ie number of messages written (or lines? is
        // there a difference?)

        // TODO could just use Message[] instead of GroupMessages, looks nicer is all

        // Get each message in groups of Client.MAX_MESSAGES. Store in chunks to
        // conserve memory.
        ChunkStorage storage = new ChunkStorage();
        int totalMessages = group.getMessageCount();
        String lastMessageId = group.getLastMessageId();
        GroupMessages messages;
        for (int i = 0; i < totalMessages; i += Client.MAX_MESSAGES) {
            messages = groupme.getMessagesBefore(groupId, lastMessageId);
            writeChunk(messages.getMessages(), storage);
            lastMessageId = messages
                    .getMessage(messages.getMessages().length - 1).getId();
        }

        // Concatenate each chunk into a single log
        try (
                OutputStream o = Files.newOutputStream(outputFile,
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

    }
    //*/

    /**
     * Scans the groupme for any new messages ocurring after the last message
     * in inputFile, and appends new messages to the bottom.
     * 
     * This method assumes that inputFile is the result of a previous
     * dumpFromTop or appendFromTop call, and thus maintains the same output
     * format.
     * 
     */
    public void append(Path sourceFile) {
        // TODO I still think this could share more code with dump().

        // TODO we could in fact share more code with dump if we only
        // made note of the last message in the group, but then got messages in
        // the same order as dump() (bottom-up) and just stop when we reach the
        // message Id we made note of. The downside is that if people add to
        // the chat while append() is running, these messages won't be added.
        // On the other hand though, this would eliminate the concern of the
        // message count being innacurate for the same reason, so maybe that's
        // a good thing?

        // TODO return lines changed.

        FileReader r;
        try {
            r = new FileReader(sourceFile.toFile());
        } catch (FileNotFoundException e) {
            // TODO could recover from this easily, ie just create file or call
            // dump. (See similar giant-ass TODO in dump method)
            System.err
                    .println("FATAL: Could not find file " + sourceFile + "!");
            return;
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

        // TODO in order to have a progress bar with this we need to know how
        // many new messages there are. Have a number at the very bottom of the
        // file containing the # of messages. When we append, provided that there
        // is no new line after the number, we can overwrite it with '\r', then
        // after appending all the messages we can append our new count.
        // (be aware that count may have changed while we were appending stuff.)
        GroupMessages messages = groupme.getMessagesAfter(groupId,
                firstMessageId);
        while (messages.getMessages().length > 0) {

            // Have to reverse message order since getMessagesAfter's order is
            // opposite to getMessagesBefore. Inconvenient, but I'm trying to
            // keep it consistent with GroupMe's API for now, for better or worse.
            List<Message> m = Arrays.asList(messages.getMessages());

            //  WARNING/TODO: This also changes order of messages in the
            // GroupMessagesObject. This is a bug, GroupMessages should've
            // devensively copied. NOTE that fixing the bug will require change
            // later on here since the current code is currently working around
            // messages being reversed.
            Collections.reverse(m);
            writeChunk(m.toArray(new Message[0]), storage);

            // Remember, we reversed the array
            firstMessageId = messages.getMessage(0).getId();
            // Different from dump(): this gets Messages AFTER messageId
            messages = groupme.getMessagesAfter(groupId, firstMessageId);
        }

        // Concatenate each chunk into a single log
        try (
                OutputStream o = Files.newOutputStream(sourceFile,
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

    }
    //*/

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
        // TODO This is lazy and wrong. Just because text is null doesn't mean
        // there's a picture, and just because there is text doesn't mean that
        // there's not also a picture.
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
