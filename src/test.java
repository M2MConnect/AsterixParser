import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class test {

    public static void runServer(int port) {
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
        } catch (IOException e) {
            System.err.println("Server-Fehler: " + e.getMessage());
        }
    }

    public static void runClient(String host, int port, String message) {
        try (DatagramSocket clientSocket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(host);
            byte[] sendData = message.getBytes(StandardCharsets.UTF_8);
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
            System.out.println("Antwort vom Server: " + response);
        } catch (IOException e) {
            System.err.println("Client-Fehler: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Nutzung:");
            System.out.println("  Server starten: java test server <port>");
            System.out.println("  Client starten: java test client <host> <port> <nachricht>");
            return;
        }

        String mode = args[0].toLowerCase();
        if ("server".equals(mode)) {
            int port = (args.length >= 2) ? Integer.parseInt(args[1]) : 5000;
            runServer(port);
        } else if ("client".equals(mode)) {
            if (args.length < 4) {
                System.out.println("Fehlende Parameter für Client. Erwartet: client <host> <port> <nachricht>");
                return;
            }

            String host = args[1];
            int port = Integer.parseInt(args[2]);
            StringBuilder messageBuilder = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                if (i > 3) {
                    messageBuilder.append(' ');
                }
                messageBuilder.append(args[i]);
            }
            runClient(host, port, messageBuilder.toString());
        } else {
            System.out.println("Unbekannter Modus: " + mode + " (erlaubt: server | client)");
        }
    }
}
