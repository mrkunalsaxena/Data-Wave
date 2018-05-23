/***********************************************************************
 * Game.java
 * Authors: Tom, Kunal, Todd
 * Date: April 2011
 * Purpose: Main game program for Data Wave
***********************************************************************/

import javax.swing.*;
import java.text.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import sun.audio.*;
import javax.sound.sampled.*;

public class Game extends Canvas {

	// images
	private BufferStrategy strategy; // take advantage of entity accelerated graphics
	private Toolkit tk = Toolkit.getDefaultToolkit();
	private Image loading = tk.getImage(getClass().getResource("images/backdrops/loading.gif"));
        private Image background; // game background
	private Image menuBackground; // menu background
	private Image helpBackground; // help background
	private Image playButton; // play buttons
	private Image playButton2; // play button hover
	private Image helpButton; // help button
	private Image helpButton2; // help button hover
	private Image quitButton; // quit button
	private Image quitButton2; // quit button hover
	private Image die; // enemies dying
	private Image vortex; // vortex
	private Image explosion; // explosion
	private Image freeze; // freeze explosion
	private Image soundIcon; // icon for toggling sound
	private Vector<ImageIcon> images = new Vector<ImageIcon>(14);

	private ArrayList alienEntities = new ArrayList(); // list of enemy entities in game
	private ArrayList powerupEntities = new ArrayList(); // list of powerup entities in game
	private ArrayList shotEntities = new ArrayList(); // list of shot entities in game
	private ArrayList removeEntities = new ArrayList(); // list of entities to remove this loop
	private ShipEntity ship; // the ship entity

	private int score; // current score
	private int combo; // current combo
	private int killed; // aliens killed
	private long comboTimer; // time left on combo timer
	private long spawnTimer; // time left on spawn timer
	private long totalTime = 0; // total time elapsed this game
	private int level = 1; // current game level
	private long lastFire = 0; // time last shot fired
	private long firingInterval = 80; // interval between shots (ms)
	private int[] spawnAmount = {4,4,4,4,6,6,7,8,9,10,10,10,12,12,12,14,14,14,16,16};

	private int gameStage = 2;
	private final int PLAY = 1;
	private final int MENU = 2;
	private final int HELP = 3;
	private boolean gamePaused = false; // true if game is currently paused
	private boolean waitingForKeyPress = false; // true if waiting for key press to go to menu

	private int cursorX; // x position of cursor
	private int cursorY; // y position of cursor
	private boolean mousePressed = false; // true if mouse button was pressed

	private Clip musicClip; // game music
	private boolean music = true; // controls all game music

	// construct and start the game
	public Game() {
		// create a frame to contain game
		JFrame container = new JFrame("Data Wave");

		// get hold the content of the frame
		JPanel panel = (JPanel) container.getContentPane();

		// set up the resolution of the game
		panel.setPreferredSize(new Dimension(600,400));
		panel.setLayout(null);

		// set up canvas size (this) and add to frame
		setBounds(0,0,600,400);
		panel.add(this);

		// make the window visible
		container.pack();
		container.setResizable(false);
		container.setLocationRelativeTo(null);
		container.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		container.setVisible(true);

		// Tell AWT not to bother repainting canvas since that will
		// be done using graphics acceleration
		setIgnoreRepaint(true);

		// if user closes window, shutdown game and jre
		container.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			} // windowClosing
		});

		// make the cursor transparent
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
		container.getContentPane().setCursor(blankCursor);

		// add key listener to this canvas
		addKeyListener(new KeyInputHandler());

		// request focus so key events are handled by this canvas
		requestFocus();

		// add mouse listener to this canvas
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				mousePressed = true;
			} // mousePressed
			public void mouseReleased(MouseEvent e) {
				mousePressed = false;
			} // mouseReleased
		}); // detects mouse button presses

		// add mouse motion listener to this canvas
		addMouseMotionListener(new MouseAdapter() {
			public void mouseMoved(MouseEvent e) {
				cursorX = e.getX();
				cursorY = e.getY();
			} // mouseMoved
		}); // detects mouse location

		// create buffer strategy to take advantage of accelerated graphics
		createBufferStrategy(2);
		strategy = getBufferStrategy();

		// loading screen
		Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
		paint(g);

		// load backdrops
		images.add(new ImageIcon(getClass().getResource("images/backdrops/background.png")));
		background = (images.get(0)).getImage();
		images.add(new ImageIcon(getClass().getResource("images/backdrops/menu_background.png")));
		menuBackground = (images.get(1)).getImage();
		images.add(new ImageIcon(getClass().getResource("images/backdrops/help_background.png")));
		helpBackground = (images.get(2)).getImage();

		// load menubuttons
		images.add(new ImageIcon(getClass().getResource("images/buttons/playButton.png")));
		playButton = (images.get(3)).getImage();
		images.add(new ImageIcon(getClass().getResource("images/buttons/playButton2.png")));
		playButton2 = (images.get(4)).getImage();
		images.add(new ImageIcon(getClass().getResource("images/buttons/helpButton.png")));
		helpButton = (images.get(5)).getImage();
		images.add(new ImageIcon(getClass().getResource("images/buttons/helpButton2.png")));
		helpButton2 = (images.get(6)).getImage();
		images.add(new ImageIcon(getClass().getResource("images/buttons/quitButton.png")));
		quitButton = (images.get(7)).getImage();
		images.add(new ImageIcon(getClass().getResource("images/buttons/quitButton2.png")));
		quitButton2 = (images.get(8)).getImage();
		images.add(new ImageIcon(getClass().getResource("images/buttons/music.png")));
		soundIcon = (images.get(9).getImage());

		// load sprites
		images.add(new ImageIcon(getClass().getResource("images/sprites/enemy_die.png")));
		die = (images.get(10)).getImage();
		images.add(new ImageIcon(getClass().getResource("images/sprites/vortex.png")));
		vortex = (images.get(11)).getImage();
		images.add(new ImageIcon(getClass().getResource("images/sprites/freeze.png")));
		freeze = (images.get(12)).getImage();
		images.add(new ImageIcon(getClass().getResource("images/sprites/explosion.png")));
		explosion = (images.get(13)).getImage();

		// start the game
		initEntities();
		playGame();

	} // constructor

	// override paint for loading screen
	public void paint(Graphics g) {
		g.drawImage(loading, 0, 0, this);
	} // paint

	// displays menus, and starts or ends the game loop
	private void playGame() {
		// run music
		new Thread(new musicRunnable()).start();

		while (true) { // loop until user ends the game

			if (gameStage == PLAY) { // play the game

				resetGame();
				gameLoop();

			} else if (gameStage == MENU) { // show the menu

				Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.drawImage(menuBackground, 0, 0, this);

				// menu buttons
				if (cursorX > 35 && cursorX < 150 && cursorY > 230 && cursorY < 275) {
					g.drawImage(playButton2, 35, 230, this);
					g.drawImage(helpButton, 35, 275, this);
					g.drawImage(quitButton, 35, 320, this);
					if (mousePressed) gameStage = PLAY; // play game
				} else if (cursorX > 35 && cursorX < 150 && cursorY > 275 && cursorY < 320) {
					g.drawImage(playButton, 35, 230, this);
					g.drawImage(helpButton2, 35, 275, this);
					g.drawImage(quitButton, 35, 320, this);
					if (mousePressed) { // help screen
						gameStage = HELP;
						waitingForKeyPress = true;
					} // if
				} else if (cursorX > 35 && cursorX < 150 && cursorY > 320 && cursorY < 365) {
					g.drawImage(playButton, 35, 230, this);
					g.drawImage(helpButton, 35, 275, this);
					g.drawImage(quitButton2, 35, 320, this);
					if (mousePressed) System.exit(0); // quit game
				} else {
					g.drawImage(playButton, 35, 230, this);
					g.drawImage(helpButton, 35, 275, this);
					g.drawImage(quitButton, 35, 320, this);
				} // else

				if (music) g.drawImage(soundIcon, 35, 370, this);

				// display game stats from previous round
				g.setColor(Color.white);
				g.setFont(new Font("sansserif", Font.BOLD, 12));
				DecimalFormat myFormatter = new DecimalFormat("###,###.###");
				g.drawString("Score: " + myFormatter.format(score), 450, 280);
				g.drawString("Highscore: " + myFormatter.format(getHighscore()), 450, 310);
				if (((totalTime%60000)/1000) < 10) {
					g.drawString("Time: " + (totalTime/60000) + " : 0" + ((totalTime%60000)/1000), 450, 340);
				} else {
					g.drawString("Time: " + (totalTime/60000) + " : " + ((totalTime%60000)/1000), 450, 340);
				} // else
				g.drawString("Dead: " + killed, 450, 370);

				// draw cursor
				g.setColor(Color.cyan);
				g.drawOval(cursorX - 3, cursorY - 3, 6, 6);
				g.drawOval(cursorX - 4, cursorY - 4, 8, 8);

				// clear graphics and flip buffer
				g.dispose();
				strategy.show();

				// pause
				try { Thread.sleep(10); } catch (Exception e) {}

			} else if (gameStage == HELP) {

				if (!waitingForKeyPress) gameStage = MENU;

				Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.drawImage(helpBackground, 0, 0, this);

				if (music) g.drawImage(soundIcon, 570, 378, this);

				// draw cursor
				g.setColor(Color.cyan);
				g.drawOval(cursorX - 3, cursorY - 3, 6, 6);
				g.drawOval(cursorX - 4, cursorY - 4, 8, 8);

				// clear graphics and flip buffer
				g.dispose();
				strategy.show();

				// pause
				try { Thread.sleep(10); } catch (Exception e) {}

			} // else if

		} // while
	} // playGame

	// initializes ship, enemy, and powerup entities
	private void initEntities() {
		// create the ship and put in center of screen
		ship = new ShipEntity(this, 300, 200);

		// create enemies and powerups
		initAliens();
		initPowerups();
	} // initEntities

	// adds aliens to the game
	private void initAliens() {
		// always spawn randomly
		for (int i = 0; i < level; i++) {
			Entity alien = new AlienEntity(this, initCoord(600), initCoord(400), 0);
			alienEntities.add(alien);
	   	} // for

		// circle or lines
		int type =  1 + (int)(Math.random() * 5);
		if (type == 1) {
	 		for (int i = 0; i < 360; i += 360/spawnAmount[level-1]) {
				int xLoc = ship.getX() + (int) (400 * Math.cos(Math.toRadians(i)));
				int yLoc = ship.getY() + (int) (400 * Math.sin(Math.toRadians(i)));
	  			Entity alien = new AlienEntity(this, xLoc, yLoc, 0);
				alienEntities.add(alien);
			} // for
		} else if (type == 2 || type == 3) {
			if (level > 10 || type == 2) {
				for (int i = -200; i < 600; i += 800/spawnAmount[level-1]) {
		  			Entity alien = new AlienEntity(this, -200, i, 1);
					alienEntities.add(alien);
				} // for
			} // if
			if (level > 10 || type == 3) {
				for (int i = -200; i < 600; i += 800/spawnAmount[level-1]) {
		  			Entity alien = new AlienEntity(this, 800, i, 2);
					alienEntities.add(alien);
				} // for
			} // if
		} else if (type == 4 || type == 5) {
			if (level > 10 || type == 4) {
				for (int i = -200; i < 800; i += 1000/spawnAmount[level-1]) {
		  			Entity alien = new AlienEntity(this, i, -200, 3);
					alienEntities.add(alien);
				} // for
			 } // if
			 if (level > 10 || type == 5) {
				for (int i = -200; i < 800; i += 1000/spawnAmount[level-1]) {
		  			Entity alien = new AlienEntity(this, i, 600, 4);
					alienEntities.add(alien);
				} // for
			} // if
		} // else if
	} // initAliens

	// randomizes coordinates for alien placement
	private int initCoord(int boundary) {
		int pos = (int)(Math.random() * 250);
		if ((Math.random() * 4) < 2) pos *= -1;
		else pos += boundary;
		return pos;
	} // initCoord

	// adds a powerup of random type and location to the game
	private void initPowerups() {
		int type = 1 + (int)(Math.random() * 9);
		int xCoord, yCoord;
		do { // ensure powerups are not close to player
			xCoord = 20 + (int)(Math.random() * 440);
			yCoord = 30 + (int)(Math.random() * 260);
		} while (Math.abs(ship.getX() - xCoord) < 80 || Math.abs(ship.getY() - yCoord) < 80);
		PowerupEntity powerup = new PowerupEntity(this,	xCoord,	yCoord, type);
		powerupEntities.add(powerup);
	} // initPowerups

	// removes an entity from the game
	public void removeEntity(Entity entity) {
		removeEntities.add(entity);
	} // removeEntity

	// notifies that the player has died and ends the game
	public void notifyDeath() {
		gameStage = MENU;
		score += 6 * combo * combo;
		setHighscore(score);
	} // notifyDeath

	// notifies that an alien was killed
	public void notifyAlienKilled() {
		killed++;
		combo++;
		score += 10;
		comboTimer = 1200;

		if (killed % (level * 80) == 0 && level < 20) level++;
	} // notifyAlienKilled

	// fire a shot (1), data wave (2), or "data-shot" trail (3)
	private void tryToFire(int type) {
		// check that we've waited long enough to fire
		if ((System.currentTimeMillis() - lastFire) < firingInterval && type != 3) {
			return;
		} // if

		// otherwise add a shot
		lastFire = System.currentTimeMillis();
		ShotEntity shot = new ShotEntity(this, ship.getX(), ship.getY(), ship.getAngle(), type);
		shotEntities.add(shot);

		if (type == 2) ship.setDataWave(); // turn off data wave if needed

		// play sound
		if (type == 1) playClip("sound/shot.wav");
		else playClip("sound/longexplode.wav");
	} // tryToFire

	// fires shots in a circle
	public void circleShot() {
		for (int i = 0; i < 360; i += 8) {
			ShotEntity shot = new ShotEntity(this, ship.getX(), ship.getY(), Math.toRadians(i), 1);
			shotEntities.add(shot);
		} // for
		playClip("sound/explode.wav");
	} // circleShot

	// freezes (0) or kills (1) aliens within a certain radius
	public void explosion(int type, int radius) {
		for (int i = 0; i < alienEntities.size(); i++) {
			AlienEntity entity = (AlienEntity) alienEntities.get(i);
			if (entity.collidesWithExplosion(ship, radius)) {
				if (type == 0) entity.setFrozen();
				else if (type == 1) removeEntities.add(entity);
			} // if
		} // for
		playClip("sound/explode.wav");
	} // explosion

	// runs the main game loop until player dies
	private void gameLoop() {
		long lastLoopTime = System.currentTimeMillis();
		int highscore = getHighscore();

		// keep loop running until game ends
		while (gameStage == PLAY) {

			// calc. time since last update and update timers
			long delta = System.currentTimeMillis() - lastLoopTime;
			lastLoopTime = System.currentTimeMillis();
			if (!gamePaused) {
				spawnTimer -= delta;
				comboTimer -= delta;
				totalTime += delta;
				ship.runPowerupTimer(delta);
				for (int i = 0; i < shotEntities.size(); i++) {
					ShotEntity entity = (ShotEntity) shotEntities.get(i);
					entity.runTimer(delta);
				} // for
			} // if

			// get graphics context for the accelerated surface and draw background
			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(background, 0, 0, this);

			// draw game information on screen (score, highscore, combo, time, sound on/off)
			g.setColor(Color.white);
			g.setFont(new Font("sansserif", Font.BOLD, 14));
			DecimalFormat myFormatter = new DecimalFormat("###,###.###");
			g.drawString(myFormatter.format(score), 15, 18);
			g.drawString(myFormatter.format(highscore), 15, 394);
			if (music) g.drawImage(soundIcon, 570, 378, this);
			if (((totalTime%60000)/1000) < 10) {
				g.drawString((totalTime/60000) + " : 0" + ((totalTime%60000)/1000),
				585 - g.getFontMetrics().stringWidth((totalTime/60000) + " : 0" + ((totalTime%60000)/1000)), 18);
			} else {
				g.drawString((totalTime/60000) + " : " + ((totalTime%60000)/1000),
				585 - g.getFontMetrics().stringWidth((totalTime/60000) + " : " + ((totalTime%60000)/1000)), 18);
			} // else
			g.setColor(Color.black);
			g.setFont(new Font("sansserif", Font.BOLD, 16));
			g.drawString("Combo ", 210, 394);
			g.drawString("  x  " + combo, 335, 394);
			g.drawString("" + (combo*6), 335 - g.getFontMetrics().stringWidth("" + (combo*6)), 394);

			// move each entity
			if (!gamePaused) {
				for (int i = 0; i < alienEntities.size(); i++) {
					AlienEntity entity = (AlienEntity) alienEntities.get(i);
					entity.move(delta);
				} // for
				for (int i = 0; i < shotEntities.size(); i++) {
					Entity entity = (Entity) shotEntities.get(i);
					entity.move(delta);
				} // for
				for (int i = 0; i < powerupEntities.size(); i++) {
					Entity entity = (Entity) powerupEntities.get(i);
					entity.move(delta);
				} // for
				if (!ship.hasFire() && !ship.trailStop()) ship.move(delta);
			} // if

			// spawn enemies
			if (spawnTimer <= 0) {
				initAliens();
				spawnTimer = 2200;
			} // if

			// spawn powerups
			if (powerupEntities.size() < 3) {
				initPowerups();
			} // if

			// calculate combo
			if (comboTimer <= 0) {
				score += 6 * combo * combo;
				comboTimer = 1200;
				combo = 0;
			} // if

			// draw explosions, freezes, or vortex
			if (ship.freezeOn()) g.drawImage(freeze, ship.exploX() - 100, ship.exploY() - 100, this);
			if (ship.explosionOn()) g.drawImage(explosion, ship.exploX() - 100, ship.exploY() - 100, this);
			if (ship.vortexOn()) g.drawImage(vortex, ship.vortX() - 50, ship.vortY() - 50, this);

			// draw all entities
			for (int i = 0; i < alienEntities.size(); i++) {
				Entity entity = (Entity) alienEntities.get(i);
				entity.draw(g);
			} // for
			for (int i = 0; i < shotEntities.size(); i++) {
				ShotEntity entity = (ShotEntity) shotEntities.get(i);
				g.rotate(entity.getAngle(), entity.getX(), entity.getY());
				entity.draw(g);
				g.rotate(-entity.getAngle(), entity.getX(), entity.getY());
			} // for
			for (int i = 0; i < powerupEntities.size(); i++) {
				Entity entity = (Entity) powerupEntities.get(i);
				entity.draw(g);
			} // for

			// draw cursor
			g.setColor(Color.cyan);
			g.drawOval(cursorX - 3, cursorY - 3, 6, 6);
			g.drawOval(cursorX - 4, cursorY - 4, 8, 8);

			// draw ship
			g.rotate(ship.getAngle(), ship.getX(), ship.getY());
			ship.draw(g);
			g.rotate(-ship.getAngle(), ship.getX(), ship.getY());

			// checks for alien collision with ship, shots, or vortex
			for (int i = 0; i < alienEntities.size(); i++) {
				Entity him = (Entity) alienEntities.get(i);
				for (int j = 0; j < shotEntities.size(); j++) {
					Entity shot = (Entity) shotEntities.get(j);
					if (shot.collidesWith(him)) {
						shot.collidedWith(him);
						him.collidedWith(shot);
					} // for
				} // for
				if (ship.collidesWith(him)) {
					ship.collidedWith(him);
					him.collidedWith(ship);
				} // if
				if (ship.vortexOn()) {
					if (Math.abs(ship.vortX() - him.getX()) < 25 && Math.abs(ship.vortY() - him.getY()) < 25) {
						removeEntity(him);
						notifyAlienKilled();
					} // if
				} // if
		 	} // for
		 	// check for powerup collisions with ship
			for (int i = 0; i < powerupEntities.size(); i++) {
				Entity him = (Entity) powerupEntities.get(i);
				if (ship.collidesWith(him)) {
					ship.collidedWith(him);
					him.collidedWith(ship);
				} // if
			} // for

			// remove dead entities and animate aliens dying
			alienEntities.removeAll(removeEntities);
			powerupEntities.removeAll(removeEntities);
			shotEntities.removeAll(removeEntities);
			for (int i = 0; i < removeEntities.size(); i++) {
				Entity entity = (Entity) removeEntities.get(i);
				if (entity instanceof AlienEntity) {
					AlienEntity alienEntity = (AlienEntity) entity;
					if (!alienEntity.isDead()) {
						g.drawImage(die, entity.getX(), entity.getY(), this);
						alienEntity.makeDead(delta);
					} else {
						removeEntities.remove(i);
					} // else
				} else {
					removeEntities.remove(i);
				} // else
			} // for

			// if the game is paused, draw a message
			if (gamePaused) {
				g.setColor(Color.white);
				g.setFont(new Font("sansserif", Font.BOLD, 12));
				g.drawString("Paused", 278, 210);
				g.drawString("Press P to continue", 244, 230);
			} // if

			// if dead, draw a message
			if (gameStage == MENU) {
				g.setColor(Color.white);
				g.setFont(new Font("sansserif", Font.BOLD, 115));
				g.drawString("YOU DEAD", 0, 235);
			} // if

			// clear graphics and flip buffer
			g.dispose();
			strategy.show();

			// set alien movement towards ship or vortex
			for (int i = 0; i < alienEntities.size(); i++) {
				AlienEntity entity = (AlienEntity) alienEntities.get(i);
				if (!ship.vortexOn()) {
					entity.setSpeed(28);
					entity.setAccel(.01);
					entity.setMovement(ship.getX(), ship.getY(), false);
				} else {
					entity.setSpeed(70);
					entity.setAccel(1);
					entity.setMovement(ship.vortX(), ship.vortY(), true);
				} // else
			} // for

			// move ship towards mouse
			ship.setMovement(cursorX, cursorY);

			// try to fire if allowed
			if (ship.hasFire() && !gamePaused) tryToFire(1);
			if (ship.hasDataWave() && !gamePaused) tryToFire(2);
			if (ship.trailOn() && !gamePaused) tryToFire(3);

			// pause
			try { Thread.sleep(10); } catch (Exception e) {}

			// pause to display death notification
			if (gameStage == MENU) try { Thread.sleep(2200); } catch (Exception e) {}

		} // while

	} // gameLoop

	// starts a new game
	private void resetGame() {
		// clear out any existing entities and initalize a new set
		alienEntities.clear();
		powerupEntities.clear();
		shotEntities.clear();
		ship.resetPowerups();
		initEntities();

		// reset game stats
		spawnTimer = 2200;
		comboTimer = 1200;
		level = 1;
		score = 0;
		combo = 0;
		killed = 0;
		totalTime = 0;
	} // startGame

	// returns an image location to the powerup constructor based on type
	public String getPowerupImage(int type) {
		switch (type) {
			case 1: return "images/powerups/shield.png";
			case 2: return "images/powerups/spike.png";
			case 3: return "images/powerups/fire.png";
			case 4: return "images/powerups/circleshot.png";
			case 5: return "images/powerups/freeze.png";
			case 6: return "images/powerups/explosion.png";
			case 7: return "images/powerups/vortex.png";
			case 8: return "images/powerups/datawave.png";
			case 9: return "images/powerups/datatrail.png";
			default: return "";
		} // switch
	} // getImage

	// returns an image location to the shot constructor based on type
	public String getShotImage(int type) {
		switch (type) {
			case 1: return "images/sprites/shot.png";
			case 2: return "images/sprites/datawave.png";
			case 3: return (Math.random() < 0.5) ? "images/sprites/datashot0.png" : "images/sprites/datashot1.png";
			default: return "";
		} // switch
	} // getImage

	// writes new highscore to file if greater than previous highscore
	private void setHighscore(int highscore) {
		if (highscore > getHighscore()) {
			try {
                                BufferedWriter out = new BufferedWriter(new FileWriter("programdata.dll"));
				out.write("" + highscore);
				out.newLine();
				out.close(); // close writer
			} catch (Exception e) {} // catch
		} // if
	} // getHighscore

	// reads highscore from file
	private int getHighscore() {
		int highscore = 0;
		try {
                        BufferedReader in = new BufferedReader(new FileReader("programdata.dll"));
			in.mark(Short.MAX_VALUE);
			highscore = Integer.parseInt(in.readLine());
			in.close(); // close reader
		} catch (Exception e) {} // catch
		return highscore;
	} // getHighscore

	// plays a sound effect clip given it's file path
	private void playClip(String clipPath) {
	        try {
                        Clip clip; // clip used for sound effects

                        // gets path to the sound file
                        URL url = getClass().getClassLoader().getResource(clipPath);

                        // Set up an audio input stream piped from the sound file.
                        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);

                        // Get a clip resource
                        clip = AudioSystem.getClip();

                        // Open audio clip and load samples from the audio input stream.
                        clip.open(audioInputStream);

                        if (clip.isRunning()) clip.stop(); // Stop the clip if it is still running
                        clip.setFramePosition(0); // rewind to the beginning
                        clip.start(); // start playing the clip

                } catch(Exception e) {};
	} // playClip

	// inner class musicRunnable to play music during the game
	private class musicRunnable implements Runnable {
		public void run() {
			try {
				musicClip = AudioSystem.getClip();

				// select file to be played
				URL url = getClass().getClassLoader().getResource("sound/music.wav");

				// open menu clip before playing
				AudioInputStream aisOne = AudioSystem.getAudioInputStream(url);
				musicClip.open(aisOne);

				// loops menu music continuously
				musicClip.loop(Clip.LOOP_CONTINUOUSLY);

				// infinite loop unless user doesn't want music
				while (music);

				// stop & close the clip
				musicClip.stop();
				musicClip.close();

			} catch(Exception e) {};
		} // run
	} // musicRunnable

	// inner class KeyInputHandler to handle keyboard input
	private class KeyInputHandler extends KeyAdapter {
		private int pressCount = 1;

		public void keyTyped(KeyEvent e) {
			// end game if esc pressed, pause if P pressed, toggle sound if S pressed
			if (e.getKeyChar() == 27) {
				System.exit(0);
			} // if escape pressed
			if (e.getKeyChar() == 's' || e.getKeyChar() == 'S') {
				if (music) {
					music = false;
				} else {
					music = true;
					new Thread(new musicRunnable()).start();
				} // if
				return;
			} // if S pressed
			if (e.getKeyChar() == 'p' || e.getKeyChar() == 'P') {
				gamePaused = (gamePaused) ? false : true;
			} // if P pressed

			if (waitingForKeyPress) { // help screen to menu
				if (pressCount == 1) {
					waitingForKeyPress = false;
					pressCount = 0;
				} else {
					pressCount++;
				} // else
			} // if
		} // keyTyped
	} // class KeyInputHandler

	// main program
	public static void main(String argv[]) {
		// instantiate this object
		new Game();
	} // main

} // Game