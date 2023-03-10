package mod.mh48.signaling.packets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import mod.mh48.signaling.ConnectorInfo;
import mod.mh48.signaling.Utils;
import mod.mh48.signaling.client.Client;
import mod.mh48.signaling.client.ClientClient;
import mod.mh48.signaling.server.Connector;
import mod.mh48.signaling.server.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetPublicServersPacket extends Packet{
    public static int id;

    public List<ConnectorInfo> infos;

    public GetPublicServersPacket(){
        infos = Collections.emptyList();
    }

    public GetPublicServersPacket(List<ConnectorInfo> pinfos){
        infos = pinfos;
    }

    @Override
    public Packet decode(ByteBuf pbuf) {
        int s = pbuf.readInt();
        List<ConnectorInfo> inf = new ArrayList<>(s);
        for(int i = 0;i<s;i++){
            inf.add(ConnectorInfo.decode(pbuf));
        }
        return new GetPublicServersPacket(inf);
    }

    @Override
    public void encode(ByteBuf pbuf) {
        super.encode(pbuf);
        pbuf.writeInt(infos.size());
        for(ConnectorInfo info:infos){
            info.encode(pbuf);
        }
    }

    @Override
    public void handle() {
        if(Utils.isSide(handler.getSide(),Side.SERVER)){
            if(this.handler.getInstance() instanceof Server server) {
                infos = server.getPublicConnectorInfos();
                Utils.sendPacket(this, ctx.channel());
            }
        }else{
            if(this.handler.getInstance() instanceof ClientClient client) {
                client.onGetPublicServersPacket(ctx.channel(), infos);
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
