package com.niparasc.papanikolis.actors.game1;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.niparasc.papanikolis.Papanikolis;
import com.niparasc.papanikolis.actors.Box2DActor;
import com.niparasc.papanikolis.stages.Box2DStage;

public class Obstacle extends Box2DActor {

	public static final String LOG = Obstacle.class.getSimpleName();

	public static final int WIDTH = 32;
	public static final int HEIGHT = 64;

	public Obstacle(Papanikolis game, Box2DStage gameStage) {
		super(game, gameStage);
		setWidth(WIDTH);
		setHeight(HEIGHT);
		setOriginX(getWidth() / 2);
		setOriginY(getHeight() / 2);
	}

	public void act(float delta) {
		super.act(delta);
		setPosition(gameStage.convertToWorld(body.getPosition().x) - getWidth()
				/ 2, gameStage.convertToWorld(body.getPosition().y)
				- getHeight() / 2);

		setRotation(MathUtils.radiansToDegrees * body.getAngle());
	}

	public void draw(SpriteBatch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		batch.end();

		shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
		shapeRenderer.setTransformMatrix(batch.getTransformMatrix());

		shapeRenderer.begin(ShapeType.FilledRectangle);
		shapeRenderer.setColor(new Color(0, 0, 1, 1));
		shapeRenderer.filledRect(getX(), getY(), getWidth(), getHeight());
		shapeRenderer.end();

		batch.begin();
	}

	/*
	 * Sets the position of the physics body.
	 */
	public void setBodyPosition(float x, float y) {
		if (body == null)
			createBody(x, y);
		else
			repositionBody(x, y);
	}

	public void createBody(float x, float y) {
		BodyDef bd = new BodyDef();
		bd.type = BodyType.StaticBody;
		bd.position.set(gameStage.convertToBox(x), gameStage.convertToBox(y));

		body = gameStage.getWorld().createBody(bd);

		PolygonShape ps = new PolygonShape();
		// Subtract the radius from the polygon shape so that there is no gap
		// when bodies collide!
		// See box2d manual section 4.4. Shrinks the skin.
		ps.setAsBox(gameStage.convertToBox(getWidth() / 2) - ps.getRadius(),
				gameStage.convertToBox(getHeight() / 2) - ps.getRadius());

		FixtureDef fd = new FixtureDef();
		fd.shape = ps;

		body.createFixture(fd);
		ps.dispose();

		body.setUserData(this);
	}

	/*
	 * Repositions the physics body (transform) at the given coordinates.
	 */
	public void repositionBody(float x, float y) {
		body.setTransform(gameStage.convertToBox(x), gameStage.convertToBox(y),
				0);
	}

	@Override
	public void dispose() {
		Gdx.app.log(LOG, "Disposing Obstacle");
		gameStage.getWorld().destroyBody(body);
		remove();
	}

}
