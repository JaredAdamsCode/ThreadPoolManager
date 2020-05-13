package cs455.scaling.server;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import cs455.scaling.client.*;
import cs455.scaling.util.*;

public class ThreadPoolManager {

	public static Timer timer;
	public static Timer statsTimer;
	
	public LinkedList<SelectionKey> acceptBatch;

	public LinkedList<SelectionKey> taskBatch;
	public LinkedList<LinkedList<SelectionKey>> taskQueue;
	public LinkedList<LinkedList<SelectionKey>> acceptQueue;

	public Selector selector;	
	public ServerSocketChannel serverSocket;
	
	public Toggle toggle;
	
	public TimerTask timerTask;
	
	public ServerTracking serverTracking;
	
	public TimerTask statsTimerTask;
	
	public int threadPoolSize;
	public int batchSize;
	public int batchTime;
	
	
	public ThreadPoolManager(Selector selector, ServerSocketChannel serverSocket, int tpSize, int batchSize, int batchTime ) {
		this.acceptBatch = new LinkedList<SelectionKey>();
		this.taskBatch = new LinkedList<SelectionKey>();

		this.taskQueue = new LinkedList<LinkedList<SelectionKey>>();
		this.acceptQueue = new LinkedList<LinkedList<SelectionKey>>();

		this.selector = selector;
		this.serverSocket = serverSocket;
		
		this.threadPoolSize = tpSize;
		this.batchSize = batchSize;
		this.batchTime = batchTime;
		
		this.toggle = new Toggle();
		
		this.timerTask = new BatchTimerTask(taskBatch, taskQueue, toggle);
		this.serverTracking = new ServerTracking();
		
		this.statsTimerTask = new StatsTimerTask(serverTracking);
				
	}
	
	public void createThreads() {
		for(int i = 0; i < threadPoolSize; i++) {
			Worker worker = new Worker(taskQueue, selector, serverSocket, acceptQueue, toggle, serverTracking);
			Thread workerThread = new Thread(worker, "Worker_Thread-" + i);
			workerThread.start();
		}
		createTimer();
		createStatsTimer();
	}
	
	private void createTimer() {
		timer = new Timer();
		timer.schedule(timerTask, batchTime, batchTime); // TODO: get batch time from command line
		
	}
	
	private void createStatsTimer() {
		statsTimer = new Timer();
		statsTimer.schedule(statsTimerTask, 20000, 20000);
	}
	
	private void resetTimer() {
		timer.cancel();
		timer = new Timer();
		timerTask = new BatchTimerTask(taskBatch, taskQueue, toggle);
		timer.schedule(timerTask, batchTime, batchTime);
	}
	
	public void checkSelectedKeys() throws ClosedChannelException {
		
				Iterator iter = selector.selectedKeys().iterator();
				
				while(iter.hasNext()) {
					// get current key
					SelectionKey key = (SelectionKey) iter.next();
					iter.remove();
					
						
					if(key.isValid() && key.isReadable() && key.attachment() != null ) {
						key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
						KeyAttachment keyAttachment = (KeyAttachment) key.attachment();
						keyAttachment.read = true;
						
						synchronized(taskBatch) {
							taskBatch.add(key);
							if(taskBatch.size() >= batchSize) {
								pushTaskBatchToTaskQueue();
								resetTimer();
							}
						}

						continue; // no need to re-accept if it's readable
					}
					
					if(key.isValid() && key.isAcceptable() ) {
						key.interestOps(key.interestOps() & (~SelectionKey.OP_ACCEPT));
						KeyAttachment keyAttachment = new KeyAttachment();
						keyAttachment.accept = true;
						key.attach(keyAttachment);
						acceptBatch.add(key);
						pushAcceptBatchToTaskQueue();
					
					}

				}
				
				selector.wakeup();
		
	}
	
	
	public void pushTaskBatchToTaskQueue() {
		
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
	
	public void pushAcceptBatchToTaskQueue() {
		
		synchronized(acceptQueue){
			synchronized(toggle) {
				acceptQueue.add(acceptBatch);
				acceptBatch = new LinkedList<SelectionKey>();
				toggle.checkAcceptsNotReads = true;
			}

		}


	}
	
}
