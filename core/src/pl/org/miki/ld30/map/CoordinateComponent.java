package pl.org.miki.ld30.map;

import com.badlogic.ashley.core.Component;

public class CoordinateComponent extends Component {

	private int x;

	private int y;
	
	private String playerName;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public CoordinateComponent(String playerName, int y, int x) {
		super();
		this.playerName = playerName;
		this.x = x;
		this.y = y;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
}
