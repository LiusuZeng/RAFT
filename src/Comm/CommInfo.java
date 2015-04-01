package Comm;

public class CommInfo {
	
	private int ID;
	private String ip;
	private int port;
	
	public CommInfo(int src_ID, String src_ip, int src_port)
	{
		this.ID = src_ID;
		this.ip = src_ip;
		this.port = src_port;
	}
	
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
