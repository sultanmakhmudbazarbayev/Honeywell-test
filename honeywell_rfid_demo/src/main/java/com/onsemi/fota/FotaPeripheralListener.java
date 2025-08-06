package com.onsemi.fota;

/**
 * Callbacks for {@link FotaPeripheral FotaPeripheral} changes
 */

public interface FotaPeripheralListener {

    /**
     * Invoked when the peripherals RSSI value changed
     * @param rssi The current RSSI value
     */
    void onRssiChanged(int rssi);

    /**
     * Invoked when the peripherals {@link FotaState state} changed
     * @param oldState The old state.
     * @param newState The new state.
     */
    void onStateChanged(FotaState oldState, FotaState newState);

    /**
     * Invoked when the connection was terminated
     * @param fromHost True when the host terminated the connection, false when the user terminated the connection
     */
    void onDisconnected(boolean fromHost);

}
