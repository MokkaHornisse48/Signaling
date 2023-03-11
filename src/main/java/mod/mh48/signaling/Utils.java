package mod.mh48.signaling;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import mod.mh48.signaling.packets.Side;
import mod.mh48.signaling.packets.Packet;
import mod.mh48.signaling.server.Server;

import java.util.Map;
import java.util.Objects;

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

    public static ChannelFuture sendPacket(Packet packet, Channel ctx){
        if(ctx.isActive()) {
            System.out.println("Debug Send:" + packet);
            ByteBuf buf = ctx.alloc().buffer();
            packet.encode(buf);
            return ctx.writeAndFlush(buf);
        }
        if(packet.handler.getInstance()instanceof Server server){
            server.updateClients();
        }
        return null;
    }

    public static boolean isSide(Side handlerSide, Side packetSide){
        if(packetSide == Side.BOTH)return true;
        return handlerSide == packetSide;
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}
