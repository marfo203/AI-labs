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

	public Stack<int[][]> unknownStack = new Stack<int[][]>();
	public int[][] goalCoordinates = new int[1][2];
	public boolean searchingForUnknown;

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

	public int[] findNeighbours(MyAgentState state) {
		int x = state.agent_x_position;
		int y = state.agent_y_position;

		System.out.println("X position: " + x);
		System.out.println("Y position: " + y);
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
		if (dirt) {
			state.updateWorld(state.agent_x_position, state.agent_y_position, state.DIRT);
			System.out.println("DIRT -> choosing SUCK action!");
			state.printWorldDebug();
			state.agent_last_action = state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;
		} else {
			state.updateWorld(state.agent_x_position, state.agent_y_position, state.CLEAR);
			state.printWorldDebug();
			return findPath(state, (DynamicPercept) percept);
		}
	}

	private Action findPath(MyAgentState state, DynamicPercept percept) {
		System.out.println("Find path function used!");
		// Vi måste hitta något sätt att hålla koll på koordinaterna för grannen
		int[] neighbours = new int[4];

		neighbours = state.findNeighbours(state);

		// Prints list of all neighbours: clean, wall, or unknown
		for (int i = 0; i < neighbours.length; i++) {
			System.out.println("Neighbours " + i + ": " + neighbours[i]);
		}
		int numberOfUnknown = 0;
		int dirUnknown = 0;
		for (int i = 0; i < neighbours.length; i++) {
			if (neighbours[i] == 0) {
				dirUnknown = i;
				numberOfUnknown++;
			}
		}

		// nextTile = neighbours[evaluateNeighbours(neighbours)]; // returns the unknown
		// Printar on det finns unknown grannar
		System.out.println("Contains Unknown: " + IntStream.of(neighbours).anyMatch(x -> x == 0));
		System.out.println("Number of Unknown: " + numberOfUnknown);
		// IntStream.of(neighbours).anyMatch(x -> x == 0)
		// Om vi har mer än 1 unknown granne söker vi dem med följande metod
		if (numberOfUnknown == 1) {
			// Gå mot unknown-grannen
			return moveTowardsUnknown(state, dirUnknown);
		} else if (numberOfUnknown > 1) {
			int index = evaluateNeighbours(neighbours, state);
			System.out.println("(index) dir of choosen unknown : " + index);
			System.out.println("agentDir: " + state.agent_direction);
			if (index == state.agent_direction) {
				return moveForward(state);
			} else if (index == 0 && state.agent_direction == 1) {
				System.out.println(state.unknownStack + "inside a nice loop");
				return turnLeft(state);
			} else if (index == 0 && state.agent_direction == 2) {
				System.out.println(state.unknownStack + "inside a nice loop");
				return turnLeft(state);
			} else if (index == 1 && state.agent_direction == 0) {
				System.out.println(state.unknownStack + "inside a nice loop");
				return turnRight(state);
			} else if (index == 1 && state.agent_direction == 2) {
				System.out.println(state.unknownStack + "inside a nice loop");
				return turnLeft(state);
			} else if (index == 2 && state.agent_direction == 1) {
				System.out.println(state.unknownStack + "inside a nice loop");
				return turnRight(state);
			} else if (index == 2 && state.agent_direction == 3) {
				System.out.println(state.unknownStack + "inside a nice loop");
				return turnLeft(state);
			} else if (index == 3 && state.agent_direction == 2) {
				System.out.println(state.unknownStack + "inside a nice loop");
				return turnRight(state);
			} else if (index == 3 && state.agent_direction == 0) {
				System.out.println(state.unknownStack + "inside a nice loop");
				return turnLeft(state);
			} else {
				System.out.println(state.unknownStack + "inside a nice loop");
				return turnRight(state);
			}
		} else {
			// Vi vet nu om hela vår lokala omgivning. Måste nu ta reda på vart ska gå
			// vidare
			return moveTowardsCoordinates(state, percept);
			// Gå till random är sista utvägen
			// return moveToRandomStartPosition(percept);
			// return turnLeft(state);
		}
	}

	private Action moveTowardsCoordinates(MyAgentState state, DynamicPercept percept) {
		int curr_x = state.agent_x_position;
		int curr_y = state.agent_y_position;
		System.out.println("Kommit till: moveTowardsCoordinates. State is: " + state.searchingForUnknown);

		if (state.searchingForUnknown == false) {
			int[][] tempCoord = state.unknownStack.pop();
			if(state.world[tempCoord[0][0]][tempCoord[0][1]] != 1 || tempCoord[0][0] != 0 || tempCoord[0][1] != 0) {
				state.goalCoordinates = tempCoord;
				state.searchingForUnknown = true;
			} else {
				moveTowardsCoordinates(state, percept);
			}
		}
		System.out.println(
				"The goal coordinates are: " + state.goalCoordinates[0][0] + "," + state.goalCoordinates[0][1]);
		System.out.println("Agents coordinates: " + state.agent_x_position + "," + state.agent_y_position);

		if (state.goalCoordinates[0][0] != curr_x || state.goalCoordinates[0][1] != curr_y) {
			System.out.println("We are not at the goal coordinates");
			// Nu jävlar ska vi gå mot "målet"
			if ((boolean) percept.getAttribute("bump")) {
				// Vi har krockat... Gå till random?
				System.out.println("bump Bool" + (boolean) percept.getAttribute("bump"));
				moveToRandomStartPosition(percept);
			} else {
				if (state.goalCoordinates[0][0] > curr_x) { // Målet ligger österut, sväng höger
					return moveTowardsUnknown(state, 1);
				} else if (state.goalCoordinates[0][0] < curr_x) { // Målet ligger startk väster ut
					return moveTowardsUnknown(state, 3);
				} else { // Vi står på rätt x-led
					System.out.println("2 The goal coordinates are: " + state.goalCoordinates[0][0] + ","
							+ state.goalCoordinates[0][1]);
					System.out
							.println("2 Agents coordinates: " + state.agent_x_position + "," + state.agent_y_position);
					if (state.goalCoordinates[0][1] > curr_y) {
						return moveTowardsUnknown(state, 2);
					} else if (state.goalCoordinates[0][1] < curr_y) {
						return moveTowardsUnknown(state, 0);
					}
				}
			}

			// We have found the unknown
		} else if (state.goalCoordinates[0][0] == curr_x && state.goalCoordinates[0][1] == curr_y) {
			System.out.println("We are at the goal coordinates");
			state.searchingForUnknown = false;
		}

		System.out.println("We have come to the end of this if-statement");
		return turnLeft(state);
	}

	private Action moveTowardsUnknown(MyAgentState state, int dirUnknown) {
		System.out.println("Agent dir: " + state.agent_direction);
		System.out.println("dirUnknown: " + dirUnknown);
		if (dirUnknown == state.agent_direction) {
			return moveForward(state);
		} else if (dirUnknown == 0 && state.agent_direction == 1) {
			return turnLeft(state);
		} else if (dirUnknown == 0 && state.agent_direction == 2) {
			return turnLeft(state);
		} else if (dirUnknown == 1 && state.agent_direction == 0) {
			return turnRight(state);
		} else if (dirUnknown == 1 && state.agent_direction == 2) {
			return turnLeft(state);
		} else if (dirUnknown == 2 && state.agent_direction == 1) {
			return turnRight(state);
		} else if (dirUnknown == 2 && state.agent_direction == 3) {
			return turnRight(state);
		} else if (dirUnknown == 3 && state.agent_direction == 2) {
			return turnRight(state);
		} else if (dirUnknown == 3 && state.agent_direction == 0) {
			return turnLeft(state);
		} else {
			return turnRight(state);
		}
	}

	private int evaluateNeighbours(int[] neighbours, MyAgentState state) {
		System.out.println("We are now in Evaluate function!");
		int direction = -1;
		int dirVar = -1;
		// HashMap<Integer, Integer> unknownTiles = new HashMap<>();

		// Skriver bara ut info
		for (int i = 0; i < neighbours.length; i++) {
			System.out.println("EvalArr" + i + ": " + neighbours[i]);
		}

		// ArrayList med alla riktningar som är unknown. Minst 1 är det.
		ArrayList<Integer> unknownNeighbours = new ArrayList<Integer>();

		// Lägger våra unknown heignbours i en ny lista
		for (int j = 0; j < neighbours.length; j++) {
			if (neighbours[j] == 0) {
				unknownNeighbours.add(j);
				System.out.println("Added unknown neighbour: " + j + " to list.");
			}
		}

		// unknownNeighbours kommer vara minst 2 lång. Annars hade vi inte hamnat här
		// Listan består av dir till en unknown granne.
		for (int k = 0; k < unknownNeighbours.size(); k++) {
			int[][] unknownTiles = new int[1][2];

			if (unknownNeighbours.get(k) == 0) {
				unknownTiles[0][0] = state.agent_x_position;
				unknownTiles[0][1] = state.agent_y_position - 1;
				state.unknownStack.push(unknownTiles);
				System.out.println("The state of unknownStack: " + state.unknownStack);
				dirVar = unknownNeighbours.get(k);
				if (unknownNeighbours.get(k) == state.agent_direction) {
					direction = unknownNeighbours.get(k);
					state.unknownStack.pop();
				}
			} else if (unknownNeighbours.get(k) == 1) {
				unknownTiles[0][0] = state.agent_x_position + 1;
				unknownTiles[0][1] = state.agent_y_position;
				state.unknownStack.push(unknownTiles);
				System.out.println("The state of unknownStack: " + state.unknownStack);
				dirVar = unknownNeighbours.get(k);
				if (unknownNeighbours.get(k) == state.agent_direction) {
					direction = unknownNeighbours.get(k);
					state.unknownStack.pop();
				}
			} else if (unknownNeighbours.get(k) == 2) {
				unknownTiles[0][0] = state.agent_x_position;
				unknownTiles[0][1] = state.agent_y_position + 1;
				state.unknownStack.push(unknownTiles);
				System.out.println("The state of unknownStack: " + state.unknownStack);
				dirVar = unknownNeighbours.get(k);
				if (unknownNeighbours.get(k) == state.agent_direction) {
					direction = unknownNeighbours.get(k);
					state.unknownStack.pop();
				}
			} else if (unknownNeighbours.get(k) == 3) {
				unknownTiles[0][0] = state.agent_x_position - 1;
				unknownTiles[0][1] = state.agent_y_position;
				state.unknownStack.push(unknownTiles);
				System.out.println(state.unknownStack);
				dirVar = unknownNeighbours.get(k);
				if (unknownNeighbours.get(k) == state.agent_direction) {
					direction = unknownNeighbours.get(k);
					state.unknownStack.pop();
				}
			}
		}

		if (direction != -1) {
			return direction;
		} else {
			state.unknownStack.pop();
			return dirVar;
		}
	}

	// if no unknown is find, go to clear tile
	private Action moveForward(MyAgentState state) {
		state.agent_last_action = state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
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