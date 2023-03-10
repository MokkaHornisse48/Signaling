package mod.mh48.signaling.packets;

import dev.onvoid.webrtc.RTCIceCandidate;
import io.netty.buffer.ByteBuf;
import mod.mh48.signaling.Utils;
import mod.mh48.signaling.client.ClientClient;
import mod.mh48.signaling.server.Server;

public class Candidate2CCPacket extends Packet {

    public static int id;

    public RTCIceCandidate candidate;
    public String cid;

    public Candidate2CCPacket(){}

    public Candidate2CCPacket(String pcid, RTCIceCandidate pCandidate){
        candidate = pCandidate;
        cid = pcid;
    }

    @Override
    public Packet decode(ByteBuf pbuf) {
        String pcid = Utils.readString(pbuf);
        String sdpMid = Utils.readString(pbuf);
        int sdpMLineIndex = pbuf.readInt();
        String sdp = Utils.readString(pbuf);
        String serverUrl = Utils.readString(pbuf);
        if(serverUrl.length()==0){
            serverUrl = null;
        }
        RTCIceCandidate c = new RTCIceCandidate(sdpMid, sdpMLineIndex, sdp, serverUrl);
        return new Candidate2CCPacket(pcid,c);
    }

    @Override
    public void encode(ByteBuf pbuf) {
        super.encode(pbuf);
        Utils.writeString(pbuf,cid);
        Utils.writeString(pbuf,candidate.sdpMid);
        pbuf.writeInt(candidate.sdpMLineIndex);
        Utils.writeString(pbuf,candidate.sdp);
        String serverUrl = candidate.serverUrl;
        if(serverUrl==null){
            Utils.writeString(pbuf,"");
        }else {
            Utils.writeString(pbuf,serverUrl);
        }
    }

    @Override
    public void handle() {
        if(Utils.isSide(handler.getSide(),Side.SERVER)) {
            if(handler.getInstance() instanceof Server server){
                if(server.clientClients.containsKey(cid)){
                    Utils.sendPacket(this,server.clientClients.get(cid));
                }else {
                    System.out.println("Wrong id:"+cid);
                    Utils.sendPacket(new ErrorPacket(id,1),ctx.channel());
                }
            }
        }else{
            if(handler.instance instanceof ClientClient clientClient){
                clientClient.onCCCandidate(ctx.channel(),candidate);
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
