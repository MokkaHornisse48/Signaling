package mod.mh48.signaling.packets;

import io.netty.buffer.ByteBuf;
import mod.mh48.signaling.LogUtils;
import mod.mh48.signaling.client.Client;

public class PongPacket extends Packet {

    public static int id;

    public int pv;

    public PongPacket(){}

    public PongPacket(int pv){
        this.pv = pv;
    }

    @Override
    public Packet decode(ByteBuf pbuf) {
        int pv = pbuf.readInt();
        return new PongPacket(pv);
    }

    @Override
    public void encode(ByteBuf pbuf) {
        super.encode(pbuf);
        pbuf.writeInt(pv);
    }

    @Override
    public void handle() {
        if(pv!=Packet.protocolVersion){
            LogUtils.fatal("Signaling protocol version wrong please update or contact the author");
            ctx.disconnect();
            return;
        }
        if(this.handler.getInstance() instanceof Client client){
            client.onConnected(ctx.channel());
            LogUtils.info("Connected successful to ("+ctx.channel().remoteAddress()+") with ("+ctx.channel().localAddress()+")");
        }
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
        return Side.CLIENT;
    }
}
