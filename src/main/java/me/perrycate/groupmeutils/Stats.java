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
    private Map<String, String> members;
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
        this(g, group, outputFile, "America/New_York");
    }

    public Stats(GroupMe g, Group group, File outputFile, String timeZone) {
        this.api = g;
        this.group = group;
        this.outputFile = outputFile;
        this.timeZone = ZoneId.of(timeZone);
        this.members = new HashMap<String, String>();

        for (Member m : group.getMembers()) {
            members.put(getPostsKey(m.getUserId()), "Posts by " + m.getNickname());
        }

        // Groupme has a quirk where meta messages ("so and so left, joined,
        // etc") are reported by a user with ID system, but system is not
        // listed as a user.
        members.put(getPostsKey("system"), "System posts");
        members.put("date", "Date");
    }

    public void write() {

        CSVWriter writer = getNewCSVWriter();

        MessageIterator messages = api.getAllMessages(group.getId());
        Message currentMessage = messages.next();
        LocalDate day = LocalDateTime.ofInstant(
                currentMessage.getCreatedAt(), this.timeZone).toLocalDate();
        Map<String, Integer> data = new HashMap<>();

        while (messages.hasNext()) {
            currentMessage = messages.next();

            // Start new day if necessary
            LocalDate currentDay = LocalDateTime
                    .ofInstant(currentMessage.getCreatedAt(), this.timeZone)
                    .toLocalDate();

            if (!sameDay(day, currentDay)) {
                long daysElapsed = ChronoUnit.DAYS.between(day, currentDay);
                // Finish writing previous day
                addRow(writer, data, day);

                // If multiple days elapsed, write empty rows
                data = new HashMap<>();
                for (int i = 1; i < daysElapsed; i++) {
                    addRow(writer, data, day.plusDays(i));
                }

                // Start new day
                data = new HashMap<>();
                day = currentDay;
            }

            // Add data for this message
            incrementPosts(currentMessage.getUserId(), data);
            if (currentMessage.getFavoritedBy() != null)
                incrementLikes(currentMessage.getFavoritedBy(), data);

            // update header - adds new member, or updates name. (or does nothing.)
            this.members.put(getPostsKey(currentMessage.getUserId()),
                    currentMessage.getName());
        }
        
        
        // Finish writing previous day
        addRow(writer, data, day);

        // Write and finish
        writer.setHeader(members);
        writer.writeTo(outputFile);

    }
    
    private void addRow(CSVWriter writer, Map<String, Integer> data, LocalDate day) {
        Map<String, String> row = new HashMap(data);
        row.put("date", day.toString());
        writer.addRow(row);
    }
    
    private void incrementPosts(String postedBy, Map<String, Integer> data) {
        String id = getPostsKey(postedBy);
        int postCount = data.getOrDefault(id, 0);
        data.put(id, postCount + 1);
    }

    private void incrementLikes(String[] likedBy, Map<String, Integer> data) {
        String id;
        for (String user : likedBy) {
            id = getLikesKey(user);
            int likeCount = data.getOrDefault(id, 0);
            data.put(id, likeCount + 1);
        }
}
    
    /**
     * Returns true iff date1 and date2 are the same day of the same year.
     */
    private boolean sameDay(LocalDate date1, LocalDate date2) {
        return date1.getYear() == date2.getYear() &&
               date1.getDayOfYear() == date2.getDayOfYear();
    }

    private CSVWriter getNewCSVWriter() {
        HashMap<String, String> entries = new HashMap<>();

        CSVWriter writer = new CSVWriter("0");
        return writer;
    }

    private String getPostsKey(String userId) {
        return userId + "_posts";
    }

    private String getLikesKey(String userId) {
        return userId + "_likes";
    }
}
