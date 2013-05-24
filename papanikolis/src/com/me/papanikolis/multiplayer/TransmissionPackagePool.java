package com.me.papanikolis.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

public class TransmissionPackagePool extends Pool<TransmissionPackage> implements Disposable {

	public static final String LOG = TransmissionPackagePool.class.getSimpleName();
	
	@Override
	protected TransmissionPackage newObject() {
		return new TransmissionPackage();
	}

	@Override
	public void dispose() {
		Gdx.app.log(LOG, "Disposing PackagePool");
		clear();
	}

}
