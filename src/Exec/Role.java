package Exec;

import java.io.*;
import java.util.*;

import Comm.Comm;
import Comm.CommUtil;
import Constants.Constants;
import Message.AckAppendMsg;
import Message.AckVoteMsg;
import Message.AppendMsg;
import Message.Instruction;
import Message.LogEntry;
import Message.VoteMsg;

public class Role implements Runnable{	
	private PrintWriter logFile;
	private State state;
	private Leader leader;
	private Candidate candidate;
	private Follower follower;

	public final int ID;
	private int term;
	private int votedFor;
	private int leaderID;

	private ArrayList<LogEntry> logs;

	// volatile state
	private int commitIndex; // materialized
	private int appliedIndex; // already applied, in memory

	public final Comm comm;

	// other parameters
	protected boolean alive;

	public Role(int ID) throws IOException {
		this.state = State.Follower;
		this.leader = null;
		this.candidate = null;
		this.follower = null;
		this.ID = ID;
		this.term = 0;
		this.votedFor = -1;
		this.leaderID = -1;
		this.comm = new Comm(this);
		this.alive = true;
		this.commitIndex = -1;
		this.appliedIndex = -1;
		//
		this.role_init();
		this.logFile = new PrintWriter(new FileWriter(Constants.logFile+ID, true));
	}

	private void role_init()
	{
		File myLogFile = new File(Constants.logFile+ID);
		if(myLogFile.exists()) this.logs = CommUtil.recoverLogging(myLogFile);
		if(this.logs == null || this.logs.isEmpty())
		{
			this.logs = new ArrayList<LogEntry>(0);
			this.logs.add(new LogEntry(0, 0, new Instruction(0)));
		}
	}

	public void run() {
		// TODO Auto-generated method stub
		while(isAlive()) {
			switch(state) {
			case Follower:
				if(follower == null) {
					follower = new Follower(this);
				}
				follower.run();
				break;
			case Candidate:
				if(candidate == null) {
					candidate = new Candidate(this);
				}
				candidate.run();
				break;
			case Leader:
				if(leader == null) {
					leader = new Leader(this);
				}
				leader.run();
				break;
			default:
				// nothing
			}
		}
	}

	public State getState() {
		return state;
	}

	public boolean isAlive() {
		return alive;
	}

	public int getLastIndex() {
		return logs.size()-1;
	}

	public synchronized void setCommitIndex(int commitIndex) {
		assert(this.commitIndex <= commitIndex);
		this.commitIndex = commitIndex;
	}

	public synchronized LogEntry getLog(int index) {
		if(index < logs.size())
			return logs.get(index);
		else
			return logs.get(logs.size() - 1); // LZ: modified to check leader election
	}

	public synchronized ArrayList<LogEntry> getLogs(int startIndex) {
		if(startIndex > logs.size()-1) return null; // LZ: modified to check leader election
		else return new ArrayList<LogEntry>(logs.subList(startIndex, logs.size()));
	}

	public int getTerm () {
		return term;
	}
	public int getVotedFor() {
		return votedFor;
	}

	public synchronized void win() {
		state = State.Leader;
	}

	public synchronized void lose() {
		state = State.Follower;
	}

	public synchronized void elect() {
		state = State.Candidate;
	}

	public synchronized void prepareElection() {
		assert(state == State.Candidate);
		++term;
		votedFor = ID;
		leaderID = -1;
	}	

	public void recvMsg(Object msg) {
		if(msg == null)
			return;
		else if(msg instanceof AckAppendMsg) {
			// LZ
			System.out.println("I got AckAppend Msg!!!!");
			//
			ackAppendMsgHandler((AckAppendMsg)msg);
			// LZ
			System.out.println("Handler called for AckAppend Msg!!!!");
			//
		}
		else if(msg instanceof AckVoteMsg) {
			ackVoteMsgHandler((AckVoteMsg)msg);			
		}
		else if(msg instanceof AppendMsg) {			
			appendMsgHandler((AppendMsg)msg);			
		}
		else if(msg instanceof VoteMsg) {
			voteMsgHandler((VoteMsg)msg);
		}
		else {
			// just ignore
		}
	}

	public synchronized void ackAppendMsgHandler(AckAppendMsg aamsg) {
		// LZ
		System.out.println("aamsg term: " + aamsg.getTerm() + " my term: " + term);
		//
		if(state != State.Leader) {
			return;
		}
		int aaTerm = aamsg.getTerm();
		if(aaTerm > term) {
			// LZ
			System.out.println("aaTerm > term");
			//
			state = State.Follower;
			term = aaTerm;
			votedFor = -1;
			leaderID = aamsg.getLeaderID();
		}
		else if(aamsg.getTerm() == term) {
			// LZ
			System.out.println("aaTerm == term");
			//
			assert(aamsg.getLeaderID() == ID);
			// LZ
			System.out.println("Get res: " + aamsg.getSuccess());
			//
			if(aamsg.getSuccess()) {				
				leader.setNextIndexByID(aamsg.getID(), 
						aamsg.getLastIndex()+1);
				leader.setMatchIndexByID(aamsg.getID(), 
						aamsg.getLastIndex());	
			}
			else {
				// guess harder!
				leader.setNextIndexByID(aamsg.getID(), 
						aamsg.getLastIndex()-1);	
			}
		}
		else {
			// just ignore
			// LZ
			System.out.println("aaTerm < term....");
			//
		}
	}

	public synchronized void ackVoteMsgHandler(AckVoteMsg avmsg) {
		if(state != State.Candidate) {
			return;
		}
		int avTerm = avmsg.getTerm();
		if(avTerm > term) {
			assert(avmsg.getSuccess() == false);
			state = State.Follower;
			term = avTerm;
			votedFor = -1;
			leaderID = avmsg.getLeaderID();
		}
		else if(avmsg.getTerm() == term) {
			if(avmsg.getSuccess())
				candidate.incAccept();
			else
				candidate.incReject();	
		}
		else {
			// just ignore
		}
	}

	public synchronized void appendMsgHandler(AppendMsg amsg) {		
		int aTerm = amsg.getTerm();
		boolean result = false;
		if(aTerm > term)
		{
			state = State.Follower;	
			term = aTerm;
			votedFor = -1;
			leaderID = amsg.getLeaderID();
			// refresh timer
			follower.refreshTimeStamp();
			// append log
			result = appendLogs(amsg);			
		}
		else if(aTerm == term) {
			assert(state != State.Leader);			
			if(leaderID == -1)
			{
				state = State.Follower;
				leaderID = amsg.getLeaderID();
			}
			assert(leaderID == amsg.getLeaderID());
			// refresh timer
			follower.refreshTimeStamp();
			// append log
			result = appendLogs(amsg);
		}
		// send back result to sender
		sendAckAppendMsg(amsg.getLeaderID(), result);
		if (!result) {
			System.out.println("append Msg Handler returns:"+result);
			System.out.println("asmg.log"+amsg.getLogs());
		}
	}

	public synchronized void voteMsgHandler(VoteMsg vmsg) {
		int voteTerm = vmsg.getCurrTerm();		
		boolean acceptVote = false;
		if(voteTerm > term) {
			int vLastTerm = vmsg.getLastAppliedTerm();
			int vLastIndex = vmsg.getLastAppliedIndex();
			if(vLastTerm >= logs.get(logs.size()-1).getTerm() && 
					vLastIndex >= logs.size()-1) {
				state = State.Follower;
				term = voteTerm;
				leaderID = -1;
				votedFor = vmsg.getCandidateID();
				// refresh timer
				follower.refreshTimeStamp();
				acceptVote = true;
			}
			// if not going to accept the voteMsg go down
		} 
		// if voteTerm == term assumption is no matter what state me is, ignore it
		// send hello with some details		
		sendAckVoteMsg(vmsg.getCandidateID(), acceptVote);
	}


	public synchronized boolean appendLogs(AppendMsg amsg) {
		assert(amsg.getLeaderID() == leaderID);
		assert(amsg.getTerm() == term);

		int lastCommonTerm = amsg.getPrevTerm();
		int lastCommonIndex = amsg.getPrevIndex();
		int leaderCommittedIndex = amsg.getCommitedIndex();
		if(lastCommonIndex < logs.size()) {
			if(logs.get(lastCommonIndex).getTerm() == lastCommonTerm) {
				// first delete entries after aLastAppliedIndex
				if(lastCommonIndex+1 < logs.size()) {
					writeDeleteLogs(lastCommonIndex+1, logs.size());
					logs.subList(lastCommonIndex+1, logs.size()).clear();
					// second appmsg logs from the amsg
					logs.addAll(amsg.getLogs());
					writeAppendLogs(lastCommonIndex+1, logs.size());
					commitIndex = Math.min(logs.size()-1,  leaderCommittedIndex);
				}
				// LZ
				assert(appliedIndex <= commitIndex);
				appliedIndex = commitIndex;
				return true;
			}
			else {
				if(lastCommonIndex == 0) {
					System.out.printf("lastCommonIndex : %d\n", lastCommonIndex);
					System.out.printf("lastCommonIndex Term : %d\n", logs.get(lastCommonIndex).getTerm());
					System.out.printf("lastCommonTerm Term : %d\n", lastCommonTerm);
				}
				return false;
			}
		}
		else {
			System.out.printf("lastCommonIndex : %d\n", lastCommonIndex);
			System.out.printf("Logs.size() : %d\n", logs.size());
			return false;
		}
	}

	public synchronized boolean appendLogs(List<LogEntry> logList) {
		if(state != State.Leader)
			return false;
		if(logList == null)
			return false;
		int startIndex = logs.size();
		logs.addAll(logList);
		int endIndex = logs.size();
		writeAppendLogs(startIndex, endIndex);
		return true;
	}

	public synchronized void writeDeleteLogs(int startIndex, int endIndex) {

		logFile.printf("DELETE IndexFrom: %d, IndexUntil: %d\n", 
				startIndex, endIndex-1);	
		return;
	}

	public synchronized void writeAppendLogs(int startIndex, int endIndex) {
		for(int i = startIndex; i < endIndex; ++i)
			logFile.printf("APPEND Term: %d, Index: %d, Value: %d\n", 
					term, i, logs.get(i).getIns().getValue());	
		return;
	}

	public synchronized void sendAppendMsg(int recvID, int prevTerm, int prevIndex, 
			ArrayList<LogEntry> logToAppend) {
		AppendMsg amsg = new AppendMsg(term, prevTerm, prevIndex, ID,
				commitIndex, logToAppend);
		// call COMM function to send
		
		Date timeStop1 = new Date(); // LZ
		
		comm.send(recvID, amsg);
		
		Date timeStop2 = new Date(); 
		System.out.println("[DEBUG] @@"+recvID+"@@ time cost of setCommit: " + (long)(timeStop2.getTime() - timeStop1.getTime()));

	}

	public synchronized void sendVoteMsg(int recvID) {
		VoteMsg vmsg = new VoteMsg(term, 
				logs.get(logs.size()-1).getTerm(),
				logs.size()-1, ID);
		// call COMM function to send
		comm.send(recvID, vmsg);
	}

	public synchronized void sendAckAppendMsg(int recvID, boolean success) {
		// LZ
		System.out.println("I ack the append msg!!!!");
		//
		AckAppendMsg aamsg = 
				new AckAppendMsg(leaderID, term, ID, success, logs.size()-1);
		comm.send(recvID, aamsg);
	}

	public synchronized void sendAckVoteMsg(int recvID, boolean success) {
		AckVoteMsg avmsg = new AckVoteMsg(recvID, term, ID, success);
		comm.send(recvID, avmsg);
	}
	
	// LZ debug
	public void printDebug()
	{
		System.out.println("***[DEBUG]***");
		System.out.println("ID: " + this.ID);
		System.out.println("State: " + this.getState());
		System.out.println("Term: " + this.getTerm());
		System.out.println("Last Index: " + this.getLastIndex());
		System.out.println("Voted for: " + this.getVotedFor() + "\n");

	}
}

