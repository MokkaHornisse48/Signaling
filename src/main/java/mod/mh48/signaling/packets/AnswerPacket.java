package mod.mh48.signaling.packets;

import io.netty.buffer.ByteBuf;
import mod.mh48.signaling.Utils;
import mod.mh48.signaling.client.ClientClient;
import mod.mh48.signaling.server.Server;

public class AnswerPacket extends Packet {

    public static int id;

    public String answer;
    public String cid;

    public AnswerPacket(){}

    public AnswerPacket(String pcid,String pAnswer){
        cid = pcid;
        answer = pAnswer;
    }

    @Override
    public Packet decode(ByteBuf pbuf) {
        String pcid = Utils.readString(pbuf);
        String pAnswer = Utils.readString(pbuf);
        return new AnswerPacket(pcid,pAnswer);
    }

    @Override
    public void encode(ByteBuf pbuf) {
        super.encode(pbuf);
        Utils.writeString(pbuf,cid);
        Utils.writeString(pbuf,answer);
    }

    @Override
    public void handle() {
        if(Utils.isSide(handler.getSide(),Side.SERVER)) {
            if(handler.getInstance() instanceof Server server){
                if(server.clientClients.containsKey(cid)){
                    Utils.sendPacket(this,server.clientClients.get(cid));
                }else {
                    Utils.sendPacket(new ErrorPacket(id,1),ctx.channel());
                }
            }
        }else{
            if(handler.instance instanceof ClientClient clientClient){
                clientClient.onAnswer(ctx.channel(),answer);
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
