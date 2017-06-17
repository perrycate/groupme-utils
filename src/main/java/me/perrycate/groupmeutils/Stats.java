package me.perrycate.groupmeutils;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import me.perrycate.groupmeutils.api.GroupMe;
import me.perrycate.groupmeutils.api.MessageIterator;
import me.perrycate.groupmeutils.data.Group;
import me.perrycate.groupmeutils.data.Member;
import me.perrycate.groupmeutils.data.Message;
import me.perrycate.groupmeutils.util.CSVWriter;

public class Stats {

    private GroupMe api;
    private Group group;
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
        Stats stats = new Stats(api, selectedGroup, out);
        stats.write();

        s.close();
    }

    public Stats(GroupMe g, Group group, File outputFile) {
        this.api = g;
        this.group = group;
        this.outputFile = outputFile;
        this.timeZone = ZoneId.of("America/New_York");
    }

    public Stats(GroupMe g, Group group, File outputFile, String timeZone) {
        this.api = g;
        this.group = group;
        this.outputFile = outputFile;
        this.timeZone = ZoneId.of(timeZone);
    }

    public void write() {

        List<Map<String, Integer>> grid = new ArrayList<>();
        HashMap<String, Integer> data = new HashMap<>();

        MessageIterator messages = api.getAllMessages(group.getId());
        Message currentMessage = messages.next();
        LocalDate day = LocalDateTime.ofInstant(
                currentMessage.getCreatedAt(), this.timeZone).toLocalDate();

        while (messages.hasNext()) {
            currentMessage = messages.next();

            // Start new day if necessary
            LocalDate currentDay = LocalDateTime
                    .ofInstant(currentMessage.getCreatedAt(), this.timeZone)
                    .toLocalDate();

            long daysElapsed = ChronoUnit.DAYS.between(day, currentDay);
            if (daysElapsed != 0) {
                // Finish writing previous day
                grid.add(data);

                // If multiple days elapsed, write empty rows
                data = new HashMap<>();
                for (int i = 0; i < daysElapsed - 1; i++) {
                    grid.add(data);
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

        // Write and finish
        grid.add(data);
        writeSums(grid, getNewCSVWriter(new String[0]));

    }

    private CSVWriter getNewCSVWriter(String[] columns) {
        HashMap<String, String> entries = new HashMap<>();
        for (Member m : group.getMembers()) {
            entries.put(getPostsKey(m.getUserId()), "Posts by " + m.getNickname());
        }
        // Groupme has a quirk where meta messages ("so and so left, joined,
        // etc") are reported by a user with ID system, but system is not
        // listed as a user.
        entries.put(getPostsKey("system"), "System posts");

        CSVWriter writer = new CSVWriter(columns, "0");
        writer.addRow(entries);
        return writer;
    }

    private void writeSums(List<Map<String, Integer>> sums, CSVWriter writer) {
        for (Map<String, Integer> row : sums) {
            writer.addRow(row);
        }

        writer.writeTo(outputFile, false);
        System.out.println("Finished!");
    }

    private void incrementPosts(String postedBy, HashMap<String, Integer> data) {
        String id = getPostsKey(postedBy);
        int postCount = data.getOrDefault(id, 0);
        data.put(id, postCount + 1);
    }

    private void incrementLikes(String[] likedBy, HashMap<String, Integer> data) {
        String id;
        for (String user : likedBy) {
            id = getLikesKey(user);
            int likeCount = data.getOrDefault(id, 0);
            data.put(id, likeCount + 1);
        }
    }

    private String getPostsKey(String userId) {
        return userId + "_posts";
    }

    private String getLikesKey(String userId) {
        return userId + "_likes";
    }
}
