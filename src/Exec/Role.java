package Exec;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import Comm.Comm;
import Constants.Constants;
import Message.AckAppendMsg;
import Message.AckVoteMsg;
import Message.AppendMsg;
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
	
	private List<LogEntry> logs;
	
	// volatile state
	private int commitIndex; // materialized
	private int appliedIndex; // already applied, in memory
	
	public final Comm comm;
	
	// other parameters
	private boolean alive;
	
	public Role(int ID) throws FileNotFoundException {
		this.logFile = new PrintWriter(Constants.logFile+ID);
		this.state = State.Follower;
		this.leader = null;
		this.candidate = null;
		this.follower = null;
		this.ID = ID;
		this.term = 0;
		this.votedFor = -1;
		this.leaderID = -1;
		this.logs = new ArrayList<LogEntry>();
		this.commitIndex = -1;
		this.appliedIndex = -1;
		this.comm = new Comm(this);
		this.alive = true;
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
			return null;
	}
	
	public synchronized List<LogEntry> getLogs(int startIndex) {
		return logs.subList(startIndex, logs.size());
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
			ackAppendMsgHandler((AckAppendMsg)msg);				
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
		if(state != State.Leader) {
			return;
		}
		int aaTerm = aamsg.getTerm();
		if(aaTerm > term) {
			state = State.Follower;
			term = aaTerm;
			votedFor = -1;
			leaderID = aamsg.getLeaderID();
		}
		else if(aamsg.getTerm() == term) {
			assert(aamsg.getLeaderID() == ID);
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
				writeDeleteLogs(lastCommonIndex+1, logs.size());
				logs.subList(lastCommonIndex+1, logs.size()).clear();				
				// second appmsg logs from the amsg
				logs.addAll(amsg.getLogs());
				writeAppendLogs(lastCommonIndex+1, logs.size());
				commitIndex = Math.min(logs.size()-1,  leaderCommittedIndex);
				return true;
			}
			else {
				return false;
			}
		}
		else {
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
	
	public void sendAppendMsg(int recvID, int prevTerm, int prevIndex, 
			List<LogEntry> logToAppend) {
		AppendMsg amsg = new AppendMsg(term, prevTerm, prevIndex, ID,
			commitIndex, logToAppend);
		// call COMM function to send
		comm.send(recvID, amsg);
	}
	
	public void sendVoteMsg(int recvID) {
		VoteMsg vmsg = new VoteMsg(term, 
				logs.get(logs.size()-1).getTerm(),
				logs.size()-1, ID);
		// call COMM function to send
		comm.send(recvID, vmsg);
	}
	
	public void sendAckAppendMsg(int recvID, boolean success) {
		AckAppendMsg aamsg = 
				new AckAppendMsg(leaderID, term, ID, success, logs.size()-1);
		comm.send(recvID, aamsg);
	}
	
	public void sendAckVoteMsg(int recvID, boolean success) {
		AckVoteMsg avmsg = new AckVoteMsg(recvID, term, ID, success);
		comm.send(recvID, avmsg);
	}	
}

