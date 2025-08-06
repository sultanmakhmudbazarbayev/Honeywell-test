package com.honeywell.bccrfid;

public class Const {
    //for writeTag,for addition data settings
    public static final int RESERVED_MAX_BLOCK = 4;
    public static final int EPC_MAX_BLOCK = 8;
    public static final int TID_MAX_BLOCK = 12;
    public static final int USER_MAX_BLOCK = 32;

    public static final int RESERVED_DEF_START_ADDR = 2;
    public static final int EPC_DEF_START_ADDR = 2;
    public static final int TID_DEF_START_ADDR = 0;
    public static final int USER_DEF_START_ADDR = 0;


    public static final String DEF_SINGLE_INVENTORY_DURATION = "200";
    public static final String DEF_SINGLE_INVENTORY_VACANCY = "0";
    public static final boolean DEF_SCAN_SOUND_STATE = true;
    public static final String SCAN_MODE_NORMAL = "0";
    public static final String SCAN_MODE_FAST = "1";

    public static final String MATERIAL_DESIGN_UI = "material_design";
    public static final String HONEYWELL_DESIGN_UI = "honeywell_ui";


    public static final String SP_FIRST_INIT = "first_init";
    public static final String SP_KEY_AUTO_CONNECT = "auto_connect";
    public static final String SP_KEY_SINGLE_READ_DURATION = "single_read_duration";
    public static final String SP_KEY_SINGLE_READ_VACANCY = "single_read_vacancy";
    public static final String SP_KEY_SCAN_MODE = "scan_mode";
    public static final String SP_KEY_PDA_SCAN_SOUND = "sound_switch_pda";
    public static final String SP_KEY_RFID_SCAN_SOUND = "sound_switch_rfid";
    public static final String SP_KEY_PAUSE_PERCENTAGE = "pause_percentage";
    public static final String SP_KEY_ADDITION_TAG_DATA_TYPE = "settings_addtion_type";

    public static final String SP_KEY_ITEM_COUNT = "item_count";
    public static final String SP_KEY_ITEM_RSSI = "item_rssi";
    public static final String SP_KEY_ITEM_ANT = "item_ant";
    public static final String SP_KEY_ITEM_FREQ = "item_freq";
    public static final String SP_KEY_ITEM_TIME = "item_time";
    public static final String SP_KEY_ITEM_RFU = "item_rfu";
    public static final String SP_KEY_ITEM_PRO = "item_pro";
    public static final String SP_KEY_ITEM_DATA = "item_data";
}
