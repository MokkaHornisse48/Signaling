package mod.mh48.signaling.packets;

import io.netty.buffer.ByteBuf;
import mod.mh48.signaling.LogUtils;
import mod.mh48.signaling.Utils;

import java.net.InetSocketAddress;

public class PingPacket extends Packet{

    public static int id;

    @Override
    public Packet decode(ByteBuf pbuf) {
        return new PingPacket();
    }

    @Override
    public void encode(ByteBuf pbuf) {
        super.encode(pbuf);
    }

    @Override
    public void handle() {
        if(ctx.channel().remoteAddress() instanceof InetSocketAddress) {
            InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
            LogUtils.info("Ping from: "+address.getHostName()+":"+address.getPort());
        }

        Utils.sendPacket(new PongPacket(Packet.protocolVersion),ctx.channel());
    }

    @Override
    public void setId(int i) {
        this.id = i;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }
}
