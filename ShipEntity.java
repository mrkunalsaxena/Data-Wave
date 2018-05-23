/***********************************************************************
 * ShipEntity.java
 * Authors: Tom, Kunal, Todd
 * Date: April 2011
 * Purpose: Represents the player's ship in the game.
***********************************************************************/

public class ShipEntity extends Entity {

	private boolean shieldOn = false; // true when shield is on
	private boolean invincOn = false; // true when invincible
	private boolean fireOn = false; // true when firing is on
	private boolean dataWave = false; // true when aiming to fire data wave
	private int vortX; // x coordinate of vortex
	private int vortY; // y coordinate of vortex
	private int exploX; // x coordinate of explosion
	private int exploY; // y coordinate of explosion
	private long powerupTimer = 0; // timer for invincibility and firing powerups
	private long vortexTimer = 0; // timer to keep vortex on screen
	private long explosionTimer = 0; // timer to display explosion image
	private long freezeTimer = 0; // timer to display freeze image
	private int trailTimer = 0; // timer to aim for data trail
	private int firingTimer = 0; // timer before data wave fires
	private double rotAngle = 0; // angle ship is facing and moving

	// construct the player's ship
	public ShipEntity(Game g, int newX, int newY) {
		super(g, "images/sprites/ship.png", newX, newY);
		this.speed = 120;
		this.accel = .02;
	} // constructor

	// returns the angle that the ship is facing
	public double getAngle() {
		return this.rotAngle;
	} // getAngle

	// moves the ship given time elapsed since last move
	public void move(long delta) {
		// stop at edge of scren
		if ((dx < 0 && x < 25) || (dx > 0 && x > 575)) dx = 0;
		if ((dy > 0 && y > 360) || (dy < 0 && y < 40)) dy = 0;

		// proceed with normal move
		super.move(delta);
	} // move

	// set ships movement and angle
	public void setMovement(int newX, int newY) {
		super.setMovement(newX, newY);

		if (newX != x || newY != y) {
			this.rotAngle = Math.atan2( (newY - y) , (newX - x) );
		} // if
	} // setMovement

	// runs powerup timers and adjusts sprites
	public void runPowerupTimer(long delta) {
		powerupTimer -= delta;
		vortexTimer -= delta;
		freezeTimer -= delta;
		explosionTimer -= delta;
		trailTimer -= delta;
		firingTimer -= delta;
		if (trailTimer <= 0) accel = .02;

		setSprite();

		if (!dataWave && trailTimer <= 0) { // blink and reset powerups
			if ((powerupTimer <= 2000 && powerupTimer > 1700) ||
				(powerupTimer <= 1400 && powerupTimer > 1100) ||
				(powerupTimer <= 800 && powerupTimer > 500)) {
				invincOn = true; // prevent accidental cancellation because of data wave
				if (shieldOn) sprite = (SpriteStore.get()).getSprite("images/sprites/ship_shield.png");
				else sprite = (SpriteStore.get()).getSprite("images/sprites/ship.png");
			} else if (powerupTimer <= 0) {
				invincOn = false;
				fireOn = false;
				if (shieldOn) sprite = (SpriteStore.get()).getSprite("images/sprites/ship_shield.png");
				else sprite = (SpriteStore.get()).getSprite("images/sprites/ship.png");
			} // if
		} // if
	} // runPowerupTimer

	// returns true when explosion image should be displayed
	public boolean explosionOn() {
		return (explosionTimer > 0 && explosionTimer % 2 == 0);
	} // explosionOn

	// returns true when freeze image should be displayed
	public boolean freezeOn() {
		return (freezeTimer > 0 && freezeTimer % 2 == 0);
	} // freezeOn

	// x and y coordinates of explosion center
	public int exploX() {
		return exploX;
	} // exploX

	public int exploY() {
		return exploY;
	} // exploY

	// returns true when the vortex is on
	public boolean vortexOn() {
		return (vortexTimer > 0);
	} // vortexOn

	// x and y coordinates of vortex center
	public int vortX() {
		return vortX;
	} // vortX

	public int vortY() {
		return vortY;
	} // vortY

	// returns true if the ship has firing powerup
	public boolean hasFire() {
		return fireOn;
	} // hasPowerup

	// timer to aim before data trail
	public boolean trailStop() {
		return (trailTimer > 400);
	} // trailOn

	// timer to leave behind a data trail
	public boolean trailOn() {
		return (trailTimer > 0 && trailTimer < 400);
	} // trailOn

	// turns off the data wave powerup
	public void setDataWave() {
		dataWave = false;
		invincOn = false;
	} // hasDataWave

	// returns true if a data wave can be fired
	public boolean hasDataWave() {
		return (firingTimer <= 0 && dataWave);
	} // hasDataWave

	// calls alienLogic or powerupLogic to handle collisions
	public void collidedWith(Entity other) {
		if (other instanceof AlienEntity) {
			alienLogic( (AlienEntity) other );
		} else if (other instanceof PowerupEntity) {
			powerupLogic( (PowerupEntity) other );
		} // else if
	} // collidedWith

	// handles collisions with enemy aliens
	private void alienLogic(AlienEntity other) {
		if (!shieldOn && !invincOn && !other.isFrozen()) {
			game.notifyDeath();
		} else {
			game.removeEntity(other);
			game.notifyAlienKilled();
			if (shieldOn && !invincOn && !other.isFrozen()) {
				shieldOn = false;
				game.explosion(1, 50);
			} // if
		} // else
	} // alienLogic

	// handles collisions with powerups
	private void powerupLogic(PowerupEntity other) {
		game.removeEntity(other);

		if (other.getType() == 1) { // shield
			shieldOn = true;
		} else if (other.getType() == 2) { // invincibility
			invincOn = true;
			fireOn = false;
			powerupTimer = 6000;
		} else if (other.getType() == 3) { // firing turret
			fireOn = true;
			invincOn = true;
			powerupTimer = 6000;
			game.explosion(1, 50);
		} else if (other.getType() == 4) { // shot explosion
			game.circleShot();
		} else if (other.getType() == 5) { // freezing
			game.explosion(0, 160);
			freezeTimer = 800;
			explosionTimer = 0;
			exploX = (int)x;
			exploY = (int)y;
		} else if (other.getType() == 6) { // explosion
			game.explosion(1, 160);
			explosionTimer = 800;
			freezeTimer = 0;
			exploX = (int) x;
			exploY = (int) y;
		} else if (other.getType() == 7) { // vortex
		    vortX = 50 + (int)(Math.random() * 500);
		    vortY = 50 + (int)(Math.random() * 300);
		    vortexTimer = 6000;
		} else if (other.getType() == 8) { // data wave
			dataWave = true;
			invincOn = true;
			firingTimer = 600;
		} else if (other.getType() == 9) { // data trail
			invincOn = true;
			accel = .05;
			trailTimer = 1000;
		} // else if
	} // powerupLogic

	// turns off all powerups
	public void resetPowerups() {
		shieldOn = false;
		invincOn = false;
		fireOn = false;
		dataWave = false;
		powerupTimer = 0;
		vortexTimer = 0;
	} // resetPowerups

	// sets the ship's sprite based on the current powerup, in order of preference
	private void setSprite() {
		if (trailTimer > 0) {
			sprite = (SpriteStore.get()).getSprite("images/sprites/ship_trail.png");
		} else if (dataWave) {
			sprite = (SpriteStore.get()).getSprite("images/sprites/ship_wave.png");
		} else if (fireOn) {
			sprite = (SpriteStore.get()).getSprite("images/sprites/ship_shot.png");
		} else if (invincOn) {
			sprite = (SpriteStore.get()).getSprite("images/sprites/ship_spike.png");
		} else if (shieldOn) {
			sprite = (SpriteStore.get()).getSprite("images/sprites/ship_shield.png");
		} // else if
	} // setSprite

} // ShipEntity