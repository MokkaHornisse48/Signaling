package mod.mh48.signaling.client;

import dev.onvoid.webrtc.RTCIceCandidate;
import io.netty.channel.Channel;
import mod.mh48.signaling.ConnectorInfo;
import mod.mh48.signaling.packets.*;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientClient extends Client{

    public List<ConnectorInfo> publicServerInfos = Collections.emptyList();

    public String CSid;

    public Mode mode;

    public P2PConnection p2pConnection;

    public Queue<Packet> candidateQueue = new ConcurrentLinkedQueue();

    public ClientClient(String pHost) {
        super(pHost);
    }


    @Override
    public void onConnected(Channel channel) {
        if(mode==Mode.getConnectors) {
            status = "Asking for Connectors";
            addPacket(new GetPublicServersPacket());
        }
        if(mode==Mode.createWEBRTC) {
            status = "Init P2P Connection";
            p2pConnection = new P2PConnection(ice -> {
                if(candidateQueue != null) {
                    candidateQueue.add(new Candidate2CSPacket(CSid, ice));
                }else{
                    addPacket(new Candidate2CSPacket(CSid, ice));
                }
            },this);
            p2pConnection.makeOffer().setOnFinished(offer -> {
                addPacket(new ConnectPacket(CSid,offer));
                status = "Send Offer waiting fo Answer";
            });
        }
        sendAllPackets();
    }

    @Override
    public void onError(Channel channel, ErrorPacket error) {
        if(Packet.packets.get(error.cause) instanceof PacketWithClientId){
            if(error.code == 1){
                failed("Id does not exist");
            }
        }
    }


    public void onCCCandidate(Channel channel, RTCIceCandidate candidate){
        p2pConnection.addCandidate(candidate);
    }

    public void onGetPublicServersPacket(Channel channel,List<ConnectorInfo> pPublicServerInfos){
        publicServerInfos = pPublicServerInfos;
        channel.close();
        status = "Connectors recieved!";
    }

    public void onAnswer(Channel channel, String answer) {
        status = "Got Answer sharing ICE Candidates";
        p2pConnection.setAnswer(answer);
        Packet p = candidateQueue.poll();
        while (p!=null){
            addPacket(p);
            p = candidateQueue.poll();
        }
        candidateQueue = null;
    }

    public static List<ConnectorInfo> getConnectors(String signalingHost) {
        ClientClient clientClient = new ClientClient(signalingHost);
        clientClient.mode = Mode.getConnectors;
        clientClient.connect();
        try {
            clientClient.channel.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        clientClient.workerGroup.shutdownGracefully();
        return clientClient.publicServerInfos;
    }

    public P2PConnection makep2pConnection() {
        while(p2pConnection==null){
            sendAllPackets();
        }
        while(!p2pConnection.isUsable()){
            sendAllPackets();
        }
        status = "P2P Connection ready disconnecting safely from Server";
        addPacket(new FinishedPacket());
        sendAllPackets();
        try {
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        status = "Finished!";
        return p2pConnection;
    }

    public static P2PConnection createWEBRTC(String CSid,String signalingHost){
        ClientClient clientClient = new ClientClient(signalingHost);
        clientClient.CSid = CSid;
        clientClient.mode = Mode.createWEBRTC;
        clientClient.connect();
        System.out.println("test");
        return clientClient.makep2pConnection();
    }



    public static enum Mode{
        getConnectors,
        createWEBRTC
    }
}
