package com.niparasc.papanikolis.multiplayer;

import java.util.Set;

public interface BluetoothInterface {

	/**
	 * Return true if the device supports Bluetooth.
	 * 
	 * @return true if Bluetooth is supported
	 */
	public boolean isBluetoothSupported();

	/**
	 * Return true if Bluetooth is currently enabled and ready for use.
	 * 
	 * @return true if the local adapter is turned on
	 */
	public boolean isBluetoothEnabled();

	/**
	 * Return true if Bluetooth is discoverable. Being discoverable means it is
	 * already enabled.
	 * 
	 * @return true if the local adapter is discoverable
	 */
	public boolean isBluetoothDiscoverable();

	/**
	 * Make the local device discoverable to other devices.
	 */
	public void enableBluetoothDiscoverability();

	/**
	 * Enable Bluetooth.
	 */
	public void enableBluetooth();

	/**
	 * Get all devices (paired and discovered). First the paired are fetched and
	 * then the discovered are placed on top of them and the final set is
	 * returned.
	 * 
	 * @return set with strings of paired and discovered devices, each
	 *         containing the device's name and address
	 */
	public Set<String> getDevices();

	/**
	 * Start the remote device discovery process.
	 * 
	 * @return true on success, false on error
	 */
	public boolean startDiscovery();

	/**
	 * Cancel the remote device discovery process.
	 * 
	 * @return true on success, false on error
	 */
	public boolean cancelDiscovery();

	/**
	 * Return true if the local Bluetooth adapter is currently in the device
	 * discovery process.
	 * 
	 * @return true if discovering
	 */
	public boolean isDiscovering();

	/**
	 * Initiates an AcceptThread and waits for a peer to join.
	 */
	public void startConnectionListening();

	/**
	 * Cancels the AcceptThread's listening socket and causes the thread to
	 * finish.
	 */
	public void stopConnectionListening();

	/**
	 * Return BluetoothManager's current state.
	 * 
	 * @return STATE_IDLE == 0, STATE_LISTEN == 1
	 */
	public int getState();

	/**
	 * Initiates connection process to the host with the specified MAC address.
	 * 
	 * @param mac
	 *            the Host's MAC address
	 */
	public void startConnectionToHost(String mac);

	/**
	 * Cancel the in-progress connection and close the socket.
	 */
	public void stopConnectionToHost();

	/**
	 * Return true if the device is in STATE_IDLE.
	 * 
	 * @return true if in STATE_IDLE, otherwise false
	 */
	public boolean isIdle();

	/**
	 * Return true if the device is in STATE_LISTEN.
	 * 
	 * @return true if in STATE_LISTEN, otherwise false
	 */
	public boolean isListening();

	/**
	 * Return true if the device is in STATE_CONNECTING.
	 * 
	 * @return true if in STATE_CONNECTING, otherwise false
	 */
	public boolean isConnecting();

	/**
	 * Return true if the device is in STATE_CONNECTED.
	 * 
	 * @return true if in STATE_CONNECTED, otherwise false
	 */
	public boolean isConnected();

	/**
	 * Shuts down the connection.
	 */
	public void shutdownConnection();

	/**
	 * Transmits the transmissionPackage to the peer.
	 * 
	 * @param transmissionPackage
	 *            the package to be transmitted to the peer
	 */
	public void transmitPackage(TransmissionPackage transmissionPackage);

	/**
	 * Return the next (first in queue) TransmissionPackage that came from the
	 * peer. If the queue is empty null is returned.
	 * 
	 * @return the first in queue TransmissionPackage received from the peer or
	 *         null if the queue is empty
	 */
	// TransmissionPackage getNextIncomingPackage();

}
