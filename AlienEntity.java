/***********************************************************************
 * AlienEntity.java
 * Authors: Tom, Kunal, Todd
 * Date: April 2011
 * Purpose: Represents an enemy alien
***********************************************************************/

public class AlienEntity extends Entity {

	private long dead = 300; // timer to display "dying" animation
	private boolean frozen = false; // is the alien frozen
	private long frozenTimer = 5000; // timer for frozen aliens
	private int type;

	// construct a new alien (type: 0, follow ship, 1/2 straight on x, 3/4 straight on y)
	public AlienEntity(Game g, int newX, int newY, int type) {
		super(g, "images/sprites/enemy.png", newX, newY);  // calls the constructor in Entity
		this.type = type;
		this.speed = 35;
		this.accel = .008;
	} // constructor

	// moves enemy given time elapsed since last move
	public void move(long delta) {
		// stop at edge of scren
		if ((dx < 0 && x < 15) || (dx > 0 && x > 585) ||
			(dy > 0 && y > 370) || (dy < 0 && y < 30)) {
			return;
		} // if

		// proceed with normal move if not frozen
		if (!frozen) super.move(delta);
		else frozenTimer(delta);
	} // move
	
	// set entities movement to a new location
	public void setMovement(int newX, int newY, boolean follow) {
		if (type == 0 || follow == true) {
			super.setMovement(newX, newY);
			return;
		} else if (type == 1) {
			dx = speed;
			dy = 0;
		} else if (type == 2) {
			dx = -speed;
			dy = 0;
		} else if (type == 3) {
			dx = 0;
			dy = speed;
		} else if (type == 4) {
			dx = 0;
			dy = -speed;
		} // else if
	} // setMovement

	// returns true for frozen aliens
	public boolean isFrozen() {
		return frozen;
	} // isFrozen

	// make an alien frozen
	public void setFrozen() {
		frozen = true;
		frozenTimer = 4000;
		sprite = (SpriteStore.get()).getSprite("images/sprites/enemy_frozen.png");
	} // setFrozen

	// run the timer to thaw alien and animate sprites
	public void frozenTimer(long delta) {
		frozenTimer -= delta;

		if (frozenTimer <= 1800 && frozenTimer > 1300) {
			sprite = (SpriteStore.get()).getSprite("images/sprites/enemy_frozen2.png");
		} else if (frozenTimer <= 1300 && frozenTimer > 800) {
			sprite = (SpriteStore.get()).getSprite("images/sprites/enemy_frozen3.png");
		} else if (frozenTimer <= 800 && frozenTimer > 0) {
			sprite = (SpriteStore.get()).getSprite("images/sprites/enemy_frozen4.png");
		} else if (frozenTimer <= 0) {
			frozen = false;
			sprite = (SpriteStore.get()).getSprite("images/sprites/enemy.png");
		} // if
	} // frozenTimer

	// dying animation
	public boolean isDead() {
		return dead <= 0;
	} // maxDead

	public void makeDead(long delta) {
		dead -= delta;
	} // makeDead

	// collisions with aliens are handled in ShotEntity and ShipEntity
	public void collidedWith(Entity other) {} // collidedWith

} // AlienEntity
