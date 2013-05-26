package com.niparasc.papanikolis.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.niparasc.papanikolis.Papanikolis;

public class MenuScreen extends AbstractScreen {

	public static final String LOG = MenuScreen.class.getSimpleName();

	private Label welcomeLabel;

	public MenuScreen(Papanikolis game) {
		super(game);
	}

	@Override
	public void show() {
		getTable().add(getLogoImage()).spaceBottom(10);
		getTable().row();

		welcomeLabel = new Label("== Welcome on board captain ==", getSkin());
		getTable().add(welcomeLabel).spaceBottom(10);
		getTable().row();

		final TextButton game1Button = new TextButton("Singleplayer", getSkin());
		getTable().add(game1Button).size(200, 40).uniform().spaceBottom(5);
		game1Button.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				game.setScreen(new Game1(game, false, false));
			}
		});

		getTable().row();

		final TextButton game2Button = new TextButton("Multiplayer", getSkin());
		getTable().add(game2Button).size(200, 40).uniform().spaceBottom(5);
		game2Button.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				game.setScreen(new MultiplayerScreen(game));
			}
		});

		getTable().row();

		final TextButton game3Button = new TextButton("Help", getSkin());
		getTable().add(game3Button).size(200, 40).uniform();
		game3Button.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				game.setScreen(new HelpScreen(game));
			}
		});
	}

	public void dispose() {
		super.dispose();
		Gdx.app.log(LOG, "Disposing MenuScreen");
	}

}
