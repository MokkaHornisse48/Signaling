package mod.mh48.signaling;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mod.mh48.signaling.packets.GetPublicServersPacket;
import mod.mh48.signaling.packets.Packet;

public class ConnectorInfo {
    public String id;
    public String name;
    public boolean isPublic;

    public ConnectorInfo(String pId,String pName,boolean pIsPublic){
        this.id = pId;
        this.name = pName;
        this.isPublic = pIsPublic;
    }

    public String toString(){
        return super.toString()+":(id="+id+",name="+name+",isPublic="+isPublic+")";
    }


    public static ConnectorInfo decode(ByteBuf pbuf) {
        String pId = Utils.readString(pbuf);
        String pName = Utils.readString(pbuf);
        boolean pIsPublic = pbuf.readBoolean();
        return new ConnectorInfo(pId,pName,pIsPublic);
    }


    public void encode(ByteBuf pbuf) {
        Utils.writeString(pbuf,id);
        Utils.writeString(pbuf,name);
        pbuf.writeBoolean(isPublic);
    }
}
