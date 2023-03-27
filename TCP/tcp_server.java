package TCP;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class tcp_server {
    
    long[] measure3Milli = new long[10];
    ServerSocket serverSocket; // Establish the server socket
    Socket clientSocket; // Receiver for the clientSocket that connects to our server
    BufferedReader fromClient; // Used to read messages sent from the client
    OutputStream imageToClient; // Used to output the image byteArray as an output stream over to the client
    PrintWriter textToClient; // Used to output text messages to the client :)
    byte[] imageData; // The array that holds the bytes obtained from reading an image from our local file system
    
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
    public tcp_server(int port) throws IOException {
        
        // First, initialize all our member variables
        serverSocket = new ServerSocket(port);
        System.out.println("Server Initialized: Ready for Clients to Connect");

        // Wait for the client to connect
        clientSocket = serverSocket.accept();
        System.out.println("Client has successfully connected from IP: " + clientSocket.getRemoteSocketAddress().toString());

        fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));;
        textToClient =  new PrintWriter(clientSocket.getOutputStream(), true);
        imageToClient = clientSocket.getOutputStream();
        // Wait for the client to be ready to accept images
        String clientSuccessReady = fromClient.readLine();
        if (!clientSuccessReady.contains("Success")) {
            throw new IOException("Error: Server registered issue in initial client response- see line 26 of code");
        }
        System.out.println("Client Ready for Images");


        // The shuffledIndices function is better explained in my ServerUDP.java file, but essentially it shuffles an array
        // with numbers 1-10. I iterate over this array from index 0 - 9 to get the "number" of the meme file i want to access
        // This ensures that every time I access meme files I do so in a random order and bypass the server file caching mechanism
        int[] randomMeme = shuffledIndices();
        for (int h = 0; h < 10; h++) {
            // get the current imageName (I explain what the randomMeme )
            int i = randomMeme[h];
            String imageName = "meme-" + i + ".jpg";
            // send that name to the client
            // Confirm Client Received it
            fromClient.readLine();
            // Try reading the image from the local directory (Also start measure 3 here)
            long measure3Start = System.currentTimeMillis();
            imageData = Files.readAllBytes(Path.of(System.getProperty("user.dir") + "/" + imageName ));
            long measure3End = System.currentTimeMillis();
            
            // Save the measurement to measure array
            measure3Milli[h] = measure3End - measure3Start;

            System.out.println("Reading " + imageName);
            System.out.println(imageData.length);
            // Once imageData is read, we can go ahead and send it over to the client via outputStream
            imageToClient.write(imageData);
            // Crazy stuff here
            imageToClient.write((new byte[1])[0] = -125);
            imageToClient.flush();
            // Wait for the client to successfully read the image and save it
            System.out.println(fromClient.readLine());
            
        }
        try {
            wait(1000);
        } catch (Exception err) {
            System.out.println(err.getMessage());
        }

        clientSocket.close();
        serverSocket.close();
        imageToClient.close();
        textToClient.close();
        fromClient.close();

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
            tcp_server server = new tcp_server(3000);
            server.printStats();
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }
}
