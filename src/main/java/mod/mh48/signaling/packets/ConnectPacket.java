package mod.mh48.signaling.packets;

import io.netty.buffer.ByteBuf;
import mod.mh48.signaling.Utils;
import mod.mh48.signaling.client.Client;
import mod.mh48.signaling.client.ClientServer;
import mod.mh48.signaling.server.Connector;
import mod.mh48.signaling.server.Server;

public class ConnectPacket extends Packet implements PacketWithClientId{

    public static int id;

    public String connectorId;
    public String offer;
    public ConnectPacket(){}

    public ConnectPacket(String pConnectorId,String pOffer){
        connectorId = pConnectorId;
        offer = pOffer;
    }
    @Override
    public Packet decode(ByteBuf pbuf) {
        String pConnectorId = Utils.readString(pbuf);
        String pOffer = Utils.readString(pbuf);
        return new ConnectPacket(pConnectorId,pOffer);
    }

    @Override
    public void encode(ByteBuf pbuf) {
        super.encode(pbuf);
        Utils.writeString(pbuf,connectorId);
        Utils.writeString(pbuf,offer);
    }

    @Override
    public void handle() {
        if(Utils.isSide(handler.getSide(),Side.SERVER)) {
            if (handler.getInstance() instanceof Server server) {
                if (server.connectors.containsKey(connectorId)) {
                    Connector connector = server.connectors.get(connectorId);
                    String id = server.newId(server.clientClients);
                    server.clientClients.put(id,ctx.channel());
                    connectorId = id;
                    Utils.sendPacket(this, connector.channel);
                } else {
                    Utils.sendPacket(new ErrorPacket(id,1,connectorId), ctx.channel());
                }
            }
        }else {
            if (handler.getInstance() instanceof ClientServer clientServer) {
                clientServer.onCCConnect(ctx.channel(),connectorId,offer);
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
