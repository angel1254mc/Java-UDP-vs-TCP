package UDP.client;
import java.io.*;
import java.net.*;
import java.nio.file.*;

public class ClientUDP {

    DatagramSocket clientSocket;
    DatagramPacket toServerPacket;
    DatagramPacket fromServerPacket;
    ByteArrayOutputStream imageDataStream;
    byte[] toServerData;
    byte[] fromServerData;

    public ClientUDP(String IP, int port) throws IOException {
        // Start up the client socket, the data to send to the server, and the packet we are sending it in
        clientSocket = new DatagramSocket();
        toServerData = (new String("Awesome")).getBytes();
        toServerPacket = new DatagramPacket(toServerData, toServerData.length, InetAddress.getByName(IP), port);
        fromServerData = new byte[1024];
        fromServerPacket =  new DatagramPacket(fromServerData, 1024);
        imageDataStream = new ByteArrayOutputStream();
        // Now shoot that bad boy and see if the server gets it
        clientSocket.send(toServerPacket);

        // Assuming the server got it, now we can start receiving image data
        for (int i = 1; i < 11; i++) {
            // This is kinda weird but I think the best way to figure out whether the image has finished being loaded
            // is by waiting for a message from the server that says "end"
            while (true) {
                // receive from the server
                // until the server sends us an "END" packet, we keep going
                // Least Confusing Java Code in the world be like: 
                clientSocket.receive(fromServerPacket);
                if ((new String(fromServerPacket.getData())).contains("END")) {
                    System.out.println("End Reached");
                    break;
                }
                imageDataStream.write(fromServerPacket.getData(), 0, fromServerPacket.getLength());
            }

            // Now write the image
            Files.write(Path.of(System.getProperty("user.dir") + '/' + "meme-" + i + ".jpg"), imageDataStream.toByteArray());
            
            // Reset the buffered output stream
            System.out.println("Successfully read and saved image: " + "meme-" + i + ".jpg");
            imageDataStream.reset();
            clientSocket.send(toServerPacket);
        }

        clientSocket.close();
    }
    public static void main(String[] args) {
        try {
            ClientUDP client = new ClientUDP("localhost", 3000);
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }
}
