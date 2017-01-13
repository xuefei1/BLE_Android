package com.bleblind.components;

import java.util.HashMap;

/**
 * Created by Fred on 2017/1/9.
 */
public class Profile {

    public static final int SUPPORTED_POSITIONS= 5;

    public static final String BLE_MODULE_FILTER = "BLE mini";

    private static final HashMap<String, String> attributes = new HashMap<>();
    public static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static final String BLE_SHIELD_TX = "713d0003-503e-4c75-ba94-3148f18d941e";
    public static final String BLE_SHIELD_RX = "713d0002-503e-4c75-ba94-3148f18d941e";
    public static final String BLE_SHIELD_SERVICE = "713d0000-503e-4c75-ba94-3148f18d941e";

    public static final byte CMD_TYPE_GET = 0x02;
    public static final byte CMD_TYPE_SET = 0x01;

    public static final byte CMD_SET_LEVEL = 0x01;

    static {
        // RBL Services.
        attributes.put("713d0000-503e-4c75-ba94-3148f18d941e",
                "BLE Shield Service");
        // RBL Characteristics.
        attributes.put(BLE_SHIELD_TX, "BLE Shield TX");
        attributes.put(BLE_SHIELD_RX, "BLE Shield RX");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

}
