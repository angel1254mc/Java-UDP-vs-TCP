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

    Socket clientSocket; // The client socket with which we connect to the server
    InputStream imageStream; // the input stream through which we receive data from the server
    ByteArrayOutputStream imageData; // the actual image data, which we then convert to a bytearray when writing to file
    byte[] streamBuffer; // Buffer to contain data as we transfer it from imageStream to imageData
    PrintWriter toServer; // OutputStream writer to send the server text messages
    BufferedReader fromServer; //InputStream reader to receive messages back from the server
    String imageName; // stores the name of the current image sent to us by the server
    public ClientTCP(String IP, int port) throws IOException {

        // Connect to server
        clientSocket = new Socket(IP, port);
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
            imageName = fromServer.readLine();
            System.out.println("Successfully obtained Image Name of: " + imageName);
            // While we still have stuff left to read
            int bytesRead = imageStream.read(streamBuffer);
            imageData.write(streamBuffer, 0, bytesRead);
            

            // We can then save the image data into a file in our local directory
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
    /**
     * Main Function!
     * @param args
     */
    public static void main(String[] args) {
        try {
            ClientTCP client = new ClientTCP("localhost", 3000);
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
