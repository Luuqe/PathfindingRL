/*
 * this class deals with maze creation and population
 */

import java.util.Random;
import java.util.Scanner;

public class mazeCreation {
    Random random = new Random();


    // allows the user to enter a maze size to be generated
    public int[][] initMaze() {
        Scanner input = new Scanner(System.in);

        /***************************************************/
        System.out.println("-------------------------");
        System.out.println("Please enter the size of maze you wish to use. Correct usage is a single number, this number will be width and height.\n");

        int size = input.nextInt();

        System.out.println("How dense would you like the walls to be in the maze? (0-100%)\nPlease keep in mind the higher density the higher " +
                "chances that the program will be unable to find a path");
        int densityInput = input.nextInt();
        double mapSize = size * size;
        System.out.println("Total map size is  " + mapSize + "\n");

        double density = mapSize * densityInput / 100;

        System.out.println("Density is " + density + "\n");

        System.out.println("Maze size " + size + " selected.");
        // fills the maze with blank spaces
        int generatedMaze[][] = fillArray(size);

        // populates the maze with walls based on the given density, a start and a finish node.
        generatedMaze = populateArray(generatedMaze, density, size);


        return generatedMaze;
    }


    // iterates through the array and fills it with movable tiles
    private int[][] fillArray(int size){

        int[][] mazeSize = new int[size][size];

        for (int row = 0; row < mazeSize.length; row++)
        {
            for (int col = 0; col < mazeSize[row].length; col++)
            {
                mazeSize[row][col] = 1;
            }
        }
        return mazeSize;
    }

    // populates the array with walls and a start/finish point
    private int[][] populateArray(int[][] blankMaze, double density, int size){

        int x;
        int y;
        int position = 0;

        for (int i = 0; i < density; i++)
        {
            do{

                x = random.nextInt(size);
                y = random.nextInt(size);
                position = blankMaze[x][y];

            }while(position == 0);
            blankMaze[x][y] = 0;
        }

        do{
            x = random.nextInt(size);
            y = random.nextInt(size);
            position = blankMaze[x][y];

        }while(position == 1);
        blankMaze[x][y] = 2;

        do{
            x = random.nextInt(size);
            y = random.nextInt(size);
            position = blankMaze[x][y];

        }while(position == 1);
        blankMaze[x][y] = 3;

        printMaze(blankMaze);

        return blankMaze;
    }

    // prints out the maze to the console
    public static void printMaze(int[][] maze){
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze.length; j++) {
                System.out.printf("%2s ", (maze[i][j]));
            }
            System.out.println("print completed");
        }
    }

    // finds the max amount of nodes in the array
    public int maxStates(int[][] maze){
        System.out.println(maze[1].length);

        return maze[0].length * maze[1].length;
    }


    // finds the vertical (or horizontal since the width/height will be the same) size
    public int mazeLength(int[][] maze){

        return maze[0].length;

    }

}
