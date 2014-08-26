package pl.org.miki.ld30.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.IntMap;

public enum TerrainType {
	

	VOID(false, true, 0, 1),
	DESERT(true, true, 200, 0.7f),
	FOREST(true,false, 400, 2f),
	MOUNTAINS(true, false, 500, 2.5f),
	OCEAN(true, true, 500, 0.3f);
	
	private static final int COST_MULTIPLIER = 2; 

	private int energyPerTurn;
	
	private float defenseModifier;
	
	private boolean isOpen;
	
	private boolean isNavigable;
	
	private TerrainType(boolean isNavigable, boolean isOpen, int energyPerTurn, float defenseModifier) {
		this.energyPerTurn = energyPerTurn;
		this.defenseModifier = defenseModifier;
		this.isOpen = isOpen;
		this.isNavigable = isNavigable;
	}

	public int getEnergyPerTurn() {
		return energyPerTurn;
	}


	public float getDefenseModifier() {
		return defenseModifier;
	}


	public boolean isOpen() {
		return isOpen;
	}


	public boolean isNavigable() {
		return isNavigable;
	}

	
	public int getCost() {
		return getEnergyPerTurn()*COST_MULTIPLIER;
	}

	public static IntMap<TerrainType> getIntToType() {
		return intToType;
	}


	private static IntMap<TerrainType> intToType;
	
	
	public static TerrainType idToTerrainType(int id) {
		if(intToType == null) {
			
			intToType = new IntMap<>();
			
			//init map
			FileHandle config = Gdx.files.internal("tileconfig");
			intToType.put(0, VOID);
			int i = 0;
			for(String terrainType : config.readString().split("\n")) {
				intToType.put(++i, TerrainType.valueOf(terrainType.toUpperCase()));
			}
		}
		
		
		return intToType.get(id);
	}
	
}
