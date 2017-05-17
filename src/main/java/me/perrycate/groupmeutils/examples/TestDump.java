package me.perrycate.groupmeutils.examples;

import java.io.File;

import me.perrycate.groupmeutils.Dumper;
import me.perrycate.groupmeutils.api.GroupMe;
import me.perrycate.groupmeutils.data.Group;

/**
 * WARNING: DO NOT COMMIT WITH TOKEN WRITTEN IN
 */
public class TestDump {
    public static void main(String[] args) {
        // required info
        String token = "YOUR API TOKEN HERE";
        String groupId = "GROUP ID HERE";

        GroupMe groupme = new GroupMe(token);
        Group group = groupme.getGroup(groupId);

        Dumper dumper = new Dumper(groupme, group);

        dumper.dump(new File("testdump.txt"));

        System.out.println("Complete");
    }
}
