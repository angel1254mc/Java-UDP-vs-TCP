package TCP;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ServerTCP {
    
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
    public ServerTCP(int port) throws IOException {
        
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


        int[] randomMeme = shuffledIndices();
        for (int h = 0; h < 10; h++) {
            // get the current imageName
            int i = randomMeme[h];
            String imageName = "meme-" + i + ".jpg";
            // send that name to the client
            textToClient.println(imageName);
            // Try reading the image from the local directory 
            imageData = Files.readAllBytes(Path.of(System.getProperty("user.dir") + "/" + imageName ));
            System.out.println("Reading " + imageName);
            System.out.println(imageData);
            // Once imageData is read, we can go ahead and send it over to the client via outputStream
            imageToClient.write(imageData);
            imageToClient.flush();
            // Wait for the client to successfully read the image and save it
            if (!(fromClient.readLine().contains("Success")))
                throw new IOException("Error: Client was not able to succcessfully read and save the image of imageName: " + imageName);
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

    public static void main(String[] args) {
        try {
            ServerTCP server = new ServerTCP(3000);
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }
}
