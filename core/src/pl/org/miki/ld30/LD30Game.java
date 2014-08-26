package pl.org.miki.ld30;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.org.miki.ld30.game.PlayerComponent;
import pl.org.miki.ld30.game.WorldPlane;
import pl.org.miki.ld30.map.CoordinateComponent;
import pl.org.miki.ld30.map.TerrainComponent;
import pl.org.miki.ld30.map.TerrainType;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.ComponentType;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class LD30Game extends ApplicationAdapter {
	
	private static List<String> PLAYER_LIST = Arrays.asList("White","Black");
	
	private static int ENERGY_ON_START = 1000;
	
	private Map<String,PlayerComponent> playerData;  

	private TiledMap map;
	private Engine engine;
	
	private SpriteBatch batch;
	private EntitySystem turnSystem;
	
	private IsometricTiledMapRenderer renderer;
	
	private OrthographicCamera camera;
	private GameCamController cameraController;

	private Stage ui;
	
	private BitmapFont font;

	
	
	@Override
	public void create () {
		
		engine = new Engine();
		
		createState();

		createController();
		
		createUi();
	}
	
	private void createState() {
		
		playerData = new HashMap<>();
		
		for(String playerName : PLAYER_LIST) {
			Entity player = new Entity();
			
			PlayerComponent playerComponent = new PlayerComponent();
			
			playerComponent.setName(playerName);
			
			playerComponent.setEnergy(ENERGY_ON_START);
		
			TiledMap map = new TmxMapLoader().load("testfill.tmx");
			//create entities for each map tile
			TiledMapTileLayer mapLayer = (TiledMapTileLayer)map.getLayers().get(MapConstants.LAYER_TERRAIN);
			
			for (int x = 0; x < mapLayer.getWidth(); x++) {
				for (int y = 0; y < mapLayer.getHeight(); y++) {
					Cell cell = mapLayer.getCell(x, y);
					
					TerrainComponent terrain = new TerrainComponent();
					
					terrain.setTerrainType(TerrainType.idToTerrainType(cell == null ? 0 : cell.getTile().getId()));
					
					Entity tileEntity = new Entity();
					tileEntity.add(terrain);
					tileEntity.add(new CoordinateComponent(playerName, x, y));
					engine.addEntity(tileEntity);
				}
			}
			
			WorldPlane world = new WorldPlane(map);
			
			playerComponent.setWorld(world);
			
			playerData.put(playerName, playerComponent);
			
			player.add(playerComponent);
			engine.addEntity(player);
		}
		
		final Family terrainFamily = Family.getFor(CoordinateComponent.class, TerrainComponent.class);
		
		
		
		turnSystem = new EntitySystem() {
			private boolean turnEnded;
			
			private int playerIndex = -1;
			
			@Override
			public boolean checkProcessing() {
				return isTurnProgress();
			}
			
			@Override
			public void update(float deltaTime) {
				playerIndex = (playerIndex+1) % PLAYER_LIST.size();
				
				//that's some impressive indirection right there...
				PlayerComponent curPlayer = playerData.get(PLAYER_LIST.get(playerIndex));
				
				//get energy from all fields
				ImmutableArray<Entity> tiles = engine.getEntitiesFor(terrainFamily);
				
				
				//wonder what I'm doing wrong here...
				for(int i = 0; i < tiles.size(); i++) {
					
					Entity terrainTile = tiles.get(i);
					
					if(!ComponentMapper.getFor(CoordinateComponent.class).get(terrainTile).getPlayerName().equals(curPlayer.getName())) {
						continue;
					}
					
					TerrainComponent terrain = ComponentMapper.getFor(TerrainComponent.class).get(terrainTile);
					
					curPlayer.setEnergy(curPlayer.getEnergy()+terrain.getTerrainType().getEnergyPerTurn());
				}
				
				//set the visible map to one of the current player
				
				map = curPlayer.getWorld().getMap();
			}
			

			private boolean isTurnProgress() {
				return playerIndex < 0 || turnEnded;
			}
			
		};
		
		engine.addSystem(turnSystem);
		engine.update(0);
	}
	
	private void createController() {
		float unitScale = 1 / 128f;
		renderer = new IsometricTiledMapRenderer(map, unitScale);
		int multiplier = 10;
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, (w / h) * multiplier, multiplier);
		camera.update();
		
		cameraController = new GameCamController(camera, new SelectionMarker(map, map.getTileSets().getTileSet("selectedTile").getTile(5)));
		Gdx.input.setInputProcessor(cameraController);
	}
	
	private void createUi() {
		font = new BitmapFont();
		batch = new SpriteBatch();
		
		ui = new Stage();
		
		LabelStyle styleStandard = new LabelStyle(font,Color.WHITE);
		
		
		Label test = new Label("status", styleStandard);
				
		Table uiHolder = new Table();
		
		
		uiHolder.add(test);
		uiHolder.row();
		
		Table orderTable = new Table();

		
		uiHolder.add(orderTable);
		uiHolder.row();
		
		Label nextTurnButton = new Label("NEXT TURN", styleStandard);
		
		nextTurnButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				nextTurn();
			}
		});
		

		
		uiHolder.add(nextTurnButton);
		
		ui.addActor(uiHolder);
		ui.getRoot().setPosition(Gdx.graphics.getWidth()-uiHolder.getMinWidth(), 
									Gdx.graphics.getHeight()-uiHolder.getMinHeight());
	}



	
	@Override
	public void render () {
		Gdx.gl.glClearColor(0.55f, 0.55f, 0.55f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		renderer.setView(camera);
		renderer.render();
		batch.begin();
		font.draw(batch, atTile(), 10, 20);
		batch.end();
		ui.act(Gdx.graphics.getDeltaTime());
		ui.draw();
	}
	
	private void nextTurn() {
		System.out.println("NEXT TURN!");
		engine.update(1);
	}
	
	private String atTile() {
		String ret = "";
		
		GridPoint2 tile = cameraController.translateScreenToTile();
		ret = "("+tile.x+","+tile.y+") / ("+Gdx.input.getX()+","+Gdx.input.getY()+")";
		
		return ret;
	}
	
	
	@Override
	public void dispose() {
		map.dispose();
		ui.dispose();
	}
	
	
	private static boolean isWithinMap(TiledMapTileLayer layer, GridPoint2 loc) {
		return loc.x >= 0 && loc.y >= 0 && loc.x < layer.getWidth() && loc.y < layer.getHeight();
	}
	
	
	private static class SelectionMarker { 
		private TiledMapTileLayer layer;
		private TiledMapTile tile;
		
		private GridPoint2 curSelection;
		
		public SelectionMarker(TiledMap selectionMap, TiledMapTile selectionTile) {
			super();
			this.layer = getSelectionLayer(selectionMap);
			this.tile = selectionTile;
			curSelection = null;
		}
		
		private TiledMapTileLayer getSelectionLayer(TiledMap map) {
			return (TiledMapTileLayer)map.getLayers().get(MapConstants.LAYER_SELECT);
		}
		
		public void switchMap(TiledMap map) {
			reset();
			this.layer = getSelectionLayer(map);
		}
		
		public void selectAt(GridPoint2 at) {
			if(!isWithinMap(layer, at)) {
				//ignore
				return;
			}
			
			
			reset();
			curSelection = at;
			
			Cell selectionCell = new Cell();
			selectionCell.setTile(tile);
			
			layer.setCell(curSelection.x, curSelection.y, selectionCell);
		}
		
		private void reset() {
			if(curSelection == null) {
				return;
			}
			layer.setCell(curSelection.x,curSelection.y,null);
		}
	}
	
	/**
	 * Based from bits and pieces of OrthoCamController and IsoTileRenderer. 
	 */
	public class GameCamController extends InputAdapter {
		final OrthographicCamera camera;
		final Vector3 curr = new Vector3();
		final Vector3 last = new Vector3(-1, -1, -1);
		final Vector3 delta = new Vector3();
		private final SelectionMarker marker;
		private boolean wasDrag = false;
		
		private Matrix4 invIsotransform;

		

		/**
		 * Pretty much copied from the renderer.
		 */
		private void initInverseMatrix() {
			// create the isometric transform
			Matrix4 isoTransform = new Matrix4();
			isoTransform.idt();

			// isoTransform.translate(0, 32, 0);
			isoTransform.scale((float)(Math.sqrt(2.0) / 2.0), (float)(Math.sqrt(2.0) / 4.0), 1.0f);
			isoTransform.rotate(0.0f, 0.0f, 1.0f, -45);

			// ... and the inverse matrix
			invIsotransform = new Matrix4(isoTransform);
			invIsotransform.inv();
		}
		
		/**
		 * 
		 */
		private GridPoint2 translateScreenToTile() {
			float x = Gdx.input.getX();
			float y = Gdx.input.getY();
			Vector3 screenPos = new Vector3();
			screenPos.set(x,y,0);
			screenPos = camera.unproject(screenPos);
			screenPos.mul(invIsotransform);
			
			screenPos.add(0.5f, -0.5f, 0); //center on tile boundary
			
			return new GridPoint2((int) screenPos.x,(int) screenPos.y);
		}
		
		public GameCamController(OrthographicCamera camera, SelectionMarker marker) {
			this.camera = camera;
			this.marker = marker;
			initInverseMatrix();
		}
		
		@Override
		public boolean touchDown(int screenX, int screenY, int pointer,
				int button) {
			wasDrag = false;
			
			//pass event to UI
			ui.touchDown(screenX, screenY, pointer, button);
			
			return false;
		}

		@Override
		public boolean touchDragged(int x, int y, int pointer) {
			wasDrag = true;
			camera.unproject(curr.set(x, y, 0));
			if (!(last.x == -1 && last.y == -1 && last.z == -1)) {
				camera.unproject(delta.set(last.x, last.y, 0));
				delta.sub(curr);
				camera.position.add(delta.x, delta.y, 0);
			}
			last.set(x, y, 0);
			return false;
		}

		
		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			last.set(-1, -1, -1);
			
			if(!wasDrag) {
				marker.selectAt(translateScreenToTile());
			}
			
			//pass event to UI
			ui.touchUp(screenX, screenY, pointer, button);
			
			return false;
		}
	}
	
}
