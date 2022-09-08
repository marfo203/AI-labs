package tddc17;

import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.stream.IntStream;

class MyAgentState {
	public int[][] world = new int[30][30];
	public int initialized = 0;
	final int UNKNOWN = 0;
	final int WALL = 1;
	final int CLEAR = 2;
	final int DIRT = 3;
	final int HOME = 4;
	final int ACTION_NONE = 0;
	final int ACTION_MOVE_FORWARD = 1;
	final int ACTION_TURN_RIGHT = 2;
	final int ACTION_TURN_LEFT = 3;
	final int ACTION_SUCK = 4;

	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;

	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;

	public Stack<HashMap> unknownStack = new Stack<HashMap>();

	MyAgentState() {
		for (int i = 0; i < world.length; i++)
			for (int j = 0; j < world[i].length; j++)
				world[i][j] = UNKNOWN;
		world[1][1] = HOME;
		agent_last_action = ACTION_NONE;
	}

	// Based on the last action and the received percept updates the x & y agent
	// position
	public void updatePosition(DynamicPercept p) {
		Boolean bump = (Boolean) p.getAttribute("bump");

		if (agent_last_action == ACTION_MOVE_FORWARD && !bump) {
			switch (agent_direction) {
			case MyAgentState.NORTH:
				agent_y_position--;
				break;
			case MyAgentState.EAST:
				agent_x_position++;
				break;
			case MyAgentState.SOUTH:
				agent_y_position++;
				break;
			case MyAgentState.WEST:
				agent_x_position--;
				break;
			}
		}

	}

	public void updateWorld(int x_position, int y_position, int info) {
		world[x_position][y_position] = info;
	}

	public void printWorldDebug() {
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world[i].length; j++) {
				if (world[j][i] == UNKNOWN)
					System.out.print(" ? ");
				if (world[j][i] == WALL)
					System.out.print(" # ");
				if (world[j][i] == CLEAR)
					System.out.print(" . ");
				if (world[j][i] == DIRT)
					System.out.print(" D ");
				if (world[j][i] == HOME)
					System.out.print(" H ");
			}
			System.out.println("");
		}
	}

	int[] findNeighbours(MyAgentState state) {
		int x = state.agent_x_position;
		int y = state.agent_y_position;

		int[] neighbourArr = new int[] { world[x][y - 1], world[x + 1][y], world[x][y + 1], world[x - 1][y] };

		return neighbourArr;

	}

}

class MyAgentProgram implements AgentProgram {

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();

	// Here you can define your variables!
	public int iterationCounter = 1000;
	public MyAgentState state = new MyAgentState();

	// moves the Agent to a random start position
	// uses percepts to update the Agent position - only the position, other
	// percepts are ignored
	// returns a random action
	private Action moveToRandomStartPosition(DynamicPercept percept) {
		int action = random_generator.nextInt(6);
		initnialRandomActions--;
		state.updatePosition(percept);
		if (action == 0) {
			state.agent_direction = ((state.agent_direction - 1) % 4);
			if (state.agent_direction < 0)
				state.agent_direction += 4;
			state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (action == 1) {
			state.agent_direction = ((state.agent_direction + 1) % 4);
			state.agent_last_action = state.ACTION_TURN_RIGHT;
			return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		}
		state.agent_last_action = state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}

	@Override
	public Action execute(Percept percept) {

		// DO NOT REMOVE this if condition!!!
		if (initnialRandomActions > 0) {
			return moveToRandomStartPosition((DynamicPercept) percept);
		} else if (initnialRandomActions == 0) {
			// process percept for the last step of the initial random actions
			initnialRandomActions--;
			state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.agent_last_action = state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;
		}

		// This example agent program will update the internal agent state while only
		// moving forward.
		// START HERE - code below should be modified!

		System.out.println("x=" + state.agent_x_position);
		System.out.println("y=" + state.agent_y_position);
		System.out.println("dir=" + state.agent_direction);

		iterationCounter--;

		if (iterationCounter == 0)
			return NoOpAction.NO_OP;

		DynamicPercept p = (DynamicPercept) percept;
		Boolean bump = (Boolean) p.getAttribute("bump");
		Boolean dirt = (Boolean) p.getAttribute("dirt");
		Boolean home = (Boolean) p.getAttribute("home");
		System.out.println("percept: " + p);

		// State update based on the percept value and the last action
		state.updatePosition((DynamicPercept) percept);
		if (bump) {
			switch (state.agent_direction) {
			case MyAgentState.NORTH:
				state.updateWorld(state.agent_x_position, state.agent_y_position - 1, state.WALL);
				break;
			case MyAgentState.EAST:
				state.updateWorld(state.agent_x_position + 1, state.agent_y_position, state.WALL);
				break;
			case MyAgentState.SOUTH:
				state.updateWorld(state.agent_x_position, state.agent_y_position + 1, state.WALL);
				break;
			case MyAgentState.WEST:
				state.updateWorld(state.agent_x_position - 1, state.agent_y_position, state.WALL);
				break;
			}
		}
		if (dirt)
			state.updateWorld(state.agent_x_position, state.agent_y_position, state.DIRT);
		else
			state.updateWorld(state.agent_x_position, state.agent_y_position, state.CLEAR);

		state.printWorldDebug();

		// Next action selection based on the percept value
		if (dirt) {
			System.out.println("DIRT -> choosing SUCK action!");
			state.agent_last_action = state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;
		} else {
			if (bump) {
				System.out.println("Bump true");
				return findPath(state);
			} else {
				System.out.println("bump false");
				return findPath(state);
			}
		}
	}

	private Action findPath(MyAgentState state) {
		System.out.println("find path");
		// Vi måste hitta något sätt att hålla koll på koordinaterna för grannen
		int[] neighbours = new int[4];
		neighbours = state.findNeighbours(state);// returns list of all neighbours: clean, wall, or unknown

		for (int i = 0; i < neighbours.length; i++) {
			System.out.println("neighbours" + i + ": " + neighbours[i]);
		}

		// nextTile = neighbours[evaluateNeighbours(neighbours)]; // returns the unknown
		
		System.out.println("Innehåller unknown: " + IntStream.of(neighbours).anyMatch(x -> x == 0));
		if (IntStream.of(neighbours).anyMatch(x -> x == 0)) { //om vi har unknown grannar söker vi dem med följande metod
			int index = evaluateNeighbours(neighbours, state);
			System.out.println("intdex: " + index);
			System.out.println("agentDir: " + state.agent_direction);
			if (index == state.agent_direction) {

				return moveForward(state);
			} else if (index == 0 && state.agent_direction == 1) {
				System.out.println(state.unknownStack + "inne inågon schysst loop");
				return turnLeft(state);
			} else if (index == 0 && state.agent_direction == 2) {
				System.out.println(state.unknownStack + "inne inågon schysst loop");
				return turnLeft(state);
			} else if (index == 1 && state.agent_direction == 0) {
				System.out.println(state.unknownStack + "inne inågon schysst loop");
				return turnRight(state);
			} else if (index == 1 && state.agent_direction == 2) {
				System.out.println(state.unknownStack + "inne inågon schysst loop");
				return turnLeft(state);
			} else if (index == 2 && state.agent_direction == 1) {
				System.out.println(state.unknownStack + "inne inågon schysst loop");
				return turnRight(state);
			} else if (index == 2 && state.agent_direction == 3) {
				System.out.println(state.unknownStack + "inne inågon schysst loop");
				return turnRight(state);
			} else if (index == 3 && state.agent_direction == 2) {
				System.out.println(state.unknownStack + "inne inågon schysst loop");
				return turnRight(state);
			} else if (index == 3 && state.agent_direction == 0) {
				System.out.println(state.unknownStack + "inne inågon schysst loop");
				return turnLeft(state);
			} else {
				System.out.println(state.unknownStack + "inne inågon schysst loop");
				return turnRight(state);
			}
		} else {
			findUnknown(state);
		}
		
		return turnRight(state);
	}

	private Action findUnknown(MyAgentState state2) {
		Map latestUnknown = state2.unknownStack.pop();
		Set<Integer> set = latestUnknown.keySet();
		
		System.out.println(state.unknownStack + "find unknown");
		
		for (Map.Entry entry : latestUnknown.entrySet()) {
		    int key = (int) entry.getKey();
		    int value = entry.getValue();
		}
		  
		
		int x = (int) latestUnknown.get(1);
		System.out.println();
		int y = 0;
		latestUnknown.forEach((key, value) -> x = (int) key  );
		latestUnknown.forEach((key, value) -> value = y );
		System.out.println(x);
		

		return turnLeft(state2);
		
	}

	private int evaluateNeighbours(int[] neighbours, MyAgentState state) {
		System.out.println("nu är vi i evaluate ");
		int direction = 0;
		Boolean unknownExists = false;
		//HashMap<Integer, Integer> unknownTiles = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> unknownTiles =  new HashMap<>();

		// Skriver ut info bara hehe

		for (int i = 0; i < neighbours.length; i++) {
			if (neighbours[i] == 2) {
				direction = i;
				System.out.println("nu är vi i evaluate neigh = 2");
			}
		}

		for (int i = 0; i < neighbours.length; i++) {
			System.out.println("EvalArr" + i + ": " + neighbours[i]);
		}

		for (int j = 0; j < neighbours.length; j++) {

			if (neighbours[j] == 0) {
				unknownExists = true;
				direction = j;
				System.out.println("nu är vi i evaluate neigh = 0");
				if (j == 0) {
					unknownTiles.put(state.agent_x_position, state.agent_y_position - 1);
					state.unknownStack.push(unknownTiles);
					System.out.println(state.unknownStack);
					if (state.agent_direction == j) {
						state.unknownStack.pop(); // Ser till att vi fortsätter rakt om vi kan och inte svänger i onödan
						return j;
					}

				} else if (j == 1) {
					unknownTiles.put(state.agent_x_position + 1, state.agent_y_position);
					state.unknownStack.push(unknownTiles);
					System.out.println(state.unknownStack);
					if (state.agent_direction == j) {
						state.unknownStack.pop();
						return j;
					}

				} else if (j == 2) {
					unknownTiles.put(state.agent_x_position, state.agent_y_position + 1);
					state.unknownStack.push(unknownTiles);
					System.out.println(state.unknownStack);
					if (state.agent_direction == j) {
						state.unknownStack.pop();
						return j;
					}

				} else if (j == 3) {
					unknownTiles.put(state.agent_x_position - 1, state.agent_y_position);
					state.unknownStack.push(unknownTiles);
					System.out.println(state.unknownStack);
					if (state.agent_direction == j) {
						state.unknownStack.pop();
						return j;
					}
				}
			}
		}

		if (unknownExists) {
			state.unknownStack.pop();
			unknownExists = false;
		}
		return direction;
	}
	// if no unknown is find, go to clear tile

	private Action moveForward(MyAgentState state) {

		if (state.agent_direction == 0) {

			state.agent_last_action = state.ACTION_MOVE_FORWARD;
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;

		} else if (state.agent_direction == 1) {

			state.agent_last_action = state.ACTION_MOVE_FORWARD;
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;

		} else if (state.agent_direction == 2) {

			state.agent_last_action = state.ACTION_MOVE_FORWARD;
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;

		} else if (state.agent_direction == 3) {

			state.agent_last_action = state.ACTION_MOVE_FORWARD;
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
		} else {
			System.out.println("Final resort");
			state.agent_last_action = state.ACTION_MOVE_FORWARD;
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
		}
	}

	private Action turnLeft(MyAgentState state) {
		if (state.agent_direction == 0) {
			state.agent_direction = 3;
		} else if (state.agent_direction == 1) {
			state.agent_direction = 0;
		} else if (state.agent_direction == 2) {
			state.agent_direction = 1;
		} else if (state.agent_direction == 3) {
			state.agent_direction = 2;
		}
		state.agent_last_action = state.ACTION_TURN_LEFT;
		return LIUVacuumEnvironment.ACTION_TURN_LEFT;
	}

	private Action turnRight(MyAgentState state) {
		if (state.agent_direction == 0) {
			state.agent_direction = 1;
		} else if (state.agent_direction == 1) {
			state.agent_direction = 2;
		} else if (state.agent_direction == 2) {
			state.agent_direction = 3;
		} else if (state.agent_direction == 3) {
			state.agent_direction = 0;
		}
		state.agent_last_action = state.ACTION_TURN_RIGHT;
		return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
	}

}

public class MyVacuumAgent extends AbstractAgent {
	public MyVacuumAgent() {
		super(new MyAgentProgram());
	}
}