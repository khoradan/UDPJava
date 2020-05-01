Author: Kevin Horadan
Date: 4/30/2020

This project was written using Java.
------------------------------------------------------
To configure the Sender:
------------------------------------------------------
place the file to be transferred in the UDPSender directory

set the file's name and tag using the FILE_NAME & FILE_TYPE strings on lines 20/21

set the number of times to send using NUM_TIME_SEND on line 17

set the size of the read/write buffer on line 19

set the ports on lines 23/24
	PORT_RECEIVER may need to be port forwarded from the router to work correctly
	PORT_SENDER will not require port forwarding

set the Receiver's IP on line 25


------------------------------------------------------
To configure the Receiver:
------------------------------------------------------
place a copy of the file to be transferred in the UDPReceiver directory

set the file's name and tag using the FILE_NAME & FILE_TYPE strings on lines 17/18

set the number of times to receive using NUM_TIME_SEND on line 15

set the size of the read/write buffer on line 16

set PORT_RECEIVER on line 35
	this may need to be port forwarded from the router to work correctly


------------------------------------------------------
To run the project:
------------------------------------------------------
Start UDPReceiver.java first
Then start UDPSender.java

You may start the programs either from the command line 
or from the bluej package. If using bluej just right click the
class and click new main()

Output will be given in the console and also in an output.txt dump
This is in both the UDPSender and UDPReceiver directories and is related
to the respective program

------------------------------------------------------
To run the project faster:
------------------------------------------------------
Turn off print statement regarding packet notifications 
in UDPSender.java on line 64
