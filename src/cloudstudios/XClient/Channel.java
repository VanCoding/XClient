package cloudstudios.XClient;

import android.util.Log;


public class Channel {
	private Client device;
	private boolean input;
	private int channel;
	private ChannelEventReceiver eventreceiver;
  
	boolean mute = true;
	int level = 0;
	int delay = 0;	
	int gain = 0;
  
	public Channel(Client device,boolean input, int channel){
		this.device = device;
		this.input = input;
		this.channel = channel;		
	}
	
	public void load(){
		Get("MUT0");
		Get("DLY3");
		Get("LVL0");
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
		if(eventreceiver != null){
			eventreceiver.OnMuteChanged();
		}
	}
	public void setMuteAsync(boolean mute){
		device.async(Client.AsyncAction.Mute, this, mute);
	}
	public boolean getMute(){
		return mute;
	}
	public void setLevel(int val){
		this.level = val;
		Set("LVL0",val);
		if(eventreceiver != null){
			eventreceiver.OnLevelChanged();
		}
	}
	public void setLevelAsync(int val){
		device.async(Client.AsyncAction.Level,this,val);
	}
	
	public int getLevel(){
		return level;
	}
	public void setDelay(int val){
		this.delay = val;
		Set("DLY3",val);
		if(eventreceiver != null){
			eventreceiver.OnDelayChanged();
		}
	}
	public void setDelayAsync(int val) {
		device.async(Client.AsyncAction.Delay,this,val);
	}
	public int getDelay(){
		return delay;
	}
	public void setGain(int val){
		this.gain = val;
		Set("MIC0",val);
		if(eventreceiver != null){
			eventreceiver.OnGainChanged();
		}
	}
	public void setGainAsync(int val) {
		device.async(Client.AsyncAction.Gain,this,val);
	}
	public int getGain(){
		return gain;
	}
	
	
	public void Set(String command, int data){
		device.write(new Command(device,this,command,data));
		device.read();
	}
	public void Get(String command){
		device.write(new Command(device,this,command));
		Command c = device.read();
		String s = c.getCommand();
		Log.d("abc", "Set "+s+" to "+c.getData());
		if(s.equals("MUT0")){
			mute = c.getData()==1?true:false;			
		}else if(s.equals("DLY3")){
			delay = c.getData();			
		}else if(s.equals("LVL0")){
			level = c.getData();
		}
	}
}
