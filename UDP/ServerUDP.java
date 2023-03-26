package UDP;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ServerUDP {
    
    long[] measure3Milli = new long[10];
    DatagramSocket serverSocket; // Establish the server socket (UDP)
    DatagramPacket clientPacket; // Holder for packet to be received by the client
    InetAddress clientAddress; // Address/IP of client
    int clientPort; // port of client
    byte[] imageByteArray; // The array that holds the bytes obtained from reading an image from our local file system
    byte[] fromClientBuffer; // Buffer to receive datagram packet from client when a message is sent
    public int[] shuffledIndices() {
        Integer[] memeArray = {1,2,3,4,5,6,7,8,9,10};
        List<Integer> memeList = Arrays.asList(memeArray);
        Collections.shuffle(memeList);

        memeList.toArray(memeArray);
        int[] memeArr = new int[10];

        int i = 0;
        for (Integer num : memeList) {
            memeArr[i] = num;
            i++;
        }
        System.out.println(memeArr);
        return memeArr;
    }
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
        
        // I just noticed that one of the steps is to randomize the image being sent
        // So I'm going to go ahead and shuffle an array of the indices, using a technique I read about
        // on a DigitalOcean Community Blog post. Link is the following: https://www.digitalocean.com/community/tutorials/shuffle-array-java
        int[] randomMeme = shuffledIndices();
        for (int h = 0; h < 10; h++) {
            // What are we doing here? We are mapping j -> 0 thru 9 to a RANDOM number 1 -> 10. This mapping is 1-to-1. This ensures that we never grab
            // images in the same order and bypass the Server's file caching mechanism to accurately measure file access time
            int i = randomMeme[h];

            // Im going to start measure3 here and end it right after this line
            long measure3Start = System.currentTimeMillis();
            imageByteArray = Files.readAllBytes(Path.of(System.getProperty("user.dir") + "/" + "meme-" + i +".jpg" ));
            long measure3End = System.currentTimeMillis();
            // Populate our measure3 Array
            measure3Milli[h] = measure3End - measure3Start;

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

    public void printStats() {
        System.out.println("Printing out Meme Access Time Data Points");
        System.out.println("-------------------------------------------");
        long sum = 0;
        long max = measure3Milli[0];
        long min = measure3Milli[0];
        for (int i = 0; i < 10; i++) {
            System.out.println("" + (i+1) + ": " + measure3Milli[i]);
            sum += measure3Milli[i];
            if (measure3Milli[i] > max)
                max = measure3Milli[i];
            if (measure3Milli[i] < min)
                min = measure3Milli[i];
        }

        System.out.println("\nMeme Access Time STATS");
        System.out.println("-------------------------------------------");
        // Calculating out mean
        double mean = sum / 10;
        System.out.println("Mean: " + mean);
        double standardDeviation = 0;
        System.out.println("Min Value: " + min);
        System.out.println("Max Value: " + max);
        
        // Calculating out the standard deviation
        for (double num : measure3Milli) {
            standardDeviation += Math.pow(num - mean, 2);
        }
        standardDeviation = Math.sqrt(standardDeviation / 10);

        System.out.println("std. Deviation: " + standardDeviation);
    }
    public static void main(String[] args) {
        try {
            ServerUDP server = new ServerUDP(3000);
            server.printStats();
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }
}
