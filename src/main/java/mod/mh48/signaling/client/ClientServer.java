package mod.mh48.signaling.client;

import dev.onvoid.webrtc.RTCIceCandidate;
import io.netty.channel.local.LocalAddress;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ClientServer extends Client implements Runnable{
    public String serverName;
    public String id;
    public boolean isPublic;

    public Consumer<String> onId;

    public LocalAddress localAddress;

    public HashMap<String,P2PConnection> clients = new HashMap();

    public List<P2PConnection> connections = new LinkedList();
    public ClientServer(String pServerName, boolean pIsPublic,String signalingHost){
        super(signalingHost);
        this.serverName = pServerName;
        this.isPublic = pIsPublic;
    }

    public void run(){
        connect();
        while(WS.isOpen()){}
    }
    @Override
    public void onConnected() {
        status = "Loging in";
        JSONObject m = new JSONObject();
        m.put("id","login");
        m.put("name",serverName);
        m.put("isPublic",isPublic);

        WS.send(m.toString());
    }

    /*@Override
    public void onError(Channel channel, ErrorPacket error) {
        if(Packet.packets.get(error.cause) instanceof PacketWithClientId){
            if(clients.containsKey(error.info)){
                clients.remove(error.info);
            }
        }
        if(Packet.packets.get(error.cause) instanceof LoginRequestPacket){
            if(error.code == 0){
                failed("Server name to long");
            }
        }
    }*/


    public void onLoginSuccess(String pId){
        status = "Login Success accepting new Clients";
        id = pId;
        System.out.println("Id:"+id);
        if(onId!=null) {
            onId.accept(id);
        }
    }

    public void onCCConnect(String cid,String offer){
        System.out.println("Client id:"+cid);
        P2PConnection p2pConnection = new P2PConnection(ice -> {
            sendCandidate(ice,cid);
        },this);
        connections.add(p2pConnection);
        p2pConnection.localAddress = localAddress;
        clients.put(cid,p2pConnection);
        p2pConnection.makeAnswer(offer).setOnFinished(answer -> {
            JSONObject m = new JSONObject();
            m.put("id","forward");
            m.put("cid",cid);
            m.put("type","answer");

            JSONObject c = new JSONObject();
            c.put("answer",answer);

            m.put("content",c);
            WS.send(m.toString());
        });
    }

    public void onCSCandidate(RTCIceCandidate candidate, String cid){
        if(clients.containsKey(cid)) {
            clients.get(cid).addCandidate(candidate);
        }
    }
}
