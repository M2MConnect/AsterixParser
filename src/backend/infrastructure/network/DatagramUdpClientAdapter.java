package backend.infrastructure.network;

import backend.domain.model.Message;
import backend.domain.model.ServerResponse;
import backend.domain.ports.UdpClientPort;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class DatagramUdpClientAdapter implements UdpClientPort {

    @Override
    public ServerResponse send(String host, int port, Message message) {
        try (DatagramSocket clientSocket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(host);
            byte[] sendData = message.value().getBytes(StandardCharsets.UTF_8);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
            clientSocket.send(sendPacket);

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            clientSocket.setSoTimeout(3000);
            clientSocket.receive(responsePacket);

            String response = new String(
                    responsePacket.getData(),
                    0,
                    responsePacket.getLength(),
                    StandardCharsets.UTF_8
            );

            return new ServerResponse(response);
        } catch (IOException exception) {
            throw new IllegalStateException("Client-Fehler: " + exception.getMessage(), exception);
        }
    }
}
