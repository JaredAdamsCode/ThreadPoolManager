package cs455.scaling.client;

import java.nio.*;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;
import java.net.*;
import cs455.scaling.util.*;
import cs455.scaling.server.*;

public class Client {

	private static SocketChannel client;
	private static ByteBuffer buffer;
	private static Thread clientReceiverThread;
	private static ClientReceiver cr;
	
	public static void main(String[] args) throws NoSuchAlgorithmException, InterruptedException {
		
		// Get command line args
		String serverHost = args[0];
		int portNumber = Integer.parseInt(args[1]);
		int messageRate = Integer.parseInt(args[2]);

		// LL to store hashes that are generated and pending response from server
		LinkedList<String> pendingHashes = new LinkedList<String>();
		
		// set up Tracking Data
		ClientTracking clientTracking = new ClientTracking();
		
		// set up Timer
		Timer timer = new Timer();
		TimerTask clientStatsTimerTask = new ClientStatsTimerTask(clientTracking);
		timer.schedule(clientStatsTimerTask, 20000, 20000);

		try {
			// Connect to server
			client = SocketChannel.open(new InetSocketAddress(serverHost, portNumber));
			cr = new ClientReceiver(client, pendingHashes, clientTracking);

			// start the receiver thread
			clientReceiverThread = new Thread(cr);
			clientReceiverThread.start();
			
			//create buffer
			buffer = ByteBuffer.allocate(Protocols.CLIENT_MESSAGE_CAPACITY);
			
		}catch(IOException e) {
			e.printStackTrace();
		}
						
		System.out.println("\nWelcome to CS455 Homework 2 - Client\n");
		
		while(true) {

			RandomBytes randomBytes = new RandomBytes();
			byte[] data = randomBytes.getRandomBytes();
			Hashing hashing = new Hashing();
			String hash = hashing.getHash(data);
			synchronized(pendingHashes) {
				pendingHashes.add(hash);
			}
			
			buffer = ByteBuffer.wrap(data);
			try {
				client.write(buffer);
				buffer.clear();
				
				clientTracking.incrementSent();

			}catch(IOException e) {
				e.printStackTrace();
				break;
			}
			
			Thread.sleep(1000 / messageRate);
		}
				
	}


}
