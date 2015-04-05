package Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AppendMsg implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int term;
	private int leaderID;	
	
	private int prevTerm;
	private int prevIndex;
	
	private int commitedIndex;
	
	private ArrayList<LogEntry> logs;
	
	public AppendMsg(int term, int prevTerm, int prevIndex, int leaderID,
			int commitedIndex, ArrayList<LogEntry> logs) {
		super();
		this.term = term;
		this.leaderID = leaderID;
		
		this.prevTerm = prevTerm;
		this.prevIndex = prevIndex;
		this.commitedIndex = commitedIndex;
		this.logs = logs;
	}

	public int getTerm() {
		return term;
	}

	public int getLeaderID() {
		return leaderID;
	}

	public int getPrevTerm() {
		return prevTerm;
	}

	public int getPrevIndex() {
		return prevIndex;
	}

	public int getCommitedIndex() {
		return commitedIndex;
	}
	
	public List<LogEntry> getLogs() {
		return logs;
	}
}
