package Exec;

import java.util.Date;
import java.util.Random;

import Constants.Constants;

public class Follower {
	Role role;
	private Date timeStamp;
	
	public Follower(Role role) {
		this.role = role;
		timeStamp = new Date();
	}
	
	public synchronized void refreshTimeStamp() {
		timeStamp = new Date();
	}

	private synchronized boolean isTimeout() {
		Date currentTime = new Date();
		if(currentTime.getTime()-timeStamp.getTime() > 
			Constants.heartbeatTimeout) {
			return true;
		}
		else
			return false;
	}
	
	public void run() {
		synchronized(role) {
			while(true) {
				//
				role.printDebug();
				//
				assert(role.getState() == State.Follower);
				if(isTimeout()) {					
					Random new_rand = new Random();
					try {
						role.wait(new_rand.nextInt((int) Constants.heartbeatTimeout));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					role.elect();
					return;
				}
				else
				{
					try {
						role.wait(Constants.refreshRate);
					} 
					catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
}
