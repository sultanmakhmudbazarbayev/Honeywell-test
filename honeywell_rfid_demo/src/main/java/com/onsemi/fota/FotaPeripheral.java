package com.onsemi.fota;

import com.onsemi.ble.UpdateController;
import com.onsemi.ble.UpdateOptions;
import com.onsemi.protocol.update.FotaFirmwareVersion;

/**
 * The interface to the {@link FotaPeripheral}
 */

public interface FotaPeripheral {
    /**
     * The manufacturer specific data out of the advertising data
     * @return The manufacturer specific data
     */
    String getName();

    /**
     * The last received RSSI value.
     * @return The RSSI value.
     */
    int getRssi();

    /**
     * The Bluetooth address of the peripheral
     * @return The Bluetooth address
     */
    String getAddress();

    /**
     * The connection state of the fota peripheral. Add a {@link FotaPeripheralListener FotaPeripheralListener} for state changed updates.
     * @return The state.
     */
    FotaState getFotaState();

    /**
     * The device id of the connected device.
     * @return The device id.
     */
    byte[] getDeviceId();

    /**
     * The bootloader version of the connected device
     * @return The bootloader version.
     */
    FotaFirmwareVersion getBootloaderVersion();

    /**
     * The ble stack version of the connected device.
     * @return The ble stack version.
     */
    FotaFirmwareVersion getBleStackVersion();

    /**
     * The application version of the connected device.
     * @return The application version.
     */
    FotaFirmwareVersion getApplicationVersion();

    /**
     * The FOTA build id of the connected device.
     * @return The fota build id.
     */
    byte[] getFotaBuildId();

    /**
     * Adds a {@link FotaPeripheralListener}
     * @param listener The listener
     */
    void addListener(FotaPeripheralListener listener);

    /**
     * Removes a {@link FotaPeripheralListener}
     * @param listener The listener
     */
    void removeListener(FotaPeripheralListener listener);

    /**
     * Establishes a connection to the peripheral. This is a blocking operation and it returns
     * when ever the connection is established successful or an exception is thrown.
     * Add a {@link FotaPeripheralListener FotaPeripheralListener} for state changed updates.
     * @throws Exception When an error occurred.
     */
    void establish() throws Exception;

    /**
     * Terminates a connection to the peripheral. This is a blocking operation and it returns
     * when the connection is terminated or an exception is thrown.
     * Add a {@link FotaPeripheralListener FotaPeripheralListener} for state changed updates.
     * @throws Exception When an error occurred.
     */
    void teardown() throws Exception;

    /**
     * update the peripheral with the given controller and options.
     * @param controller The update controller.
     * @param options The update options
     * @throws Exception Wen an error occurred
     */
    void update(UpdateController controller, UpdateOptions options) throws  Exception;


}
