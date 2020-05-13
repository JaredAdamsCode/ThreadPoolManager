README.txt

JARED ADAMS
CS455 HW2
03/11/2020

INFORMATION FOR GRADER:

To run the program:

1. unpack the .tar file (you should have already done this) with this command: tar -xvf Jared-Adams-ASG2.tar
2. move into CS455HW2 directory and issue command: gradlew build (or gradle build if you have gradle is in your PATH)
3. move into the ./build/libs directory
4. run these commands to run Server and Client:

java -cp CS455HW2.jar cs455.scaling.server.Server <port number> <thread pool size> <batch-size> <batch-time>

***PLEASE NOTE THAT THE THE BATCH TIME IS IN SECONDS. For example, if you want the batch time to be 2 seconds, enter 2. 

java -cp CS455HW2.jar cs455.scaling.client.Client <server host> <server port number> <message rate>
 
Please note the command line arguments as the program will fail without the proper arguments.

Other Notes:

There is no command to exit the program and it is not set up for a graceful exit. When you are finished running the program you must perform a hard stop from the command line (ctrl + c) or close the window. 

BRIEF DESCRIPTION OF CLASSES

Client

Client is the main program that generates the hashes and sends the hashes to the server at the rate specified by the user from the command line. It also starts a receiver thread to receive the acknowledgements from the server. Client also maintains the timer to print the stats every 20 seconds. 

ClientReceiver

ClientReceiver runs on a separate thread from Client so it can listen for responses from the server and process those responses. When a message is received, it is read and the message is removed from the pending hashes linked list. 

ClientStatsTimerTask

This extends TimerTask and executes the methods in ClientTracking to print the stats. 

RandomBytes

This class generates the random bytes that are sent to the server and used the generate the hash. 

BatchTimerTask

BatchTimerTask extends TimerTask and when executed it will push tasks to the thread pool for processing. 

KeyAttachment

This class is used as an attachment for SelectionKeys the booleans are used to determine what action should be taken on the key inside the worker class. 

Server

Server is the main class for the Server portion of the program. It creates the connections to run the Java NIO and loops over the selector and when a connection is ready for processing it call the ThreadPoolManager to handle the connection. It also creates the ThreadPoolManager.

ServerTracking

This class keeps track of all the stats that are printed to the screen every 20 seconds by the ThreadPoolManager. 

StatsTimerTask

This class extends TimerTask and when it executes it prints the stats to the console. 

ThreadPoolManager

As its name suggestions, ThreadPoolManager manages the thread pool. It creates all of the workes and moves them into seperate threads. It also checks the selectionkeys to determine how they should be processed (either as read or accept). When a batch is created it sends that batch to the thread pool to be processed by a worker. It also maintains the timers to print stats and send partially filled batches to the thread pool if the batch time expires. 

Toggle

Toggle is a wrapper class that holds a boolean used to toggle between accepting reads or accepts in a worker class. 

Worker

Worker is used in the thread pool to either register a new connection or read from an existing connection that has data to read. It checks the linkedlists to see if there  is work it can perform. 

Hashing

This class is used by both the Client and the Worker to hash the bytes into a 40 character string. 

Protocols

Protocols holds constants that are used throughout the program. 
