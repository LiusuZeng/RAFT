package test;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import Exec.RoleTest;
import Message.LogEntry;

public class UserInterface {
	private static RoleTest role;
	private static int id;
	private static Thread role_thr;

	public static void main (String[] args){
		if (args.length != 1) {
			System.out.println("Wrong input. Now exit.");
			System.exit(0);
		}
		id = Integer.parseInt(args[0]);
		try {
			role = new RoleTest(id);
		} catch (IOException e) {
			System.out.println("Cannot initialize. Now exit.");
			System.exit(0);
		}
		role_thr = new Thread(role);
		role_thr.start();

		// begin to take user input, "info" or "quit"
		Scanner userinput = new Scanner(System.in);
		while(true) {
			System.out.println("\nType \"info\" to check this server's data or \n type \"quit\"to leave: ");
			String command = null;
			command = userinput.next();
			if (command.startsWith("quit")) {
				role.setAlive(false);
				//+ close listener
				System.out.println("Now quit.");
				System.exit(0);
			}
			else if (command.startsWith("info")) {
				printInfo();
			}
		}
	}

	public static synchronized void printInfo () {
		if (role!= null) {

			System.out.println("\nID:\t"+id);

			String state = null;
			int state_num = role.getState().getValue();
			switch (state_num) {
			case 0:
				state = "Leader";
			case 1:
				state = "Candidate";
			case 2:
				state = "Follower";
			}
			System.out.println("\nState: "+state);

			System.out.println("\nTerm: "+role.getTerm());

			System.out.println("\nLogs: ");
			List<LogEntry> logs = role.getLogs(0);
			for (LogEntry e : logs) {
				System.out.println("Index="+e.getIndex()+"\tTerm="+e.getTerm()+"\tInstrn="+e.getIns().getValue());
			}
		}
	}

}
