package com.me.papanikolis.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.Gdx;

import android.bluetooth.BluetoothSocket;

public class ConnectedThread extends Thread {

	public static final String LOG = ConnectedThread.class.getSimpleName();
	
	private BluetoothManager mBluetoothManager;
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
 
    public ConnectedThread(BluetoothManager mBluetoothManager, BluetoothSocket socket) {
    	this.mBluetoothManager = mBluetoothManager;
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
 
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
        	Gdx.app.log(LOG, "Constructor: " + e.getMessage());
        }
 
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        final byte[] buffer = new byte[1024];  // buffer store for the stream
//        int bytes; // bytes returned from read()
 
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                mmInStream.read(buffer);
//                Gdx.app.log(LOG, "RECEIVED: " + buffer.toString());
            	
                // Post a Runnable to the rendering thread that processes the result
                Gdx.app.postRunnable(new Runnable() {
                   @Override
                   public void run() {
                       // Let BluetoothManager's handler handle the incoming bytes
                       mBluetoothManager.getHandler().obtainMessage(BluetoothManager.MESSAGE_READ, buffer).sendToTarget();
                   }
                });
            } catch (IOException e) {
            	Gdx.app.log(LOG, "Read: " + e.getMessage());
                break;
            }
        }
    }
 
    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
//        	Gdx.app.log(LOG, "SENDING: " + bytes.toString());
            mmOutStream.write(bytes);
        } catch (IOException e) {
        	Gdx.app.log(LOG, "Write: " + e.getMessage());
        }
    }
 
    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
        	Gdx.app.log(LOG, "Will shutdown the connection");
            mmSocket.close();
        } catch (IOException e) {
        	Gdx.app.log(LOG, "Cancel: " + e.getMessage());
        }
    }

}
