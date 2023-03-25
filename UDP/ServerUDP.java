package UDP;
import java.io.*;
import java.net.*;
import java.nio.file.*;

public class ServerUDP {
    
    DatagramSocket serverSocket; // Establish the server socket (UDP)
    DatagramPacket clientPacket; // Holder for packet to be received by the client
    InetAddress clientAddress; // Address/IP of client
    int clientPort; // port of client
    byte[] imageByteArray; // The array that holds the bytes obtained from reading an image from our local file system
    byte[] fromClientBuffer; // Buffer to receive datagram packet from client when a message is sent

    public ServerUDP(int port) throws IOException {
        //Initialize Server
        serverSocket = new DatagramSocket(port);
        fromClientBuffer = new byte[1024]; // Arbitrarily allocate a 1024 byte buffer
        clientPacket = new DatagramPacket(fromClientBuffer, 1024); // Make a DatagramPacket with the buffer to allow for listening to and receiving messages from client
        
        // Listen for a packet from the client (In this case, this will be for us to get the addres of the client to send back our image info)
        serverSocket.receive(clientPacket);

        // Read the data from the clientPacket datagram
        String client_message = new String (clientPacket.getData(), clientPacket.getOffset(), clientPacket.getLength());
        System.out.println("Initial Client Message: ");
        System.out.println(client_message);
        // Now get the info from the client
        clientAddress = clientPacket.getAddress();
        clientPort = clientPacket.getPort();

        // Now that we have client info, we can target them and start sending packets
        // We have to do this for each image 🥹🥹🥹
        // Why are my emojis italic?

        for (int i = 1; i < 11; i++) {

            imageByteArray = Files.readAllBytes(Path.of(System.getProperty("user.dir") + "/" + "meme-" + i +".jpg" ));
            // Split the data into packets
            // essentially, we are dividing the length of the byte array into kb-sized packets (we Math.ceil() our number since we can only send whole packets)
            int totalPackets = (int)Math.ceil((double) imageByteArray.length / 1024);
            for (int j = 0; j < totalPackets; j++) {
                int window_start = j*1024;
                // Dont want to accidentally go out of bounds
                int window_end = imageByteArray.length < window_start + 1024 ? (imageByteArray.length) : window_start + 1024;
                // We need the length of the packet we are sending
                int length = window_end - window_start;

                // Make a temp array to basically be the buffer of our packet we are sending
                byte[] packet = new byte[length];
                // I dont want to make another inner for loop 🤮 so I'll just System.Arraycopy
                System.arraycopy(imageByteArray, window_start, packet, 0, length);

                // FINALly send over that packet array as a packet, to the client we grabbed earlier
                serverSocket.send(new DatagramPacket(packet, length, clientAddress, clientPort));
                // we do this over and over again with all the images
            }
            byte[] endMessage = (new String("\nEND\n")).getBytes();
            serverSocket.send(new DatagramPacket(endMessage, endMessage.length, clientAddress, clientPort));
            System.out.println("Successfully sent image data for: " + "meme-" + i +".jpg");
            serverSocket.receive(clientPacket);
        }

        serverSocket.close();
    }
    public static void main(String[] args) {
        try {
            ServerUDP server = new ServerUDP(3000);
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }
}
