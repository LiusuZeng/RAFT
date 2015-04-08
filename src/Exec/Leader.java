package Exec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import Constants.Constants;
import Message.Instruction;
import Message.LogEntry;
import Message.Request;

public class Leader {
	// volatile state
	Role role;
	private Date timeStamp;
	private int[] nextIndex; // next log index to send to that server
	private int[] matchIndex; // highest matched index

	private List<Request> rqstList;
	
	//CX
	private int appendFrequency = Constants.appendFrequency;
	
	
	//private int chance = 0; // LZ: chance > 50, sleep 10 sec
	//private boolean con = true;

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
		// LZ: chance
		//Random seed = new Random();
		//chance = seed.nextInt(100);
	}

	public void setNextIndexByID(int ID, int index) {
		nextIndex[ID] = index;
		return;
	}

	public void setMatchIndexByID(int ID, int index) {
		assert(matchIndex[ID] <= index);
		assert(matchIndex[ID] < nextIndex[ID]);
		if(role.getLog(index).getTerm() == role.getTerm()) {
			matchIndex[ID] = index;
		}
		else
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
				// LZ
				//System.out.println("Check nextIndex:");
				//for(int i = 0; i < this.nextIndex.length; i++)
				//{
				//	System.out.print(this.nextIndex[i] + " ");
				//}
				//System.out.println("Check end!");
				//
				Date timeStop1 = new Date(); // LZ
				role.sendAppendMsg(index, role.getLog(nextIndex[index]-1).getTerm(), 
						nextIndex[index]-1, role.getLogs(nextIndex[index]));
				//System.out.println("nextIndex[index]="+nextIndex[index]+" role.getLogs="+role.getLogs(nextIndex[index]));
				
				Date timeStop2 = new Date(); 
				//System.out.println("[DEBUG] @@"+index+"@@ time cost of setCommit: " + (long)(timeStop2.getTime() - timeStop1.getTime()));


			}
		}		
	}
	
//	//CX
//	public void appendRealLog() {
//
//		for(int index = 0; index < Constants.numServer; ++index) {
//			if(index != role.ID) {
//				// LZ
//				System.out.println("Check nextIndex:");
//				for(int i = 0; i < this.nextIndex.length; i++)
//				{
//					System.out.print(this.nextIndex[i] + " ");
//				}
//				System.out.println("Check end!");
//				//
//				Date timeStop1 = new Date(); // LZ
//				role.sendAppendMsg(index, role.getLog(nextIndex[index]).getTerm(), 
//						nextIndex[index], role.getLogs(nextIndex[index]));
//				Date timeStop2 = new Date(); 
//				System.out.println("[DEBUG] @@"+index+"@@ time cost of setCommit: " + (long)(timeStop2.getTime() - timeStop1.getTime()));
//
//
//			}
//		}		
//	}
//	//
	
	public int getCommitIndex(int[] matchIndex)
	{
		int length = matchIndex.length;
		int[] tmp = new int[length];
		System.arraycopy(matchIndex, 0, tmp, 0, length );
		Arrays.sort(tmp);
		assert(length%2 == 1);
		return tmp[(length+1)/2];
	}
	
	// Lz
	public void printfMatchIndex()
	{
		for(int i = 0; i < this.matchIndex.length; i++)
		{
			System.out.print("Val at pos " + i + ": " + this.matchIndex[i] + "\t");
		}
		System.out.println("");
	}
	//

	public void run() {
		synchronized(role) {
			while(role.getState() == State.Leader) {
				//
				role.printDebug();
				matchIndex[role.ID] = role.getLastIndex();
				// LZ: if great chance, sleep
				/*
				if(con && chance > 50)
					System.out.println("Going 2 sleep!");
				con = false;
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				*/
				//
				List<Request> rqsts = null; //getRequest();
				if(rqsts != null) {
					// convert request to log entry
					List<LogEntry> logsToAppend = requestsToLogs(rqsts);
					role.appendLogs(logsToAppend);
				}
				// periodically send heart beat
				Date currentTime = new Date();
				long timeRemaining = Constants.heartbeatRate-currentTime.getTime()+
						timeStamp.getTime();
				// LZ: debug leader heartbeat timing
				/*
				System.out.println("Calc remain time: " + timeRemaining);
				System.out.println("timestamp time: " + timeStamp.getTime());
				System.out.println("current   time: " + currentTime.getTime());
				 */
				if(timeRemaining <= 0) {
					//Date timeStop1 = new Date(); // LZ
					role.setCommitIndex(getCommitIndex(matchIndex));
					//Date timeStop2 = new Date(); // LZ
					
					//CX
					if (appendFrequency <= 0) {
						// append to leader itself
						Random random = new Random();
						LogEntry newentry = new LogEntry(this.role.getTerm(), this.role.getLastIndex()+1, new Instruction(random.nextInt(100)));
						List<LogEntry> newlist = new ArrayList<LogEntry>();
						newlist.add(newentry);
						this.role.appendLogs(newlist);
						appendFrequency = Constants.appendFrequency;
						//appendRealLog();
						
					}
					else {
						appendFrequency--;
						
					}
					//
					
					heartbeat();
					
					//heartbeat();
					//Date timeStop3 = new Date(); // LZ
					//System.out.println("[DEBUG] time cost of setCommit: " + (long)(timeStop2.getTime() - timeStop1.getTime()));
					//System.out.println("[DEBUG] time cost of heartbeat: " + (long)(timeStop3.getTime() - timeStop2.getTime()));
					// LZ: debug leader heartbeat sending
					System.out.println("Heartbeat sent!");
					//
					timeStamp = currentTime;
				}
				else {
					try {
						//System.out.println("qnmlgb!!!");
						role.wait(timeRemaining);
						//System.out.println("iܳ");
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
