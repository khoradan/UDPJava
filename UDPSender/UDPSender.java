//Kevin Horadan

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.Writer;

public class UDPSender 
{
    public static void main(String argv[]) throws Exception
    {
        
		//Change only the final variables to configure the program.
		//number of times to send the file to the receiver. NEEDS TO BE IDENTICAL TO UDPReceiver.java value
		final int NUM_TIME_SEND = 5;
		//maximum amount of data to transfer at a time in bytes. NEEDS TO BE IDENTICAL TO UDPReceiver.java value
        final int MAX_BUFFER_SIZE = 8192;
        final String FILE_NAME = "100mb";
        final String FILE_TYPE = ".txt";
	    
		final int PORT_SENDER = 6788;
		final int PORT_RECEIVER = 8888;
		final InetAddress IP_RECEIVER = InetAddress.getByName("75.143.75.136");
        
		
		int numLines = 0;
        int numTimes = 0;
        int numBytes;
        long startTime, endTime, tGap;
        long totalTime = 0;
        byte [] dataBuffer = new byte[MAX_BUFFER_SIZE+1];
        byte[] eof = new byte[]{-1};
        int packetNum;
        
		File dump = new File("output.txt");
        FileWriter writer = new FileWriter(dump);
		BufferedWriter dumpWriter = new BufferedWriter(writer);
		
		File file = new File(FILE_NAME + FILE_TYPE);
        FileInputStream fileIn = new FileInputStream(file);
        int fileLength = (int)file.length();
        
        DatagramSocket UDPsocket = new DatagramSocket(PORT_SENDER);
        UDPsocket.setSoTimeout(2000);
        DatagramPacket packet = new DatagramPacket(dataBuffer, MAX_BUFFER_SIZE+1, IP_RECEIVER, PORT_RECEIVER);
        DatagramPacket eofPacket = new DatagramPacket(eof, 1, IP_RECEIVER, PORT_RECEIVER);
		while(true)
        {
            packetNum = 1;
			UDPsocket.connect(IP_RECEIVER, PORT_RECEIVER);
			System.out.println("Sending file for the " + (numTimes + 1) + "th time.");
            dumpWriter.write("Sending file for the " + (numTimes + 1) + "th time.");
			dumpWriter.newLine();
			startTime = System.currentTimeMillis();
            
            if(fileLength < MAX_BUFFER_SIZE) numBytes = fileIn.read(dataBuffer, 1, fileLength);
            else                                         numBytes = fileIn.read(dataBuffer, 1, MAX_BUFFER_SIZE);
            
			while(numBytes != -1) //-1 is return from read() when EOF
            {            
                dataBuffer[0] = (byte)(packetNum % 2);
				System.out.println("sending packet " + packetNum);
                packet.setData(dataBuffer, 0, numBytes+1);
                UDPsocket.send(packet);
                while(true)
				{
					try
					{
						UDPsocket.receive(packet);
						break;
					}
					catch(SocketTimeoutException e)
					{
						System.out.println("Resending packet " + packetNum);
						UDPsocket.send(packet);
					}
				}
                numBytes = fileIn.read(dataBuffer, 1, MAX_BUFFER_SIZE);
				packetNum++;
            }
            
			//empty packet s.t. Receiver changes to EOF state
			//necesary for files that are increments of buffer size
			UDPsocket.send(eofPacket);
			UDPsocket.receive(packet);
			
			fileIn.close();
            endTime = System.currentTimeMillis();
            tGap = endTime - startTime;
            totalTime += tGap;
            System.out.println("Finished sending "+(numTimes + 1)+ "th time sending with time: " + tGap + "ms");
            dumpWriter.write("Finished sending "+(numTimes + 1)+ "th time sending with time: " + tGap + "ms");
			dumpWriter.newLine();
			//open file again
            numTimes++;
            if(numTimes == NUM_TIME_SEND) break;
            file = new File(FILE_NAME + FILE_TYPE);
            fileIn = new FileInputStream(file);
            fileLength = (int)file.length();
        }
        UDPsocket.close();
		
		System.out.println("I am done. Sent file " + numTimes + 
                           " times with an average time of: " + (double)totalTime/numTimes + "ms");
		dumpWriter.write("I am done. Sent file " + numTimes + 
                           " times with an average time of: " + (double)totalTime/numTimes + "ms");
		dumpWriter.newLine();
		dumpWriter.close();
    }
}