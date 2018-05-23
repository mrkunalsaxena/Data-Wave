/***********************************************************************
 * Entity.java
 * Authors: Tom, Kunal, Todd
 * Date: April 2011
 * Purpose: An entity is any object that appears in the game.
 * It is responsible for resolving collisions and movement.
***********************************************************************/

import java.awt.*;
import java.awt.Graphics;

public abstract class Entity {

	protected Game game; // the game in which the entity exists
	protected Sprite sprite; // this entity's sprite
	protected double x; // current x location
	protected double y; // current y location
	protected double dx; // horizontal speed (px/s)  + -> right
	protected double dy; // vertical speed (px/s) + -> down
	protected double speed; // constant speed (px/s) in any direction
	protected double accel; // acceleration (if required)

	private Rectangle me = new Rectangle(); // bounding rectangle of this entity
	private Rectangle him = new Rectangle(); // bounding rect. of other entities

	// constructs an entity and its sprite in the game
	public Entity(Game g, String r, int newX, int newY) {
		x = newX;
		y = newY;
		game = g;
		sprite = (SpriteStore.get()).getSprite(r);
	} // constructor

	// moves entity given time elapsed since last move
	public void move(long delta) {
		x += (delta * dx) / 1000;
		y += (delta * dy) / 1000;
	} // move

	// set entities movement to a new location
	public void setMovement(int newX, int newY) {
		double accel = this.accel;
		double hypot = Math.sqrt(Math.pow( (newY - y) , 2) + Math.pow( (newX - x) , 2));
		if (accel * hypot < 1 || accel == 1) accel = (1 / hypot); // minimum speed

		if (Math.abs(newX - x) < 2 && Math.abs(newY - y) < 2) { // already at new locat.
			x = newX;
			y = newY;
			dx = 0;
			dy = 0;
		} else if (newX != x || newY != y) { // not yet at new location
			double angle = Math.atan2( (newY - y) , (newX - x) );
			dx = (speed * Math.cos(angle)) * hypot * accel;
			dy = (speed * Math.sin(angle)) * hypot * accel;
		} // else
	} // setMovement

	// get position
	public int getX() {
		return (int) x;
	} // getX

	public int getY() {
		return (int) y;
	} // getY

	public void setSpeed(double speed) {
		this.speed = speed;
	} // setSpeed

	public void setAccel(double accel) {
		this.accel = accel;
	} // setAccel

	// draw this entity to the graphics object provided at (x, y)
	public void draw(Graphics g) {
		sprite.draw(g, (int)(x - Math.ceil(sprite.getWidth()/2)), (int)(y - Math.ceil(sprite.getHeight()/2)));
	} // draw

	// checks if this entity has collided with another
	public boolean collidesWith(Entity other) {
		me.setBounds((int)x, (int)y, sprite.getWidth(), sprite.getHeight());
		him.setBounds(other.getX(), other.getY(), other.sprite.getWidth(), other.sprite.getHeight());
		return me.intersects(him);
	} // collidesWith

	// checks if this entity is within an explosion radius
	public boolean collidesWithExplosion(Entity other, int radius) {
		me.setBounds((int)x, (int)y, sprite.getWidth(), sprite.getHeight());
		him.setBounds(other.getX() - radius, other.getY() - radius, 2 * radius, 2 * radius);
		return him.contains(me) || me.intersects(him);
	} // collidesWithExplosion

	// notification that this entity collided with another
	public abstract void collidedWith(Entity other);

} // Entity