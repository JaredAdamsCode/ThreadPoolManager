package cs455.scaling.client;

import java.util.TimerTask;

public class ClientStatsTimerTask extends TimerTask {

	public ClientTracking clientTracking;
	
	public ClientStatsTimerTask(ClientTracking clientTracking){
		
		this.clientTracking = clientTracking;
		
	}
	
	
	@Override
	public void run() {
		synchronized(clientTracking) {
			clientTracking.printStats();
			clientTracking.resetTracking();
		}
		
	}

}
