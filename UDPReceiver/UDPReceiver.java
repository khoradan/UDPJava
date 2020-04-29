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
        final int NUM_TIME_SEND = 5;
        final int MAX_BUFFER_SIZE = 8192;//64KB - 20Byte IP Header - 8 Byte UDP Header
        final String FILE_NAME = "100mb";
        final String FILE_TYPE = ".txt";
        
		
        int numTimes = 0;
        int numError = 0;
        long totalTime = 0;
        int numBytes;
        long startTime, endTime, tGap;
        
        File dump = new File("output.txt");
        FileWriter writer = new FileWriter(dump);
		BufferedWriter dumpWriter = new BufferedWriter(writer);
		
		FileOutputStream fileOut = new FileOutputStream("receivedFile" + numTimes + FILE_TYPE);
        byte[] dataBuffer = new byte[MAX_BUFFER_SIZE+1];
        byte[] ackBuffer = new byte[1];
		
        DatagramSocket UDPsocket = new DatagramSocket(8888);
        DatagramPacket packet = new DatagramPacket(dataBuffer, MAX_BUFFER_SIZE+1);
		DatagramPacket ack = new DatagramPacket(ackBuffer, 1);

        System.out.println("Starting server. Listening for packets");
        dumpWriter.write("Starting server. Listening for packets");
		dumpWriter.newLine();
		
		while(true)
        {
            UDPsocket.receive(packet);
            startTime = System.currentTimeMillis();//Start time when connection made
            
            System.out.println("Starting to receive file for the " + (numTimes + 1) + "th time.");
            dumpWriter.write("Starting to receive file for the " + (numTimes + 1) + "th time.");
			dumpWriter.newLine();
            
			dataBuffer = packet.getData();
            numBytes = packet.getLength();
            fileOut.write(dataBuffer, 1, numBytes - 1);
            
			ackBuffer[0] = dataBuffer[0];
			ack = new DatagramPacket(ackBuffer, 1, packet.getSocketAddress());
			
			UDPsocket.send(ack);
			
            while(numBytes != 0)
            {
                UDPsocket.receive(packet);
                dataBuffer = packet.getData();
                numBytes = packet.getLength();
                
				if(dataBuffer[0] == -1)
				{
					System.out.println("Received eof packet");
					UDPsocket.send(ack);
					break;
					
				}
				if(Byte.compare(ackBuffer[0], dataBuffer[0]) != 0)
				{
					fileOut.write(dataBuffer, 1, numBytes - 1);
					ackBuffer[0] = dataBuffer[0];
					ack.setData(ackBuffer);
				}
				UDPsocket.send(ack);
            }
			
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
        
		//Output results
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