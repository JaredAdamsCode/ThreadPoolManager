package cs455.scaling.client;

import java.sql.Timestamp;

public class ClientTracking {
	
	private Integer messagesSent;
	private Integer messagesReceived;
	
	public ClientTracking() {
		this.messagesSent = 0;
		this.messagesReceived = 0;
	}
	
	public synchronized void resetTracking() {
		messagesSent = 0;
		messagesReceived = 0;
	}
	
	public synchronized void incrementSent() {
		messagesSent += 1;
	}
	
	public synchronized void incrementReceived() {
		messagesReceived += 1;
	}
	
	public synchronized void printStats() {
		
		Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
		System.out.println(timeStamp + "  Total Sent Count: " + messagesSent + ", Total Received Count: " + messagesReceived);
		
	}

}
