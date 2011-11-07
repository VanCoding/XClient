package cloudstudios.XClient;

import java.net.Socket;
import java.util.ArrayList;

public class Client {
	private Socket socket;
	private ArrayList<Channel> channels;
	private int device;
	
	public Client(String ip, int device){
		this.device = device;
		try {
			socket = new Socket(ip,10001);
			channels = new ArrayList<Channel>();
			for(int i = 0; i < 16; i++){
				channels.add(new Channel(socket,this.device,i < 8,i<8?i:i-8));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	public Channel getInputChannel(int i){
		return channels.get(i);
	}
	public Channel getOutputChannel(int i){
		return channels.get(i+8);
	}
}
