package mod.mh48.signaling.packets;

import io.netty.buffer.ByteBuf;

public class PongPacket extends Packet {

    public static int id;

    @Override
    public Packet decode(ByteBuf pbuf) {
        return new PongPacket();
    }

    @Override
    public void encode(ByteBuf pbuf) {
        super.encode(pbuf);
    }

    @Override
    public void handle() {
        System.out.println("Pong");
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
        return Side.BOTH;
    }
}
