/***********************************************************************
 * ShotEntity.java
 * Authors: Tom, Kunal, Todd
 * Date: April 2011
 * Purpose: Represents a shot fired by the player's ship.
***********************************************************************/

public class ShotEntity extends Entity {

	private boolean used = false; // true if shot hits something
	private double rotAngle; // angle at which the shot is moving
	private int type; // shot, giant shot, or non-moving shot trail
	private int trailTimer = 8000; // timer for "data trail" to stay on screen

	// construct the shot and set its movement
	public ShotEntity(Game g, int newX, int newY, double rotAngle, int type) {
		super(g, g.getShotImage(type), newX, newY);
		this.rotAngle = rotAngle;
		switch (type) {
			case 1: this.speed = 220; break;
			case 2: this.speed = 140; break;
			case 3: this.speed = 0; break;
			default: break;
		} // switch
		this.type = type;
		dx = (speed * Math.cos(rotAngle)); // permanent horizontal speed
		dy = (speed * Math.sin(rotAngle)); // permanent vertical speed
	} // constructor

	// returns the angle at which the shot is moving
	public double getAngle() {
		return rotAngle;
	} // getAngle

	// moves shot given time elapsed since last move
	public void move(long delta) {
		super.move(delta);

		// if shot moves off the edge of screen, remove it
		if (x < 0 || x > 600 || y < 0 || y > 400) {
			game.removeEntity(this);
		} // if
	} // move

	// run timer for "data trail" shots
	public void runTimer(long delta) {
		if (type == 3) trailTimer -= delta;
		if (trailTimer < 0) game.removeEntity(this);
	} // runTimer

	// kill enemy that the shot has collided with
	public void collidedWith(Entity other) {
		// prevents double kills
		if (used && type == 1) {
			return;
		} // if

		// kill the alien and remove the shot if needed
		AlienEntity entity = (AlienEntity) other;
		if (!entity.isDead()) {
			game.removeEntity(other);
			if (type == 1) game.removeEntity(this);
			game.notifyAlienKilled();
			used = true;
		} // if
	} // collidedWith

} // ShotEntity