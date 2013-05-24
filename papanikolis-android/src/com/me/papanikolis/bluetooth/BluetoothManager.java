package com.me.papanikolis.bluetooth;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.SerializationUtils;

import com.badlogic.gdx.Gdx;
import com.me.papanikolis.MainActivity;
import com.me.papanikolis.multiplayer.BluetoothInterface;
import com.me.papanikolis.multiplayer.TransmissionPackage;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

@SuppressLint("HandlerLeak")
public class BluetoothManager implements BluetoothInterface {

	public static final String LOG = BluetoothManager.class.getSimpleName();

	// The service's name
	public static final String SERVICE_NAME = "papanikolis-submarine";
	// The game's UUID string, used by host and client
	public static final String PAPANIKOLIS_UUID = "48d775e0-ba31-11e2-9e96-0800200c9a66";

	// Constants that indicate the current connection state
	public static final int STATE_IDLE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device

	// Message types sent from the Handler (used only for read messages, for
	// now)
	public static final int MESSAGE_READ = 1;

	// Request to enable Bluetooth discoverability
	public static final int REQUEST_ENABLE_BT_DISCOVERABILITY = 1;
	// Request to enable Bluetooth
	public static final int REQUEST_ENABLE_BT = 2;

	// The one and only game activity (MainActivity).
	private MainActivity currentActivity;
	// BluetoothManager's current state
	private int state;
	// Android Bluetooth Adapter
	private BluetoothAdapter bluetoothAdapter;
	// Paired devices
	private Set<BluetoothDevice> pairedDevices;
	// Discovered devices
	private Set<BluetoothDevice> discoveredDevices = new HashSet<BluetoothDevice>();

	// Host thread (accept())
	private AcceptThread acceptThread;
	// Join thread (connect())
	private ConnectThread connectThread;
	// Communication thread (read(), write())
	private ConnectedThread connectedThread;

	// Here we queue the packages coming from the peer
	// private Array<TransmissionPackage> incomingPackages = new
	// Array<TransmissionPackage>();

	// The Handler that gets information back from the ConnectedThread
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;

				try {
					// Deserialize bytes to Package
					TransmissionPackage transmissionPackage = (TransmissionPackage) SerializationUtils
							.deserialize(readBuf);

					// Notify game about incoming data
					currentActivity.getPapanikolis().notify_PeerDataReceived(
							transmissionPackage);

					// Add package to queue
					// incomingPackages.add(transmissionPackage);
				} catch (Exception e) {
					e.printStackTrace();
					Gdx.app.log(LOG,
							"Deserialization problem ? " + e.getMessage());
				}

				break;
			}
		}
	};

	public BluetoothManager(MainActivity currentActivity) {
		this.currentActivity = currentActivity;
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		state = STATE_IDLE;
	}

	public BluetoothAdapter getBluetoothAdapter() {
		return bluetoothAdapter;
	}

	public void setState(int state) {
		this.state = state;
		// Notify about state change
		currentActivity.onBTMStateChange();
	}

	public ConnectedThread getConnectedThread() {
		return connectedThread;
	}

	public void setConnectedThread(ConnectedThread connectedThread) {
		this.connectedThread = connectedThread;
	}

	public Handler getHandler() {
		return handler;
	}

	/**
	 * Adds the device to the discovered devices set.
	 * 
	 * @param device
	 *            the discovered device
	 */
	public void addDiscoveredDevice(BluetoothDevice device) {
		discoveredDevices.add(device);
	}

	/* BluetoothInterface methods */

	public boolean isBluetoothSupported() {
		return (bluetoothAdapter != null) ? true : false;
	}

	public boolean isBluetoothEnabled() {
		return bluetoothAdapter.isEnabled();
	}

	public boolean isBluetoothDiscoverable() {
		return (bluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) ? true
				: false;
	}

	public void enableBluetoothDiscoverability() {
		Intent discoverableIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(
				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		currentActivity.startActivityForResult(discoverableIntent,
				REQUEST_ENABLE_BT_DISCOVERABILITY);
	}

	@Override
	public void enableBluetooth() {
		Intent enableBtIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_ENABLE);
		currentActivity.startActivityForResult(enableBtIntent,
				REQUEST_ENABLE_BT);
	}

	@Override
	public Set<String> getDevices() {
		SortedSet<String> devices = new TreeSet<String>();

		// Fetch from the bluetooth adapter the paired devices
		pairedDevices = bluetoothAdapter.getBondedDevices();

		// Add the discovered devices first...
		for (BluetoothDevice device : discoveredDevices)
			devices.add(device.getName() + " , " + device.getAddress());

		// ...and then the paired devices
		for (BluetoothDevice device : pairedDevices)
			devices.add(device.getName() + " , " + device.getAddress());

		return devices;
	}

	@Override
	public boolean startDiscovery() {
		return bluetoothAdapter.startDiscovery();
	}

	@Override
	public boolean cancelDiscovery() {
		if (!bluetoothAdapter.isDiscovering())
			return true;
		return bluetoothAdapter.cancelDiscovery();
	}

	@Override
	public boolean isDiscovering() {
		return bluetoothAdapter.isDiscovering();
	}

	@Override
	public void startConnectionListening() {
		if (state == STATE_IDLE) {
			Gdx.app.log(LOG, "== START LISTEN ==");
			acceptThread = new AcceptThread(this);
			acceptThread.start();
			setState(STATE_LISTEN);
		}
	}

	@Override
	public void stopConnectionListening() {
		if (state == STATE_LISTEN) {
			acceptThread.cancel();
			acceptThread = null;
			setState(STATE_IDLE);
		}
	}

	@Override
	public int getState() {
		return state;
	}

	@Override
	public void startConnectionToHost(String mac) {
		// Merge discovered and paired devices in one set
		Set<BluetoothDevice> devices = new HashSet<BluetoothDevice>();

		for (BluetoothDevice device : discoveredDevices)
			devices.add(device);
		for (BluetoothDevice device : pairedDevices)
			devices.add(device);

		// Find the device that matches the given mac
		for (BluetoothDevice device : devices) {
			if (device.getAddress().equalsIgnoreCase(mac)) {
				// Initiate connection process
				connectThread = new ConnectThread(this, device);
				connectThread.start();
				setState(STATE_CONNECTING);
				break;
			}
		}
	}

	@Override
	public void stopConnectionToHost() {
		if (state == STATE_CONNECTING || state == STATE_CONNECTED) {
			connectThread.cancel();
			connectThread = null;
			setState(STATE_IDLE);
		}
	}

	@Override
	public boolean isIdle() {
		return (state == STATE_IDLE) ? true : false;
	}

	@Override
	public boolean isListening() {
		return (state == STATE_LISTEN) ? true : false;
	}

	@Override
	public boolean isConnecting() {
		return (state == STATE_CONNECTING) ? true : false;
	}

	@Override
	public boolean isConnected() {
		return (state == STATE_CONNECTED) ? true : false;
	}

	@Override
	public void shutdownConnection() {
		if (state == STATE_CONNECTED) {
			connectedThread.cancel();
			connectedThread = null;
			setState(STATE_IDLE);
		}
	}

	@Override
	public void transmitPackage(TransmissionPackage transmissionPackage) {
		try {
			// Serialize package to bytes
			byte[] data = SerializationUtils.serialize(transmissionPackage);
			// Transmit data
			connectedThread.write(data);
		} catch (Exception e) {
			Gdx.app.log(LOG, "transmitPackage() - " + e.getMessage());
		}
	}

	// @Override
	// public TransmissionPackage getNextIncomingPackage() {
	// // TODO: Don't like this code...
	// TransmissionPackage transmissionPackage = null;
	//
	// try {
	// transmissionPackage = incomingPackages.removeIndex(0);
	// } catch (Exception e) {
	// // Gdx.app.log(LOG, "getNextIncomingPackage - " + e.getMessage());
	// }
	//
	// return transmissionPackage;
	// }

}
