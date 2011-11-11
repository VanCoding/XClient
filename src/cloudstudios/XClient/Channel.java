package cloudstudios.XClient;


public class Channel {
	private Client device;
	private boolean input;
	private int channel;
	private ChannelEventReceiver eventreceiver;
  
	boolean mute;
	int delay;
  
  
	public Channel(Client device,boolean input, int channel){
		this.device = device;
		this.input = input;
		this.channel = channel;
	
		
	}
	
	public void LoadSettings(){
		Get("MUT0");
		Get("DLY3");
	}
  
	public boolean getInput(){
		return input;
	}
	public int getNumber(){
		return channel;
	}
	
	public void setMute(boolean mute){
		this.mute = mute;
		Set("MUT0",mute?1:0);
		eventreceiver.OnMuteChanged();
	}
	public void setMuteAsync(boolean mute){
		device.async(Client.MUTE, mute);
	}
	public boolean getMute(){
		return mute;
	}
	
	public void setDelay(int val){
		this.delay = val;
		Set("DLY3",val);
		eventreceiver.OnDelayChanged();
	}
	public void setDelayAsync(int val) {
		device.async(Client.DELAY);
	}
	public int getDelay(){
		return delay;
	}	

	public void Set(String command, int data){
		device.write(new Command(device,this,command,data));
		device.read();
	}
	public void Get(String command){
		device.write(new Command(device,this,command));
		Command c = device.read();
		String s = c.getCommand();
		if(s.equals("MUT0")){
			mute = c.getData()==1?true:false;
		}else if(s.equals("DLY3")){
			delay = c.getData();			
		}
	}
}
