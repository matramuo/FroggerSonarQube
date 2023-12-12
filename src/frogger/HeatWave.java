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

import java.util.Random;

import jig.engine.util.Vector2D;

public class HeatWave {
	static final int PERIOD    = 2000;  //milliseconds
	static final int DURATION  = 1000;  //milliseconds
	
	Random r;
	
	private long timeMs;
	private long durationMs;
	private long heatWaveMs;
	
	private boolean isHot;
	
	public HeatWave() {
		isHot = false;
		timeMs = 0;
		heatWaveMs = 0;
		r = new Random(System.currentTimeMillis());
	}
	
	/**
	 * This is checked with every game update
	 * @param f - reference Frogger's hw_hasMoved
	 */
	public void perform (Frogger f, final int level) {	
		if (!f.isAlive()) {
			isHot = false;
			return;
		}
		
		if (isHot && durationMs > (DURATION - (level*10)) && !f.isHwHasMoved()) {
			f.randomJump(r.nextInt(4));
			isHot = false;
		}
		
		if (f.isHwHasMoved())
			isHot = false;
	}
	
	/**
	 * Initiate the Heat Wave effect
	 * 
	 * @param f
	 * @param temp - based on the GameTemp, this effects occurs more often
	 */
	public void start (Frogger f, final int GameLevel) {
		
		if (!isHot && timeMs > PERIOD) {		
			if (r.nextInt(100) < GameLevel*10) {
				durationMs = 1;
				isHot = true;
				f.setHwHasMoved(false);
				AudioEfx.heat.play(0.2);
			}		
			timeMs = 0;
		}
	}
	
	/**
	 * Generating particles
	 * 
	 * @param f
	 * @return
	 */
	public MovingEntity genParticles(Vector2D pos) {
		if (!isHot)
			return null;
		
		if (r.nextInt(100) > 10)
			return null;
		
		// Generate particles from center of the Frogger to all directions around
		Vector2D v = new Vector2D((r.nextDouble()-0.5)*0.1,(r.nextDouble()-0.5)*0.1);
		
		return new Particle(Main.SPRITE_SHEET + "#smoke_cloud", pos,v,1000);
	}
	
	/**
	 * Increase GameTemp by 1 every 2 seconds (2000 milliseconds)
	 * @param deltaMs
	 * @return
	 */
	public int computeHeatValue(final long deltaMs, final int level, int curTemp) {
		heatWaveMs += deltaMs;
		
		if (heatWaveMs > 2000) {
			heatWaveMs = 0;
			curTemp += level;
		}
		
	    if (curTemp > 170) curTemp = 170;
	 
		return curTemp;
	}
	
	public void update(final long deltaMs) {
		timeMs += deltaMs;
		durationMs += deltaMs;
	}
}
