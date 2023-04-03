package mod.mh48.signaling.client;

import dev.onvoid.webrtc.RTCIceCandidate;
import mod.mh48.signaling.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public abstract class Client {

    public static int protocolVersion = 2;
    public String status;

    public boolean failed = false;

    public WebSocketClient WS;

    String host = "127.0.0.1";

    public Client(String pHost){
        host = pHost;
        status = "Initialized";
    }

    public void connect() {
        URI uri;
        try {
            uri = new URI("ws://"+host+":27776");
        }catch (URISyntaxException e){
            failed("URISyntaxException:"+e.getMessage());
            return;
        }
        WS = new WebSocketClient(uri)
        {
            @Override
            public void onMessage( String message ) {
                JSONObject c = new JSONObject(message);
                String id = c.getString("id");
                if(id.equals("pong")){
                    int pv = c.getInt("pv");
                    if(protocolVersion==pv){
                        preOnConnected();
                    }else{
                        LogUtils.fatal("Signaling protocol version wrong please update or contact the author client:"+protocolVersion+" server:"+pv);
                    }
                }
                if(id.equals("login")){
                    if(Client.this instanceof ClientServer clientServer){
                        clientServer.onLoginSuccess(c.getString("cid"));
                    }
                }
                if(id.equals("connect")){
                    if(Client.this instanceof ClientServer clientServer){
                        clientServer.onCCConnect(c.getString("cid"),c.getString("offer"));
                    }
                }
                if(id.equals("forward")){
                    String type = c.getString("type");
                    JSONObject c2 = c.getJSONObject("content");
                    if(type.equals("answer")){
                        if(Client.this instanceof ClientClient clientClient){
                            clientClient.onAnswer(c2.getString("answer"));
                        }
                    }
                    if(type.equals("candidate")){
                        String sdpMid = c2.getString("sdpMin");
                        int sdpMLineIndex = c2.getInt("sdpMLineIndex");
                        String sdp = c2.getString("sdp");
                        String serverUrl = c2.optString("serverUrl","");
                        RTCIceCandidate candidate = new RTCIceCandidate(sdpMid, sdpMLineIndex, sdp, serverUrl);
                        System.out.println("Recieved candidate:"+sdp);
                        if(Client.this instanceof ClientClient clientClient){
                            clientClient.onCCCandidate(candidate);
                        }
                        if(Client.this instanceof ClientServer clientServer){
                            clientServer.onCSCandidate(candidate,c.getString("cid"));
                        }
                    }
                }
                if(id.equals("publicServers")){
                    if(Client.this instanceof ClientClient clientClient){
                        JSONArray pcs = c.getJSONArray("pcs");
                        List<ConnectorInfo> inf = new ArrayList<>(pcs.length());
                        for(int i = 0;i<pcs.length();i++){
                            JSONObject ci = pcs.getJSONObject(i);
                            inf.add(new ConnectorInfo(ci.getString("id"),ci.getString("name")));
                        }
                        clientClient.onGetPublicServersPacket(inf);
                    }
                }
            }

            @Override
            public void onOpen( ServerHandshake handshake ) {
                LogUtils.info("opened websocket connection");
                JSONObject m = new JSONObject();
                m.put("id","ping");
                WS.send(m.toString());
            }

            @Override
            public void onClose( int code, String reason, boolean remote ) {
                LogUtils.info("closed websocket connection");
            }

            @Override
            public void onError( Exception ex ) {
                LogUtils.error("Exception in Websocket:"+ex);
            }

        };
        WS.connect();
    }

    public void sendCandidate(RTCIceCandidate candidate,String cid){
        JSONObject m = new JSONObject();
        m.put("id","forward");
        m.put("cid",cid);
        m.put("type","candidate");

        JSONObject c = new JSONObject();
        c.put("sdpMin",candidate.sdpMid);
        c.put("sdpMLineIndex",candidate.sdpMLineIndex);
        c.put("sdp",candidate.sdp);
        c.put("serverUrl",candidate.serverUrl);

        m.put("content",c);

        WS.send(m.toString());
    }

    public void failed(String s){
        LogUtils.fatal(s);
        failed = true;
        status = s;
    }

    public String getStatus(){
        return status;
    }
    public void preOnConnected(){
        status = "Connected";
        this.onConnected();
    }

    public abstract void onConnected();

    //todo public abstract void onError(Channel channel, ErrorPacket error);
}
