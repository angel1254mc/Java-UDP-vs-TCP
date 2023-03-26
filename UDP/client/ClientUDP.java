package UDP.client;
import java.io.*;
import java.net.*;
import java.nio.file.*;

public class ClientUDP {

    long[] measure1Milli = new long[10];
    long measure2Milli;
    InetAddress address;
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
        // This is where we start our second measurement, right before resolving INetAddress
        long measure2Start = System.currentTimeMillis();
        address = InetAddress.getByName(IP);
        // Measure 2 ends literally right after
        long measure2End = System.currentTimeMillis();
        // Save this measure to our measure2Milli member variable
        measure2Milli = measure2End - measure2Start;

        toServerPacket = new DatagramPacket(toServerData, toServerData.length, address, port);
        fromServerData = new byte[1024];
        fromServerPacket =  new DatagramPacket(fromServerData, 1024);
        imageDataStream = new ByteArrayOutputStream();
        // Now shoot that bad boy and see if the server gets it
        clientSocket.send(toServerPacket);

        // Assuming the server got it, now we can start receiving image data
        for (int i = 1; i < 11; i++) {
            // This is kinda weird but I think the best way to figure out whether the image has finished being loaded
            // is by waiting for a message from the server that says "end"
            // This is also where we start our first measurement - Here is when we start receiving data from the serverr
            long measure1Start = System.currentTimeMillis();
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
            // Once we are out of this while loop, the file has been fully downloaded and stored in our buffer
            long measure1End = System.currentTimeMillis();
            /// Populate the array of our 10 measurements
            measure1Milli[i - 1] = measure1End - measure1Start;
            // Now write the image
            Files.write(Path.of(System.getProperty("user.dir") + '/' + "meme-" + i + ".jpg"), imageDataStream.toByteArray());
            
            // Reset the buffered output stream
            System.out.println("Successfully read and saved image: " + "meme-" + i + ".jpg");
            imageDataStream.reset();
            clientSocket.send(toServerPacket);
        }

        clientSocket.close();
    }

    public void printStats() {
        System.out.println("DNS Resolution Time: " + measure2Milli);
        System.out.println("Printing out Total Round-Trip Time Data Points");
        System.out.println("-------------------------------------------");
        long sum = 0;
        long max = measure1Milli[0];
        long min = measure1Milli[0];
        for (int i = 0; i < 10; i++) {
            System.out.println("" + (i+1) + ": " + measure1Milli[i]);
            sum += measure1Milli[i];
            if (measure1Milli[i] > max)
                max = measure1Milli[i];
            if (measure1Milli[i] < min)
                min = measure1Milli[i];
        }

        System.out.println("\nRound-Trip Time STATS");
        System.out.println("-------------------------------------------");
        // Calculating out mean
        double mean = sum / 10;
        System.out.println("Mean: " + mean);
        double standardDeviation = 0;
        System.out.println("Min Value: " + min);
        System.out.println("Max Value: " + max);
        
        // Calculating out the standard deviation
        for (double num : measure1Milli) {
            standardDeviation += Math.pow(num - mean, 2);
        }
        standardDeviation = Math.sqrt(standardDeviation / 10);

        System.out.println("std. Deviation: " + standardDeviation);
    }
    public static void main(String[] args) {
        try {
            ClientUDP client;
            if (args.length != 0) 
                client = new ClientUDP(args[0], 3000);
            else
                client = new ClientUDP("localhost", 3000);
            client.printStats();
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }
}
