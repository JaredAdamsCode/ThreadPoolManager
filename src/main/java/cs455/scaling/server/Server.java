package cs455.scaling.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.security.NoSuchAlgorithmException;

import cs455.scaling.util.*;
import cs455.scaling.client.*;

public class Server {

	
	public static void main(String[] args) throws InterruptedException, IOException, NoSuchAlgorithmException {
		
		// get command line arguments
		int portNumber = Integer.parseInt(args[0]);
		int threadPoolSize = Integer.parseInt(args[1]);
		int batchSize = Integer.parseInt(args[2]); 
		int batchTime = Integer.parseInt(args[3]) * 1000; // convert to seconds
		
		// Welcome Message
		System.out.println("\nWelcome to CS455 Homework 2 - Server\n");

		// open the selector
		Selector selector = Selector.open();
		
		// create input channel
		ServerSocketChannel serverSocket = ServerSocketChannel.open();
		serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost().getHostName(), portNumber));
		serverSocket.configureBlocking(false);
		//register channel to selector
		serverSocket.register(selector, SelectionKey.OP_ACCEPT);
		
		// create ThreadPool and threads
		ThreadPoolManager tpManager = new ThreadPoolManager(selector, serverSocket, threadPoolSize, batchSize, batchTime);
		tpManager.createThreads();
		
		// loop on selector
		while(true) {

				//Block here
				selector.select();
				tpManager.checkSelectedKeys();
				
		}
	}
	


}
