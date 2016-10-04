package sampleMCTS;

import java.util.ArrayList;
import java.util.Random;

import core.game.StateObservation;
import core.game.Types;
import core.game.Types.ACTIONS;
import core.player.AbstractPlayer;
import tools.ElapsedCpuTimer;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

    public int NUM_ACTIONS;
    public static int ROLLOUT_DEPTH = 50;
    public static double K = Math.sqrt(2);
    public Types.ACTIONS[] actions;

    /**
     * Random generator for the agent.
     */
    private SingleMCTSPlayer mctsPlayer;

    /**
     * Public constructor with state observation and time due.
     * @param availableActions actions that can be taken in this game.
     */
    public Agent(ArrayList<ACTIONS> availableActions)
    {
        //Get the actions in a static array.
        actions = new Types.ACTIONS[availableActions.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = availableActions.get(i);
        }
        NUM_ACTIONS = actions.length;

        //Create the player.
        mctsPlayer = new SingleMCTSPlayer(new Random(), this);
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        //ArrayList<Observation> obs[] = stateObs.getFromAvatarSpritesPositions();
        //ArrayList<Observation> grid[][] = stateObs.getObservationGrid();

        //Set the state observation object as the new root of the tree.
        mctsPlayer.init(stateObs, this);

        //Determine the action using MCTS...
        int action = mctsPlayer.run(elapsedTimer);

        //... and return it.
        return actions[action];
    }

    /**
     * Function called when the game is over. Gets best action from current state.
     * @param stateObservation the game state at the end of the game
     * @param elapsedCpuTimer timer when this method is meant to finish.
     * @return 
     */
    public Types.ACTIONS actBestAction(StateObservation stateObservation, ElapsedCpuTimer elapsedTimer)
    {
//        System.out.println("MCTS avg iters: " + SingleMCTSPlayer.iters / SingleMCTSPlayer.num);
        //Include your code here to know how it all ended.
        //System.out.println("Game over? " + stateObservation.isGameOver());

        //Set the state observation object as the new root of the tree.
        mctsPlayer.init(stateObservation, this);

        //Determine the action using MCTS...
        int action = mctsPlayer.runBestAction(elapsedTimer);

        //... and return it.
        return actions[action];
    }


}
