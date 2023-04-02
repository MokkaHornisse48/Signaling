package mod.mh48.signaling.client;

import dev.onvoid.webrtc.RTCIceCandidate;
import mod.mh48.signaling.ConnectorInfo;
import mod.mh48.signaling.LogUtils;
import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientClient extends Client{

    public volatile List<ConnectorInfo> publicServerInfos = null;

    public String CSid;

    public Mode mode;

    public volatile P2PConnection p2pConnection;

    public Queue<AbstractMap.SimpleEntry<String,RTCIceCandidate>> candidateQueue = new ConcurrentLinkedQueue();

    public ClientClient(String pHost) {
        super(pHost);
    }


    @Override
    public void onConnected() {
        if(mode==Mode.getConnectors) {
            status = "Asking for Connectors";
            JSONObject m = new JSONObject();
            m.put("id","publicServers");
            WS.send(m.toString());
        }
        if(mode==Mode.createWEBRTC) {
            status = "Init P2P Connection";
            p2pConnection = new P2PConnection(ice -> {
                if(candidateQueue != null) {
                    candidateQueue.add(new AbstractMap.SimpleEntry<>(CSid, ice));
                }else{
                    sendCandidate(ice,CSid);
                }
            },this);
            p2pConnection.makeOffer().setOnFinished(offer -> {
                JSONObject m = new JSONObject();
                m.put("id","connect");
                m.put("cid",CSid);
                m.put("offer",offer);
                WS.send(m.toString());
                status = "Send Offer waiting fo Answer";
            });
        }
    }

    /*@Override
    public void onError(Channel channel, ErrorPacket error) {
        if(Packet.packets.get(error.cause) instanceof PacketWithClientId){
            if(error.code == 1){
                failed("Id does not exist");
            }
        }
    }*/


    public void onCCCandidate(RTCIceCandidate candidate){
        p2pConnection.addCandidate(candidate);
    }

    public void onGetPublicServersPacket(List<ConnectorInfo> pPublicServerInfos){
        publicServerInfos = pPublicServerInfos;
        WS.close();
        status = "Connectors recieved!";
    }

    public void onAnswer(String answer) {
        status = "Got Answer sharing ICE Candidates";
        p2pConnection.setAnswer(answer);


        AbstractMap.SimpleEntry<String, RTCIceCandidate> p = candidateQueue.poll();
        while (p!=null){
            sendCandidate(p.getValue(),p.getKey());
            p = candidateQueue.poll();
        }
        candidateQueue = null;
    }

    public static List<ConnectorInfo> getConnectors(String signalingHost) {
        ClientClient clientClient = new ClientClient(signalingHost);
        clientClient.mode = Mode.getConnectors;
        clientClient.connect();
        while (clientClient.publicServerInfos == null){}//todo non blocking
        return clientClient.publicServerInfos;
    }

    public P2PConnection makep2pConnection() {
        while(p2pConnection==null){}//WAIT until P2PConnection is usable
        while(!p2pConnection.isUsable()){}
        status = "P2P Connection ready disconnecting safely from Server";
        //todo reAdd addPacket(new FinishedPacket());
        WS.close();
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
