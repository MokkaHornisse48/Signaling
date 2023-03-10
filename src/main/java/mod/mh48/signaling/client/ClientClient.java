package mod.mh48.signaling.client;

import dev.onvoid.webrtc.RTCIceCandidate;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import mod.mh48.p2pNetty.nettyservertestlol;
import mod.mh48.signaling.ConnectorInfo;
import mod.mh48.signaling.Utils;
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

    public ClientClient(){
    }

    @Override
    public void onConnected(Channel channel) {
        if(mode==Mode.getConnectors) {
            addPacket(new GetPublicServersPacket());
        }
        if(mode==Mode.createWEBRTC) {
            p2pConnection = new P2PConnection(ice -> {
                if(candidateQueue != null) {
                    candidateQueue.add(new Candidate2CSPacket(CSid, ice));
                }else{
                    addPacket(new Candidate2CSPacket(CSid, ice));
                }
            });
            p2pConnection.makeOffer().setOnFinished(offer -> {
                addPacket(new ConnectPacket(CSid,offer));
            });
        }
        sendAllPackets();
    }

    public void onCCCandidate(Channel channel, RTCIceCandidate candidate){
        p2pConnection.addCandidate(candidate);
    }

    public void onGetPublicServersPacket(Channel channel,List<ConnectorInfo> pPublicServerInfos){
        publicServerInfos = pPublicServerInfos;
        channel.close();
    }

    public void onAnswer(Channel channel, String answer) {
        p2pConnection.setAnswer(answer);
        Packet p = candidateQueue.poll();
        while (p!=null){
            addPacket(p);
            p = candidateQueue.poll();
        }
        candidateQueue = null;
    }

    public static List<ConnectorInfo> getConnectors() {
        ClientClient clientClient = new ClientClient();
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
        addPacket(new FinishedPacket());
        sendAllPackets();
        try {
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return p2pConnection;
    }

    public static P2PConnection createWEBRTC(String CSid){
        ClientClient clientClient = new ClientClient();
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
