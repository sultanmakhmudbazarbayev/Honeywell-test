package com.onsemi.fota;

/**
 * All possible state of the fota peripheral
 */

public enum FotaState {
    /**
     * Peripheral is not used.
     */
    Idle,

    /**
     * Attempt to establish a link to the peripheral.
     */
    EstablishLink,

    /**
     * Peripheral is connected and the services are going to be discovered.
     */
    DiscoveringServices,

    /**
     * The initial checks and readings from the peripherals are done now.
     */
    Initialize,

    /**
     * Firmware update is in progress.
     */
    Update,

    /**
     * The peripheral is ready to be used.
     */
    Ready,

    /**
     * Tear down the peripheral link.
     */
    TearDownLink,
}
