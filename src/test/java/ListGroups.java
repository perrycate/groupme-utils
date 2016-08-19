import java.io.File;
import java.util.Scanner;

import me.perrycate.groupmeutils.Dumper;
import me.perrycate.groupmeutils.api.Client;
import me.perrycate.groupmeutils.data.Group;
import me.perrycate.groupmeutils.data.GroupMessages;

/**
 * Simple test client that reads a user's groups.
 */
public class ListGroups {
    public static void main(String[] args) {

        System.out.println("Please enter your groupme API Token: ");
        Scanner s = new Scanner(System.in);
        String token = s.nextLine();
        System.out.println("Fetching groups...");

        Client groupme = new Client(token);
        Group[] groups = groupme.getGroups();

        for (int i = 0; i < groups.length; i++) {
            System.out.println(groups[i].getName());
        }

    }
}
