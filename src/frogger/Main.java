/**
 * Copyright (c) 2009 Vitaliy Pavlenko
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package frogger;

import java.awt.event.KeyEvent;
import jig.engine.ImageResource;
import jig.engine.PaintableCanvas;
import jig.engine.RenderingContext;
import jig.engine.ResourceFactory;
import jig.engine.PaintableCanvas.JIGSHAPE;
import jig.engine.hli.ImageBackgroundLayer;
import jig.engine.hli.StaticScreenGame;
import jig.engine.physics.AbstractBodyLayer;
import jig.engine.util.Vector2D;

public class Main extends StaticScreenGame {
	static final int WORLD_WIDTH = (13*32);
	static final int WORLD_HEIGHT = (14*32);
	private static final Vector2D FROGGER_START = new Vector2D(6*32,WORLD_HEIGHT-32);
	
	static final String RSC_PATH = "resources/";
	static final String SPRITE_SHEET = RSC_PATH + "frogger_sprites.png";
	
    static final int FROGGER_LIVES      = 5;
    static final int STARTING_LEVEL     = 1;
	static final int DEFAULT_LEVEL_TIME = 60;
	
	private FroggerCollisionDetection frogCol;
	private Frogger frog;
	private AudioEfx audiofx;
	private FroggerUI ui;
	private WindGust wind;
	private HeatWave hwave;
	private GoalManager goalmanager;
	
	private AbstractBodyLayer<MovingEntity> movingObjectsLayer;
	private AbstractBodyLayer<MovingEntity> particleLayer;
	
	private MovingEntityFactory roadLine1;
	private MovingEntityFactory roadLine2;
	private MovingEntityFactory roadLine3;
	private MovingEntityFactory roadLine4;
	private MovingEntityFactory roadLine5;
	
	private MovingEntityFactory riverLine1;
	private MovingEntityFactory riverLine2;
	private MovingEntityFactory riverLine3;
	private MovingEntityFactory riverLine4;
	private MovingEntityFactory riverLine5;
	
	private ImageBackgroundLayer backgroundLayer;
	
    static final int GAME_INTRO        = 0;
    static final int GAME_PLAY         = 1;
    static final int GAME_FINISH_LEVEL = 2;
    static final int GAME_INSTRUCTIONS = 3;
    static final int GAME_OVER         = 4;
    
	protected int gameState = GAME_INTRO;
	protected int gameLevel = STARTING_LEVEL;
	
    private static int gameLives    = FROGGER_LIVES;
    private static int gameScore    = 0;
    
    private static int levelTimer = DEFAULT_LEVEL_TIME;
    
    private boolean spaceHasBeenReleased = false;
	private boolean keyPressed = false;
	private boolean listenInput = true;
	
    /**
	 * Initialize game objects
	 */
	public Main () {
		
		super(WORLD_WIDTH, WORLD_HEIGHT, false);
		
		gameframe.setTitle("Frogger");
		
		ResourceFactory.getFactory().loadResources(RSC_PATH, "resources.xml");

		ImageResource bkg = ResourceFactory.getFactory().getFrames(
				SPRITE_SHEET + "#background").get(0);
		backgroundLayer = new ImageBackgroundLayer(bkg, WORLD_WIDTH,
				WORLD_HEIGHT, ImageBackgroundLayer.TILE_IMAGE);
		
		// Used in CollisionObject, basically 2 different collision spheres
		// 30x30 is a large sphere (sphere that fits inside a 30x30 pixel rectangle)
		//  4x4 is a tiny sphere
		PaintableCanvas.loadDefaultFrames("col", 30, 30, 2, JIGSHAPE.RECTANGLE, null);
		PaintableCanvas.loadDefaultFrames("colSmall", 4, 4, 2, JIGSHAPE.RECTANGLE, null);
			
		frog = new Frogger(this);
		frogCol = new FroggerCollisionDetection(frog);
		audiofx = new AudioEfx(frogCol,frog);
		ui = new FroggerUI(this);
		wind = new WindGust();
		hwave = new HeatWave();
		goalmanager = new GoalManager();
		
		movingObjectsLayer = new AbstractBodyLayer.IterativeUpdate<>();
		particleLayer = new AbstractBodyLayer.IterativeUpdate<>();
		
		initializeLevel(1);
	}
	
	
	public void initializeLevel(int level) {

		/* dV is the velocity multiplier for all moving objects at the current game level */
		double dV = level*0.05 + 1;
		
		movingObjectsLayer.clear();
		
		/* River Traffic */
		riverLine1 = new MovingEntityFactory(new Vector2D(-(32*3),2*32), 
				new Vector2D(0.06*dV,0)); 
		
		riverLine2 = new MovingEntityFactory(new Vector2D(Main.WORLD_WIDTH,3*32),  
				new Vector2D(-0.04*dV,0)); 
		
		riverLine3 = new MovingEntityFactory(new Vector2D(-(32*3),4*32), 
				new Vector2D(0.09*dV,0)); 
		
		riverLine4 = new MovingEntityFactory(new Vector2D(-(32*4),5*32),  
				new Vector2D(0.045*dV,0));
		
		riverLine5 = new MovingEntityFactory(new Vector2D(Main.WORLD_WIDTH,6*32), 
				new Vector2D(-0.045*dV,0));
		
		/* Road Traffic */
		roadLine1 = new MovingEntityFactory(new Vector2D(Main.WORLD_WIDTH, 8*32), 
				new Vector2D(-0.1*dV, 0)); 
		
		roadLine2 = new MovingEntityFactory(new Vector2D(-(32*4), 9*32), 
				new Vector2D(0.08*dV, 0)); 
		
		roadLine3 = new MovingEntityFactory(new Vector2D(Main.WORLD_WIDTH, 10*32),
			    new Vector2D(-0.12*dV, 0)); 
		
		roadLine4 = new MovingEntityFactory(new Vector2D(-(32*4), 11*32),
				new Vector2D(0.075*dV, 0));
		
		roadLine5 = new MovingEntityFactory(new Vector2D(Main.WORLD_WIDTH, 12*32),
				new Vector2D(-0.05*dV, 0)); 
		
		goalmanager.init(level);
		for (Goal g : goalmanager.get()) {
			movingObjectsLayer.add(g);
		}
			
		/* Build some traffic before game starts buy running MovingEntityFactories for fews cycles */
		for (int i=0; i<500; i++)
			cycleTraffic(10);
	}
	
	
	/**
	 * Populate movingObjectLayer with a cycle of cars/trucks, moving tree logs, etc
	 * 
	 * @param deltaMs
	 */
	public void cycleTraffic(long deltaMs) {
		roadTrafficUpdates(deltaMs);
		
		riverTrafficUpdates(deltaMs);
	    
	    // Do Wind
		MovingEntity mWind = wind.genParticles(gameLevel);
	    if (mWind != null) particleLayer.add(mWind);
	    
	    // HeatWave
	    MovingEntity mWave = hwave.genParticles(frog.getCenterPosition());
	    if (mWave != null) particleLayer.add(mWave);
	        
	    movingObjectsLayer.update(deltaMs);
	    particleLayer.update(deltaMs);
	}
	
	private void roadTrafficUpdates(long deltaMs) {
		/* Road traffic updates */
		roadLine1.update(deltaMs);
		MovingEntity mRoadLine1 = roadLine1.buildVehicle();
	    if (mRoadLine1 != null) movingObjectsLayer.add(mRoadLine1);
		
		roadLine2.update(deltaMs);
		MovingEntity mRoadLine2 = roadLine2.buildVehicle();
	    if (mRoadLine2 != null) movingObjectsLayer.add(mRoadLine2);
	    
		roadLine3.update(deltaMs);
		MovingEntity mRoadLine3 = roadLine3.buildVehicle();
	    if (mRoadLine3 != null) movingObjectsLayer.add(mRoadLine3);
	    
		roadLine4.update(deltaMs);
		MovingEntity mRoadLine4 = roadLine4.buildVehicle();
	    if (mRoadLine4 != null) movingObjectsLayer.add(mRoadLine4);

		roadLine5.update(deltaMs);
		MovingEntity mRoadLine5 = roadLine5.buildVehicle();
	    if (mRoadLine5 != null) movingObjectsLayer.add(mRoadLine5);
	}
	
	private void riverTrafficUpdates(long deltaMs) {
		/* River traffic updates */
		riverLine1.update(deltaMs);
		MovingEntity mRiverLine1 = riverLine1.buildShortLogWithTurtles(40);
	    if (mRiverLine1 != null) movingObjectsLayer.add(mRiverLine1);
		
		riverLine2.update(deltaMs);
		MovingEntity mRiverLine2 = riverLine2.buildLongLogWithCrocodile(30);
	    if (mRiverLine2 != null) movingObjectsLayer.add(mRiverLine2);
		
		riverLine3.update(deltaMs);
		MovingEntity mRiverLine3 = riverLine3.buildShortLogWithTurtles(50);
	    if (mRiverLine3 != null) movingObjectsLayer.add(mRiverLine3);
		
		riverLine4.update(deltaMs);
		MovingEntity mRiverLine4 = riverLine4.buildLongLogWithCrocodile(20);
	    if (mRiverLine4 != null) movingObjectsLayer.add(mRiverLine4);

		riverLine5.update(deltaMs);
		MovingEntity mRiverLine5 = riverLine5.buildShortLogWithTurtles(10);
	    if (mRiverLine5 != null) movingObjectsLayer.add(mRiverLine5);
	}
	
	/**
	 * Handling Frogger movement from keyboard input
	 */
	public void froggerKeyboardHandler() {
 		keyboard.poll();
		
 		boolean keyReleased = false;
        boolean downPressed = keyboard.isPressed(KeyEvent.VK_DOWN);
        boolean upPressed = keyboard.isPressed(KeyEvent.VK_UP);
		boolean leftPressed = keyboard.isPressed(KeyEvent.VK_LEFT);
		boolean rightPressed = keyboard.isPressed(KeyEvent.VK_RIGHT);
		
		enableDisableCheating();
		
		keyStrokesCheck(keyReleased, downPressed, upPressed, leftPressed, rightPressed);
		
	}
	
	private void enableDisableCheating() {
		// Enable/Disable cheating
				if (keyboard.isPressed(KeyEvent.VK_C))
					frog.setCheating(true);
				if (keyboard.isPressed(KeyEvent.VK_V))
					frog.setCheating(false);
				if (keyboard.isPressed(KeyEvent.VK_0)) {
					gameLevel = 10;
					initializeLevel(gameLevel);
				}
	}
	
	private void keyStrokesCheck(boolean keyReleased, boolean downPressed, boolean upPressed,
			boolean leftPressed, boolean rightPressed) {
		/*
		 * This logic checks for key strokes.
		 * It registers a key press, and ignores all other key strokes
		 * until the first key has been released
		 */
		
		if (downPressed || upPressed || leftPressed || rightPressed)
			keyPressed = true;
		else if (keyPressed)
			keyReleased = true;
		
		
		listenInputMethod(downPressed, upPressed, leftPressed, rightPressed);
		
		if (keyReleased) {
			listenInput = true;
			keyPressed = false;
		}
		
		if (keyboard.isPressed(KeyEvent.VK_ESCAPE))
			gameState = GAME_INTRO;
	}
	
	private void listenInputMethod(boolean downPressed, boolean upPressed,
			boolean leftPressed, boolean rightPressed) {
		if (listenInput) {
		    if (downPressed) {
		    	frog.moveDown();
		    }
		    if (upPressed) {
		    	frog.moveUp();
		    }
		    if (leftPressed) {
		    	frog.moveLeft();
		    }
	 	    if (rightPressed) {
	 	    	frog.moveRight();
	 	    }
	 	    
	 	    if (keyPressed) {
	 	    	listenInput = false;
	 	    }
		}
	}
	
	/**
	 * Handle keyboard events while at the game intro menu
	 */
	public void menuKeyboardHandler() {
		keyboard.poll();
		
		// Following 2 if statements allow capture space bar key strokes
		if (!keyboard.isPressed(KeyEvent.VK_SPACE)) {
			spaceHasBeenReleased = true;
		}
		
		if (!spaceHasBeenReleased)
			return;
		
		if (keyboard.isPressed(KeyEvent.VK_SPACE)) {
			switch (gameState) {
			case GAME_INSTRUCTIONS:
			case GAME_OVER:
				gameState = GAME_INTRO;
				spaceHasBeenReleased = false;
				break;
			default:
				setGameLives(FROGGER_LIVES);
				setGameScore(0);
				gameLevel = STARTING_LEVEL;
				setLevelTimer(DEFAULT_LEVEL_TIME);
				frog.setPosition(getFroggerStart());
				gameState = GAME_PLAY;
				audiofx.playGameMusic();
				initializeLevel(gameLevel);			
			}
		}
		if (keyboard.isPressed(KeyEvent.VK_H))
			gameState = GAME_INSTRUCTIONS;
	}
	
	/**
	 * Handle keyboard when finished a level
	 */
	public void finishLevelKeyboardHandler() {
		keyboard.poll();
		if (keyboard.isPressed(KeyEvent.VK_SPACE)) {
			gameState = GAME_PLAY;
			audiofx.playGameMusic();
			initializeLevel(++gameLevel);
		}
	}
	
	
	/**
	 * w00t
	 */
	public void update(long deltaMs) {
		switch(gameState) {
		default:
		case GAME_PLAY:
			froggerKeyboardHandler();
			wind.update(deltaMs);
			hwave.update(deltaMs);
			frog.update(deltaMs);
			audiofx.update(deltaMs);
			ui.update(deltaMs);

			cycleTraffic(deltaMs);
			frogCol.testCollision(movingObjectsLayer);
			
			// Wind gusts work only when Frogger is on the river
			if (frogCol.isInRiver())
				wind.start(gameLevel);		
			wind.perform(frog, gameLevel, deltaMs);
			
			// Do the heat wave only when Frogger is on hot pavement
			if (frogCol.isOnRoad())
				hwave.start(frog, gameLevel);
			hwave.perform(frog, gameLevel);
			
	
			if (!frog.isAlive())
				particleLayer.clear();
			
			goalmanager.update(deltaMs);
			
			if (goalmanager.getUnreached().isEmpty()) {
				gameState = GAME_FINISH_LEVEL;
				audiofx.playCompleteLevel();
				particleLayer.clear();
			}
			
			if (getGameLives() < 1) {
				gameState = GAME_OVER;
			}
			
			break;
		
		case GAME_OVER:		
		case GAME_INSTRUCTIONS:
		case GAME_INTRO:
			goalmanager.update(deltaMs);
			menuKeyboardHandler();
			cycleTraffic(deltaMs);
			break;
			
		case GAME_FINISH_LEVEL:
			finishLevelKeyboardHandler();
			break;		
		}
	}
	
	
	/**
	 * Rendering game objects
	 */
	public void render(RenderingContext rc) {
		switch(gameState) {
		default:
		case GAME_FINISH_LEVEL:
		case GAME_PLAY:
			backgroundLayer.render(rc);
			
			if (frog.isAlive()) {
				movingObjectsLayer.render(rc);
				frog.render(rc);		
			} else {
				frog.render(rc);
				movingObjectsLayer.render(rc);				
			}
			
			particleLayer.render(rc);
			ui.render(rc);
			break;
			
		case GAME_OVER:
		case GAME_INSTRUCTIONS:
		case GAME_INTRO:
			backgroundLayer.render(rc);
			movingObjectsLayer.render(rc);
			ui.render(rc);
			break;		
		}
	}
	
	public static void main (String[] args) {
		Main f = new Main();
		f.run();
	}


	public static int getGameLives() {
		return gameLives;
	}


	public static void setGameLives(int gameLives) {
		Main.gameLives = gameLives;
	}


	public static int getGameScore() {
		return gameScore;
	}


	public static void setGameScore(int gameScore) {
		Main.gameScore = gameScore;
	}


	public static int getLevelTimer() {
		return levelTimer;
	}


	public static void setLevelTimer(int levelTimer) {
		Main.levelTimer = levelTimer;
	}


	public static Vector2D getFroggerStart() {
		return FROGGER_START;
	}
}