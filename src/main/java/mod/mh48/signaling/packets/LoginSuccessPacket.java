package mod.mh48.signaling.packets;

import io.netty.buffer.ByteBuf;
import mod.mh48.signaling.Utils;
import mod.mh48.signaling.client.ClientServer;

public class LoginSuccessPacket extends Packet{
    public static int id;

    public String connectorId;

    public LoginSuccessPacket(){}

    public LoginSuccessPacket(String pcid){
        this.connectorId = pcid;
    }

    @Override
    public Packet decode(ByteBuf pbuf) {
        String pConnectorId = Utils.readString(pbuf);
        return new LoginSuccessPacket(pConnectorId);
    }

    @Override
    public void encode(ByteBuf pbuf) {
        super.encode(pbuf);
        Utils.writeString(pbuf,connectorId);
    }

    @Override
    public void handle() {
        if(handler.getInstance() instanceof ClientServer clientServer){
            clientServer.onLoginSuccess(connectorId);
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
