package pl.org.miki.ld30.game;

import com.badlogic.ashley.core.Component;

public class PlayerComponent extends Component {
	
	private WorldPlane world;
	
	private int energy;
	
	private String name;

	public WorldPlane getWorld() {
		return world;
	}

	public void setWorld(WorldPlane world) {
		this.world = world;
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
