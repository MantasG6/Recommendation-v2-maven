import org.main.SlopeOne;

import java.util.Scanner;

public class Run {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter the number of users");
        int numUsers = in.nextInt();
        SlopeOne.slopeOne(numUsers);
        in.close();
    }
}