package Tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Comm.CommUtil;
import Message.Instruction;
import Message.LogEntry;

public class TestLogRecovery {
	
	private String logpath;
	private PrintWriter wrLog;
	private List<LogEntry> ans;
	
	public TestLogRecovery(String path)
	{
		this.logpath = path;
		try {
			// create new, no append
			this.wrLog = new PrintWriter(new FileWriter(new File(path), false));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Error in log file path!");
		}
		this.ans = new ArrayList<LogEntry>(0);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestLogRecovery mainBrain = new TestLogRecovery("./test_log.out");
		//
		try {
			mainBrain.perform();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		System.out.println("End of main.");
	}
	
	public void perform() throws Exception
	{
		Random myrand = new Random();
		System.out.println("Generating dummy log file...");
		// APPEND
		for(int i = 0; i < 10; i++)
		{
			this.SingleExec(false, i/3+1, i, myrand.nextInt(100));
		}
		// DELETE
		for(int x = 0; x < 3; x++)
		{
			int boundary = this.ans.size() - 1;
			if(boundary <= 0) break;
			//
			int op1 = myrand.nextInt(boundary);
			int op2 = myrand.nextInt(boundary);
			int big = op1 > op2 ? op1 : op2;
			int small = op1 > op2 ? op2 : op1;
			this.SingleExec(true, small, big, -1);
		}
		//
		this.wrLog.close();
		System.out.println("Generated! Resources retrieval completed!");
		// Verification begins
		List<LogEntry> res = CommUtil.recoverLogging(new File(this.logpath));
		if(verify(res)) System.out.println("Test passed!");
		else throw new Exception("Incorrect recovery result!");
	}
	
	public boolean verify(List<LogEntry> res)
	{
		if(this.ans.size() != res.size()) return false;
		else
		{
			for(int i = 0; i < this.ans.size(); i++)
			{
				LogEntry left = this.ans.get(i);
				LogEntry right = res.get(i);
				boolean eqcond = left.getIndex() == right.getIndex() && left.getTerm() == right.getTerm() && left.getIns().getValue() == right.getIns().getValue();
				if(!eqcond) return false;
			}
		}
		return true;
	}
	
	protected void SingleExec(boolean type, int para1, int para2, int para3)
	{
		// DELETE
		if(type)
		{
			this.wrLog.printf("DELETE IndexFrom: %d, IndexUntil: %d\n", para1, para2);
			wrLog.flush();
			this.ans.subList(para1, para2).clear();
		}
		// APPEND
		else
		{
			this.wrLog.printf("APPEND Term: %d, Index: %d, Value: %d\n", para1, para2, para3);
			wrLog.flush();
			this.ans.add(new LogEntry(para1, para2, new Instruction(para3)));
		}
	}
}
