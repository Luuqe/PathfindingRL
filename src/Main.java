import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class is the driver class. Used to interact with the system.
 */
public class Main {
    public static void main(String[] a) {
        mazeCreation mc = new mazeCreation();
        menu menuobj = new menu();


        // For reference;
        // 4 is the solution path (only shown after testing has been run, or to display movement through the maze)
        // 3 is the goal
        // 2 is starting point
        // 1 is open paths
        // 0 is walls

        int maze[][] = mc.initMaze();

        int menuSelection = menuobj.display();
        menuobj.selection(menuSelection, maze);




    }



}
