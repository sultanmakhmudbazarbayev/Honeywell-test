package com.onsemi.fota;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;

import com.onsemi.ble.BleException;
import com.onsemi.ble.BleResult;
import com.onsemi.ble.Characteristic;
import com.onsemi.ble.PeripheralChangedListener;
import com.onsemi.ble.PeripheralImpl;
import com.onsemi.ble.PeripheralState;
import com.onsemi.ble.Service;
import com.onsemi.protocol.update.FotaFirmwareVersion;
import com.onsemi.protocol.utility.Log;

import java.util.LinkedList;
import java.util.List;


/**
 * The implementation of the FotaPeripheral interface
 */

class FotaPeripheralImpl extends PeripheralImpl implements FotaPeripheral {

    private final static String TAG = "FotaPeripheralImpl";

    public String DeviceName;


    /**
     * The device id of the connected device
     */
    private byte[] deviceId;
    public void setDeviceId(byte[] id) { deviceId = id; }
    public byte[] getDeviceId() { return deviceId; }

    /**
     * The bootloader version of the connected device
     */
    private FotaFirmwareVersion bootloaderVersion;
    public void setBootloaderVersion(FotaFirmwareVersion version) { bootloaderVersion = version; }
    public FotaFirmwareVersion getBootloaderVersion() { return bootloaderVersion; }

    /**
     * The ble stack version of the connected device
     */
    private FotaFirmwareVersion bleStackVersion;
    public void setBleStackVersion(FotaFirmwareVersion version) { bleStackVersion = version; }
    public FotaFirmwareVersion getBleStackVersion() { return bleStackVersion; }

    /**
     * The application version of the connected device
     */
    private FotaFirmwareVersion applicationVersion;
    public void setApplicationVersion(FotaFirmwareVersion version) { applicationVersion = version; }
    public FotaFirmwareVersion getApplicationVersion() { return applicationVersion; }

    /**
     * The FOTA build id of the connected device
     */
    private byte[] fotaBuildId;
    public void setFotaBuildId(byte[] id) { fotaBuildId = id; }
    public byte[] getFotaBuildId() { return fotaBuildId; }

    private final LinkedList<FotaPeripheralListener> fotaListenerList;

    /**
     * The constructor
     * @param device The native bluetooth device
     * @param rssi The current rssi value
     * @param scanRecord The scan record with the advertising data
     */
    FotaPeripheralImpl(BluetoothDevice device, int rssi, ScanRecord scanRecord) {
        super(device, rssi, scanRecord);
        fotaListenerList = new LinkedList<>();

        addListener(new PeripheralChangedListener() {
            @Override public void onNameChanged(String name) { }
            @Override public void onManufacturerDataChanged(byte[] data) { }
            @Override  public void onRssiChanged(int rssi) {
                invokeRssiChanged(rssi);
            }

            @Override
            public void onStateChanged(PeripheralState oldState, PeripheralState newState) {
                invokeStateChanged(oldState, newState);
            }

            @Override public void onDisconnected(boolean fromHost) {
                invokeDisconnected(fromHost);
            }
        });
    }


    @Override
    protected void initialize(int timeout) throws BleException {

        String ServiceUuidDefault = "b2152466-d600-11e8-9f8b-f2801f1b9fd1";
        String DeviceIdCharacteristicUuid = "b2152466-d602-11e8-9f8b-f2801f1b9fd1";
        String BootloaderVersionCharacteristicUuid = "b2152466-d603-11e8-9f8b-f2801f1b9fd1";
        String BleStackVersionCharacteristicUuid = "b2152466-d604-11e8-9f8b-f2801f1b9fd1";
        String ApplicationVersionCharacteristicUuid = "b2152466-d605-11e8-9f8b-f2801f1b9fd1";
        String BleStackBuildIdCharacteristicUuid = "b2152466-d606-11e8-9f8b-f2801f1b9fd1";
        String ServiceUuid = ServiceUuidDefault;


        // check for the required BLE service
        Service productService = findService(ServiceUuid);
        if (productService == null) {
            Log.i(TAG, "FOTA service not found");
            throw new BleException("FOTA service not found", BleResult.Failure);
        }

        // get the required uuids
        Characteristic deviceIdCharacteristic = productService.getCharacteristic(DeviceIdCharacteristicUuid);
        Characteristic bootloaderVersionCharacteristic = productService.getCharacteristic(BootloaderVersionCharacteristicUuid);
        Characteristic bleStackVersionCharacteristic = productService.getCharacteristic(BleStackVersionCharacteristicUuid);
        Characteristic applicationVersionCharacteristic = productService.getCharacteristic(ApplicationVersionCharacteristicUuid);
        Characteristic bleStackBuildIdCharacteristic = productService.getCharacteristic(BleStackBuildIdCharacteristicUuid);

        // initial read
        try
        {
            if (deviceIdCharacteristic != null)
            {
                setDeviceId(deviceIdCharacteristic.readData());
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }

        try
        {
            if (bootloaderVersionCharacteristic != null)
            {
                setBootloaderVersion(new FotaFirmwareVersion(bootloaderVersionCharacteristic.readData()));
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }

        try
        {
            if (bleStackVersionCharacteristic != null)
            {
                setBleStackVersion( new FotaFirmwareVersion(bleStackVersionCharacteristic.readData()));
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }

        try
        {
            if (applicationVersionCharacteristic != null)
            {
                setApplicationVersion(new FotaFirmwareVersion(applicationVersionCharacteristic.readData()));
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }

        try
        {
            if (bleStackBuildIdCharacteristic != null)
            {
                setFotaBuildId(bleStackBuildIdCharacteristic.readData());
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }


    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void onStateChanged(PeripheralState oldState, PeripheralState newState) {
        super.onStateChanged(oldState, newState);
        // stop task when connection get lost
        if (newState == PeripheralState.Initialize) {
            // start task
        } else if ((oldState == PeripheralState.Initialize || oldState == PeripheralState.Ready) &&
                (newState != PeripheralState.Initialize && newState != PeripheralState.Ready)) {
        }
    }


    @Override
    public FotaState getFotaState() {
        return toFotaState(getState());
    }


    @Override
    public void addListener(FotaPeripheralListener listener) {
        synchronized (fotaListenerList) {
            fotaListenerList.add(listener);
        }
    }

    @Override
    public void removeListener(FotaPeripheralListener listener) {
        synchronized (fotaListenerList) {
            fotaListenerList.remove(listener);
        }
    }


    /**
     * Invoke the onRssiChanged callback
     * @param rssi The rssi value
     */
    private void invokeRssiChanged(int rssi) {
        List<FotaPeripheralListener> listCopy;
        synchronized (fotaListenerList) {
            listCopy = new LinkedList<>(fotaListenerList);
        }
        try {
            for (FotaPeripheralListener listener : listCopy) {
                listener.onRssiChanged(rssi);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    /**
     * Invoke the onStateChanged callback
     * @param oldState The previous state of the peripheral
     * @param newState The new state of the peripheral
     */
    private void invokeStateChanged(PeripheralState oldState, PeripheralState newState) {
        List<FotaPeripheralListener> listCopy;
        synchronized (fotaListenerList) {
            listCopy = new LinkedList<>(fotaListenerList);
        }
        try {
            for (FotaPeripheralListener listener : listCopy) {
                listener.onStateChanged(toFotaState(oldState), toFotaState(newState));
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private FotaState toFotaState(PeripheralState state) {
        switch (state) {

            case Idle:
                return FotaState.Idle;
            case EstablishLink:
                return FotaState.EstablishLink;
            case DiscoveringServices:
                return FotaState.DiscoveringServices;
            case Initialize:
                return FotaState.Initialize;
            case Update:
                return FotaState.Update;
            case Ready:
                return FotaState.Ready;
            case TearDownLink:
                return FotaState.TearDownLink;
        }
        return FotaState.Idle;
    }

    /**
     * Invoke the onDisconnected callback
     * @param fromHost True, if the disconnect was triggered from host, false otherwise
     */
    private void invokeDisconnected(boolean fromHost) {
        List<FotaPeripheralListener> listCopy;
        synchronized (fotaListenerList) {
            listCopy = new LinkedList<>(fotaListenerList);
        }
        try {
            for (FotaPeripheralListener listener : listCopy) {
                listener.onDisconnected(fromHost);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }
}
