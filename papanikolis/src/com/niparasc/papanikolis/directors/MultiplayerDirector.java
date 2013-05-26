package com.niparasc.papanikolis.directors;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.niparasc.papanikolis.Papanikolis;
import com.niparasc.papanikolis.actors.game1.Obstacle;
import com.niparasc.papanikolis.actors.game1.PeerSubmarine;
import com.niparasc.papanikolis.actors.game1.Submarine;
import com.niparasc.papanikolis.multiplayer.BluetoothInterface;
import com.niparasc.papanikolis.multiplayer.TransmissionPackage;
import com.niparasc.papanikolis.multiplayer.TransmissionPackagePool;
import com.niparasc.papanikolis.stages.Box2DStage;

public abstract class MultiplayerDirector extends Game1Director {

	public static final String LOG = MultiplayerDirector.class.getSimpleName();

	protected BluetoothInterface bluetoothInterface;
	protected TransmissionPackagePool transmissionPackagePool;
	protected PeerSubmarine peerSubmarine;
	// Used in direct() to position obstacles on Y
	protected int k = 0;
	// Flag indicating if the client's first message was received
	protected boolean firstPeerMessageReceived = false;

	public MultiplayerDirector(Papanikolis game, Box2DStage gameStage) {
		super(game, gameStage);

		bluetoothInterface = game.getBluetoothInterface();
		transmissionPackagePool = new TransmissionPackagePool();

		createPeerSubmarine(submarine.getX() - submarine.getWidth() / 2,
				submarine.getY());
	}

	public Submarine getSubmarine() {
		return submarine;
	}

	public PeerSubmarine getPeerSubmarine() {
		return peerSubmarine;
	}

	public abstract void updatePeer();

	public abstract void notify_PeerDataReceived(
			TransmissionPackage transmissionPackage);

	@Override
	public void direct() {

		if (firstPeerMessageReceived && !peerSubmarine.isCrashed()) {
			// Update peer submarine's X position
			peerSubmarine.setX(submarine.getX());
		}

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

			// Extends the surface and recreate its mirror points
			surfaceBottom.extend(nextSurfacePointBottom);
			surfaceBottom.setMirror(createSurfaceMirrorBottom());

			nextSurfacePointTop = nextSurfacePointBottom.cpy();
			nextSurfacePointTop.y += topMinY;

			// Extends the surface and recreate its mirror points
			surfaceTop.extend(nextSurfacePointTop);
			surfaceTop.setMirror(createSurfaceMirrorTop());

			// If the surface has more than 7 points, prune it. Keeps it small,
			// saves resources.
			if (surfaceBottom.getSurface().size > 7) {
				surfaceBottom.prune();
				surfaceTop.prune();
			}

			/* === Add obstacle if needed === */

			if (addObstacle) {
				xObstacle = nextSurfacePointBottom.x;
				yObstacle = nextSurfacePointBottom.y
						+ randomYObstacleDistance[k];
				k++;
				if (k == randomYObstacleDistance.length - 1) {
					k = 0;
				}

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
			submarineBody.applyLinearImpulse(new Vector2(0, yImpulse),
					submarineBody.getLocalCenter());
			// submarineBody.applyLinearImpulse(new Vector2(0, 0),
			// submarineBody.getLocalCenter());
			// v.y += yUp;
			pitch = 0.1f;
		} else {
			// v.y += yDown;
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

		// If the first obstacle (leftmost) moves out of screen, put it back in
		// the pool.
		Obstacle firstObstacle = obstacles.first();
		if (firstObstacle.getX() + firstObstacle.getWidth() < camera.position.x
				- Papanikolis.VIEWPORT_WIDTH / 2) {
			// Put the obstacle in the pool
			obstaclePool.free(firstObstacle);
			// Remove the object from the queue
			obstacles.removeIndex(0);
			// Remove the obstacle from the stage
			firstObstacle.remove();
		}
	}

	/*
	 * Creates the random point distances that will be used (recycled) to create
	 * the surfaces and position the obstacles.
	 */
	public void createRandomPointDistances() {
		// For surface points
		for (int i = 0; i < randomPointXDistance.length; i++) {
			xVariation = utils.random(xMinVariation, xMaxVariation);
			randomPointXDistance[i] = xDistance + xVariation;
			randomPointYDistance[i] = utils.random(bottomMinY, bottomMaxY);
		}

		// For obstacles
		for (int i = 0; i < randomYObstacleDistance.length; i++) {
			randomYObstacleDistance[i] = utils.random(Obstacle.HEIGHT / 2,
					topMinY - bottomMaxY);
		}
	}

	/*
	 * Creates the surface points of the tunnel and the obstacle Y positions.
	 */
	public void createTunnelPoints() {
		createRandomPointDistances();
		surfacePoints = new ArrayList<Vector2>();

		// Get the last surface point
		Vector2 lastSurfacePoint = surfaceBottom.getSurface().peek();
		Vector2 lastPoint = lastSurfacePoint.cpy();

		int k = 0;

		for (int i = 0; i < numberOfSurfacePoints; i++) {
			Vector2 nextSurfacePointBottom = new Vector2(lastPoint.x
					+ randomPointXDistance[k], randomPointYDistance[k]);
			k++;
			if (k == randomPointXDistance.length - 1)
				k = 0;
			lastPoint = nextSurfacePointBottom.cpy();
			surfacePoints.add(nextSurfacePointBottom);
		}
	}

	/*
	 * Creates the peer submarine.
	 */
	public void createPeerSubmarine(float x, float y) {
		peerSubmarine = new PeerSubmarine(game, gameStage, x, y);
		// A little trick to put the submarine on top of the peer submarine
		submarine.remove();
		gameStage.addActor(peerSubmarine);
		gameStage.addActor(submarine);
	}

	/* Dispose */

	@Override
	public void dispose() {
		super.dispose();
		Gdx.app.log(LOG, "Disposing MultiplayerDirector");
		transmissionPackagePool.dispose();
	}

}
