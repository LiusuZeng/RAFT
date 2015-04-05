package Exec;

import java.util.Date;
import java.util.List;
import java.util.Random;

import Constants.Constants;
import Message.AppendMsg;
import Message.LogEntry;
import Message.VoteMsg;

public class Candidate {
	private Role role;
	Random rand;
	int numAccept;
	int numReject;	
	long remainingTime;
	
	public Candidate(Role role) {
		this.role = role;		
		this.rand = new Random();
		init();
	}
	
	private void init() {
		this.numAccept = 0;
		this.numReject = 0;	
		this.remainingTime = Constants.requestTimeout;
	}
		
	public void election() {
		for(int index = 0; index < Constants.numServer; ++index) {
			if(index != role.ID) {
				role.sendVoteMsg(index);
			}
		}
	}
	
	public void run() {
		synchronized(role) {
			if(role.getState() != State.Candidate)
				return;
			init();
			role.prepareElection();
			election();
			while(remainingTime > 0) {
				//
				role.printDebug();
				//
				Date oldDate = new Date();
				try {
					role.wait(remainingTime);		
				} 
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally {
					if(role.getState() != State.Candidate)
						return;
				}
				
				Date newDate = new Date();
				remainingTime -= newDate.getTime()-oldDate.getTime();
				
				if(numAccept > Constants.numServer/2+1) {
					role.win();
					return;
				}
				else if(numReject > Constants.numServer/2+1) {
					role.lose();
					return;
				}
			}
			// backoff a randomized time		
			int backOffTime = rand.nextInt((int) Constants.backOffTimeout);
			try {
				role.wait(backOffTime);
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void incAccept() {
		++numAccept;
		notify();
	}
	
	public synchronized void incReject() {
		++numReject;
		notify();
	}
}