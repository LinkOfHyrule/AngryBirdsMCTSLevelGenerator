package core.game;

import java.util.*;

import core.game.Types.WINNER;


/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 17/10/13
 * Time: 13:42
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public abstract class Game
{
    /**
     * Random number generator for this game. It can only be received when the game is started.
     */
    private Random random;

    /**
     * Avatars last actions.
     * Array for all avatars in the game.
     * Index in array corresponds to playerID.
     */
    protected Types.ACTIONS[] avatarLastAction;

    public int no_players = 1; //default to single player

    /**
     * Default constructor.
     */
    public Game()
    {
    }

	public WINNER getWinner() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getScore() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getGameTick() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isGameOver() {
		// TODO Auto-generated method stub
		return false;
	}

}
