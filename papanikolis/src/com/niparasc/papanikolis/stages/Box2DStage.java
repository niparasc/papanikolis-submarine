package com.niparasc.papanikolis.stages;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.niparasc.papanikolis.actors.Box2DActor;

public class Box2DStage extends Stage {

	public static final String LOG = Box2DStage.class.getSimpleName();

	// Box2D world stepping parameters, Libgdx recommends 1/45f (or 1/300f) for
	// mobile phones
	public static final float TIME_STEP = 1 / 45f;
	public static final int VELOCITY_ITERATIONS = 6;
	public static final int POSITION_ITERATIONS = 2;

	// Conversion parameters
	private final float WORLD_TO_BOX = 0.01f;
	// private final float WORLD_TO_BOX = 1f;
	private final float BOX_TO_WORLD = 100f;
	// private final float BOX_TO_WORLD = 1f;

	private World world;

	// private Box2DDebugRenderer debugRenderer;

	public Box2DStage() {
		world = new World(new Vector2(0, -4f), true);
		world.setAutoClearForces(false);
		// debugRenderer = new Box2DDebugRenderer();
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	// public Box2DDebugRenderer getDebugRenderer() {
	// return debugRenderer;
	// }

	// public void setDebugRenderer(Box2DDebugRenderer debugRenderer) {
	// this.debugRenderer = debugRenderer;
	// }

	/*
	 * Gets pixels, returns box2d units
	 */
	public float convertToBox(float x) {
		return x * WORLD_TO_BOX;
	}

	/*
	 * Gets box2d units, returns pixels
	 */
	public float convertToWorld(float x) {
		return x * BOX_TO_WORLD;
	}

	/*
	 * === Semi-fixed timestep ===
	 */

	// public void update(float dt) {
	// while (dt > 0) {
	// float timeStep = Math.min(dt, TIME_STEP);
	// world.step(timeStep, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
	// dt -= timeStep;
	// }
	// }

	/*
	 * === Free the physics ===
	 * 
	 * (solution implemented by Rotating Canvas too) This causes a subtle but
	 * visually unpleasant stuttering of the physics simulation on the screen
	 * known as temporal aliasing.
	 */
	// private float accumulator = 0f;
	// public void update(float dt) {
	// accumulator += dt;
	//
	// while (accumulator >= TIME_STEP) {
	// world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
	// accumulator -= TIME_STEP;
	// }
	// }

	/*
	 * === The final touch ===
	 */
	float fixedTimestepAccumulator = 0;
	float fixedTimestepAccumulatorRatio = 0;
	// Maximum number of steps, to avoid degrading to an halt.
	final int MAX_STEPS = 5;

	public void update(float dt) {
		fixedTimestepAccumulator += dt;

		int nSteps = (int) Math.floor(fixedTimestepAccumulator / TIME_STEP);

		// To avoid rounding errors, touches fixedTimestepAccumulator only if
		// needed
		if (nSteps > 0) {
			fixedTimestepAccumulator -= nSteps * TIME_STEP;
		}

		fixedTimestepAccumulatorRatio = fixedTimestepAccumulator / TIME_STEP;

		// Allows above calculations of fixedTimestepAccumulator_ and
		// fixedTimestepAccumulatorRatio_ to remain unchanged.
		int nStepsClamped = Math.min(nSteps, MAX_STEPS);

		for (int i = 0; i < nStepsClamped; ++i) {
			resetSmoothStates();
			singleStep();
		}

		world.clearForces();

		// We "smooth" positions and orientations using
		// fixedTimestepAccumulatorRatio (alpha).
		smoothStates();
	}

	public void singleStep() {
		// apply physics forces, poll inputs etc ???
		world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
		// process collisions ???
	}

	/* Extrapolation */
	/*
	 * public void smoothStates() { float dt = fixedTimestepAccumulatorRatio *
	 * TIME_STEP;
	 * 
	 * Iterator<Body> it = world.getBodies();
	 * 
	 * while (it.hasNext()) { Body b = (Body) it.next();
	 * 
	 * if (b.getType() == BodyType.StaticBody) continue;
	 * 
	 * Box2DActor actor = (Box2DActor) b.getUserData(); Vector2 position =
	 * b.getLinearVelocity().mul(dt).add(b.getPosition());
	 * actor.setSmoothedPositionX(convertToWorld(position.x));
	 * actor.setSmoothedPositionY(convertToWorld(position.y)); float angle =
	 * b.getAngle() + dt * b.getAngularVelocity();
	 * actor.setSmoothedAngle(angle); } }
	 * 
	 * public void resetSmoothStates() { Iterator<Body> it = world.getBodies();
	 * 
	 * while (it.hasNext()) { Body b = (Body) it.next();
	 * 
	 * if (b.getType() == BodyType.StaticBody) continue;
	 * 
	 * Box2DActor actor = (Box2DActor) b.getUserData();
	 * actor.setSmoothedPositionX(convertToWorld(b.getPosition().x));
	 * actor.setSmoothedPositionY(convertToWorld(b.getPosition().y));
	 * actor.setSmoothedAngle(b.getAngle()); } }
	 */

	/* Interpolation */

	public void smoothStates() {
		float oneMinusRatio = 1 - fixedTimestepAccumulatorRatio;

		Iterator<Body> it = world.getBodies();

		while (it.hasNext()) {
			Body b = (Body) it.next();

			if (b.getType() == BodyType.StaticBody)
				continue;

			Box2DActor actor = (Box2DActor) b.getUserData();
			Vector2 previousPosition = new Vector2(
					convertToBox(actor.getPreviousPosition().x),
					convertToBox(actor.getPreviousPosition().y));
			Vector2 position = b.getPosition()
					.mul(fixedTimestepAccumulatorRatio)
					.add(previousPosition.mul(oneMinusRatio));
			actor.setSmoothedPositionX(convertToWorld(position.x));
			actor.setSmoothedPositionY(convertToWorld(position.y));
			float angle = fixedTimestepAccumulatorRatio * b.getAngle()
					+ oneMinusRatio * actor.getPreviousAngle();
			actor.setSmoothedAngle(angle);
		}
	}

	public void resetSmoothStates() {
		Iterator<Body> it = world.getBodies();

		while (it.hasNext()) {
			Body b = (Body) it.next();

			if (b.getType() == BodyType.StaticBody)
				continue;

			Box2DActor actor = (Box2DActor) b.getUserData();
			Vector2 position = b.getPosition();
			actor.setPreviousPositionX(convertToWorld(position.x));
			actor.setPreviousPositionY(convertToWorld(position.y));
			actor.setSmoothedPositionX(actor.getPreviousPosition().x);
			actor.setSmoothedPositionY(actor.getPreviousPosition().y);
			float angle = b.getAngle();
			actor.setPreviousAngle(angle);
			actor.setSmoothedAngle(actor.getPreviousAngle());
		}
	}

	public void dispose() {
		super.dispose();
		Gdx.app.log(LOG, "Disposing Box2DStage");
		world.dispose();
	}

}
