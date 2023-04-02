package mod.mh48.signaling.test;

import io.netty.buffer.ByteBuf;

public class Utils {
    public static String readString(ByteBuf buffer){
        int len = buffer.readInt();
        byte[] abyte = new byte[len];
        buffer.readBytes(abyte);
        return  new String(abyte);
    }

    public static void writeString(ByteBuf buffer,String str){
        byte[] abyte = str.getBytes();
        buffer.writeInt(abyte.length);
        buffer.writeBytes(abyte);
    }
}
