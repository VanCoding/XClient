package cloudstudios.XClient;

public class Channel{
	private Client device;
	private boolean input;
	private int channel;
	
	private DataField[] fields = new DataField[CommandType.ChannelName.ordinal()+1];
  
	public Channel(Client device,boolean input, int channel){
		this.device = device;
		this.input = input;
		this.channel = channel;
		
		for(int i = 0; i < fields.length; i++){
			fields[i] = new DataField(CommandType.values()[i]);
		}
	}
  
	public boolean getInput(){
		return input;
	}
	public int getNumber(){
		return channel;
	}
	
	public Client getClient(){
		return device;
	}
	
	public String getName(){
		return getField(CommandType.ChannelName).getString();
	}
	public void setName(String name){
		//todo
	}
	
	public boolean getMute(){
		return getField(CommandType.Mute).getValue() == 1;
	}
	public void setMute(boolean mute){
		device.write(new Command(device,this,CommandType.Mute,mute?1:0));
	}
	
	public int getLevel(){
		return getField(CommandType.SignalLevel).getValue();
	}
	public void setLevel(int level){
		device.write(new Command(device,this,CommandType.SignalLevel,level));
	}
	
	public int getDelay(){
		return getField(CommandType.SignalDelay).getValue();
	}
	public void setDelay(int delay){
		device.write(new Command(device,this,CommandType.SignalDelay,delay));
	}
	
	public DataField getField(CommandType type){
		for(int i = 0; i < fields.length; i++){
			if(fields[i].getType() == type){
				return fields[i];
			}
		}
		return null;
	}
}
