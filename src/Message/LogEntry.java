package Message;

import java.io.Serializable;

public class LogEntry implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int term;
	private int index;
	private Instruction ins;
	
	public LogEntry(int term, int index, Instruction ins) {
		super();
		this.term = term;
		this.index = index;
		this.ins = ins;
	}

	public int getTerm() {
		return term;
	}

	public int getIndex() {
		return index;
	}

	public Instruction getIns() {
		return ins;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof LogEntry) {
			LogEntry rhs = (LogEntry)obj;
			return rhs.term == this.term && rhs.index == this.index;
		}
		else {
			return false;
		}
	}
}
