package pl.org.miki.ld30.game;

import com.badlogic.ashley.core.Component;

public class TurnStateComponent extends Component {

	private String activePlayer;
	
	private int turnNumber;

	public String getActivePlayer() {
		return activePlayer;
	}

	public void setActivePlayer(String activePlayer) {
		this.activePlayer = activePlayer;
	}

	public int getTurnNumber() {
		return turnNumber;
	}

	public void setTurnNumber(int turnNumber) {
		this.turnNumber = turnNumber;
	}
	
}
