package com.niparasc.papanikolis.actors.game1;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.niparasc.papanikolis.Papanikolis;
import com.niparasc.papanikolis.actors.Box2DActor;
import com.niparasc.papanikolis.stages.Box2DStage;

public class Surface extends Box2DActor {

	public static final String LOG = Surface.class.getSimpleName();

	// The surface points
	private Array<Vector2> surface;

	// The mirror points of the surface, used to fill the surface with colored
	// triangles.
	private Array<Vector2> mirror;

	public Surface(Papanikolis game, Box2DStage gameStage,
			Array<Vector2> surface) {
		super(game, gameStage);
		this.surface = surface;
		mirror = new Array<Vector2>();
		createBody();
	}

	public Array<Vector2> getSurface() {
		return surface;
	}

	public void setSurface(Array<Vector2> surface) {
		this.surface = surface;
	}

	public Array<Vector2> getMirror() {
		return mirror;
	}

	public void setMirror(Array<Vector2> mirror) {
		this.mirror = mirror;
	}

	public void draw(SpriteBatch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		batch.end();

		shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
		shapeRenderer.setTransformMatrix(batch.getTransformMatrix());

		// Draw lines
		/*
		 * shapeRenderer.begin(ShapeType.Line); shapeRenderer.setColor(new
		 * Color(0, 0, 1, 1)); for (int i = 0; i < surface.size - 1; i++) {
		 * Vector2 point1 = surface.get(i); Vector2 point2 = surface.get(i + 1);
		 * shapeRenderer.line(point1.x, point1.y, point2.x, point2.y); }
		 * shapeRenderer.end();
		 */

		// Draw T1s
		shapeRenderer.begin(ShapeType.FilledTriangle);
		shapeRenderer.setColor(new Color(0, 0, 1, 1));
		for (int i = 0; i < surface.size - 1; i++) {
			Vector2 p1 = surface.get(i);
			Vector2 p2 = mirror.get(i);
			Vector2 p3 = mirror.get(i + 1);
			shapeRenderer.filledTriangle(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
		}
		shapeRenderer.end();

		// Draw T2s
		shapeRenderer.begin(ShapeType.FilledTriangle);
		shapeRenderer.setColor(new Color(0, 0, 1, 1));
		for (int i = 0; i < surface.size - 1; i++) {
			Vector2 p1 = surface.get(i + 1);
			Vector2 p2 = mirror.get(i + 1);
			Vector2 p3 = surface.get(i);
			shapeRenderer.filledTriangle(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
		}
		shapeRenderer.end();

		// Draw connection points
		// shapeRenderer.begin(ShapeType.FilledRectangle);
		// shapeRenderer.setColor(new Color(0, 1, 0, 1));
		// for (int i = 0; i < surface.size; i++) {
		// Vector2 point1 = surface.get(i);
		// shapeRenderer.filledRect(point1.x, point1.y, 10, 10);
		// }
		// shapeRenderer.end();

		batch.begin();
	}

	public void createBody() {
		BodyDef bd = new BodyDef();
		bd.type = BodyType.StaticBody;
		body = gameStage.getWorld().createBody(bd);

		ChainShape chain = new ChainShape();
		Vector2[] vertices = new Vector2[surface.size];

		for (int i = 0; i < vertices.length; i++) {
			Vector2 worldV = surface.get(i);
			vertices[i] = new Vector2(gameStage.convertToBox(worldV.x),
					gameStage.convertToBox(worldV.y));
		}

		chain.createChain(vertices);

		body.createFixture(chain, 0);
		chain.dispose();

		body.setUserData(this);
	}

	/*
	 * Extends the surface with point. Destroys and recreates the body's fixture
	 * and chain shape.
	 * 
	 * TODO: Couldn't make it work like this: get shape, destroy fixture, create
	 * fixture with the same (updated) shape.
	 */
	public void extend(Vector2 point) {
		surface.add(point);

		Fixture fixture = body.getFixtureList().get(0);
		body.destroyFixture(fixture);

		ChainShape chain = new ChainShape();
		Vector2[] vertices = new Vector2[surface.size];

		for (int i = 0; i < vertices.length; i++) {
			Vector2 worldV = surface.get(i);
			vertices[i] = new Vector2(gameStage.convertToBox(worldV.x),
					gameStage.convertToBox(worldV.y));
		}

		chain.createChain(vertices);

		body.createFixture(chain, 0);
		chain.dispose();
	}

	/*
	 * Prunes the surface by removing the first point from it.
	 */
	public void prune() {
		surface.removeIndex(0);
		mirror.removeIndex(0);
	}

	@Override
	public void dispose() {
		Gdx.app.log(LOG, "Disposing Surface");
		gameStage.getWorld().destroyBody(body);
		remove();
	}

}
