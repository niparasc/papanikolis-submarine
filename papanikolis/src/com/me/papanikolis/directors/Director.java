package com.me.papanikolis.directors;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.utils.Disposable;
import com.me.papanikolis.Papanikolis;
import com.me.papanikolis.stages.Box2DStage;

public abstract class Director implements InputProcessor, ContactListener, Disposable {

	protected Papanikolis game;
	protected Box2DStage gameStage;
	
	public Director(Papanikolis game, Box2DStage gameStage) {
		this.game = game;
		this.gameStage = gameStage;
	}
	
	public Papanikolis getGame() {
		return game;
	}

	public void setGame(Papanikolis game) {
		this.game = game;
	}

	public Box2DStage getGameStage() {
		return gameStage;
	}
	
	public void setGameStage(Box2DStage gameStage) {
		this.gameStage = gameStage;
	}
	
	public abstract void createTunnelPoints();
//	public abstract void readinessTest();
	public abstract void direct();
//	public abstract void syncWithPeer();
	
}
