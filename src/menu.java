/*
 * this class creates and deals with user inputs
 * the menu will be used to display options then take in a user input and run code based on their input
 */
import java.util.Scanner;

public class menu {
    mazeCreation mc = new mazeCreation();

    public int display() {

        int selection;
        Scanner input = new Scanner(System.in);

        /***************************************************/
        System.out.println("-------------------------");
        System.out.println("Select what you wish the program to do\n");
        System.out.println("1 - Train");
        System.out.println("2 - Solve maze, create learned policy");
        System.out.println("3 - Solve using the policy");
        System.out.println("4 - Print Rewards table");
        System.out.println("5 - Print Q table");
        System.out.println("6 - Change goal state");
        System.out.println("7 - Single runs for testing purposes");
        System.out.println("8 - Train with no randomised movements, epsilon = 0");
        System.out.println("9 - Train with only randomised movements, epsilon = 1");
        System.out.println("10 - Print policy");
        System.out.println("11 - Quit");
        System.out.println("13 - Test");


        selection = input.nextInt();
        System.out.println("Menu option " + selection + " selected.");
        return selection;
    }

    public void selection(int menuSelection, int[][] generatedMaze) {
        RL rat = new RL();
        int maxStates = mc.maxStates(generatedMaze);
        int mazeLength = mc.mazeLength(generatedMaze);

        // if the starting state is 99999 then the calcQ method will assume that the starting state is the original starting
        // state of the maze, if it is not 99999 then it will take that state value and run it from a specific state
        // (this is only used when trying to run the maze after changing the goal state)
        int startingState = 99999;

        // sets the epsilon where needed. overrides the base evalue
        double epsilon = 0.08;

        // menu system
        while (menuSelection == 1) {
            int timesToLoop = 50; // amount of time the rat will run the maze to train
            rat.init(generatedMaze, maxStates, mazeLength);
            rat.calcQ(generatedMaze, timesToLoop, startingState, epsilon);
//            rat.printQ();
//            rat.printR();
            rat.solveMaze(generatedMaze, epsilon);
            RL.writeCSV("C:\\Users\\Luke\\Desktop\\Output\\");
            menuSelection = display();
        }

        while (menuSelection == 2) {
            System.out.println("Number 2 selected");
            rat.solveMaze(generatedMaze, 0);
            rat.printLearnedMovePolicy();
            menuSelection = display();
        }
        while (menuSelection == 3) {
            System.out.println("Running");
            rat.solveMazeLocalCues(generatedMaze);
            menuSelection = display();
        }

        while (menuSelection == 4) {
            rat.printR();
            menuSelection = display();
        }

        while (menuSelection == 5) {
            rat.printQ();
            menuSelection = display();
        }

        while (menuSelection == 6) {
            rat.changeGoalState();
            rat.printOriginalMaze();
            rat.solveMazeLocalCues(generatedMaze);
            RL.writeCSV("C:\\Users\\Luke\\Desktop\\Output\\");
            menuSelection = display();
        }
        while (menuSelection == 7) {
            int timesToLoop = 1;
            int iterations = 100;
            for (int i = 0; i < iterations; i++) {
                rat.init(generatedMaze, maxStates, mazeLength);
                rat.calcQ(generatedMaze, timesToLoop, startingState, 0);
            }
            RL.writeCSV("C:\\Users\\Luke\\Desktop\\Output\\");
            menuSelection = display();
        }
        while (menuSelection == 8) {
            int timesToLoop = 50; // amount of time the rat will run the maze to train
            rat.init(generatedMaze, maxStates, mazeLength);
            rat.calcQ(generatedMaze, timesToLoop, startingState, 0);
//            rat.printQ();
//            rat.printR();
            RL.writeCSV("C:\\Users\\Luke\\Desktop\\Output\\");
            menuSelection = display();
        }
        while (menuSelection == 9) {
            int timesToLoop = 50; // amount of time the rat will run the maze to train
            rat.init(generatedMaze, maxStates, mazeLength);
            rat.calcQ(generatedMaze, timesToLoop, startingState, 1);
//            rat.printQ();
//            rat.printR();
            RL.writeCSV("C:\\Users\\Luke\\Desktop\\Output\\");
            menuSelection = display();
        }
        if (menuSelection == 10) {
            rat.printPolicy();
            menuSelection = display();
        }
        if (menuSelection == 11) {
            System.out.println("Thank you for using my program.");
            System.exit(0);
        }
        if (menuSelection == 13) {

            mazeCreation.printMaze(generatedMaze);
        }

    }
}
