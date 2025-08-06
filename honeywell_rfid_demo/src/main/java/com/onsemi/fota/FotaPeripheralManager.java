package com.onsemi.fota;

import java.util.List;

/**
 * The interface to the {@link FotaPeripheralManager}
 */

public interface FotaPeripheralManager {
    /**
     * A list of visible Bluetooth LE peripherals. The list is updated when a new peripheral is found
     * or when a peripheral is not seen until the InvisibleTimeout.
     * Register for an update event with {@link FotaPeripheralManager#addListener(FotaPeripheralManagerListener)  addListener}.
     * @return a list of visible peripherals.
     */
    List<FotaPeripheral> peripherals();

    /**
     * Returns if the scan for Bluetooth LE peripherals is started or not.
     * @return True when the scan is started, false otherwise.
     */
    boolean isScanStarted();

    /**
     * Starts the scan for nearby peripherals.
     */
    void startScan();

    /**
     * Stops a running scan for peripherals
     */
    void stopScan();

    /**
     * Clears the list of available peripherals
     */
    void clearPeripherals();

    /**
     * Adds a {@link FotaPeripheralManagerListener listener}
     * @param listener The listener to add
     */
    void addListener(FotaPeripheralManagerListener listener);

    /**
     * Removes a {@link FotaPeripheralManagerListener listener}
     * @param listener The listener to remove
     */
    void removeListener(FotaPeripheralManagerListener listener);


    /**
     * Returns the state of the Bluetooth adapter.
     * In a transition from disabled to enabled, it can happen that
     * {@link #isBluetoothEnabled() isBluetoothEnabled} == {@link #isBluetoothDisabled() isBluetoothDisabled}
     * @return True if enabled, false otherwise
     */

    boolean isBluetoothEnabled();
    /**
     * Returns the state of the Bluetooth adapter.
     * In a transition from disabled to enabled, it can happen that
     * {@link #isBluetoothEnabled() isBluetoothEnabled} == {@link #isBluetoothDisabled() isBluetoothDisabled}
     * @return True if disabled, false otherwise
     */
    boolean isBluetoothDisabled();

    /**
     * Sets the selected peripheral. This perhipheral is not removed from the
     * {@link #peripherals()}  list of peripherals}
     * @param peripheral
     */
    void setSelected(FotaPeripheral peripheral);

    /**
     * Returns the selected peripheral.
     * @return The selected peripheral
     */
    FotaPeripheral getSelected();

}
