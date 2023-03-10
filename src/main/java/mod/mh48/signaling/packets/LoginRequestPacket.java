package mod.mh48.signaling.packets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import mod.mh48.signaling.Utils;
import mod.mh48.signaling.server.Connector;
import mod.mh48.signaling.server.Server;

public class LoginRequestPacket extends Packet{
    public static int id;

    public String serverName;

    public boolean isPublic;



    public LoginRequestPacket(){}

    public LoginRequestPacket(String pServerName, boolean pIsPublic){
        this.serverName = pServerName;
        this.isPublic = pIsPublic;
    }

    @Override
    public Packet decode(ByteBuf pbuf) {
        String pServerName = Utils.readString(pbuf);
        boolean pIsPublic = pbuf.readBoolean();
        return new LoginRequestPacket(pServerName,pIsPublic);
    }

    @Override
    public void encode(ByteBuf pbuf) {
        super.encode(pbuf);
        Utils.writeString(pbuf,serverName);
        pbuf.writeBoolean(isPublic);
    }

    @Override
    public void handle() {
        //System.out.println("Login");
        if(serverName.length()>20){
            ChannelFuture f = Utils.sendPacket(new ErrorPacket(id,0),ctx.channel());
            f.addListener((ChannelFutureListener) future -> {
                assert f == future;
                ctx.close();
            });
            return;
        }
        //UUID.randomUUID().
        if(handler.getInstance() instanceof Server server) {
            String id = server.newId(server.connectors);
            server.connectors.put(id, new Connector(ctx.channel(), serverName, isPublic));
            Utils.sendPacket(new LoginSuccessPacket(id), ctx.channel());
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
        return Side.SERVER;
    }
}
