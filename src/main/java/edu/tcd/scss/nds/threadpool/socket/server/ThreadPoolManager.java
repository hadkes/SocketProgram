package edu.tcd.scss.nds.threadpool.socket.server;

import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ThreadPoolManager {
	private int maximumCapasity;
	private static ThreadPoolManager instance; 
	
	private Set<ResponderThread>  pool;
	
	private ThreadPoolManager(){
		maximumCapasity = 4;		
		pool = new HashSet<ResponderThread>(maximumCapasity);
	}
	
	public static ThreadPoolManager getInstance(){
		if(instance == null){
			instance = new ThreadPoolManager();
		}		
		return instance;
	}
	
	public int getMaximumPoolSize(){
		return maximumCapasity;
	}
	
	public int getCurrentPoolSize(){
		return pool.size();
	}
	
	public int getAvailableThreads(){
		int counter = 0;
		for (Iterator<ResponderThread> iterator = pool.iterator(); iterator.hasNext();) {
			ResponderThread thread = (ResponderThread) iterator.next();
			if(thread.getRunnbingStatus().equals(ResponderThread.STATUS_AVAILABLE)){
				++counter;
			}			
		}
		return counter;
	}
	
	public int getBusyThreads(){
		int counter = 0;
		for (Iterator<ResponderThread> iterator = pool.iterator(); iterator.hasNext();) {
			ResponderThread thread = (ResponderThread) iterator.next();
			if(thread.getRunnbingStatus().equals(ResponderThread.STATUS_BUSY)){
				++counter;
			}			
		}
		return counter;
	}

	public void performTask(Socket clientSocket){
		ResponderThread thread = getThreadFromPool();
    	thread.setClientSocket(clientSocket);
    	
    	System.out.println("Setting socket and update thread status from "+thread.getRunnbingStatus()+" of thread "+thread.getName()+" to "+ResponderThread.STATUS_BUSY);
    	thread.setRunningStatus(ResponderThread.STATUS_BUSY);    	
	}
	
	public void releaseTask(){
		ResponderThread responderThread = (ResponderThread)Thread.currentThread();
		System.out.println("Changing thread "+responderThread.getName()+" status from "+responderThread.getRunnbingStatus()+" to available.");
		responderThread.setRunningStatus(ResponderThread.STATUS_AVAILABLE);		
	}
	
	
	public ResponderThread getThreadFromPool(){
		System.out.println("Current size of pool "+pool.size());
		// ask client to wait for some time and try again
		if(pool.size() == maximumCapasity){
			System.out.println("Running out of resource. Can not create new thread please wait for some time.");
			return null;
		}
		
		// if threads exist in pool then check which one is free and return that
		for (Iterator<ResponderThread> iterator = pool.iterator(); iterator.hasNext();) {
			ResponderThread thread = (ResponderThread) iterator.next();
			String threadStatus = thread.getRunnbingStatus();
			if(threadStatus.equals(ResponderThread.STATUS_AVAILABLE)){
				System.out.println("Thread name "+thread.getName());
				System.out.println("Thread is available, returning "+thread.getName()+ "with status "+thread.getRunnbingStatus());
				System.out.println("This thread will wait for status change from "+thread.getRunnbingStatus()+" to "+ResponderThread.STATUS_BUSY);
				return thread;
			}
		}
		
		// if no thread exist in pool then create one add to pool and return back
		ResponderThread newThread = new ResponderThread();
		newThread.setRunningStatus(ResponderThread.STATUS_AVAILABLE);
		newThread.start();
		pool.add(newThread);
		System.out.println("Thread name "+newThread.getName());
		System.out.println("Created, set to busy and started new thread "+newThread.getName()); 
		System.out.println("This thread will wait for status change from "+newThread.getRunnbingStatus()+" to "+ResponderThread.STATUS_BUSY);
		
		return newThread;
	}
	
}
