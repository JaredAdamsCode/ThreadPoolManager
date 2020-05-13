package cs455.scaling.server;

import java.nio.channels.SelectionKey;
import java.util.LinkedList;
import java.util.TimerTask;

public class BatchTimerTask extends TimerTask{
	
	public LinkedList<SelectionKey> taskBatch;
	public LinkedList<LinkedList<SelectionKey>> taskQueue;
	public Toggle toggle;

	
	public BatchTimerTask(LinkedList<SelectionKey> taskBatch, LinkedList<LinkedList<SelectionKey>> taskQueue,
			Toggle toggle) {
		
		this.taskBatch = taskBatch;
		this.taskQueue = taskQueue;
		this.toggle = toggle;
	}

	@Override
	public void run() {

		synchronized(taskBatch) {

			synchronized(taskQueue){
				
				synchronized(toggle) {

					LinkedList<SelectionKey> tempKeys = new LinkedList<SelectionKey>();
					for(SelectionKey sk : taskBatch) {
						tempKeys.add(sk);
					}
					taskQueue.add(tempKeys);					
					taskBatch.clear();
					toggle.checkAcceptsNotReads = false;

				}

			}

		}
		
	}



}
