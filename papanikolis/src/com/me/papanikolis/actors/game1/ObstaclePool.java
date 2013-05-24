package com.me.papanikolis.actors.game1;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.me.papanikolis.Papanikolis;
import com.me.papanikolis.stages.Box2DStage;

public class ObstaclePool extends Pool<Obstacle> implements Disposable {

	public static final String LOG = ObstaclePool.class.getSimpleName();

	private Papanikolis game;
	private Box2DStage gameStage;

	public ObstaclePool(Papanikolis game, Box2DStage gameStage) {
		this.game = game;
		this.gameStage = gameStage;
	}

	@Override
	protected Obstacle newObject() {
		return new Obstacle(game, gameStage);
	}

	@Override
	public void dispose() {
		Gdx.app.log(LOG, "Disposing ObstaclePool");
		clear();
	}

}
