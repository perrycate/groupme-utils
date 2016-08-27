package me.perrycate.groupmeutils.examples;

import java.io.File;
import java.nio.file.Paths;
import java.util.Scanner;

import me.perrycate.groupmeutils.Dumper;
import me.perrycate.groupmeutils.api.GroupMe;
import me.perrycate.groupmeutils.data.Group;
import me.perrycate.groupmeutils.data.GroupMessages;

/**
 * WARNING: DO NOT COMMIT WITH TOKEN WRITTEN IN
 */
public class Append {
    public static void main(String[] args) {

        System.out.println("starting");
        // required info
        String token = "YOUR TOKEN HERE";
        String groupId = "GROUP ID HERE"; // Test Group

        GroupMe groupme = new GroupMe(token);
        Group group = groupme.getGroup(groupId);

        Dumper dumper = new Dumper(groupme, group);

        dumper.append(Paths.get("testfile.txt"));

        System.out.println("Complete");
    }
}
