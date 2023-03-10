package mod.mh48.signaling.client;

import mod.mh48.signaling.packets.Packet;

public class MainCS {
    public static void main(String[] args) {
        System.out.println("Starting signaling ClientServer!");
        Packet.packets.isEmpty();
        new ClientServer("test",true).run();

    }
}