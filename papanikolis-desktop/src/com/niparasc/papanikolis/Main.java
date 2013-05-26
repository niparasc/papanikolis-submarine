package com.niparasc.papanikolis;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.niparasc.papanikolis.Papanikolis;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Papanikolis Submarine";
		cfg.useGL20 = false;
		cfg.width = 800;
		cfg.height = 600;

		new LwjglApplication(new Papanikolis(), cfg);
	}
}
