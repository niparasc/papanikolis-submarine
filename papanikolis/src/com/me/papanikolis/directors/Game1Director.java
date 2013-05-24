package com.me.papanikolis.directors;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;
import com.me.papanikolis.actors.game1.Obstacle;
import com.me.papanikolis.actors.game1.ObstaclePool;
import com.me.papanikolis.actors.game1.Submarine;
import com.me.papanikolis.actors.game1.Surface;
import com.me.papanikolis.screens.Game1;
import com.me.papanikolis.screens.Game1.State;
import com.me.papanikolis.stages.Box2DStage;
import com.me.papanikolis.utils.Utils;
import com.me.papanikolis.Papanikolis;

public class Game1Director extends Director {

    public static final String LOG = Game1Director.class.getSimpleName();
	
	protected Utils utils = new Utils();
	
	// World gravity on X
	protected static final float xGravity = 0;
	// World gravity on Y
	protected static final float yGravity = -3f;
	
	/* === Surfaces === */

	// Bottom surface
	protected Surface surfaceBottom;
	// Top surface
	protected Surface surfaceTop;
	// Next surface points
	protected Vector2 nextSurfacePointBottom;
	protected Vector2 nextSurfacePointTop;
    // Next surface point shall be generated at (xDistance + xVariation) from the last surface point
	protected int xDistance = Papanikolis.VIEWPORT_WIDTH / 3;
	protected int xMinVariation = -Papanikolis.VIEWPORT_WIDTH / 10;
	protected int xMaxVariation = Papanikolis.VIEWPORT_WIDTH / 10;
    // Random number between [xMinVariation, xMaxVariation]
	protected int xVariation;
	// Bottom surface points shall be between [bottomMinY, bottomMaxY]
	protected int bottomMinY = 0;
	protected int bottomMaxY = Papanikolis.VIEWPORT_HEIGHT / 4;
	// Top surface points shall be between [topMinY, topMaxY]
	protected int topMinY = Papanikolis.VIEWPORT_HEIGHT - bottomMaxY;
	protected int topMaxY = Papanikolis.VIEWPORT_HEIGHT;
	// Number of surface points to be added to the surfaces
	protected int numberOfSurfacePoints = 1000;
	// The surface points created before the game starts
	protected ArrayList<Vector2> surfacePoints = new ArrayList<Vector2>();
	// Random distances X and Y between surface points, used in multiplayer to create the surfaces (recycled)
	protected int[] randomPointXDistance = new int[10];
	protected int[] randomPointYDistance = new int[20];
	
	/* === Obstacles === */

	// The obstacles, there will be 3 obstacles max that will be reused.
	protected Array<Obstacle> obstacles;
	// Obstacle pool to avoid allocating new obstacles thus garbage collection
	protected ObstaclePool obstaclePool;
	// Flag indicating if a new obstacle should be added or not
	protected boolean addObstacle = true;
	// Counts the number of surface points generated after the last obstacle
	protected int pointsAfterObstacle = 0;
	// X and Y position of next obstacle
	protected float xObstacle;
	protected float yObstacle;
	// Random Y distances for the obstacles, used in multiplayer to sync obstacle positioning between peers (recycled)
	protected int[] randomYObstacleDistance = new int[30];

	/* === Submarine === */
	
	protected Submarine submarine;
	// Initial submarine position
	protected float initSubX = Papanikolis.VIEWPORT_WIDTH / 10;
	protected float initSubY = Papanikolis.VIEWPORT_HEIGHT / 2;
	// Flag indicating if the submarine should move upwards or downwards
	protected boolean elevateSub = false;
	// Velocity on X axis (m/s)
	protected float xVelocity = 1.2f;
	// How much shall the submarine's velocity be increased 
	protected static final float xVelocityIncrease = 0.05f;
	// Impulse applied on Y axis when screen is touched
	protected static final float yImpulse = 0.165f;
//	protected static final float yImpulse = 0;
	// DEPRECATED: How much is velocity increased on Y axis when elevateSub is true
//	protected static final float yUp = 0.08f;
	// DEPRECATED: How much is velocity decreased on Y axis when elevateSub is false
//	protected static final float yDown = -0.05f;
	// The angle of the submarine when it is elevated (gives a feeling of speed up)
	protected float pitch = 0;
	// The distance traveled by the submarine
	protected int distance = 0;
	// Distance at which the velocity is increased (keeps always the last checkpoint)
	protected int velocityCheckpoint = 0;
	// After how much distance shall the submarine's velocity be increased
	protected static final int distanceThreshold = 5;
	
	public Game1Director(Papanikolis game, Box2DStage gameStage) {
		super(game, gameStage);

		// Set the desired gravity to the physics world
		gameStage.getWorld().setGravity(new Vector2(xGravity, yGravity));

		createSurfaceBottom();
		createSurfaceTop();
		obstacles = new Array<Obstacle>();
		obstaclePool = new ObstaclePool(game, gameStage);
		createSubmarine(initSubX, initSubY);
		createTunnelPoints();
	}
	
	public int getDistance() {
		return distance;
	}
	
	@Override
	public void direct() {
		
		/* === Follow the submarine with the camera === */
		
		Camera camera = gameStage.getCamera();
		followSubmarine(camera);
		
		/* === Extend and prune the tunnel === */
		
		// Get the last surface point
		Vector2 lastSurfacePoint = surfaceBottom.getSurface().peek();
		
		// If it's time to extend, extend.
		if (lastSurfacePoint.x - camera.position.x <= Papanikolis.VIEWPORT_WIDTH / 2) {
			xVariation = utils.random(xMinVariation, xMaxVariation);
			
			nextSurfacePointBottom = surfacePoints.remove(0);
			
			// Extend the surface and recreate its mirror points
			surfaceBottom.extend(nextSurfacePointBottom);
			surfaceBottom.setMirror(createSurfaceMirrorBottom());
			
			nextSurfacePointTop = nextSurfacePointBottom.cpy();
			nextSurfacePointTop.y += topMinY;
			
			// Extend the surface and recreate its mirror points
			surfaceTop.extend(nextSurfacePointTop);
			surfaceTop.setMirror(createSurfaceMirrorTop());
			
			// If the surface has more than 7 points, prune it. Keeps it small, saves resources.
			if (surfaceBottom.getSurface().size > 7) {
				surfaceBottom.prune();
				surfaceTop.prune();
			}
			
			/* === Add obstacle if needed === */

			if (addObstacle) {
				xObstacle = nextSurfacePointBottom.x;
				yObstacle = utils.random(nextSurfacePointBottom.y + Obstacle.HEIGHT / 2, nextSurfacePointTop.y - Obstacle.HEIGHT / 2);
				
				// Get obstacle from pool
				Obstacle obstacle = obstaclePool.obtain();
				// Set its position
				obstacle.setBodyPosition(xObstacle, yObstacle);
				// Keep a reference of it
				obstacles.add(obstacle);
				// Add it on stage
				gameStage.addActor(obstacle);
				
				// Lower the flag
				addObstacle = false;
				
				// Restart counting
				pointsAfterObstacle = 0;
				
			} else {
				pointsAfterObstacle++;
				
				if (pointsAfterObstacle == 1)
					addObstacle = true;
			}
		}
		
		/* === Move the submarine === */
		
		Body submarineBody = submarine.getBody();

		// Update speed
		Vector2 v = submarineBody.getLinearVelocity();
		v.x = xVelocity;
		submarineBody.setLinearVelocity(v);
		
		// Apply impulse
		if (elevateSub) {
			submarineBody.applyLinearImpulse(new Vector2(0, yImpulse), submarineBody.getLocalCenter());
//			v.y += yUp;
			pitch = 0.1f;
		} else {
//			v.y += yDown;
			pitch = 0;
		}
		
		submarineBody.setTransform(submarineBody.getPosition(), pitch);
		
		// Retrieve traveled distance
		distance = (int) submarineBody.getPosition().x;
		
		// Velocity increase logic
		if (distance - velocityCheckpoint >= distanceThreshold) {
			xVelocity += xVelocityIncrease;
			velocityCheckpoint = distance;
		}
		
		/* === Dispose obstacle if needed === */
		
		// If the first obstacle (leftmost) moves out of screen, put it back in the pool.
		Obstacle firstObstacle = obstacles.first();
		if (firstObstacle.getX() + firstObstacle.getWidth() < camera.position.x - Papanikolis.VIEWPORT_WIDTH / 2) {
			// Put the obstacle in the pool
			obstaclePool.free(firstObstacle);
			// Remove the object from the queue
			obstacles.removeIndex(0);
			// Remove the obstacle from the stage
			firstObstacle.remove();
		}
	}
	
	/*
	 * Creates the bottom surface.
	 */
	public void createSurfaceBottom() {
		Array<Vector2> vertices = new Array<Vector2>();

		vertices.add(new Vector2(0, bottomMaxY / 2));
		vertices.add(new Vector2(Papanikolis.VIEWPORT_WIDTH, bottomMaxY / 2));
		
		surfaceBottom = new Surface(game, gameStage, vertices);
		gameStage.addActor(surfaceBottom);
		
		surfaceBottom.setMirror(createSurfaceMirrorBottom());
	}
	
	/*
	 * Creates the top surface.
	 */
	public void createSurfaceTop() {
		Array<Vector2> vertices = new Array<Vector2>();

		vertices.add(new Vector2(0, topMinY + (topMaxY - topMinY) / 2));
		vertices.add(new Vector2(Papanikolis.VIEWPORT_WIDTH, topMinY + (topMaxY - topMinY) / 2));
		
		surfaceTop = new Surface(game, gameStage, vertices);
		gameStage.addActor(surfaceTop);
		
		surfaceTop.setMirror(createSurfaceMirrorTop());
	}
	
	/*
	 * Creates the submarine.
	 */
	public void createSubmarine(float x, float y) {
		submarine = new Submarine(game, gameStage, x, y);
		gameStage.addActor(submarine);
	}
	
	/*
	 * Creates the surface points of the tunnel and the obstacle Y positions.
	 */
	public void createTunnelPoints() {
		// Get the last surface point
		Vector2 lastSurfacePoint = surfaceBottom.getSurface().peek();
		Vector2 lastPoint = lastSurfacePoint.cpy();
		
		for (int i = 0; i < numberOfSurfacePoints; i++) {
			Vector2 nextSurfacePointBottom = new Vector2(lastPoint.x + (xDistance + xVariation), utils.random(bottomMinY, bottomMaxY));
			lastPoint = nextSurfacePointBottom.cpy();
			surfacePoints.add(nextSurfacePointBottom);
		}
	}
	
	/*
	 * Creates and returns the mirror points of the bottom surface.
	 * These points are used to fill the surface with colored triangles.
	 */
	public Array<Vector2> createSurfaceMirrorBottom() {
		Array<Vector2> mirror = new Array<Vector2>();
		
		for (Vector2 v : surfaceBottom.getSurface()) {
			Vector2 vCpy = v.cpy();
			vCpy.y += -(Papanikolis.VIEWPORT_HEIGHT - v.y);
			mirror.add(vCpy);
		}
		
		return mirror;
	}
	
	/*
	 * Creates and returns the mirror points of the top surface.
	 * These points are used to fill the surface with colored triangles.
	 */
	public Array<Vector2> createSurfaceMirrorTop() {
		Array<Vector2> mirror = new Array<Vector2>();
		
		for (Vector2 v : surfaceTop.getSurface()) {
			Vector2 vCpy = v.cpy();
			vCpy.y += Papanikolis.VIEWPORT_HEIGHT - v.y;
			mirror.add(vCpy);
		}
		
		return mirror;
	}
	
	/*
	 * Repositions the camera so that it follows the submarine.
	 */
	public void followSubmarine(Camera camera) {
		float x = submarine.getX() + (Papanikolis.VIEWPORT_WIDTH / 2 - Papanikolis.VIEWPORT_WIDTH / 10) + submarine.getWidth() / 2;
		float y = Papanikolis.VIEWPORT_HEIGHT / 2;
		camera.position.set(x, y, 0f);
		camera.update();
	}
	
	/*
	 * What happens when the game is over.
	 */
	public void gameOver() {
		// Persist best distance
		Preferences preferencesManager = game.getPreferencesManager();
		int bestDistance = preferencesManager.getInteger("bestDistance", 0);
		
		if (distance > bestDistance) {
			preferencesManager.putInteger("bestDistance", distance);
			preferencesManager.flush();
		}
		
		// Update GUI
		Game1 game1 = (Game1)game.getScreen();
		game1.getBestDistanceLabel().setText("Best: " + preferencesManager.getInteger("bestDistance", 0));
		game1.getInfoLabel().setText("Crashed!\nDistance: " + distance + "\nTouch screen to play again.");
		game1.getMenuButton().setVisible(true);
		
		// Switch state
		game1.setState(State.CRASHED);
	}
	
	/* Process Input */
	
	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		case Keys.DPAD_CENTER:
			switch (((Game1)game.getScreen()).getState()) {
			case  READINESS_TEST:
				((Game1)game.getScreen()).getInfoLabel().setText("");
				((Game1)game.getScreen()).getMenuButton().setVisible(false);
				((Game1)game.getScreen()).setState(State.SAILING);
				break;
			case CRASHED:
				game.setScreen(new Game1(game, false, false));
				break;
			}				
			elevateSub = true;
			break;
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
		case Keys.DPAD_CENTER:
			elevateSub = false;
			break;
		}
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		switch (((Game1)game.getScreen()).getState()) {
		case  READINESS_TEST:
			((Game1)game.getScreen()).getInfoLabel().setText("");
			((Game1)game.getScreen()).getMenuButton().setVisible(false);
			((Game1)game.getScreen()).setState(State.SAILING);
			break;
		case CRASHED:
			game.setScreen(new Game1(game, false, false));
			break;
		}
		elevateSub = true;
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		elevateSub = false;
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	/* Process Collisions */
	
	@Override
	public void beginContact(Contact contact) {
		Object o1 = contact.getFixtureA().getBody().getUserData();
		Object o2 = contact.getFixtureB().getBody().getUserData();
		
		// Which one of the two collided objects is the submarine?
		
		if (o1.getClass() == Submarine.class) {
			if (game.getVibrator() != null)
				game.getVibrator().vibrateOnCrash();
			((Submarine)o1).crashed();
			gameOver();
		} else {
			if (game.getVibrator() != null)
				game.getVibrator().vibrateOnCrash();
			((Submarine)o2).crashed();
			gameOver();
		}
	}

	@Override
	public void endContact(Contact contact) {
		
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		
	}

	/* Dispose */
	
	@Override
	public void dispose() {
		Gdx.app.log(LOG, "Disposing Director");
		surfaceBottom.dispose();
		surfaceTop.dispose();
		// Dispose in game obstacles
		for (Obstacle obstacle : obstacles)
			obstacle.dispose();
		// Dispose obstacles in pool (out of game). Does this destroy the physics bodies? Don't think so.
		obstaclePool.dispose();
		submarine.dispose();
	}

}
