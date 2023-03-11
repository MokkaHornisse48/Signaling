package mod.mh48.signaling.packets;

import io.netty.buffer.ByteBuf;
import mod.mh48.signaling.ConnectorInfo;
import mod.mh48.signaling.Utils;
import mod.mh48.signaling.client.ClientServer;
import mod.mh48.signaling.server.Connector;
import mod.mh48.signaling.server.Server;

import java.util.Map;

public class FinishedPacket extends Packet{
    public static int id;

    public String cId;

    public FinishedPacket(){}

    public FinishedPacket(String cId){
        this.cId = cId;
    }

    @Override
    public Packet decode(ByteBuf pbuf) {
        if(pbuf.readBoolean()){
            String cId = Utils.readString(pbuf);
            return new FinishedPacket(cId);
        }
        return new FinishedPacket();
    }

    @Override
    public void encode(ByteBuf pbuf) {
        super.encode(pbuf);
        pbuf.writeBoolean(cId!=null);
        if(cId!=null){
            Utils.writeString(pbuf,cId);
        }
    }

    @Override
    public void handle() {
        if(handler.getInstance() instanceof Server server){
            String cid = Utils.getKeyByValue(server.clientClients,ctx.channel());
            if(server.clientClients.containsKey(cid)) {
                //Utils.sendPacket(new FinishedPacket(),server.clientClients.get(cid));
                server.clientClients.remove(cid);
                for (Map.Entry<String, Connector> set : server.connectors.entrySet()) {
                    Utils.sendPacket(new FinishedPacket(cid),set.getValue().channel);//todo mach das sicher ):
                }
            }
            ctx.channel().close();
        }
        if(handler.getInstance() instanceof ClientServer clientServer){
            if(clientServer.connections.contains(cId)){
                clientServer.connections.remove(cId);
            }
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
        return Side.BOTH;
    }
}
