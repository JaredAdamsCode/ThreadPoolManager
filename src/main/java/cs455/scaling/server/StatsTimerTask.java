package cs455.scaling.server;

import java.util.TimerTask;

public class StatsTimerTask extends TimerTask{

	ServerTracking serverTracking;
	
	public StatsTimerTask(ServerTracking serverTracking) {
		this.serverTracking = serverTracking;
	}
	
	
	@Override
	public void run() {
		synchronized(serverTracking){
			serverTracking.printStats();
			serverTracking.resetStats();
		}
		
	}

}
