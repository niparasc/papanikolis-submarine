package com.me.papanikolis.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.me.papanikolis.Papanikolis;

public class HelpScreen extends AbstractScreen {

	public static final String LOG = HelpScreen.class.getSimpleName();

	private Label helpLabel;
	private String helpString = "Touch and hold screen or trackball to ascend.\nRelease to descend.\nPretty easy, huh?";

	public HelpScreen(Papanikolis game) {
		super(game);
	}

	@Override
	public void show() {
		getTable().add(getLogoImage()).spaceBottom(10);
		getTable().row();

		helpLabel = new Label(helpString, getSkin());
		helpLabel.setAlignment(Align.center);
		getTable().add(helpLabel).spaceBottom(10);
		getTable().row();

		final TextButton backButton = new TextButton("Back", getSkin());
		getTable().add(backButton).size(200, 40).expandX().uniform();
		backButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				game.setScreen(new MenuScreen(game));
			}
		});
	}

	public void dispose() {
		super.dispose();
		Gdx.app.log(LOG, "Disposing HelpScreen");
	}

}
