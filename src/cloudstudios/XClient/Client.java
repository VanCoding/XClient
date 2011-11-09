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
			int in = 0,out = 0;
			Command cmd;
			
			cmd = new Command(true,"%SU0");
			cmd.setDevice(this.device);
			socket.getOutputStream().write(cmd.getBytes());
			while(true){
				cmd = new Command(socket);
				String c = cmd.getCommand();
				if(c.equals("%SU0")){
					break;
				}else if(c.equals("%nI0")){
					in = cmd.getData();
				}else if(c.equals("%nO0")){
					out = cmd.getData();
				}
			}
			

			for(int i = 0; i < in; i++){
				inputchannels.add(new Channel(socket,this.device,true,i));
			}
			for(int i = 0; i < out; i++){
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
