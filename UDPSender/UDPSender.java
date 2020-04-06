//Kevin Horadan

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;


public class UDPSender 
{
    public static void main(String argv[]) throws Exception
    {
        final int NUM_TIME_SEND = 100;
        final int MAX_BUFFER_SIZE = 8192;//64KB - 20Byte IP Header - 8 Byte UDP Header
        final String FILE_NAME = "text";
        final String FILE_TYPE = ".txt";
	//InetAddress server = InetAddress.getByName("192.168.0.108");
        InetAddress server = InetAddress.getByName("75.143.75.136");
        int numLines = 0;
        int numTimes = 0;
        int numBytes;
        long startTime, endTime, tGap;
        long totalTime = 0;
        byte [] dataBuffer = new byte[MAX_BUFFER_SIZE];
        
        File file = new File(FILE_NAME + FILE_TYPE);
        FileInputStream fileIn = new FileInputStream(file);
        int fileLength = (int)file.length();
        
        DatagramSocket UDPsocket = new DatagramSocket(7777);
        DatagramPacket packet = new DatagramPacket(dataBuffer, MAX_BUFFER_SIZE, server, 8888);
        while(true)
        {
            UDPsocket.connect(server, 8888);
			System.out.println("Sending file for the " + (numTimes + 1) + "th time.");
            startTime = System.currentTimeMillis();
            
            if(fileLength < MAX_BUFFER_SIZE) numBytes = fileIn.read(dataBuffer, 0, fileLength);
            else                                         numBytes = fileIn.read(dataBuffer, 0, MAX_BUFFER_SIZE);
            
			while(numBytes != -1) //-1 is return from read() when EOF
            {            
                packet.setData(dataBuffer, 0, numBytes);
                UDPsocket.send(packet);
				UDPsocket.receive(packet);
                numBytes = fileIn.read(dataBuffer, 0, MAX_BUFFER_SIZE);
            }
            fileIn.close();
            endTime = System.currentTimeMillis();
            tGap = endTime - startTime;
            totalTime += tGap;
            System.out.println("Finished sending "+(numTimes + 1)+ "th time sending with time: " + tGap + "ms");
            
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
    }
}
