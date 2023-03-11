package mod.mh48.signaling.packets;

import io.netty.buffer.ByteBuf;
import mod.mh48.signaling.LogUtils;
import mod.mh48.signaling.Utils;
import mod.mh48.signaling.client.Client;

public class ErrorPacket extends Packet{
    public static int id;

    public int code;//0 = name to long //1 = id not found

    public int cause;//Packet id

    public String info;

    public ErrorPacket(){}

    public ErrorPacket(int pcause,int pcode,String info){
        this.code = pcode;
        this.cause = pcause;
        this.info = info;
    }

    public ErrorPacket(int pcause,int pcode){
        this.code = pcode;
        this.cause = pcause;
    }

    @Override
    public Packet decode(ByteBuf pbuf) {
        int pCause = pbuf.readInt();
        int pCode = pbuf.readInt();
        if(pbuf.readBoolean()){
            info = Utils.readString(pbuf);
        }
        return new ErrorPacket(pCause,pCode);
    }

    @Override
    public void encode(ByteBuf pbuf) {
        super.encode(pbuf);
        pbuf.writeInt(cause);
        pbuf.writeInt(code);
        pbuf.writeBoolean(info!=null);
        if(info!=null){
            Utils.writeString(pbuf,info);
        }
    }

    @Override
    public void handle() {
        LogUtils.error( "Packet="+Packet.packets.get(cause)+" with Errorcode="+code);
        if(this.handler.getInstance() instanceof Client client){
            client.onError(ctx.channel(), this);
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
