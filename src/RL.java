import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * This class deals with everything at the moment to do with the maze and the rat.
 */
public class RL {
    // Throughout this program i'll be transforming from state positions to x/y coordinates
    // to do this i will use;
    // int x = state / mazeWidth;
    // int y = state - x * mazeWidth;

    // initialising variables, mazeWidth/height start from 1 although the array starts from 0
    private int mazeW;
    private int mazeH;
    private int maxStates;
    // maximum amount of states, every possible move on the board
    // this will be used to create an array allowing for states to be saved
    // private final int maxStates = mazeWidth * mazeHeight;
    private int firstGoalStateReached = 0;
    private int startingState;

    // reward/penality, the reward is the finishing tile/state
    // penalty is given to states that end in a dead-end
    // small penalty is added on to the reverse of the move that has just happened as to try and dissuade the agent from
    // returning to the previous state
    private final int reward = 100;
    private final int penalty = -10;
    private final int smallPenalty = -2;


    // initialising the reward, qlearning and maze arrays
    // the maze is actually held in the main class, is passed through to this class and is saved under
    // the new array
    private int[][] R; // Reward
    private double[][] Q; // Q learning
    private int[][] maze = new int[0][0]; // Maze layout
    private int[][] originMaze = new int[0][0]; // Referencing the original maze, edit to edit the original maze
    private int[][] mazeSolution = new int[0][0]; // The maze I'll use when using the rat to move through the maze
    private ArrayList<Integer> policy = new ArrayList<>(); // 1 is up, 2 is down, 3 is left and 4 is right
    private static ArrayList<Integer> results = new ArrayList<>();
    private ArrayList<Integer> previouslyVisited = new ArrayList<>();

    boolean finalStateCheck = false; // a check to see if the rat is beside/on the final state or not
    boolean finalLocalStateCheck = false; // a check to see if the rat is beside/on the final state or not
    boolean isTrained = false;
    boolean firstRun = false;
    boolean firstRunTests = false;

    // alpha is the learning rate
    // gamma is the discount factor quantifying how much importance we put on future rewards
    // the closer to 0 it is the more likely it will go for immediate rewards, the closer it is to 1
    // the more it will consider future rewards
    private final double alpha = 0.1;
    private final double gamma = 0.1;
    // Epsilon is the exploitation/exploration variable
    // 1 is 100%, effectively making every move random. 0.1 is the recommended setting as it will
    // allow the agent to pick a random move every once in 10 or so turns.
    private double epsilon = 0;


    // initialisation of the program
    public void init(int originalMaze[][], int maxState, int mazeLength){
        mazeH = mazeLength;
        mazeW = mazeLength;
        maxStates = maxState;

        maze = new int[mazeW][mazeH]; // Maze layout
        originMaze = new int[mazeW][mazeH]; // Referencing the original maze, edit to edit the original maze
        mazeSolution = new int[mazeW][mazeH]; // The maze I'll use when using the rat to move through the maze

        // a first run boolean
        firstRun = false;
        // variable to see which iteration the goal state is found after
        firstGoalStateReached = 0;

        // gets the starting state
        startingState = findStartingState(originalMaze);
        // this is the original maze being held as a variable to be used wherever else in this class
        originMaze = originalMaze;

        // initialise the R and Q arrays, making them the same size as the maximum amount of states in the
        // maze by the maximum amount of states, these arrays will hold information to help the rat traverse the maze
        R = new int[maxStates][maxStates];
        Q = new double[maxStates][maxStates];

        // copies the original maze array to maze as to not effect the original maze
        for(int z=0; z<originalMaze.length; z++)
            for(int j=0; j<originalMaze[z].length; j++) {
                maze[z][j] = originalMaze[z][j];
                maze[z][j] = originalMaze[z][j];
            }

        // navigates through the reward array with k as our index to fill in the reward array
        for (int k = 0; k < maxStates; k++){
            // we navigate the maze with i and j, for this to work we need to turn k into i and j
            int x = k / mazeW;
            int y = k - x * mazeH;

            // sets all the values in the rewards array to -1
            for (int s = 0; s < maxStates; s++){
                R[k][s] = -1;
            }

            // randomised movements, if the rat is not in the final state it will try to
            // move in each direction to map out the reward matrix
            if (maze[x][y] != 3){
//                System.out.println("This is running");

                moveLeft(x, y, k);

                moveRight(x, y, k);

                moveDown(x, y, k);

                moveUp(x, y, k);


            }
        }
        initialiseQ();
    }

    // this method changes the goal state from the current goal state to a state that has been input
    public void changeGoalState(){
        printOriginalMaze();

        // takes in the new goal state from the console
        int newGoal;
        Scanner i = new Scanner(System.in);

        /***************************************************/
        System.out.println("-------------------------");
        System.out.println("Please enter the new goal state.");

        // takes in a state from an input
        newGoal = i.nextInt();

        // checks that the state is a valid state
        // valid states are 0 to max states - 1, as arrays start at 0 instead of 1.
        if(newGoal < ((mazeW * mazeH) - 1)) {
            // grabs the current goal state
            int goalState = findGoalState();
            // translates the goal state into an x/y value
            int x = goalState / mazeW;
            int y = goalState - x * mazeW;

            // checks that the state returned is actually the goal state (error check incase I implemented something wrong,
            // this is not needed)
            if(originMaze[x][y] == 3){
                // sets the previous goal state to a path cell
                originMaze[x][y] = 0;

                // translates the new goal state into an x/y value
                int newx = newGoal / mazeH;
                int newy = newGoal - newx * mazeW;

                // sets the new goal state
                originMaze[newx][newy] = 3;
            }

        }else{
            System.out.println("Please enter a valid state");
        }

    }


    // this will solve the maze with the optimal path based off of learning and the Q table
    void solveMaze(int[][] originalMaze, double ep) {
        epsilon = ep;
//        System.out.println("This is being called");
        // clears the local cueues that the rat already knows and sets the final state check to false
        finalStateCheck = false;
        int currX;
        int currY;
        int x;
        int y;
        int iteration = 0;
        policy.clear();


        // copies the arrays as to not be editing the original maze
        for(int z=0; z<originalMaze.length; z++)
            for(int j=0; j<originalMaze[z].length; j++) {
                mazeSolution[z][j] = originalMaze[z][j];
                mazeSolution[z][j] = originalMaze[z][j];
            }

        // check to make sure that the rat has actually trained on the maze before going in
        if(!isTrained){
            System.out.println("Agent is not trained. Please train the agent using menu selection 1.");
        }else {
            // sets current state and previous state to the starting state
            int currentState = startingState;
            int previousState = startingState;
            // gets the state that is the best choice based on Q values
            int nextState = maxQReturnState(currentState, previousState);

            while(!isFinalState(nextState)){
                // an iteration count
                iteration = iteration + 1;
                // transforms the state to an x/y coordinate
                currX = currentState / mazeW;
                currY = currentState - currX * mazeW;

                System.out.println("State: " + nextState);
                iteration = iteration + 1;
                System.out.println("Iteration: " + iteration);
//                printSolMaze();
                x = nextState / mazeW;
                y = nextState - x * mazeW;
                // sets the x/y coordinate to 4
                mazeSolution[x][y] = 4;

                mazeSolution[currX][currY] = 1;

                printSolMaze();

                // adds to the learnt move policy based on most recent move
                if(x < currX){
                    policy.add(1);
                }
                if(x > currX){
                    policy.add(2);
                }
                if(y < currY){
                    policy.add(3);
                }
                if(y > currY){
                    policy.add(4);
                }

                previousState = currentState;
                currentState = nextState;
                // sets the next state to the most optimal move from this state
                nextState = maxQReturnState(currentState, previousState);
            }


            // this caps off the method after the goal state has been found
            currX = currentState / mazeW;
            currY = currentState - currX * mazeW;

            x = nextState / mazeW;
            y =  nextState - x * mazeW;

            mazeSolution[x][y] = 4;

            if (x < currX) {
                policy.add(1);
            }
            if (x > currX) {
                policy.add(2);
            }
            if (y < currY) {
                policy.add(3);
            }
            if (y > currY) {
                policy.add(4);
            }
            mazeSolution[x][y] = 4;

            printSolMaze();
            results.add(iteration);
            System.out.println("Final state found!");
        }
    }

    // this method deals with running the rat through the maze without using Q/R values and only moves through the maze using
    // previously learned moves of a the last successful run.
    // solveMaze must be ran before this as it's the method that gives the rat the understanding on how to move through
    // the maze after a successful attempt
    void solveMazeLocalCues(int[][] originalMaze){
        int[][] localMaze = new int[mazeW][mazeH];
        startingState = findStartingState(originalMaze);
        finalStateCheck = false;
        int currentState = startingState;
//        System.out.println("This is running");
        printLearnedMovePolicy();
        int x = startingState / mazeW;
        int y = startingState - x * mazeW;
        int currX;
        int currY;
        int previousState = 0;

        // copies the original maze as to not be editing it when we're runnign through it
        for(int z=0; z<originalMaze.length; z++)
            for(int j=0; j<originalMaze[z].length; j++) {
                localMaze[z][j] = originalMaze[z][j];
                localMaze[z][j] = originalMaze[z][j];
            }


        // loops through the moves learned by the rat
        for(int i = 0; i < policy.size(); i++){

            int size = policy.size();
            System.out.println("Size: " + size);
            printLearnedMovePolicy();
            int move = policy.get(i);
            // reads from the rats brain, this allows the rat to move through the maze without interacting with the
            // q or r table
            // as mentioned above in the initialisation of the list 1 is up, 2 is down, 3 is left and 4 is right
            // the moves happen by manipulating the place in the maze based off of the moves in order, for example,
            // to move up we need to remove 1 from the x value, the x and y values are flipped throughout this program
            // as when i started to code the program i didnt realise that the first array is the y value and second is the x
            // due to this, I just kept going using the flipped x/y values, just consider these as variables and not
            // coordinates.
            if(move == 1){
                x = x-1;
                localMaze[x][y] = 4;
                previousState = currentState;
                currentState = currentState - mazeW;
                System.out.println("Move up: " + currentState);
            }
            if (move == 2) {
                x = x+1;
                localMaze[x][y] = 4;
                previousState = currentState;
                currentState = currentState + mazeW;
                System.out.println("Move down: " + currentState);
            }
            if (move == 3) {
                y = y-1;
                localMaze[x][y] = 4;
                previousState = currentState;
                currentState = currentState - 1;
                System.out.println("Move left: " + currentState);
            }
            if (move == 4) {
                y = y+1;
                localMaze[x][y] = 4;
                previousState = currentState;
                currentState = currentState + 1;
                System.out.println("Move right: " + currentState);
            }


            // finds if the x/y coordinate is the final state or not by returning the value of its cell

        }
        currX = currentState / mazeW;
        currY = currentState - currX * mazeW;

        if(originalMaze[currX][currY] == 3){
            System.out.println("Final state found!");
            finalStateCheck = true;
        }else{
            System.out.println("is not final state.");
            // if the final state is not found then the calcQ method is ran starting from the last state (which was the
            // old goal state_
            calcQ(originMaze, 1, previousState, 1);

        }
        printLocalMaze(localMaze);
    }

    // prints out the rats movement list for debugging
    void printLearnedMovePolicy(){
        for (int i : policy) {
            System.out.println(i);
        }
    }

    // finds the starting state of the maze
    int findStartingState(int maze[][]){
//        System.out.println("This is running: ");
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze.length; j++) {
                if (maze[i][j] == 2) {
                    int state1 = i * mazeW;
                    int state2 = state1 + j;
                    System.out.println("Starting state: " + state2);
                    return state2;
                }
            }
        }
        return 0;
    }

    // finds the goal state of the maze
    int findGoalState(){
//        System.out.println("This is running: ");
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze.length; j++) {
                if (maze[i][j] == 3) {
                    int state1 = i * mazeW;
                    int state2 = state1 + j;
                    System.out.println("Goal state: " + state2);
                    return state2;
                }
            }
        }
        return 0;
    }


    // prints out a single array, used for debugging
    void printArray(int array[]){
        System.out.println("Array: ");
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i]);
            System.out.println();
        }
    }

    // Prints out the original maze
    void printOriginalMaze() {
        System.out.println("Solution maze: ");
        for (int i = 0; i < originMaze.length; i++) {
            for (int j = 0; j < originMaze.length; j++) {
                System.out.printf("%2s ", (originMaze[i][j]));
            }
            System.out.println();
        }
    }

    // Prints out the local maze
    void printLocalMaze(int[][] localMaze) {
        System.out.println("Local maze: ");
        for (int i = 0; i < localMaze.length; i++) {
            for (int j = 0; j < localMaze.length; j++) {
                System.out.printf("%2s ", (localMaze[i][j]));
            }
            System.out.println();
        }
    }

    // Prints out the solution maze
    void printSolMaze() {
        System.out.println("Solution maze: ");
        for (int i = 0; i < mazeSolution.length; i++) {
            for (int j = 0; j < mazeSolution.length; j++) {
                System.out.printf("%2s ", (mazeSolution[i][j]));
            }
            System.out.println();
        }
    }

    void printTrainingMaze(int trainingMaze[][]) {
        System.out.println("Training maze: ");
        for (int i = 0; i < trainingMaze.length; i++) {
            for (int j = 0; j < trainingMaze.length; j++) {
                System.out.printf("%2s ", (trainingMaze[i][j]));
            }
            System.out.println();
        }
    }

    // the next 3 methods are used to apply rewards and penalties to a specific cell in the Reward table
    void applyReward(int currentState, int nextState){
        R[currentState][nextState] = reward;
    }

    void applyPenalty(int currentState, int nextState){
        R[currentState][nextState] = penalty;
    }

    void applyZero(int currentState, int nextState){
        R[currentState][nextState] = 0;
    }

    // this is applied to the reverse of the move just carried out, to dissuade the agent from moving back
    // for example, if the agent was moving from state 1 to state 2, a small penalty will be applied to state 2 moving to
    // state 1
    void applyTurnAroundPenalty(int currentState, int nextState){
        Q[currentState][nextState] = Q[currentState][nextState] + smallPenalty;

    }


    // the next 4 methods are used to help fill in the R table, allowing for the reward values to feed back
    // through the rewards matrix from the goal state
    void moveLeft(int i, int j, int k){
        // attempt to move left
        int left = j - 1;
        if (left >= 0){
            int target = i * mazeW + left;
            if (maze[i][left] == 1){
                applyZero(k, target);
            }
            else if (maze[i][left] == 3){
                applyReward(k, target);
            }
            else{
                applyPenalty(k, target);
            }
        }
    }

    void moveRight(int i, int j, int k){
        // attempt to move right
        int right = j + 1;
        if (right < mazeW){
            int target = i * mazeW + right;
            if (maze[i][right] == 1){
                applyZero(k, target);
            }
            else if (maze[i][right] == 3){
                applyReward(k, target);
            }
            else{
                applyPenalty(k, target);
            }
        }
    }

    void moveDown(int i, int j, int k){
        // attempt to move down
        int down = i + 1;
        if (down < mazeH){
            int target = mazeW * down + j;
            if (maze[down][j] == 1){
                applyZero(k, target);
            }
            else if (maze[down][j] == 3){
                applyReward(k, target);
            }
            else{
                applyPenalty(k, target);
            }
        }
    }

    void moveUp(int i, int j, int k){
        // attempt to move up
        int up = i - 1;
        if (up >= 0){
            int target = mazeW * up + j;
            if (maze[up][j] == 1){
                applyZero(k, target);
            }
            else if (maze[up][j] == 3){
                applyReward(k, target);
            }
            else{
                applyPenalty(k, target);
            }
        }
    }

    // sets the QLearning array to the rewards values
    void initialiseQ(){
        for(int i = 0; i < maxStates; i++){
            for(int j = 0; j < maxStates; j++){
                Q[i][j] = R[i][j];
            }
        }
    }


    // calculates the Q values using the Reinforcement learning equation
    void calcQ(int originalMaze[][], int timesToLoop, int localStartingState, double ep) {
        epsilon = ep;
        Random rand = new Random();
        isTrained = true;

        // how many times the algorithm will be run
        for (int i = 0; i < timesToLoop; i++) {
            int currentState;
            int moveNumber = 0;
            int nextState;
            int previousState = 0;


            // set the initial starting state to the starting state of the maze
            // if the state passed through is 99999 then it is being ran from the normal starting state of the maze
            // otherwise it is starting from somewhere else in the maze, such as the old goal state before it has been
            // moved
            if(localStartingState == 99999){
                currentState = startingState;
                previousState = startingState;
            }else{
                currentState = localStartingState;
                previousState = localStartingState;
            }




            System.out.println("Training loop: " + i);

            // this loop runs the rat through the maze to train it
            // it starts off by getting the available actions from the starting state(currentState),
            // it picks a state at random from the
            // available actions from that state,
            // it takes the q value from the state it is currently in moving to the next state chosen,
            // the maxQ value of the next state and the reward value of moving to the next state, it then uses these
            // values in the reinforcement learning equation alongside the learning rate and discount factor to give us
            // a value that represents the Q value of moving from the original state to the next state.

            // this is the core algorithm of the program as this is how the Q table is filled up.

            while (!isFinalState(currentState)) {
                double doubleRand = rand.nextDouble();

//                System.out.println("Random int " + doubleRand);
                moveNumber = moveNumber + 1;
                firstGoalStateReached = firstGoalStateReached + 1;
                int[][] trainingMaze = new int[mazeW][mazeH];

                // copies the original maze so it does not overwrite the original maze
                for(int z=0; z<originalMaze.length; z++)
                    for(int j=0; j<originalMaze[z].length; j++)
                        trainingMaze[z][j]=originalMaze[z][j];

//                printTrainingMaze(trainingMaze);
                System.out.println("Times looped: " + moveNumber);

//                printArray(availableActions);

                // randomise the action from the possible actions available to be taken
                // translation to x/y from a random available state

                int x = currentState / mazeW;
                int y = currentState - x * mazeW;
                int[] availableActions = availableActionsCurrentState(currentState);

                trainingMaze[x][y] = 4;
                printTrainingMaze(trainingMaze);

//                printTrainingMaze(trainingMaze);

//                int[] availableActions = availableActionsCurrentState(currentState);
//                int action = rand.nextInt(availableActions.length);

                // takes the randomly selected action and sets it as the next state

                if (doubleRand < epsilon){
                    printArray(availableActions);
                    int action = rand.nextInt(availableActions.length);
                    nextState = availableActions[action];
//                    System.out.println("Next state " + nextState);
//                    System.out.println("Epsilon popped");

                }else{
                    nextState = maxQReturnState(currentState, previousState);
//                    System.out.println("Epsilon not popped");
//                    System.out.println("Next state " + nextState);
                }

                System.out.println("Current state " + currentState);
//*
                System.out.println("Move number: " + moveNumber);
//                printR();
                System.out.println("---");



                // initialising the values used in the RL Equation below
                double q = Q[currentState][nextState];
                double maxQ = maxQ(currentState);
//                System.out.println("maxQ: " + maxQ);
                int r = R[currentState][nextState];

                // Reinforcement learning equation
                // Q(state,action)= Q(state,action) + alpha * (R(state,action) + gamma * Max(next state, all actions) - Q(state,action))
                double value = q + alpha * (r + gamma * maxQ - q);
//                System.out.println("value: " + value);
                Q[currentState][nextState] = value;

                // applies the penalty to the reverse of the most recent move
                applyTurnAroundPenalty(currentState, previousState);

                // keeps a trail of visited states, for possible use in the future. Currently this doesn't do much
                previouslyVisited.add(currentState);
                previousState = currentState;
                currentState = nextState;

            }
            results.add(moveNumber);
            System.out.println("---------------------------");
        }

        finalStateCheck = true;
    }


    // method for writing the CSV file for export to python
    public static void writeCSV(String filepath){
        PrintWriter pw = null;
        File firstTimeGoalState = new File(filepath + " firstTimeGoalState" + ".csv");
        System.out.println("CSV File created.");
        try{
            pw = new PrintWriter(firstTimeGoalState);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (double goalState : results){
            pw.write(String.valueOf(goalState));
            pw.write(",");
        }
        pw.close();
        pw.flush();
    }

    // checks if it's the final state
    boolean isFinalState(int state) {
        int i = state / mazeW;
        int j = state - i * mazeW;

        return originMaze[i][j] == 3;
    }

    // Checks all possible actions able to be taken from the state the rat is currently at
    // will not allow for the rat to go outside the maze
    int[] availableActionsCurrentState(int state) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < maxStates; i++) {
            if ((R[state][i] != -1 && R[state][i] != -10)) {
                int x = i / mazeW;
                int y = i - x * mazeW;
                if(originMaze[x][y] != 0) {
                    result.add(i);
                }
            }
        }

        // https://winterbe.com/posts/2014/07/31/java8-stream-tutorial-examples/ stream syntax
        return result.stream().mapToInt(i -> i).toArray();
    }

    // Calculates the highest Q value of a given state
    double maxQ(int state) {
        // grabs the possible actions from a given state
        int[] actionsFromState = availableActionsCurrentState(state);

        // the eagerness/learning rate will keep the Q value higher than the lowest reward value
        double maxValue = -999999;

        // loops the next action through the possible actions able to be taken from the next state
        for (int nextAction : actionsFromState) {
            double value = Q[state][nextAction];
//            System.out.println("Value value: " + value);

            // if the q value is higher than the previous value it will set the maxValue to this value
            if (value > maxValue)
                maxValue = value;
//            System.out.println("Max value: " + maxValue);
        }
        return maxValue;
    }

    // Calculates the highest Q value of a given state
    int maxQReturnState(int state, int previousState) {

        int bestState = 0;
        // grabs the possible actions from a given state
        int[] actionsFromState = availableActionsCurrentState(state);

        // the eagerness/learning rate will keep the Q value higher than the lowest reward value
        double maxValue = -999999;

        // loops the next action through the possible actions able to be taken from the next state
        for (int nextAction : actionsFromState) {
            double value = Q[state][nextAction];
//            System.out.println("Value value: " + value);
            if(actionsFromState.length == 1){
                maxValue = value;
                bestState = nextAction;
            }
            // if the q value is higher than the previous value it will set the maxValue to this value
            if (value > maxValue && nextAction != previousState) {
                maxValue = value;
                bestState = nextAction;
            }
        }
        return bestState;
    }

    // Prints out the Q array
    void printQ() {
        if(isTrained == true) {
            System.out.println("Q array");
            for (int i = 0; i < Q.length; i++) {
                System.out.print("Q values from state " + i + ":  ");
                for (int j = 0; j < Q[i].length; j++) {
                    System.out.printf("%6.2f ", (Q[i][j]));
                }
                System.out.println();
            }
            System.out.println("Key: ");
            System.out.println("1- = impossible move");
            System.out.println("The higher the number, the better the move is, bringing it closer to the goal");
            System.out.println();
        }else{
            System.out.println("Agent is not trained. Please train the agent using menu selection 1.");
        }
    }

    // prints the overall policy for every state
    void printPolicy() {
        System.out.println("\nPrint policy");
        for (int i = 0; i < maxStates; i++) {
            System.out.println("From state " + i + " goto state " + getPolicyFromState(i));
        }
    }

    // gets the policy from a given state
    // it is done by finding the best Q value from every state
    int getPolicyFromState(int state) {
        int[] actionsFromState = availableActionsCurrentState(state);

//        printArray(actionsFromState);
        double maxValue = -9999999;
        int policyGotoState = state;

        // Pick to move to the state that has the maximum Q value
        for (int nextState : actionsFromState) {
            double value = Q[state][nextState];

            if (value > maxValue) {
                maxValue = value;
                policyGotoState = nextState;
            }
        }
        return policyGotoState;
    }

    // Prints out the reward array
    void printR() {
        if(isTrained == true) {
            // https://www.baeldung.com/java-printstream-printf printf syntax
            System.out.printf("%25s", "States: ");
            // prints out states to show which state the possible moves are referring to
            for (int i = 0; i <= maxStates - 1; i++) {
                System.out.printf("%4s", i);
            }
            System.out.println();

            // loops through the reward array, printing out each possible state
            // loops through
            for (int i = 0; i < maxStates ; i++) {
                System.out.print("Possible states from " + i + " :[");
                // loops through each individual possible move
                for (int j = 0; j < maxStates; j++) {
                    System.out.printf("%4s", R[i][j]);
                }
                System.out.println("]");
            }
            System.out.println("Key: ");
            System.out.println("-1 = impossible move/current state");
            System.out.println("-10 = illegal move");
            System.out.println("0 = legal move");
            System.out.println("100 = legal move, final state");
        }else{
            System.out.println("Agent is not trained. Please train the agent using menu selection 1.");
        }
    }

}
