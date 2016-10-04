package core.player;

import core.game.StateObservation;
import core.game.Types;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 13:42
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */

/**
 * Subclass of Player, for Single Player games.
 * Implements multi player act method (returns NULL).
 */
public abstract class AbstractPlayer extends Player {

    public Types.ACTIONS act(StateObservation stateObs) {
        return Types.ACTIONS.ACTION_NIL;
    }

}
