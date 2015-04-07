package test;

import java.io.IOException;
import java.util.ArrayList;

import Exec.Role;
import Exec.State;
import Message.LogEntry;

public class StressTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		System.out.println("Stress Test parameters initializing...");
		Role[] group = new Role[5];
		Thread[] myThr = new Thread[5];
		for(int i = 0; i < group.length; i++)
		{
			group[i] = new Role(i);
			myThr[i] = new Thread(group[i]);
			myThr[i].start();
		}
		// Find the leader
		int sel = -1;
		while(true)
		{
			for(int i = 0; i < group.length; i++)
			{
				if(group[i].getState() == State.Leader)
				{
					sel = i;
					break;
				}
			}
			//
			if(sel != -1) break;
		}
		//
		System.out.println("Stress Test begins...");
		System.out.println("Stress Test Thread sleeping (5s)...");
		Thread.sleep(5000);
		// Pause the current leader
		group[sel].pause();
		//
		System.out.println("Stress Test Thread waits for the rest to have a new leader and modify their logs (5s)...");
		Thread.sleep(10000);
		//
		System.out.println("Now resume the initial leader...");
		group[sel].resume();
		//
		System.out.println("Wait for syc (10s)...");
		Thread.sleep(5000);
		// Find the leader again
		int sel2 = -1;
		while(true)
		{
			for(int i = 0; i < group.length; i++)
			{
				if(group[i].getState() == State.Leader)
				{
					sel2 = i;
					break;
				}
			}
			//
			if(sel2 != -1) break;
		}
		//
		System.out.println("Now stop everything and check logs (5s)...");
		for(int i = 0; i < group.length; i++)
		{
			myThr[i].stop();
			group[i].terminator();
		}
		Thread.sleep(5000);
		//
		System.out.println("Now check the nums...");
		group[sel2].getLeaderInst().printfMatchIndex();
		//
		System.out.println("sel: " + sel + " sel2: " + sel2);
	}
}
