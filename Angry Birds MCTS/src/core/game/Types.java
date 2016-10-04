package core.game;

import java.awt.Color;
import java.awt.event.KeyEvent;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 17/10/13
 * Time: 11:05
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Types {

    public static enum ACTIONS {
		ACTION_PLACE_STRUCTURE(0),
		ACTION_PLACE_PIG(1),
        ACTION_NIL(2);

        private int key;
        ACTIONS(int val) {key=val;}
        public int key() {return key;}
    }


    public static enum WINNER {
        PLAYER_DISQ(-100),
        NO_WINNER(-1),
        PLAYER_LOSES(0),
        PLAYER_WINS(1);

        private int key;
        WINNER(int val) {key=val;}
        public int key() {return key;}
    }

}
