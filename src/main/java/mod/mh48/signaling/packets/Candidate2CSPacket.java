package mod.mh48.signaling.packets;

import dev.onvoid.webrtc.RTCIceCandidate;
import io.netty.buffer.ByteBuf;
import mod.mh48.signaling.Utils;
import mod.mh48.signaling.client.ClientServer;
import mod.mh48.signaling.server.Server;

public class Candidate2CSPacket extends Packet implements PacketWithClientId{

    public static int id;

    public RTCIceCandidate candidate;
    public String cid;

    public Candidate2CSPacket(){}

    public Candidate2CSPacket(String pcid, RTCIceCandidate pCandidate){
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
        return new Candidate2CSPacket(pcid,c);
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
                if(server.connectors.containsKey(cid)){
                    String ckey = Utils.getKeyByValue(server.clientClients,ctx.channel());
                    if(ckey != null) {
                        String tcid = cid;
                        cid = ckey;
                        Utils.sendPacket(this, server.connectors.get(tcid).channel);
                    }else {
                        Utils.sendPacket(new ErrorPacket(id,1,cid),ctx.channel());
                    }
                }
            }
        }else {
            if(handler.getInstance() instanceof ClientServer clientServer){
                clientServer.onCSCandidate(ctx.channel(),candidate,cid);
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
