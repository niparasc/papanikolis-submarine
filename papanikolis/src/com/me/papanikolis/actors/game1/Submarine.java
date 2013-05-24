package com.me.papanikolis.actors.game1;

import aurelienribon.bodyeditor.BodyEditorLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.me.papanikolis.actors.Box2DActor;
import com.me.papanikolis.stages.Box2DStage;
import com.me.papanikolis.Papanikolis;

public class Submarine extends Box2DActor {

    public static final String LOG = Submarine.class.getSimpleName();
	
	private TextureRegion submarine;
	private boolean crashed = false;
	
	public Submarine(Papanikolis game, Box2DStage gameStage, float x, float y) {
		super(game, gameStage);
		submarine = new TextureRegion(game.getAssetManager().get("papanikolis/submarine-64x32.png", Texture.class));
		setPosition(x, y);
		setSmoothedPositionX(x);
		setSmoothedPositionY(y);
		setPreviousPositionX(x);
		setPreviousPositionY(y);
		setWidth(submarine.getRegionWidth());
		setHeight(submarine.getRegionHeight());
		setOriginX(getWidth() / 2);
		setOriginY(getHeight() / 2);
		createBody(x, y);
	}
	
	public boolean isCrashed() {
		return crashed;
	}
	
	public void act(float delta) {
		super.act(delta);
		
		// Subtracting the submarine's half width and height from the body's position.
		// The body shape created in the physics-body-editor has its reference point at
		// the center of the submarine. Libgdx has it at the lower left corner.
//        setPosition(gameStage.convertToWorld(body.getPosition().x) - getWidth() / 2, 
//        		gameStage.convertToWorld(body.getPosition().y) - getHeight() / 2);
        
        setPosition(smoothedPosition.x - getWidth() / 2, smoothedPosition.y - getHeight() / 2);
        
//        setRotation(MathUtils.radiansToDegrees * body.getAngle());
        
        setRotation(MathUtils.radiansToDegrees * smoothedAngle);
	}
	
	public void draw(SpriteBatch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		
		batch.draw(submarine, getX(), getY(), getOriginX(), getOriginY(), 
				getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
	}
	
	public void createBody(float x, float y) {
		// Create a loader for the file saved from the physics-body-editor.
	    BodyEditorLoader loader = new BodyEditorLoader(Gdx.files.internal("papanikolis/submarine-shape.json"));
	    
	    BodyDef bd = new BodyDef();
		bd.type = BodyType.DynamicBody;
		bd.position.set(gameStage.convertToBox(x), gameStage.convertToBox(y));
		
		body = gameStage.getWorld().createBody(bd);
		
		FixtureDef fd = new FixtureDef();
	    
		// Create the body fixture automatically by using the loader.
	    loader.attachFixture(body, "papa-nikolis", fd, gameStage.convertToBox(getWidth()));
	    
	    body.setUserData(this);
	}
	
	/*
	 * Switches the submarine's texture to the crashed one.
	 */
	public void crashed() {
		submarine.setTexture(game.getAssetManager().get("papanikolis/submarine-crashed-64x32.png", Texture.class));
		crashed = true;
	}
	
	@Override
	public void dispose() {
		Gdx.app.log(LOG, "Disposing Submarine");
		// DESTROY THE PHYSICS BODY !!! FPS KILLER !!!
		gameStage.getWorld().destroyBody(body);
		// Remove actor from parent (stage)
		remove();
	}

}
