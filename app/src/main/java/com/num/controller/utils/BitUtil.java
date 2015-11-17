package com.num.controller.utils;

/**
 * Created by Andrew on 11/16/15.
 */
public class BitUtil {
    public static short getUnsignedByte(byte value)
    {
        return (short)(value & 0xFF);
    }

    public static int getUnsignedShort(short value)
    {
        return value & 0xFFFF;
    }

    public static long getUnsignedInt(int value)
    {
        return value & 0xFFFFFFFFL;
    }
}
