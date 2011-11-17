package cloudstudios.XClient;


public class Command {	
	private String command = "";
	private Client device = null;
	private Channel channel = null;
	private int data = -1;
	private String aux = "";
	
	public Command(){
	}
	public Command(Client device, String command){
		setDevice(device);
		setCommand(command);
	}
	public Command(Client device, Channel channel, String command){
		this(device,command);
		setChannel(channel);
	}
	public Command(Client device, Channel channel, String command, int data){
		this(device,channel,command);
		setData(data);
	}
	public Command(Client device, String command, int data){
		this(device,command);
		setData(data);
	}
	
	
	public void setDevice(Client device){
		this.device = device;
	}
	public Client getDevice(){
		return device;
	}
	public void setChannel(Channel channel){
		this.channel = channel;
	}
	public Channel getChannel(){
		return channel;
	}
	public void setCommand(String command){
		this.command = command;
	}
	public String getCommand(){
		return command;
	}
	public void setData(int data){
		this.data = data;
	}
	public int getData(){
		return data;
	}
	public String getAux(){
		return aux;
	}
	public void setAux(String aux){
		this.aux = aux;
	}
}
