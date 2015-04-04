package Tests.Networking;

import Exec.Role;
import Message.AckAppendMsg;
import Message.AckVoteMsg;
import Message.AppendMsg;
import Message.LogEntry;
import Message.VoteMsg;

import java.util.*;
import java.io.*;

public class TestRole extends Role {

	public TestRole(int id) throws IOException
	{
		super(id);
	}
	
	/********SEND SERIES*******/
	@Override
	public void sendAppendMsg(int recvID, int prevTerm, int prevIndex, List<LogEntry> logToAppend)
	{
		AppendMsg amsg = new AppendMsg(1, prevTerm, prevIndex, this.ID, 0, logToAppend);
		this.comm.send(recvID, amsg);
	}
	
	@Override
	public void sendVoteMsg(int recvID)
	{
		VoteMsg vmsg = new VoteMsg(0, 0, 0, this.ID);
		this.comm.send(recvID, vmsg);
	}
	
	@Override
	public void sendAckAppendMsg(int recvID, boolean success)
	{
		AckAppendMsg aamsg = new AckAppendMsg(0, 0, this.ID, success, 0);
		this.comm.send(recvID, aamsg);
	}
	
	@Override
	public void sendAckVoteMsg(int recvID, boolean success)
	{
		AckVoteMsg avmsg = new AckVoteMsg(recvID, 0, this.ID, success);
		this.comm.send(recvID, avmsg);
	}
	
	
	/********HANDLER SERIES*******/
	@Override
	public synchronized void ackAppendMsgHandler(AckAppendMsg aamsg)
	{
		System.out.println("Machine " + this.ID + " has received ackAppendMsg");
	}
	
	@Override
	public synchronized void ackVoteMsgHandler(AckVoteMsg avmsg)
	{
		System.out.println("Machine " + this.ID + " has received ackVoteMsg");
	}
	
	@Override
	public synchronized void appendMsgHandler(AppendMsg amsg)
	{
		System.out.print("Machine " + this.ID + " has received appendMsg. Prepare to ack back.");
		this.sendAckAppendMsg(amsg.getLeaderID(), true);
	}
	
	@Override
	public synchronized void voteMsgHandler(VoteMsg vmsg)
	{
		System.out.println("Machine " + this.ID + " has received voteMsg. Prepare to ack back.");
		this.sendAckVoteMsg(vmsg.getCandidateID(), true);
	}
}
