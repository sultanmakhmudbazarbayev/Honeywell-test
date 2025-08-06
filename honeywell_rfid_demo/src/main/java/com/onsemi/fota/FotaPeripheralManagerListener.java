package com.onsemi.fota;

/**
 * Callback for {@link FotaPeripheralManager FotaPeripheralManager} events
 */

public interface FotaPeripheralManagerListener {

    /**
     * Invoked when the selected fota peripheral changed
     */
    void selectedChanged(FotaPeripheral peripheral);

    /**
     * Invoked when the selected fota peripheral is going to be changed
     */
    void selectedChanging(FotaPeripheral peripheral);

    /**
     * Invoked when the {@link FotaPeripheralManager#peripherals() peripherals} list changed
     */
    void onPeripheralsListUpdated();

    /**
     * Invoked when the Bluetooth adapter is enabled
     */
    void onBluetoothEnabled();

    /**
     * Invoked when the Bluetooth adapter is disabled
     */
    void onBluetoothDisabled();

}
