package com.me.papanikolis.actors.game1;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.me.papanikolis.Papanikolis;
import com.me.papanikolis.actors.Box2DActor;
import com.me.papanikolis.stages.Box2DStage;

public class PeerSubmarine extends Box2DActor {

	public static final String LOG = PeerSubmarine.class.getSimpleName();

	private TextureRegion peerSubmarine;
	public boolean crashed = false;

	public PeerSubmarine(Papanikolis game, Box2DStage gameStage, float x,
			float y) {
		super(game, gameStage);
		peerSubmarine = new TextureRegion(game.getAssetManager().get(
				"papanikolis/peer-submarine-64x32.png", Texture.class));
		setPosition(x, y);
		// setSmoothedPositionX(x);
		// setSmoothedPositionY(y);
		// setPreviousPositionX(x);
		// setPreviousPositionY(y);
		setWidth(peerSubmarine.getRegionWidth());
		setHeight(peerSubmarine.getRegionHeight());
		setOriginX(getWidth() / 2);
		setOriginY(getHeight() / 2);
		// createBody(x, y);
	}

	public boolean isCrashed() {
		return crashed;
	}

	public void act(float delta) {
		super.act(delta);

		// Subtracting the submarine's half width and height from the body's
		// position.
		// The body shape created in the physics-body-editor has its reference
		// point at
		// the center of the submarine. Libgdx has it at the lower left corner.
		// setPosition(gameStage.convertToWorld(body.getPosition().x) -
		// getWidth() / 2,
		// gameStage.convertToWorld(body.getPosition().y) - getHeight() / 2);

		// setPosition(smoothedPosition.x - getWidth() / 2, smoothedPosition.y -
		// getHeight() / 2);

		// setRotation(MathUtils.radiansToDegrees * body.getAngle());

		// setRotation(MathUtils.radiansToDegrees * smoothedAngle);
	}

	public void draw(SpriteBatch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		batch.draw(peerSubmarine, getX(), getY(), getOriginX(), getOriginY(),
				getWidth(), getHeight(), getScaleX(), getScaleY(),
				getRotation());
	}

	// public void createBody(float x, float y) {
	// // Create a loader for the file saved from the physics-body-editor.
	// BodyEditorLoader loader = new
	// BodyEditorLoader(Gdx.files.internal("papanikolis/submarine-shape.json"));
	//
	// BodyDef bd = new BodyDef();
	// bd.type = BodyType.DynamicBody;
	// bd.position.set(gameStage.convertToBox(x), gameStage.convertToBox(y));
	//
	// body = gameStage.getWorld().createBody(bd);
	//
	// FixtureDef fd = new FixtureDef();
	//
	// // Create the body fixture automatically by using the loader.
	// loader.attachFixture(body, "papa-nikolis", fd,
	// gameStage.convertToBox(getWidth()));
	//
	// body.setUserData(this);
	// }

	/*
	 * Switches the submarine's texture to the crashed one.
	 */
	public void crashed() {
		peerSubmarine.setTexture(game.getAssetManager().get(
				"papanikolis/peer-submarine-crashed-64x32.png", Texture.class));
		crashed = true;
	}

	@Override
	public void dispose() {
		Gdx.app.log(LOG, "Disposing PeerSubmarine");
		// DESTROY THE PHYSICS BODY !!! FPS KILLER !!!
		gameStage.getWorld().destroyBody(body);
		// Remove actor from parent (stage)
		remove();
	}

}
