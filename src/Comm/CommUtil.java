package Comm;

import java.util.*;
import java.io.*;

import Message.Instruction;
import Message.LogEntry;

public class CommUtil {

	public static HashMap<Integer, CommInfo> parse(File src)
	{
		try {
			HashMap<Integer, CommInfo> ret = new HashMap<Integer, CommInfo>();
			ArrayList<CommInfo> raw_ret = splitStr(readByLine(src));
			for(int i = 0; i < raw_ret.size(); i++)
			{
				ret.put(raw_ret.get(i).getEntryID(), raw_ret.get(i));
			}
			//
			return ret;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static List<LogEntry> recoverLogging(File src)
	{
		List<LogEntry> ret = new ArrayList<LogEntry>(0);
		try {
			ArrayList<String> scripts = readByLine(src);
			for(int i = 0; i < scripts.size(); i++)
			{
				execSingle(ret, scripts.get(i));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.print("Error in local log file!");
			return null;
		}
		//
		return ret;
	}

	protected static void execSingle(List<LogEntry> ret, String step) throws Exception
	{
		String[] raw_data = step.split(" |,");
		int[] para = null;
		int temp = -1;
		int ptr = 0;
		boolean flag = true;
		if(raw_data[0].equals("DELETE")) para = new int[2];
		else
		{
			para = new int[3];
			flag = false;
		}
		//
		for(int i = 0; i < raw_data.length; i++)
		{
			try {
				temp = Integer.parseInt(raw_data[i]);
			} catch (NumberFormatException e)
			{}
			//
			if(temp >= 0)
			{
				para[ptr++] = temp;
				temp = -1;
			}
		}
		// perform execution
		// DELETE [a,b)
		if(flag) ret.subList(para[0], para[1]).clear();
		// APPEND
		else ret.add(new LogEntry(para[0], para[1], new Instruction(para[2])));
	}

	protected static ArrayList<String> readByLine(File src) throws IOException
	{
		ArrayList<String> ret = new ArrayList<String>(0);
		FileReader reader = new FileReader(src);
		BufferedReader br = new BufferedReader(reader);
		String new_entry = null;
		//
		while((new_entry = br.readLine()) != null)
		{
			ret.add(new_entry);
		}
		//
		br.close();
		reader.close();
		//
		return ret;
	}

	protected static ArrayList<CommInfo> splitStr(ArrayList<String> src)
	{
		ArrayList<CommInfo> ret = new ArrayList<CommInfo>(0);
		//
		for(int i = 0; i < src.size(); i++)
		{
			String temp = src.get(i);
			String[] para = temp.split(" ");
			if(para.length != 3) throw new IllegalArgumentException();
			//
			int myID = Integer.parseInt(para[0]);
			String myIP = new String(para[1]);
			int myPort = Integer.parseInt(para[2]);
			ret.add(new CommInfo(myID, myIP, myPort));
		}
		//
		return ret;
	}
}
