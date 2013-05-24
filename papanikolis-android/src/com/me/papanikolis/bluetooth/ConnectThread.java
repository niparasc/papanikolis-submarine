package com.me.papanikolis.bluetooth;

import java.io.IOException;
import java.util.UUID;

import com.badlogic.gdx.Gdx;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class ConnectThread extends Thread {

	public static final String LOG = ConnectThread.class.getSimpleName();
	
	private BluetoothManager mBluetoothManager;
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    public ConnectThread(BluetoothManager mBluetoothManager, BluetoothDevice device) {
    	this.mBluetoothManager = mBluetoothManager;

        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;
 
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
        	Gdx.app.log(LOG, "ConnectThread to " + device.toString());
            tmp = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString(BluetoothManager.PAPANIKOLIS_UUID));
        } catch (IOException e) {
        	Gdx.app.log(LOG, "Any trouble here?");
        	e.printStackTrace();
        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        mBluetoothManager.getBluetoothAdapter().cancelDiscovery();

        try {
        	Gdx.app.log(LOG, "Will call connect()");
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
            	Gdx.app.log(LOG, "Unable to connect. Will call close on mmSocket");
                mmSocket.close();
                mBluetoothManager.setState(BluetoothManager.STATE_IDLE);
            } catch (IOException closeException) {
            	Gdx.app.log(LOG, closeException.getMessage());
            	mBluetoothManager.setState(BluetoothManager.STATE_IDLE);
            }
            return;
        }
 
        // Do work to manage the connection (in a separate thread)
        manageConnectedSocket(mmSocket);
    }

	/**
	 * Initiates the thread that will manage the connection.
	 * 
	 * @param socket the connected BluetoothSocket 
	 */
	public void manageConnectedSocket(BluetoothSocket socket) {
    	Gdx.app.log(LOG, "CLIENT: CONNECTION ESTABLISHED");
    	mBluetoothManager.setConnectedThread(new ConnectedThread(mBluetoothManager, socket));
    	mBluetoothManager.getConnectedThread().start();

    	// Switching the state to STATE_CONNECTED will have an effect on the rendering thread,
    	// thus we need to do this the libgdx way.
    	Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
            	mBluetoothManager.setState(BluetoothManager.STATE_CONNECTED);
            }
         });
	}
    
    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
        	Gdx.app.log(LOG, "Will cancel in-progress connection. Will call close on mmSocket");
            mmSocket.close();
        } catch (IOException e) {
        	Gdx.app.log(LOG, e.getMessage());
        }
    }

}
