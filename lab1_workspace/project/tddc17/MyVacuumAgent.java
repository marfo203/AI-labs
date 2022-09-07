package tddc17;

import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.Random;

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
}

class MyAgentProgram implements AgentProgram {

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();

	// Here you can define your variables!
	public int iterationCounter = 10;
	public MyAgentState state = new MyAgentState();
	public int bumpCounter = 0;
	public int startDir = 4;
	public boolean startSearch = false;
	public int maxWorldArea;
	public boolean hasTurned = false;
	public boolean moveOneStep = false;
	public boolean caseRightTurn = false;
	public int caseRightTurnCounter = 1;
	public boolean caseLeftTurn = false;
	public int caseLeftTurnCounter = 1;

	public int maxCornerX;
	public int maxCornerY;
	public int horCounter;
	public int verCounter;

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
			startDir = state.agent_direction;
			System.out.println("Start Dir: " + startDir);
			state.agent_last_action = state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;
		}

		// This example agent program will update the internal agent state while only
		// moving forward.
		// START HERE - code below should be modified!

		System.out.println("x=" + state.agent_x_position);
		System.out.println("y=" + state.agent_y_position);
		System.out.println("dir=" + state.agent_direction);

		// iterationCounter--;

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
			bumpCounter++;
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
				if (!startSearch) {
					if (startDir == 3 || startDir == 0) {

						if (bumpCounter < 3) {
							if (state.agent_direction == 0 || state.agent_direction == 1) {
								state.agent_last_action = state.ACTION_NONE;
								state.agent_direction = state.agent_direction + 1;
								return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
							} else {
								state.agent_last_action = state.ACTION_NONE;
								state.agent_direction = state.agent_direction - 1;
								return LIUVacuumEnvironment.ACTION_TURN_LEFT;
							}

						} else {
							// Nu är vi i hörnet
							maxCornerX = state.agent_x_position;
							maxCornerY = state.agent_y_position;
							startSearch = true;

							setWalls(maxCornerX, maxCornerY);
							maxWorldArea = maxCornerX * maxCornerY;
							System.out.println(maxWorldArea);
						}

					} else {
						if (bumpCounter < 2) {
							if (state.agent_direction == 1) {
								state.agent_last_action = state.ACTION_TURN_RIGHT;
								state.agent_direction = state.agent_direction + 1;
								return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
							} else {
								state.agent_last_action = state.ACTION_TURN_LEFT;
								state.agent_direction = state.agent_direction - 1;
								return LIUVacuumEnvironment.ACTION_TURN_LEFT;
							}

						} else {
							// Nu är vi i hörnet
							maxCornerX = state.agent_x_position;
							maxCornerY = state.agent_y_position;
							horCounter = maxCornerX;
							verCounter = maxCornerY;
							startSearch = true;

							setWalls(maxCornerX, maxCornerY);
							maxWorldArea = maxCornerX * maxCornerY;
							System.out.println(maxWorldArea);
							System.out.println("Vi är nu i hörnet");

						}
					}
				}

			}
		}

		// startSearch här
		state.agent_last_action = state.ACTION_NONE;
		// svänga ut ur hörnet norr
		if (state.agent_direction == 1 && state.agent_x_position == maxCornerX
				&& state.agent_y_position == maxCornerY) {
			return turnLeft(state);
			// svänga ut ur hörnet väst
		} else if (state.agent_direction == 2 && state.agent_x_position == maxCornerX
				&& state.agent_y_position == maxCornerY) {
			return turnRight(state);
			// Flytta väst
		} else if (horCounter > 1 && verCounter > 1 && state.agent_direction == 3 && !moveOneStep) {
			state.agent_last_action = state.ACTION_MOVE_FORWARD;
			horCounter--;
			System.out.println("minus hor - " + horCounter);
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
			// Flytta väst 1 steg
		} else if (horCounter > 1 && verCounter > 1 && state.agent_direction == 3 && moveOneStep) {
			state.agent_last_action = state.ACTION_MOVE_FORWARD;
			horCounter--;
			moveOneStep = false;
			System.out.println("minus hor - " + horCounter);
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
			// Flytta öst
		} else if (horCounter > 1 && verCounter > 1 && state.agent_direction == 1 && !moveOneStep) {
			state.agent_last_action = state.ACTION_MOVE_FORWARD;
			horCounter--;
			moveOneStep = false;
			System.out.println("minus hor - " + horCounter + " EAST");
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
			// Flytta norr
		} else if (horCounter > 1 && verCounter > 1 && state.agent_direction == 0 && !moveOneStep) {
			state.agent_last_action = state.ACTION_MOVE_FORWARD;
			verCounter--;
			System.out.println("minus ver - " + verCounter);
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
			// Flytta norr 1 steg
		} else if (horCounter > 1 && verCounter > 1 && state.agent_direction == 0 && moveOneStep) {
			state.agent_last_action = state.ACTION_MOVE_FORWARD;
			verCounter--;
			moveOneStep = false;
			System.out.println("minus ver - " + verCounter);
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
			// Dags att svänga norr när vi går väst
		} else if (horCounter == 1 && (state.agent_direction == 3 || state.agent_direction == 0)
				&& state.agent_last_action != state.ACTION_TURN_LEFT) {
			caseRightTurn = true;
			return cornerTurn(state);
			// Dags att svänga norr när vi går öst
		} else if (horCounter == 1 && (state.agent_direction == 1 || state.agent_direction == 0)) {
			caseLeftTurn = true;
			System.out.println("Sväng norr kommer från öst");
			return cornerTurn(state);
			// Dags att svänga väst
		} else if (verCounter == 1) {
			caseLeftTurn = true;
			return cornerTurn(state);
			// Annars Move
		} else if (state.agent_direction == 0 && !moveOneStep) {
			state.agent_last_action = state.ACTION_MOVE_FORWARD;
			System.out.println("bitch 0");
			hasTurned = false;
			horCounter = maxCornerX;
			verCounter = maxCornerY - 1;
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
		} else if (state.agent_direction == 3 && !moveOneStep) {
			state.agent_last_action = state.ACTION_MOVE_FORWARD;
			System.out.println("bitch 3");
			hasTurned = false;
			verCounter = maxCornerY;
			horCounter = maxCornerX - 1;
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
		} else {
			System.out.println("Sista else");
			state.agent_last_action = state.ACTION_MOVE_FORWARD;
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
		}
	}

	private Action cornerTurn(MyAgentState state) {
		if (caseRightTurn) {
			if (caseRightTurnCounter == 1) {
				caseRightTurnCounter++;
				return turnRight(state);
			} else if (caseRightTurnCounter == 2) {
				System.out.println("minus ver - " + verCounter);
				state.agent_last_action = state.ACTION_MOVE_FORWARD;
				caseRightTurnCounter++;
				verCounter--;
				return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
			} else if (caseRightTurnCounter == 3) {
				caseRightTurnCounter++;
				return turnRight(state);
			} else if (caseRightTurnCounter == 4) {
				caseRightTurn = false;
				caseRightTurnCounter = 1;
				horCounter = maxCornerX;
				state.agent_last_action = state.ACTION_MOVE_FORWARD;
				return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
			}
		} else if (caseLeftTurn) {
			if (caseLeftTurnCounter == 1) {
				caseLeftTurnCounter++;
				return turnLeft(state);
			} else if (caseLeftTurnCounter == 2) {
				System.out.println("minus ver - " + verCounter);
				state.agent_last_action = state.ACTION_MOVE_FORWARD;
				caseLeftTurnCounter++;
				verCounter--;
				return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
			} else if (caseLeftTurnCounter == 3) {
				caseLeftTurnCounter++;
				return turnLeft(state);
			} else if (caseLeftTurnCounter == 4) {
				caseLeftTurn = false;
				caseLeftTurnCounter = 1;
				horCounter = maxCornerX;
				state.agent_last_action = state.ACTION_MOVE_FORWARD;
				return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
			}
		} // Sista vägen ut....
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

	private void setWalls(int maxCornerX2, int maxCornerY2) {
		for (int i = 0; i < maxCornerY2 + 1; i++) {
			state.updateWorld(0, i, state.WALL);
			state.updateWorld(maxCornerX2 + 1, i, state.WALL);
		}
		for (int i = 0; i < maxCornerX2 + 1; i++) {
			state.updateWorld(i, 0, state.WALL);
			state.updateWorld(i, maxCornerX2 + 1, state.WALL);

		}

	}
}

public class MyVacuumAgent extends AbstractAgent {
	public MyVacuumAgent() {
		super(new MyAgentProgram());
	}
}
