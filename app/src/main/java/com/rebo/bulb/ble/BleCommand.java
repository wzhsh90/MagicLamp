package com.rebo.bulb.ble;

import com.google.common.primitives.Bytes;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wzhsh90 on 2016/9/21.
 */

public class BleCommand {
    public static AtomicInteger atomicInteger = new AtomicInteger(0);

    public static byte[] getHead(int seq, int lns) {
        byte[] head = new byte[6];
        head[0] = 0x55;
        head[1] = getAtomicIntegerByte();
        head[2] = 0x03;
        head[3] = 0x01;
        head[4] = 0x02;
        return head;
    }

    public static byte getAtomicIntegerByte() {
        atomicInteger.getAndIncrement();
        int val=atomicInteger.get();
        int realVal=val%256;
        if(val>=256){
            atomicInteger.set(256);
        }
        return Integer.valueOf(realVal).byteValue();
    }

    public static int getCurrentSeq(byte[] data){
        return data[2];
    }
    public static void setCurrentSeq(byte[] data,int seq){
         data[2]=Integer.valueOf(seq).byteValue();
    }
    private static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        //由高位到低位
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }
    public static byte[] lockOrUnlockBody(boolean lock) {
        return lockOrUnlockBody(lock,100);
    }

    public static byte[] lockOrUnlockBody(boolean lock,int Intensity) {
        byte[] body = new byte[5];
        body[0] = 0x04;
        if (!lock) {
            body[1] = 0x01;
        } else {
            body[1] = 0x00;
        }
        body[2] = 0x64;
        body[4] = 0x0A;
        return body;
    }

    public static byte[] colorBody(byte[] color,int Intensity) {
        byte[] body = new byte[7];
        body[0] = 0x02;
        body[1] = color[0];
        body[2] = color[1];
        body[3] = color[2];
        body[4] = (byte)(Intensity & 0xFF);
        body[6] = 0x0A;
        return  body;
    }
    public static byte[] musicStartBody(byte crestNum) {
        byte[] body = new byte[5];
        body[0] = 0x03;
        body[1] = 0x01;
        body[2] = crestNum;
        body[3] = 0x0A;
        return  body;
    }
    public static byte[] musicEndBody(byte crestNum) {
        byte[] body = new byte[5];
        body[0] = 0x03;
        body[1] = 0x00;
        body[2] = crestNum;
        body[4] = 0x0A;
        return  body;
    }
    public static byte[] getAllData(byte[] head,byte[] body){
        int headlen=head.length-1;
        int bodyLen=body.length-2;
        head[headlen]=(byte)body.length;
        body[bodyLen]= (byte)(head[0]^body[bodyLen-1]);
        return Bytes.concat(head,body);
    }
}
