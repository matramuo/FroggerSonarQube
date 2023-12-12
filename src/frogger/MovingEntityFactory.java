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

import java.util.Random;

public class MovingEntityFactory {
	
	private static int  car   = 0;
	private static int  truck = 1;
	private static int  slog  = 2;
	private static int  llog  = 3;
	
	private Vector2D position;
	private Vector2D velocity;
	
	private Random r;
	
	private long updateMs = 0;
	private long copCarDelay = 0;
	
	private long rateMs = 1000;

	private int padding = 64; // distance between 2 objects in a traffic/river line
	
	private int[] creationRate = new int[4];	
	
	/**
	 * Moving Entity factory
	 * 
	 * @param pos
	 * @param v
	 * @param rate
	 */
	public MovingEntityFactory(Vector2D pos, Vector2D v) {
		setPosition(pos);
		setVelocity(v);
		setR(new Random(System.currentTimeMillis()));

		creationRate[getCar()]   = (int) Math.round(((Car.LENGTH) + padding + 32) / 
				Math.abs(getVelocity().getX()));
		creationRate[getTruck()] = (int) Math.round(((Truck.LENGTH) + padding + 32) / 
				Math.abs(getVelocity().getX()));
		creationRate[getSlog()]  = (int) Math.round(((ShortLog.LENGTH) + padding - 32) / 
				Math.abs(getVelocity().getX()));
		creationRate[getLlog()]  = (int) Math.round(((LongLog.LENGTH) + padding - 32) / 
				Math.abs(getVelocity().getX()));
	}
	
	/**
	 * Building basic moving object {car, truck, short log, long log}
	 * @param type - {CAR, TRUCK, SLOG, LLOG}
	 * @param chance - of production (n out of 100)
	 * @return MovingEntity on chance of success, otherwise return null
	 * chance gives some holes in the production pattern, looks better.
	 */
	public MovingEntity buildBasicObject(int type, int chance) {
		if (updateMs > rateMs) {
			updateMs = 0;
			
			if (getR().nextInt(100) < chance)			
				switch(type) {
					case 0: // CAR
						rateMs = creationRate[getCar()];
						return new Car(getPosition(), getVelocity(), getR().nextInt(Car.TYPES));
					case 1: // TRUCK
						rateMs = creationRate[getTruck()];
						return new Truck(getPosition(), getVelocity());
					case 2: // SLOG
						rateMs = creationRate[getSlog()];
						return new ShortLog(getPosition(), getVelocity());
					case 3: // LLOG
						rateMs = creationRate[getLlog()];
						return new LongLog(getPosition(), getVelocity());
					default:
						return null;
				}
		}
		
		return null;
	}
	
	public MovingEntity buildShortLogWithTurtles(int chance) {
		MovingEntity m = buildBasicObject(getSlog(),80);
		if (m != null && getR().nextInt(100) < chance)
			return new Turtles(getPosition(), getVelocity(), getR().nextInt(2));
		return m;
	}
	
	/**
	 * Long Tree Logs with a some chance of Crocodile!
	 * @return
	 */
	public MovingEntity buildLongLogWithCrocodile(int chance) {
		MovingEntity m = buildBasicObject(getLlog(),80);
		if (m != null && getR().nextInt(100) < chance)
			return new Crocodile(getPosition(), getVelocity());
		return m;
	}

	/**
	 * Cars appear more often than trucks
	 * If traffic line is clear, send a faaast CopCar!
	 * @return
	 */
	public MovingEntity buildVehicle() {
		
		// Build slightly more cars that trucks
		MovingEntity m = getR().nextInt(100) < 80 ? buildBasicObject(getCar(),50) : buildBasicObject(getTruck(),50);

		if (m != null) {
			
			/* If the road line is clear, that is there are no cars or truck on it
			 * then send in a high speed cop car
			 */
			if (Math.abs(getVelocity().getX()*copCarDelay) > Main.WORLD_WIDTH) {
				copCarDelay = 0;
				return new CopCar(getPosition(), getVelocity().scale(5));
			}
			copCarDelay = 0;
		}
		return m;
	}
	
	public void update(final long deltaMs) {
		updateMs += deltaMs;
		copCarDelay += deltaMs;
	}

	public static int getCar() {
		return car;
	}

	public static void setCar(int car) {
		MovingEntityFactory.car = car;
	}

	public static int getTruck() {
		return truck;
	}

	public static void setTruck(int truck) {
		MovingEntityFactory.truck = truck;
	}

	public static int getSlog() {
		return slog;
	}

	public static void setSlog(int slog) {
		MovingEntityFactory.slog = slog;
	}

	public static int getLlog() {
		return llog;
	}

	public static void setLlog(int llog) {
		MovingEntityFactory.llog = llog;
	}

	public Vector2D getPosition() {
		return position;
	}

	public void setPosition(Vector2D position) {
		this.position = position;
	}

	public Vector2D getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector2D velocity) {
		this.velocity = velocity;
	}

	public Random getR() {
		return r;
	}

	public void setR(Random r) {
		this.r = r;
	}
}