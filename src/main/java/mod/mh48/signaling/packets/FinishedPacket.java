package mod.mh48.signaling.packets;

import io.netty.buffer.ByteBuf;
import mod.mh48.signaling.Utils;
import mod.mh48.signaling.server.Server;

public class FinishedPacket extends Packet{
    public static int id;


    public FinishedPacket(){}

    @Override
    public Packet decode(ByteBuf pbuf) {
        return new FinishedPacket();
    }

    @Override
    public void encode(ByteBuf pbuf) {
        super.encode(pbuf);
    }

    @Override
    public void handle() {
        if(handler.getInstance() instanceof Server server){
            String cid = Utils.getKeyByValue(server.clientClients,ctx.channel());
            if(server.clientClients.containsKey(cid)) {
                server.clientClients.remove(cid);
            }
        }
        ctx.channel().close();
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
