package me.perrycate.groupmeutils;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Scanner;

import me.perrycate.groupmeutils.api.GroupMe;
import me.perrycate.groupmeutils.api.MessageIterator;
import me.perrycate.groupmeutils.data.Group;
import me.perrycate.groupmeutils.data.Message;
import me.perrycate.groupmeutils.util.CSVWriter;

public class Stats {

    private GroupMe api;
    private CSVWriter writer;
    private String groupID;
    private File outputFile;
    private ZoneId timeZone;

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
        this.writer = new CSVWriter("0");
        this.groupID = groupID;
        this.outputFile = outputFile;
        this.timeZone = ZoneId.of("America/New_York");
    }

    public Stats(GroupMe g, String groupID, File outputFile, String timeZone) {
        this.api = g;
        this.writer = new CSVWriter("0");
        this.groupID = groupID;
        this.outputFile = outputFile;
        this.timeZone = ZoneId.of(timeZone);
    }

    public void write() {

        // Collect data
        MessageIterator messages = api.getAllMessages(groupID);
        Message currentMessage = messages.next();
        LocalDate day = LocalDateTime.ofInstant(
                currentMessage.getCreatedAt(), this.timeZone).toLocalDate();
        HashMap<String, Integer> data = new HashMap<>();
        while (messages.hasNext()) {
            currentMessage = messages.next();

            // Start new day if necessary
            LocalDate currentDay = LocalDateTime
                    .ofInstant(currentMessage.getCreatedAt(), this.timeZone)
                    .toLocalDate();

            long daysElapsed = ChronoUnit.DAYS.between(day, currentDay);
            if (daysElapsed != 0) {
                // Finish writing previous day
                writer.addRow(data);

                // If multiple days elapsed, write empty rows
                data = new HashMap<>();
                for (int i = 0; i < daysElapsed - 1; i++) {
                    writer.addRow(data);
                }

                // Start new day
                data = new HashMap<>();
                day = currentDay;
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

}
