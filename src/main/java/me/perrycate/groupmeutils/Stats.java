package me.perrycate.groupmeutils;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Scanner;

import me.perrycate.groupmeutils.api.GroupMe;
import me.perrycate.groupmeutils.api.MessageIterator;
import me.perrycate.groupmeutils.data.Group;
import me.perrycate.groupmeutils.data.Message;
import me.perrycate.groupmeutils.util.CSVWriter;

public class Stats {

    private GroupMe api;
    private CSVWriter<Integer> writer;
    private String groupID;
    private File outputFile;

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);

        // User auth
        System.out.println("Please enter your GroupMe API Token: ");
        GroupMe api = new GroupMe(s.nextLine());

        // Select group
        System.out.println("Fetching groups list...");
        Group[] groups = api.getGroups();
        for (int i = 0; i < groups.length; i++) {
            System.out.format("[%d] %s\n", i, groups[i].getName());
        }
        System.out.print("Please select a group to dump: ");
        Group selectedGroup = groups[Integer.parseInt(s.nextLine())];
        System.out.println("Selected \"" + selectedGroup.getName() + "\".");

        // Create output file
        System.out.println("Please enter a new file to save data to: ");
        File out = new File(s.nextLine());

        // Analyze and dump
        Stats stats = new Stats(api, selectedGroup.getId(), out);
        stats.write();

        s.close();
    }

    public Stats(GroupMe g, String groupID, File outputFile) {
        this.api = g;
        this.writer = new CSVWriter<Integer>();
        this.groupID = groupID;
        this.outputFile = outputFile;
    }

    public void write() {

        // Collect data
        MessageIterator messages = api.getAllMessages(groupID);
        Message currentMessage;
        int day = -1;
        HashMap<String, Integer> data = new HashMap<String, Integer>();
        while (messages.hasNext()) {
            currentMessage = messages.next();

            // Start new day if necessary
            int currentDay = getDay(currentMessage.getCreatedAt());
            if (day != currentDay) {
                writer.addRow(data);
                day = currentDay;
                data = new HashMap<String, Integer>();
            }

            // Add data for this message
            incrementPosts(currentMessage.getUserId(), data);
            if (currentMessage.getFavoritedBy() != null)
                incrementLikes(currentMessage.getFavoritedBy(), data);

        }
        // Add final row
        writer.addRow(data);

        // Write and finish
        writer.writeTo(outputFile);
        System.out.println("Finished!");

    }

    private void incrementPosts(String postedBy, HashMap<String, Integer> data) {
        String id = postedBy + "_posts";
        int postCount = data.getOrDefault(id, 0);
        data.put(id, postCount + 1);
    }

    private void incrementLikes(String[] likedBy, HashMap<String, Integer> data) {
        String id;
        for (String user : likedBy) {
            id = user + "_likes";
            int likeCount = data.getOrDefault(id, 0);
            data.put(id, likeCount + 1);
        }
    }

    /**
     * Returns the day of the year that i occurred in.
     */
    private int getDay(Instant i) {
        LocalDateTime date = LocalDateTime.ofInstant(i, ZoneId.of("America/New_York"));
        return date.getDayOfYear();
    }

}
