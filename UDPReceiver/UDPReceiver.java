
//Kevin Horadan

import java.io.*;
import java.net.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UDPReceiver
{
    public static void main(String argv[]) throws Exception
    {
        final int NUM_TIME_SEND = 100;
        final int MAX_BUFFER_SIZE = 8192;//64KB - 20Byte IP Header - 8 Byte UDP Header
        final String FILE_NAME = "text";
        final String FILE_TYPE = ".txt";
        
        int numTimes = 0;
        int numError = 0;
        int totalTime = 0;
        int numBytes;
        long startTime, endTime, tGap;
        
        File dump = new File("output.txt");
        FileWriter writer = new FileWriter(dump);
		BufferedWriter dumpWriter = new BufferedWriter(writer);
		
		FileOutputStream fileOut = new FileOutputStream("receivedFile" + numTimes + FILE_TYPE);
        byte[] dataBuffer = new byte[MAX_BUFFER_SIZE];
        
        DatagramSocket UDPsocket = new DatagramSocket(8888);
        DatagramPacket packet = new DatagramPacket(dataBuffer, MAX_BUFFER_SIZE);

        System.out.println("Starting server. Listening for packets");
        dumpWriter.write("Starting server. Listening for packets");
		dumpWriter.newLine();
		while(true)
        {
            //TODO Method to check connection made
            //timer starts here
            //Blocks while waiting for a DatagramPacket to come in
            UDPsocket.receive(packet);
            startTime = System.currentTimeMillis();//Start time when connection made
            
            System.out.println("Starting to receive file for the " + (numTimes + 1) + "th time.");
            dumpWriter.write("Starting to receive file for the " + (numTimes + 1) + "th time.");
			dumpWriter.newLine();
            dataBuffer = packet.getData();
            numBytes = packet.getLength();
            DatagramPacket packetOut = new DatagramPacket(dataBuffer, numBytes, packet.getSocketAddress());
            fileOut.write(dataBuffer, 0, numBytes);
            UDPsocket.send(packetOut);
            while(numBytes == MAX_BUFFER_SIZE)
            {
                UDPsocket.receive(packet);
                dataBuffer = packet.getData();
                numBytes = packet.getLength();
                fileOut.write(dataBuffer, 0, numBytes);
                UDPsocket.send(packetOut);
            
            }
			//fileOut.write needed?
            fileOut.close();
            endTime = System.currentTimeMillis();
            tGap = endTime - startTime;
            totalTime += tGap;
            System.out.println("Finished receiving file for the " + (numTimes + 1) + "th time.");
            System.out.println("Time taken: " + tGap + "ms");
            dumpWriter.write("Finished receiving file for the " + (numTimes + 1) + "th time.");
			dumpWriter.write("Time taken: " + tGap + "ms");
			dumpWriter.newLine();
            numTimes++;
            if(numTimes == NUM_TIME_SEND) break;
            fileOut = new FileOutputStream("receivedFile" + numTimes + FILE_TYPE);
        }
        UDPsocket.close();
        
        //check the received files
        
        numError = compareFiles(FILE_NAME, FILE_TYPE, NUM_TIME_SEND);
        
        System.out.println("Average time: " + (double)totalTime/numTimes + "ms.");
        System.out.println(numError + " Errors: " + (numTimes-numError) + " of " + numTimes + " files transferred correctly!");
		dumpWriter.write("Average time: " + (double)totalTime/numTimes + "ms.");
		dumpWriter.write(numError + " Errors: " + (numTimes-numError) + " of " + numTimes + " files transferred correctly!");
		dumpWriter.newLine();
		dumpWriter.close();
    }
    
    //Returns number of imperfect file transfers
	//Return -1 on FileNotFound or NoSuchAlgorithm error
    public static int compareFiles(String name, String fileType, int n) throws IOException
    {
        FileInputStream recFile;
        
        int errors = 0;
        int numBytes;
        byte[] buffer = new byte[32768];
        byte[] stdHash;
        byte[] recHash;
	
        
        try{
            FileInputStream stdFile = new FileInputStream(name+fileType);
            try
            {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                //Calculate standardFile hash
                do
                {
                    numBytes = stdFile.read(buffer);
                    digest.update(buffer);
                }while(numBytes != -1);
                stdFile.close();
                System.out.println("Comparing files...");
                stdHash = digest.digest();
                //System.out.println("Control Hash: " + stdHash);
                
                for(int i = 0; i < n; i++)
                {
                    try
                    {
                        recFile = new FileInputStream("receivedFile" + i + fileType);
                        //Calculate receivedFile hash
                        do
                        {
                            numBytes = recFile.read(buffer);
                            digest.update(buffer);
                        }while(numBytes != -1);
                        recFile.close();
                        recHash = digest.digest();
                        //System.out.println("File " + i + " Hash: " + recHash);
                        //Compare to standardFile hash
                        if(!digest.isEqual(stdHash, recHash)) errors++;
                    }
					catch (FileNotFoundException e) 
					{
                        System.out.println("receivedFile" + i + fileType + " not found");
                    }
                }
                return errors;
            }
            catch (NoSuchAlgorithmException e)
            { 
                System.out.println("Hashing failed, MD5 not found");
                return -1;
            }
            
        }catch (FileNotFoundException e) {
            System.out.println("Standard file not found");
            return -1;
        }
    }
}