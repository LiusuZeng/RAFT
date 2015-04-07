package Comm;

import java.net.InetSocketAddress;

public class CommInfo {
	
	private int ID;
	private String ip;
	private int port;
	private InetSocketAddress inetAddr; 
	
	public CommInfo(int src_ID, String src_ip, int src_port)
	{
		this.ID = src_ID;
		this.ip = src_ip;
		this.port = src_port;
		inetAddr = new InetSocketAddress(ip, port);
	}
	
	// edited by eclipce
	public int hashCode() {
		return inetAddr.hashCode();
	}
	
	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(!(o instanceof CommInfo))
			return false;
		CommInfo O = (CommInfo)o;
		if(inetAddr.equals(O)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public InetSocketAddress getSocketAddress() {
		return inetAddr;
	}
	// edited by eclipce
	
	public int getEntryID()
	{
		return this.ID;
	}
	
	public String getEntryIP()
	{
		return this.ip;
	}
	
	public int getEntryPort()
	{
		return this.port;
	}
}
