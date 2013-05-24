package com.me.papanikolis.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.me.papanikolis.Papanikolis;

public abstract class AbstractScreen implements Screen {

    public static final String LOG = AbstractScreen.class.getSimpleName();
	
	protected Papanikolis game;
	
	private Stage uiStage;
	private Skin skin;
	private Table table;
	private Image logoImage;
	
	public AbstractScreen(Papanikolis game) {
		this.game = game;
	}
	
	/* Singleton access to resources */
	
	public Stage getUIStage() {
		if (uiStage == null) {
			uiStage = new Stage();
			Gdx.input.setInputProcessor(uiStage);
		}
		return uiStage;
	}
	
	public Skin getSkin() {
		if (skin == null) {
			skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
		}
		return skin;
	}
	
	public Table getTable() {
		if (table == null) {
			table = new Table(getSkin());
			table.setFillParent(true);
			table.center();
			table.debug();
			getUIStage().addActor(getTable());
		}
		return table;
	}
	
	public Image getLogoImage() {
		if (logoImage == null) {
			logoImage = new Image(game.getAssetManager().get("papanikolis/logo.png", Texture.class));
		}
		return logoImage;
	}
	
	/* Screen implementation */
	
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		getUIStage().act();	// Nothing happens here
		getUIStage().draw();
//		Table.drawDebug(uiStage);
	}
	
	public void resize(int width, int height) {
//		getUIStage().setViewport(Papanikolis.VIEWPORT_WIDTH, Papanikolis.VIEWPORT_HEIGHT, false);
		getUIStage().setViewport(width, height, false);
	}
	
	public void show() {
		
	}
	
	public void hide() {
		Gdx.app.log(LOG, "Will call dispose on screen");
		// This call is necessary to dispose the screen's resources
		// after hiding it. Otherwise, the resources will reside in memory!
		dispose();
	}
	
	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}
	
	public void dispose() {
		Gdx.app.log(LOG, "Disposing AbstractScreen");
		if (uiStage != null) uiStage.dispose();
		if (skin != null) skin.dispose();
	}
}
