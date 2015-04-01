package Exec;

import java.util.Date;

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
			Constants.Constants.heartbeatTimeout) {
			return true;
		}
		else
			return false;
	}
	
	public void run() {
		synchronized(role) {
			while(true) {
				assert(role.getState() == State.Follower);
				if(isTimeout()) {
					role.elect();
					return;
				}
				else
				{
					try {
						wait(Constants.Constants.refreshRate);
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
