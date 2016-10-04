package core.game;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 16/10/13
 * Time: 14:00
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class BasicGame extends Game {

    /**
     * Default constructor for a basic game.
     * @param content Contains parameters for the game.
     */
    public BasicGame()
    {
        super();
    }

    @Override
    public boolean isGameOver() {
        return false;
    }

}


