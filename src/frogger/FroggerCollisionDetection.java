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
import java.util.List;

import jig.engine.physics.AbstractBodyLayer;
import jig.engine.util.Vector2D;

public class FroggerCollisionDetection  {

	private Frogger frog;
	private CollisionObject frogSphere;
	
	// River and Road bounds, all we care about is Y axis in this game
    private int riverY0 = 1*32;
    private int riverY1 = getRiverY0() + 6* 32;
    private int roadY0 = 8*32;
    private int roadY1 = getRoadY0() + 5*32;
	
	public FroggerCollisionDetection (Frogger f) {
		setFrog(f);
		setFrogSphere(getFrog().getCollisionObjects().get(0));
	}
	
	public void testCollision(AbstractBodyLayer<MovingEntity> l) {

		if (!getFrog().isAlive())
			return;
		
		Vector2D frogPos = getFrogSphere().getCenterPosition();
		double dist2;
		
		if (isOutOfBounds()) {
			getFrog().die();
			return;
		}
		
		for (MovingEntity i : l) {
			if (!i.isActive())
				continue;
			
			List<CollisionObject> collisionObjects = i.getCollisionObjects();

			for (CollisionObject objectSphere : collisionObjects) {
				dist2 = (getFrogSphere().getRadius() + objectSphere.getRadius()) 
				      * (getFrogSphere().getRadius() + objectSphere.getRadius());

				if (frogPos.distance2(objectSphere.getCenterPosition()) < dist2) {
					collide(i, objectSphere);
					return;
				}
			}
		}
		
		if (isInRiver()) {
			getFrog().die();
		}
		
	}
	
	/**
	 * Check game area bounds
	 * @return
	 */
	public boolean isOutOfBounds() {
	    Vector2D frogPos = getFrogSphere().getCenterPosition();
	    return (frogPos.getY() < 32 || frogPos.getY() > Main.WORLD_HEIGHT) ||
	           (frogPos.getX() < 0 || frogPos.getX() > Main.WORLD_WIDTH);
	}
	
	/**
	 * Bound check if the frog is in river
	 * @return
	 */
	public boolean isInRiver() {
		Vector2D frogPos = getFrogSphere().getCenterPosition();
		
		return frogPos.getY() > getRiverY0() && frogPos.getY() < getRiverY1();
	}
	
	/**
	 * Bound check if the frog is on the road
	 * @return
	 */
	public boolean isOnRoad() {
		Vector2D frogPos = getFrogSphere().getCenterPosition();
		
		return frogPos.getY() > getRoadY0() && frogPos.getY() < getRoadY1();

	}
	
	public void collide(MovingEntity m, CollisionObject s) {

		if (m instanceof Truck  || m instanceof CopCar) {
			getFrog().die();
		}
		
		if (m instanceof Crocodile) {
			if (s == ((Crocodile) m).head)
				getFrog().die();
			else
				getFrog().follow(m);
		}
		
		/* Follow the log */
		if (m instanceof LongLog || m instanceof ShortLog) {
			getFrog().follow(m);
		}
		
		if (m instanceof Turtles) {
			
			getFrog().follow(m);
		}
		
		/* Reach a goal */
		if (m instanceof Goal) {
			getFrog().reach((Goal)(m));
		}
	}

	public Frogger getFrog() {
		return frog;
	}

	public void setFrog(Frogger frog) {
		this.frog = frog;
	}

	public CollisionObject getFrogSphere() {
		return frogSphere;
	}

	public void setFrogSphere(CollisionObject frogSphere) {
		this.frogSphere = frogSphere;
	}

	public int getRiverY0() {
		return riverY0;
	}

	public void setRiverY0(int riverY0) {
		this.riverY0 = riverY0;
	}

	public int getRiverY1() {
		return riverY1;
	}

	public void setRiverY1(int riverY1) {
		this.riverY1 = riverY1;
	}

	public int getRoadY0() {
		return roadY0;
	}

	public void setRoadY0(int roadY0) {
		this.roadY0 = roadY0;
	}

	public int getRoadY1() {
		return roadY1;
	}

	public void setRoadY1(int roadY1) {
		this.roadY1 = roadY1;
	}
}
	