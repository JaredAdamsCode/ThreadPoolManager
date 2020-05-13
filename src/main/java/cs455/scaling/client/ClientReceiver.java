package cs455.scaling.client;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import cs455.scaling.util.Protocols;

public class ClientReceiver implements Runnable{

	public SocketChannel socketChannel;
	public ByteBuffer buffer;
	public LinkedList<String> pendingHashes;
	public ClientTracking clientTracking;
	
	public volatile boolean run;
	
	public ClientReceiver(SocketChannel client, LinkedList<String> pendingHashes, ClientTracking clientTracking) {
		this.socketChannel = client;
		this.buffer = ByteBuffer.allocate(Protocols.HASH_MESSAGE_CAPACITY);
		this.pendingHashes = pendingHashes;
		this.clientTracking = clientTracking;
		
	}
	
	@Override
	public void run() {
		while(true) {
			int bytesRead = 0;
			while ( buffer.hasRemaining() && bytesRead != -1 ) {
				try {
					bytesRead = socketChannel.read( buffer );
					if(bytesRead == -1) {
						System.out.println("Server closed connection. Terminating thread...");
						return;
					}
					buffer.rewind();
					String response = new String(buffer.array()).trim();
					checkHash(response);
					buffer.clear();
					
					clientTracking.incrementReceived();

				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		}

	}
	
	private void checkHash(String hash) {
		synchronized(pendingHashes) {
			if(pendingHashes.contains(hash)) {
				pendingHashes.removeFirstOccurrence(hash);
			}

		}

	}
	

}
