package cloudstudios.XClient;

import java.net.Socket;
import java.util.ArrayList;

public class Client {
	private Socket socket;
	private ArrayList<Channel> inputchannels = new ArrayList<Channel>();
	private ArrayList<Channel> outputchannels = new ArrayList<Channel>();;
	private int device;
	
	public Client(String ip, int device){
		this.device = device;
		try {
			socket = new Socket(ip,10001);
			Command cmd;
			
			//load input channels
			cmd = new Command(true,"%nI0");
			cmd.setDevice(this.device);
			socket.getOutputStream().write(cmd.getBytes());
			cmd = new Command(socket);
			for(int i = 0; i < cmd.getData(); i++){
				inputchannels.add(new Channel(socket,this.device,true,i));
			}
			
			//load output channels
			cmd = new Command(true,"%nO0");
			cmd.setDevice(this.device);
			socket.getOutputStream().write(cmd.getBytes());
			cmd = new Command(socket);
			for(int i = 0; i < cmd.getData(); i++){
				outputchannels.add(new Channel(socket,this.device,true,i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Channel getInputChannel(int i){
		return inputchannels.get(i);
	}
	public Channel getOutputChannel(int i){
		return outputchannels.get(i+8);
	}
	public int getInputChannelCount(){
		return inputchannels.size();
	}
	public int getOutputChannelCount(){
		return outputchannels.size();
	}
}
