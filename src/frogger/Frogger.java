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

import jig.engine.util.Vector2D;

/**
 * Main sprite in the game that a player can control
 * 
 * @author vitaliy
 *
 */
public class Frogger extends MovingEntity {
	
	public static final int MOVE_STEP = 32;
	
	// Animation related variables 
	private static final int ANIMATION_STEP = 4; // 32/4 = 8, 8 animation frames, 10 ms each
	
	private int curAnimationFrame = 0;
	private int finalAnimationFrame = 0;
	private long animationDelay = 10; // milliseconds
	private long animationBeginTime = 0;
	private boolean isAnimating = false;
	private Vector2D dirAnimation = new Vector2D(0,0);
	
	// Object to follow, such as Tree Log in the river
	private MovingEntity followObject = null;
	
	
	private boolean isAlive = false;
    private long timeOfDeath = 0;
    
    // Current sprite frame displayed
    private int currentFrame = 0;
    private int tmpFrame = 0;
    
    private int deltaTime = 0;
    
    private boolean cheating = false;
    
    private boolean hwHasMoved = false;
    
    
    /**
     * Build frogger!
     */
	public Frogger (Main g) {
		super(Main.SPRITE_SHEET + "#frog");
		resetFrog();
		collisionObjects.add(new CollisionObject(position));
	}
	
	/**
	 * Reset the Frogger to default state and position
	 */
	public void resetFrog() {
		setAlive(true);
		setAnimating(false);
		currentFrame = 0;
		followObject = null;
		position = Main.getFroggerStart();
		Main.setLevelTimer(Main.DEFAULT_LEVEL_TIME);
	}
	
	/**
	 * Moving methods, called from Main upon key strokes
	 */
	public void moveLeft() {
		if (getCenterPosition().getX()-16 > 0 && isAlive() && !isAnimating()) {
			currentFrame = 3;
		    move(new Vector2D(-1,0));
		    AudioEfx.frogJump.play(0.2);
		}
	}
	
	public void moveRight() {
		
		if (getCenterPosition().getX()+32 < Main.WORLD_WIDTH && isAlive() && !isAnimating()) {
			currentFrame = 2;
		    move(new Vector2D(1,0));
		    AudioEfx.frogJump.play(0.2);
		}
	}
	
	public void moveUp() {
		if (position.getY() > 32  && isAlive() && !isAnimating()) {
			currentFrame = 0;
		    move(new Vector2D(0,-2));
		    AudioEfx.frogJump.play(0.2);
		}
	}
	
	public void moveDown() {
		if (position.getY() < Main.WORLD_HEIGHT - MOVE_STEP && isAlive() && !isAnimating()) {
			currentFrame = 1;
		    move(new Vector2D(0,1));
		    AudioEfx.frogJump.play(0.2);
		}
	}
	
	/**
	 * Short-cut for systems current time
	 * @return
	 */
	public long getTime() {
		return System.currentTimeMillis();
	}
	
	/**
	 * Initiate animation sequence into specified direction, given by
	 * 
	 * @param dir - specifies direction to move
	 * 
	 * The collision sphere of Frogger is automatically moved to the final
	 * position. The animation then lags behind by a few seconds(or frames). 
	 * This resolves the positioning bugs when objects collide during the animation.
	 */
	public void move(Vector2D dir) {
		followObject = null;
		curAnimationFrame = 0;
		finalAnimationFrame = MOVE_STEP/ANIMATION_STEP;
		setAnimating(true);
		setHwHasMoved(true);
		animationBeginTime = getTime();
		dirAnimation = dir;

		tmpFrame = currentFrame;
		currentFrame += 5;
		
		// Move CollisionSphere to an already animated location
		sync(new Vector2D(
				position.getX()+dirAnimation.getX()*MOVE_STEP, 
				position.getY()+dirAnimation.getY()*MOVE_STEP)
		);
	}
	
	/**
	 * Cycle through the animation frames
	 */
	public void updateAnimation() {
		// If not animating, sync position of the sprite with its collision sphere
		if (!isAnimating() || !isAlive()) {
			sync(position);
			return;
		}
		
		// Finish animating
		if (curAnimationFrame >= finalAnimationFrame) {
			setAnimating(false);
			currentFrame = tmpFrame;
			return;
		}
		
		// Cycle animation
		if (animationBeginTime + animationDelay < getTime()) {
			animationBeginTime = getTime();
			position = new Vector2D(
					position.getX() + dirAnimation.getX()*ANIMATION_STEP,
					position.getY() + dirAnimation.getY()*ANIMATION_STEP
					);
			curAnimationFrame++;
		}
	}
	
	/**
	 * Re-align frog to a grid
	 */
	public void allignXPositionToGrid() {
		if (isAnimating() || followObject != null) 
			return;
		double x = position.getX();
		x = Math.round(x/32)*(double)32;
		position = new Vector2D(x, position.getY());
		
	}
	
	/**
	 * Following a Tree Log on a river by getting it's velocity vector
	 * @param deltaMs
	 */
	public void updateFollow(long deltaMs) {
		if (followObject == null || !isAlive()) 
			return;
		Vector2D dS = followObject.getVelocity().scale(deltaMs);
		position = new Vector2D(position.getX()+dS.getX(), position.getY()+dS.getY());
	}
	
	/**
	 * Setting a moving entity to follow
	 * @param log
	 */
	public void follow(MovingEntity log) {
		followObject = log;
	}
	
	
	/**
	 * Effect of a wind gust on Frogger
	 * @param d
	 */
	public void windReposition(Vector2D d) {
		if (isAlive()) {
			setHwHasMoved(true);
			setPosition(new Vector2D(getPosition().getX()+d.getX(), getPosition().getY()));
			sync(position);
		}
	}
	
	/**
	 * Effect of Heat Wave on Frogger
	 * @param randDuration
	 */
	public void randomJump(final int rDir) {
		switch(rDir) {
		case 0:
			moveLeft();
			break;
		case 1:
			moveRight();
			break;
		case 2:
			moveUp();
			break;
		default:
			moveDown();
		}
	}
	
    /**
     * Frogger dies
     */
	public void die() {
		if (isAnimating())
			return;
		
		if (!isCheating()) {
		    AudioEfx.frogDie.play(0.2);
		    followObject = null;
		    setAlive(false);
		    currentFrame = 4;	// dead sprite   
		    Main.setGameLives(Main.getGameLives() - 1);
		    setHwHasMoved(true);
		}
		
		timeOfDeath = getTime();
		Main.setLevelTimer(Main.DEFAULT_LEVEL_TIME);
	}
	
	/**
	 * Frogger reaches a goal
	 */
	public void reach(final Goal g) {
		if (!g.isReached) {
			AudioEfx.frogGoal.play(0.4);
			Main.setGameScore(Main.getGameScore() + 100);
			Main.setGameScore(Main.getGameScore() + Main.getLevelTimer());
			if (g.isBonus) {
				AudioEfx.bonus.play(0.2);
				Main.setGameLives(Main.getGameLives() + 1);
			}
			g.reached();
			resetFrog();
		}
		else {
			setPosition(g.getPosition());
		}
	}
	
	@Override
	public void update(final long deltaMs) {
		if (Main.getGameLives() <= 0)
			return;
		
		// if dead, stay dead for 2 seconds.
		if (!isAlive() && timeOfDeath + 2000 < System.currentTimeMillis())
				resetFrog();
		
		updateAnimation();	
		updateFollow(deltaMs);
		setFrame(currentFrame);
		
		// Level timer stuff
		setDeltaTime(getDeltaTime() + (int)deltaMs);
		if (getDeltaTime() > 1000) {
			setDeltaTime(0);
			Main.setLevelTimer(Main.getLevelTimer() - 1);
		}
		
		if (Main.getLevelTimer() <= 0)
			die();
	}

	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	public int getDeltaTime() {
		return deltaTime;
	}

	public void setDeltaTime(int deltaTime) {
		this.deltaTime = deltaTime;
	}

	public boolean isCheating() {
		return cheating;
	}

	public void setCheating(boolean cheating) {
		this.cheating = cheating;
	}

	public boolean isHwHasMoved() {
		return hwHasMoved;
	}

	public void setHwHasMoved(boolean hwHasMoved) {
		this.hwHasMoved = hwHasMoved;
	}

	public boolean isAnimating() {
		return isAnimating;
	}

	public void setAnimating(boolean isAnimating) {
		this.isAnimating = isAnimating;
	}
}