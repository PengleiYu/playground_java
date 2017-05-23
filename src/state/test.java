package state;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by yupenglei on 17/5/18.
 */
public class test {
    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            Scanner scanner = new Scanner(System.in);
            if (scanner.hasNextLine()) {
                System.out.println(scanner.nextLine());
            }
            scanner.close();
        }
    }
}
