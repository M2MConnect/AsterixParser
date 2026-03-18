package backend.infrastructure.network;

import backend.domain.ports.UdpServerPort;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class DatagramUdpServerAdapter implements UdpServerPort {

    @Override
    public void start(int port) {
        byte[] buffer = new byte[1024];

        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            System.out.println("UDP-Server gestartet auf Port " + port);

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(receivePacket);

                String message = new String(
                        receivePacket.getData(),
                        0,
                        receivePacket.getLength(),
                        StandardCharsets.UTF_8
                );

                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                System.out.println("Empfangen von " + clientAddress + ":" + clientPort + " -> " + message);

                String response = "Server ACK: " + message;
                byte[] responseData = response.getBytes(StandardCharsets.UTF_8);
                DatagramPacket responsePacket = new DatagramPacket(
                        responseData,
                        responseData.length,
                        clientAddress,
                        clientPort
                );
                serverSocket.send(responsePacket);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Server-Fehler: " + exception.getMessage(), exception);
        }
    }
}
