package Comm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;

import Constants.Constants;
import Exec.Role;

public class Comm implements Runnable {

	private HashMap<Integer, CommInfo> sysConfig = null;
	private Role role;
	private String motherIP;
	private int motherPort;
	private boolean alive; // edited by eclipce
	// LZ: server UDP
	private DatagramSocket server;

	public Comm(Role src_role) throws SocketException {
		// Load configuration info...
		File file = new File(Constants.confFile);
		this.sysConfig = CommUtil.parse(file);
		// get op obj
		this.role = src_role;
		this.motherIP = this.sysConfig.get(this.role.ID).getEntryIP();
		this.motherPort = this.sysConfig.get(this.role.ID).getEntryPort();
		
		this.alive = true; // edited by eclipce
		
		// LZ: client UDP
		this.server = new DatagramSocket(this.motherPort);
		
		// Start ManagerThr
		Thread ManagerThr = new Thread(this);
		ManagerThr.start();
	}

	public void send(int recvID, Object obj) {
		
		// edited by eclipce
		if(!alive)
			return;
		// edited by eclipce
		
		Date timeStop1 = new Date();
		//
		try {
			// edited by eclipce
			InetSocketAddress addr = sysConfig.get(recvID).getSocketAddress();
			System.out.println("###############send des: " + addr.toString());
			assert(addr != null);
			// edited by eclipce
			
			// LZ: Change networking communication to UDP
			ByteArrayOutputStream raw = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(raw);
			oos.writeObject(obj);
			byte[] data_pack = raw.toByteArray();
			//
			System.out.println("******************data packet size: " + data_pack.length);
			//
			raw.close();
			oos.close();
			// LZ: get send addr info and construct UDP packet
			DatagramPacket toBsent = new DatagramPacket(data_pack, data_pack.length, addr);
			this.server.send(toBsent);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Date timeStop2 = new Date();
			System.out.println("Failed connection time cost: " + (long)(timeStop2.getTime() - timeStop1.getTime()));
			return;
		}
		//
		Date timeStop3 = new Date();
		System.out.println("Success connection time cost: " + (long)(timeStop3.getTime() - timeStop1.getTime()));
	}

	public void Terminator()
	{
		try {
			this.server.close();
			// edited by eclipce
			if(sysConfig != null) {
				
			}	
			// edited by eclipce			
		}
		finally {
			alive = false;
		}
	}

	public void run() {
		// TODO Auto-generated method stub
		try {
			byte[] buffer = new byte[Constants.udpBufferSize];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			Object msg = null;
			//
			while(alive)
			{
				System.out.println("In while!!!!!!!");
				// LZ: accept UDP packet
				this.server.receive(packet);
				System.out.println("I recv packet! " + this.role.ID);
				// LZ: notify mallicious attack
				/*
				SocketAddress recv_addr = packet.getSocketAddress();
				assert(this.sysConfig.containsValue());
				*/
				// LZ: reconstruct obj
				byte[] raw = packet.getData();
				assert(raw != null);
				assert(raw.length != 0);
				ByteArrayInputStream bi = new ByteArrayInputStream(raw);
				ObjectInputStream ois = new ObjectInputStream(bi);
				msg = ois.readObject();
				// LZ: call recv
				this.role.recvMsg(msg);
			}
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Machine " + this.role.ID + " resources retrieved!");
			return;
		}
	}
}
