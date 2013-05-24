package com.me.papanikolis.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.me.papanikolis.Papanikolis;
import com.me.papanikolis.directors.Director;
import com.me.papanikolis.stages.Box2DStage;

public abstract class GameScreen extends AbstractScreen {

    public static final String LOG = GameScreen.class.getSimpleName();
    
	protected Box2DStage gameStage;
	protected Director director;
	
	public GameScreen(Papanikolis game) {
		super(game);
	}
	
	public Director getDirector() {
		return director;
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		gameStage.setViewport(Papanikolis.VIEWPORT_WIDTH, Papanikolis.VIEWPORT_HEIGHT, false);
	}

	@Override
	public void show() {		
		gameStage = new Box2DStage();
		director = createDirector(game, gameStage);
		gameStage.getWorld().setContactListener(director);
		
		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(getUIStage());
		multiplexer.addProcessor(director);
		Gdx.input.setInputProcessor(multiplexer);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		Gdx.app.log(LOG, "Disposing GameScreen");
		director.dispose();
		gameStage.dispose();
	}
	
	/* 
	 * Factory method for creating Directors. The concrete subclasses know which kind of
	 * director they need.
	 */
	protected abstract Director createDirector(Papanikolis game, Box2DStage gameStage);

}
