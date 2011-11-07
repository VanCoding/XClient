package cloudstudios.XConsole;

import java.net.Socket;
import java.util.ArrayList;

public class XConsole {
	private Socket socket;
	private ArrayList<XChannel> channels;
	private int device;
	
	public XConsole(String ip, int device){
		this.device = device;
		try {
			socket = new Socket(ip,10001);
			channels = new ArrayList<XChannel>();
			for(int i = 0; i < 16; i++){
				channels.add(new XChannel(socket,device,i < 8,i<8?i:i-8));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void LoadSettings(){
		try {
			//socket.getOutputStream().write(new XCommand(true,"%SU0",device).getBytes());
			socket.getOutputStream().write(new XCommand(true,"MUT0",device,true,8).getBytes());
			while(true){
				XCommand cmd = new XCommand(socket);
				
				if(cmd.getCommand().equals("%US0")){
					break;
				}else{
					String c = cmd.getCommand();
					//if(c.equals("MUT0")){
						XChannel.log("Command: "+c+" Device: "+cmd.getDevice()+" Input: "+cmd.getInput()+" Channel: "+cmd.getChannel()+" Data: "+cmd.getData());
					//}
					
				}
			}
		} catch (Exception e) {
		}
	}
	
	public XChannel getInputChannel(int i){
		return channels.get(i);
	}
	public XChannel getOutputChannel(int i){
		return channels.get(i+8);
	}
}
