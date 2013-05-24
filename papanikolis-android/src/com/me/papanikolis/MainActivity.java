package com.me.papanikolis;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.me.papanikolis.bluetooth.BluetoothManager;

public class MainActivity extends AndroidApplication {

	public static final String LOG = MainActivity.class.getSimpleName();

	private BluetoothManager mBluetoothManager;
	private VibratorManager vibratorManager;
	private Papanikolis papanikolis;

	public Papanikolis getPapanikolis() {
		return papanikolis;
	}

	// Receives Bluetooth events
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			// BT discoverability state change
			if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
				final int state = intent.getIntExtra(
						BluetoothAdapter.EXTRA_SCAN_MODE,
						BluetoothAdapter.ERROR);

				switch (state) {
				// BT is discoverable
				case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
					Gdx.app.log(LOG,
							"BroadcastReceiver: SCAN_MODE_CONNECTABLE_DISCOVERABLE");
					papanikolis.notify_BT_SCAN_MODE_CONNECTABLE_DISCOVERABLE();
					break;
				// BT is NOT discoverable
				case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
					Gdx.app.log(LOG, "BroadcastReceiver: SCAN_MODE_CONNECTABLE");
					papanikolis.notify_BT_SCAN_MODE_CONNECTABLE();
					break;
				// BT is NOT discoverable
				case BluetoothAdapter.SCAN_MODE_NONE:
					Gdx.app.log(LOG, "BroadcastReceiver: SCAN_MODE_NONE");
					papanikolis.notify_BT_SCAN_MODE_NONE();
					break;
				}
			}
			// BT state change
			else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				final int state = intent.getIntExtra(
						BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

				switch (state) {
				case BluetoothAdapter.STATE_OFF:
					Gdx.app.log(LOG, "BroadcastReceiver: STATE_OFF");
					papanikolis.notify_BT_STATE_OFF();
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
					Gdx.app.log(LOG, "BroadcastReceiver: STATE_TURNING_OFF");
					break;
				case BluetoothAdapter.STATE_ON:
					Gdx.app.log(LOG, "BroadcastReceiver: STATE_ON");
					papanikolis.notify_BT_STATE_ON();
					break;
				case BluetoothAdapter.STATE_TURNING_ON:
					Gdx.app.log(LOG, "BroadcastReceiver: STATE_TURNING_ON");
					break;
				}
			}
			// When discovery finds a device
			else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				Gdx.app.log(LOG, "BroadcastReceiver: ACTION_FOUND");
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Gdx.app.log(LOG, "DETECTED DEVICE = " + device);
				// Add the discovered device to bluetooth manager's list of
				// discovered devices
				mBluetoothManager.addDiscoveredDevice(device);
				// Notify game that a device was found
				papanikolis.notify_BT_DEVICE_FOUND();
			}
			// When discovery starts
			else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				Gdx.app.log(LOG, "BroadcastReceiver: ACTION_DISCOVERY_STARTED");
				papanikolis.notify_BT_ACTION_DISCOVERY_STARTED();
			}
			// When discovery finishes
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				Gdx.app.log(LOG, "BroadcastReceiver: ACTION_DISCOVERY_FINISHED");
				papanikolis.notify_BT_ACTION_DISCOVERY_FINISHED();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.useGL20 = false;
		cfg.useAccelerometer = false;
		cfg.useCompass = false;

		mBluetoothManager = new BluetoothManager(this);
		vibratorManager = new VibratorManager(
				(Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
		papanikolis = new Papanikolis(mBluetoothManager, vibratorManager);

		initialize(papanikolis, cfg);

		// Register for BluetoothAdapter broadcasts
		IntentFilter filter = new IntentFilter(
				BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		Gdx.app.log(LOG, "== onCreate ==");
	}

	public void onDestroy() {
		super.onDestroy();

		// Unregister broadcast listener
		this.unregisterReceiver(mReceiver);

		Gdx.app.log(LOG, "== onDestroy ==");
	}

	public void onPause() {
		super.onPause();
		Gdx.app.log(LOG, "== onPause ==");
	}

	public void onResume() {
		super.onResume();
		Gdx.app.log(LOG, "== onResume ==");
		// this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	// Called to get the result of the user's decision to enable BT (and/or make
	// it discoverable) or not.
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BluetoothManager.REQUEST_ENABLE_BT_DISCOVERABILITY) {
			switch (resultCode) {

			// The "yes" case is handled by the BroadcastReceiver.

			case RESULT_CANCELED:
				papanikolis.notify_BT_SCAN_MODE_NONE();
				break;
			}
		} else if (requestCode == BluetoothManager.REQUEST_ENABLE_BT) {
			switch (resultCode) {

			// The "yes" case is handled by the BroadcastReceiver.

			case RESULT_CANCELED:
				papanikolis.notify_BT_STATE_OFF();
				break;
			}
		}
	}

	public void onBTMStateChange() {
		papanikolis.notify_BMT_STATE_CHANGE();
	}

}