package cloudstudios.XClient;

import java.net.Socket;

public class Channel {
	private Socket connection;
	private int device;
	private boolean input;
	private int channel;
  
	boolean mute;
	int delay;
  
  
	public Channel(Socket connection,int device, boolean input, int channel){
		this.connection = connection;
		this.device = device;
		this.input = input;
		this.channel = channel;
	
		Get("MUT0");
		Get("DLY3");
	}
  

	public void Set(String cmd, int data){
		Execute(new Command(cmd,data));
	}
	public void Get(String cmd){
		Execute(new Command(true,cmd));
	}
	public void Execute(Command cmd){
		try {
			cmd.setDevice(device);
			cmd.setChannel(input, channel);
			this.connection.getOutputStream().write(cmd.getBytes());
      
			cmd = new Command(this.connection);
			String c = cmd.getCommand();
			int d = cmd.getData();
      
			if(c.equals("MUT0")){
				mute = d==0?false:true;
			}else if(c.equals("DLY3")){
				delay = d;
			} 
		} catch (Exception e) {
		}
	}
    
  
	public void setMute(boolean on){
		Set("MUT0",on?1:0);   
	}
	public boolean getMute(){
		return mute;
	}
	public void setDelay(int val){
		Set("DLY3",val);
	}
	public int getDelay(){
		return delay;
	}
}
