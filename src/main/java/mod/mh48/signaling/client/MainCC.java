package mod.mh48.signaling.client;

import mod.mh48.signaling.packets.Packet;

import java.util.Base64;
import java.util.Scanner;

public class MainCC {
    public static void main(String[] args) {
        System.out.println("Starting signaling ClientClient!");
        System.out.println(ClientClient.getConnectors());
        Packet.packets.isEmpty();
        System.out.println("id:");
        Scanner my_scan = new Scanner(System.in);
        String id = my_scan.nextLine();
        ClientClient.createWEBRTC(id);

    }
}