package TCP.client;
import java.io.*;
import java.net.*;
import java.nio.file.*;
/**
 * This file contains the TCP Client code for this project
 * Most of the functionality will be encapsulated in the Client class
 */
import java.util.Arrays;

/** Importing all the necessary files */
public class ClientTCP {

    long[] measure1Milli = new long[10];
    long measure2Milli;
    Socket clientSocket; // The client socket with which we connect to the server
    InputStream imageStream; // the input stream through which we receive data from the server
    ByteArrayOutputStream imageData; // the actual image data, which we then convert to a bytearray when writing to file
    byte[] streamBuffer; // Buffer to contain data as we transfer it from imageStream to imageData
    PrintWriter toServer; // OutputStream writer to send the server text messages
    BufferedReader fromServer; //InputStream reader to receive messages back from the server
    String imageName; // stores the name of the current image sent to us by the server
    public ClientTCP(String IP, int port) throws IOException {

        // Connect to server (measure before and after connection occurs)
        long measure2Start = System.currentTimeMillis();
        clientSocket = new Socket(IP, port);
        long measure2End = System.currentTimeMillis();
        measure2Milli = measure2End - measure2Start;
        // Listen and wait for image data by getting the input stream
        System.out.println("Successfully connected to Server");
        imageStream = clientSocket.getInputStream();
        // create the ByteArrayOutputStream we need to populate with the data we get from imageStream
        imageData = new ByteArrayOutputStream();
        // Also allocate about a kylobyte for the buffer array since we expect to be sending like a kb of data per shot
        streamBuffer = new byte[100*1024];
        // Additionally, allocate a printWriter and BufferedReader to send text messages to and from the server :)
        toServer = new PrintWriter(clientSocket.getOutputStream(), true);
        fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        // This next part I found a bit confusing but user @Ravinder Reddy on StackOverflow explains it pretty well

        System.out.println("Printing out success to Server");
        toServer.println("Successfully Connected. Please start sending Memes");

        // There are 10 images, so lets loop ten times
        
        for (int i = 0; i < 10; i++) {
            // Image is requested from server by above print statement
            long measure1Start = System.currentTimeMillis();
            
            imageName = fromServer.readLine();
            System.out.println("Successfully obtained Image Name of: " + imageName);
            // Notify the server that we read the image data
            toServer.println("Read Image, no awaiting imageInfo");
            // While we still have stuff left to read
            int bytesRead = imageStream.read(streamBuffer);

            // Image is  downloaded after this write statement is over
            imageData.write(streamBuffer, 0, bytesRead);
            

            long measure1End = System.currentTimeMillis();

            // Calculate the total round-trip time and save
            measure1Milli[i] = measure1End - measure1Start;
            // We then save it to our local dir
            Files.write(Path.of(System.getProperty("user.dir") + '/' + imageName), imageData.toByteArray());

            // Empty the imageData output stream to take in the next image
            imageData.reset();

            toServer.println("Successfully Read Image: " + imageName);
        }
        
        // Close out all the sockets
        imageStream.close();
        imageData.close();
        fromServer.close();
        toServer.close();
        clientSocket.close();
    }
    public void printStats() {
        System.out.println("TCP Setup Time: " + measure2Milli);
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
    /**
     * Main Function!
     * @param args Arry of input arguments. First one should be the IP
     */
    public static void main(String[] args) {
        try {
            ClientTCP client;
            if (args.length != 0)
                client = new ClientTCP(args[0], 3000);
            else 
                client = new ClientTCP("localhost", 3000);

            client.printStats();
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }
}

/**
 * int bytesRead = 0;
 * 
 * while ((bytesRead = imageStream.read(streamBuffer)) != -1) {
                // Write this stuff into our imageData output stream
                System.out.println("Hello");
                System.out.println(bytesRead);
                imageData.write(streamBuffer, 0, bytesRead);
            }

                    /**
         * Essentially what we are doing is reading the inputStream until it can no longer be read.
         * We do this by loading what we read from the inputStream into our buffer array, streamBuffer.
         * This function inputStream.read(content) actually returns an integer denoting the bytes read at that instance
         * When this function returns -1, we know that no bytes were read as we are at the end of our stream, thus there
         * is no more image to read, thus we can stop :)
         */
