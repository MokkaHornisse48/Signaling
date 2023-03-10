package mod.mh48.signaling.client;

import dev.onvoid.webrtc.RTCIceCandidate;
import io.netty.channel.Channel;
import io.netty.channel.local.LocalAddress;
import mod.mh48.signaling.Utils;
import mod.mh48.signaling.packets.AnswerPacket;
import mod.mh48.signaling.packets.Candidate2CCPacket;
import mod.mh48.signaling.packets.LoginRequestPacket;

import java.util.HashMap;
import java.util.function.Consumer;

public class ClientServer extends Client implements Runnable{
    public String serverName;
    public String id;
    public boolean isPublic;

    public Consumer<String> onId;

    public LocalAddress localAddress;

    public HashMap<String,P2PConnection> clients = new HashMap();
    public ClientServer(String pServerName, boolean pIsPublic){
        this.serverName = pServerName;
        this.isPublic = pIsPublic;
    }

    public void run(){
        connect();
        while(channel.isActive()){
            sendAllPackets();
        }
        workerGroup.shutdownGracefully();
    }
    @Override
    public void onConnected(Channel channel) {
        addPacket(new LoginRequestPacket(serverName,isPublic));
        sendAllPackets();
    }

    public void onLoginSuccess(String pId){
        id = pId;
        System.out.println("Id:"+id);
        if(onId!=null) {
            onId.accept(id);
        }
    }

    public void onCCConnect(Channel channel,String cid,String offer){
        System.out.println("Client id:"+cid);
        P2PConnection p2pConnection = new P2PConnection(ice -> {
            addPacket(new Candidate2CCPacket(cid,ice));
        });
        p2pConnection.localAddress = localAddress;
        clients.put(cid,p2pConnection);
        p2pConnection.makeAnswer(offer).setOnFinished(answer -> {
            addPacket(new AnswerPacket(cid,answer));
        });
    }

    public void onCSCandidate(Channel channel, RTCIceCandidate candidate, String cid){
        if(clients.containsKey(cid)) {
            clients.get(cid).addCandidate(candidate);
        }
    }
}
