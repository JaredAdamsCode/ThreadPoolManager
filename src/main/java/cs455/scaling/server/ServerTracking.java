package cs455.scaling.server;

import java.sql.Timestamp;
import java.util.ArrayList;

public class ServerTracking {

	public Integer numberOfActiveConnections;
	
	public Integer totalMessagesProcessed;

	public Integer connectionID;

	public ArrayList<Integer> messagesPerClient;

	public ArrayList<Integer> clientsToRemoveOnReset;

	
	public ServerTracking() {
		this.numberOfActiveConnections = 0;
		this.totalMessagesProcessed = 0;
		this.connectionID = 0;
		
		this.messagesPerClient = new ArrayList<Integer>();
		this.clientsToRemoveOnReset = new ArrayList<Integer>();
	}
	
	public synchronized void incrementConnectionsCount() {
		numberOfActiveConnections++;
	}
	
	public synchronized void decrementConnectionsCount() {
		numberOfActiveConnections--;
	}
	
	public synchronized Integer getConnectionsCount() {
		return numberOfActiveConnections;
	}
	
	public synchronized void incrementTotalMessages() {
		totalMessagesProcessed++;
	}
	
	public synchronized Integer getTotalMessages() {
		return totalMessagesProcessed;
	}
	
	public synchronized void incrementConnectionID() {
		connectionID++;
	}
	
	public synchronized int getAndIncrementConnectionID() {
		int ret = connectionID;
		connectionID++;
		return ret;
	}
	
	public synchronized Integer getConnectionID() {
		return connectionID;
	}
	
	public synchronized void addNewConnection(int id) {
		messagesPerClient.add(id, 0);
	}
	
	public synchronized void removeConnection(int id) {
		clientsToRemoveOnReset.add(id);
	}
	
	public synchronized void incrementIndividualConnectionCount(int id) {
		Integer temp = messagesPerClient.get(id);
		temp++;
		messagesPerClient.set(id, temp);
	}
	
	private double standardDeviation() {
		double mean = meanThroughput();
		int count = 0;
		ArrayList<Double> copyList = new ArrayList<Double>();
		for(Integer i : messagesPerClient) {
			if(i == null) {
				continue;
			}
			else {
				double temp = (double) i;

				copyList.add(temp);
				count++;
			}
		}
		double sum = 0;
		for(Double j : copyList) {
			j = Math.pow(j - mean, 2);
			sum += j;
		}
				
		double ret = sum / count;
		
		return Math.sqrt(ret);
		
		
	}
	
	private double meanThroughput() {
		int numberOfClients = 0;
		for(Integer i : messagesPerClient) {
			if(i == null) {
				continue;
			}
			else {
				numberOfClients++;
			}
		}
		return (double) totalMessagesProcessed / numberOfClients;
		
	}
	
	public synchronized void printStats() {
		Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
		double mean = meanThroughput();
		double stdDev = standardDeviation();
		System.out.println("\n" + timeStamp + "  Server Throughput: " + totalMessagesProcessed + ", Active Client Connections: " + numberOfActiveConnections
				+ ", Mean Per-Client Throughput: " + mean + ", Std Dev of Per-Client Throughput: " + stdDev + "\n");
	}
	
	public synchronized void resetStats() {
		totalMessagesProcessed = 0;
		for(int i = 0; i < messagesPerClient.size(); i++) {
			if(messagesPerClient.get(i) == null) {
				continue;
			}
			else {
				messagesPerClient.set(i, 0);
			}
		}
		for(Integer i : clientsToRemoveOnReset) {
			messagesPerClient.set(i, null);
		}
		clientsToRemoveOnReset.clear();
	}
	
	
	
	
	
}
