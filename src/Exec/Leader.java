package Exec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import Constants.Constants;
import Message.LogEntry;
import Message.Request;

public class Leader {
	// volatile state
	Role role;
	private Date timeStamp;
	private int[] nextIndex; // next log index to send to that server
	private int[] matchIndex; // highest matched index

	private List<Request> rqstList;

	public Leader(Role role) {
		this.role = role;
		//
		timeStamp = new Date();
		int numServer = Constants.numServer;
		nextIndex = new int[numServer];
		matchIndex = new int[numServer];
		for(int ID = 0; ID < numServer; ++ID) {
			matchIndex[ID] = -1;
			nextIndex[ID] = role.getLastIndex()+1;
		}
		matchIndex[role.ID] = role.getLastIndex();
		rqstList = new ArrayList<Request>(); 
	}

	public void setNextIndexByID(int ID, int index) {
		nextIndex[ID] = index;
		return;
	}

	public void setMatchIndexByID(int ID, int index) {
		assert(matchIndex[ID] <= index);
		assert(matchIndex[ID] < nextIndex[ID]);
		matchIndex[ID] = index;
		return;
	}

	private synchronized List<Request> getRequest() {
		if(rqstList.isEmpty())
			return null;
		else {
			List<Request> rqsts = rqstList;
			rqstList = new ArrayList<Request>();
			return rqsts;
		}
	}

	public synchronized void putRequest(Request rqst) {
		rqstList.add(rqst);
	}

	public List<LogEntry> requestsToLogs(List<Request> requests) {
		return null;
	} 

	public void heartbeat() {		
		for(int index = 0; index < Constants.numServer; ++index) {
			if(index != role.ID) {
				role.sendAppendMsg(index, role.getLog(nextIndex[index]).getTerm(), 
						nextIndex[index], role.getLogs(nextIndex[index]));
			}
		}		
	}

	public int getCommitIndex(int[] matchIndex)
	{
		int length = matchIndex.length;
		int[] tmp = new int[length];
		System.arraycopy(matchIndex, 0, tmp, 0, length );
		Arrays.sort(tmp);
		assert(length%2 == 1);
		return tmp[(length+1)/2];
	}

	public void run() {
		synchronized(role) {
			while(role.getState() == State.Leader) {
				//
				role.printDebug();
				//
				List<Request> rqsts = getRequest();
				if(rqsts != null) {
					// convert request to log entry
					List<LogEntry> logsToAppend = requestsToLogs(rqsts);
					role.appendLogs(logsToAppend);
				}
				// periodically send heart beat
				Date currentTime = new Date();
				long timeRemaining = currentTime.getTime()-
						timeStamp.getTime()-Constants.heartbeatRate;
				// LZ: debug leader heartbeat timing
				System.out.println("Calc remain time: " + timeRemaining);
				//
				//				if(timeRemaining <= 0) {
				//					role.setCommitIndex(getCommitIndex(matchIndex));
				//					heartbeat();
				//					// LZ: debug leader heartbeat sending
				//					System.out.println("Heartbeat sent!");
				//					//
				//					timeStamp = currentTime;
				//				}
				//				else {
				//					try {
				//						role.wait(timeRemaining);
				//					} 
				//					catch (InterruptedException e) {
				//						// TODO Auto-generated catch block
				//						e.printStackTrace();
				//					}
				//				}

				if (timeRemaining < 0) {
					try {
						role.wait(-1*timeRemaining);
					} 
					catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					currentTime = new Date();
					timeRemaining = currentTime.getTime()-
							timeStamp.getTime()-Constants.heartbeatRate;
				}
				role.setCommitIndex(getCommitIndex(matchIndex));
				heartbeat();
				// LZ: debug leader heartbeat sending
				System.out.println("Heartbeat sent!");
				//
				timeStamp = currentTime;

			}
		}
	}
}
