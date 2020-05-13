package cs455.scaling.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import cs455.scaling.util.Hashing;
import cs455.scaling.util.Protocols;

public class Worker implements Runnable{

	public LinkedList<LinkedList<SelectionKey>> taskQueue;
	public LinkedList<LinkedList<SelectionKey>> acceptQueue;
 
	public Selector selector;	
	public ServerSocketChannel serverSocket;
	public Toggle toggle;
	public ServerTracking serverTracking;
	
	public Worker(LinkedList<LinkedList<SelectionKey>> taskQueue, Selector selector, 
			ServerSocketChannel serverSocket, LinkedList<LinkedList<SelectionKey>> acceptQueue, 
			Toggle toggle, ServerTracking serverTracking ) {
		this.taskQueue = taskQueue;
		this.selector = selector;
		this.serverSocket = serverSocket;
		this.acceptQueue = acceptQueue;
		this.toggle = toggle;
		this.serverTracking = serverTracking;
	}
	
	
	@Override
	public void run(){

		while(true) {
			
			try { 
					LinkedList<SelectionKey> task = null;
									
					if(toggle.checkAcceptsNotReads) {
						synchronized(acceptQueue) {
							if(acceptQueue.size() == 0) {
								continue;
							}
							else {
								task = acceptQueue.pop();
							}
						}
					}
					else {
						synchronized(taskQueue) {
							if(taskQueue.size() == 0) {
								continue;
							}
							else {
								task = taskQueue.pop();
							}
						}
					}

					// New connection
					for(SelectionKey key : task) {
						KeyAttachment keyAttachment = (KeyAttachment) key.attachment();
						if(keyAttachment.accept) {

							register(selector, serverSocket, key);

						}
					
					// Data to read
						if(keyAttachment.read) {
							read(key);
						}
					}
						
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void read(SelectionKey key) {
		ByteBuffer buffer = ByteBuffer.allocate(Protocols.CLIENT_MESSAGE_CAPACITY);
		SocketChannel client = (SocketChannel) key.channel();

		int bytesRead = 0;
		
		while ( buffer.hasRemaining() && bytesRead != -1 ) {
			try {
				bytesRead = client.read( buffer );
				
				// if there is nothing to read, break out of this and return. this avoid the issue noted above
				if(bytesRead == 0) {
					key.interestOps (key.interestOps() | SelectionKey.OP_READ); // reset so it can read again
					buffer.clear(); // probably not needed but just to be safe
					return; // nothing to do so let's leave
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		
		if(bytesRead == -1) {
			key.cancel();
			System.out.println("client closed connection. Deregistering...");
			serverTracking.decrementConnectionsCount();
			KeyAttachment keyAttachment = (KeyAttachment) key.attachment();
			serverTracking.removeConnection(keyAttachment.id);
			return;
		}

		byte[] receivedData = new byte[Protocols.CLIENT_MESSAGE_CAPACITY];
		buffer.rewind();
		buffer.get(receivedData);
		buffer.clear();
		

		Hashing hashing = new Hashing();
		try {
			String hash = hashing.getHash(receivedData);
			
			byte[] byteResponse = hash.getBytes();
			
			ByteBuffer buffer2 = ByteBuffer.wrap( byteResponse );
			
			while ( buffer2.hasRemaining() ) {
				client.write(buffer2);
			}
			
			// clear buffer2
			buffer2.clear();
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		serverTracking.incrementTotalMessages();
		KeyAttachment keyAttachment = (KeyAttachment) key.attachment();
		serverTracking.incrementIndividualConnectionCount(keyAttachment.id);
		
		key.interestOps (key.interestOps() | SelectionKey.OP_READ); // reset so it can read again

	}
		
		
	
	private void register(Selector selector, ServerSocketChannel serverSocket, SelectionKey key ) throws IOException {
				
		// Get incoming socket
		SocketChannel client = serverSocket.accept();
		// configure so selector will monitor and attach a buffer
		client.configureBlocking(false);
		
		// Let's put on a keyAttachment
		KeyAttachment keyAttachment = new KeyAttachment();
		keyAttachment.read = true;
		keyAttachment.id = serverTracking.getAndIncrementConnectionID();
		serverTracking.addNewConnection(keyAttachment.id);
		serverTracking.incrementConnectionsCount();
		
		client.register(selector, SelectionKey.OP_READ, keyAttachment);
		System.out.println("New client registered");
		key.interestOps(key.interestOps() | (SelectionKey.OP_ACCEPT)); // reset to accept again

	}

}
