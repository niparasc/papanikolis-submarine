package com.me.papanikolis.actors;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;
import com.me.papanikolis.Papanikolis;
import com.me.papanikolis.stages.Box2DStage;

public abstract class Box2DActor extends Actor implements Disposable {
	
    public static final String LOG = Box2DActor.class.getSimpleName();

    protected Papanikolis game;
    protected Box2DStage gameStage;
	protected Body body;
	protected ShapeRenderer shapeRenderer = new ShapeRenderer();
	
	// Used for smoothing the physics (see "the final touch" algorithm)
	protected Vector2 smoothedPosition = new Vector2();
	protected float smoothedAngle;
	protected Vector2 previousPosition = new Vector2();
	protected float previousAngle;

	public Box2DActor(Papanikolis game, Box2DStage gameStage) {
		this.game = game;
		this.gameStage = gameStage;
	}
	
	public Body getBody() {
		return body;
	}
	
	public void setBody(Body body) {
		this.body = body;
	}
	
	/* Glenn's algorithm helper methods */
	
	public Vector2 getSmoothedPosition() {
		return smoothedPosition;
	}
	
	public float getSmoothedAngle() {
		return smoothedAngle;
	}
	
	public Vector2 getPreviousPosition() {
		return previousPosition;
	}
	
	public float getPreviousAngle() {
		return previousAngle;
	}
	
	public void setSmoothedPositionX(float x) {
		smoothedPosition.x = x;
	}
	
	public void setSmoothedPositionY(float y) {
		smoothedPosition.y = y;
	}
	
	public void setSmoothedAngle(float a) {
		smoothedAngle = a;
	}
	
	public void setPreviousPositionX(float x) {
		previousPosition.x = x;
	}
	
	public void setPreviousPositionY(float y) {
		previousPosition.y = y;
	}
	
	public void setPreviousAngle(float a) {
		previousAngle = a;
	}
	
}
