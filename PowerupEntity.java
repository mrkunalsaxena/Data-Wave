/***********************************************************************
 * PowerupEntity.java
 * Authors: Tom, Kunal, Todd
 * Date: April 2011
 * Purpose: Represents a floating powerup in the game.
***********************************************************************/

public class PowerupEntity extends Entity {

	private int type; // type of powerup
	private double rotAngle; // angle at which the powerup should move
	private double newX; // target x coordinate
	private double newY; // target y coordinate

	// constructs a powerup of the given type
	public PowerupEntity(Game g, int newX, int newY, int type) {
		super(g, g.getPowerupImage(type), newX, newY);
		this.type = type;
		this.speed = 16;
		this.newX = x;
		this.newY = y;
	} // constructor

	// randomly moves the powerup given time elapsed since last move
	public void move(long delta) {
		if (Math.abs(x - newX) < 2 || Math.abs(y - newY) < 2) {
			rotAngle = -Math.PI + (Math.random() * 2 * Math.PI);
			dx = (speed * Math.cos(rotAngle));
			dy = (speed * Math.sin(rotAngle));
			newX = x + Math.cos(rotAngle) * speed * 8;
			newY = y + Math.sin(rotAngle) * speed * 8;
		} // if

		// stop at left or right side of screen
		if ( (dx < 0 && x < 15) || (dx > 0 && x > 585) ) {
			newX = x;
			return;
		} // if
		// stop at top or bottom of screen
		if ( (dy > 0 && y > 370) || (dy < 0 && y < 30) ) {
			newY = y;
			return;
		} // if

		// proceed with normal move
		super.move(delta);
	} // move

	// returns the type of powerup
	public int getType() {
		return type;
	} // getType

	// collisions with ship	are handled in ShipEntity
	public void collidedWith(Entity other) {} // collidedWith

} // PowerupEntity