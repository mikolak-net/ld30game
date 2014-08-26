package pl.org.miki.ld30.game;

import com.badlogic.gdx.maps.tiled.TiledMap;

public class WorldPlane {

	private TiledMap map;

	
	public WorldPlane(TiledMap map) {
		super();
		this.map = map;
	}

	public TiledMap getMap() {
		return map;
	}

	public void setMap(TiledMap map) {
		this.map = map;
	}
	
	
}
