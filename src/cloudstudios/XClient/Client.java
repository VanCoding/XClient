package cloudstudios.XClient;

import java.net.Socket;
import java.util.ArrayList;

public class Client extends Thread {
	private ClientEventReceiver eventreceiver;
	private ArrayList<Channel> inputchannels = new ArrayList<Channel>();
	private ArrayList<Channel> outputchannels = new ArrayList<Channel>();
	private ArrayList<Integer> codes = new ArrayList<Integer>();
	private ArrayList<Object[]> arguments = new ArrayList<Object[]>();
	private int number;
	private String ip;
	private Socket socket;
	
	public static final int CONNECT = 0;
	public static final int MUTE = 1;
	public static final int DELAY = 2;
	
	public Client(String ip, int number) {
		this.ip = ip;
		this.number = number;
		start();
	}
	
	public void setEventReceiver(ClientEventReceiver eventreceiver){
		this.eventreceiver = eventreceiver;
	}
	
	public void connect(){
		try {
			socket = new Socket(ip,10001);
			int in = 0,out = 0;
			
			write(new Command(this,"%SU0"));
			while(true){
				Command cmd = read();
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
				inputchannels.add(new Channel(this,true,i));
			}
			for(int i = 0; i < in; i++){
				inputchannels.get(i).LoadSettings();
			}
			for(int i = 0; i < out; i++){
				outputchannels.add(new Channel(this,false,i));
				//outputchannels.get(outputchannels.size()-1).LoadSettings();
			}
			
			eventreceiver.OnConnect();
		} catch (Exception e) {
			eventreceiver.OnError(e.getMessage());
		}		
	}
	
	public void async(int code, Object...args){
		codes.add(code);
		arguments.add(args);
	}
	
	public void connectAsync(){
		async(CONNECT);
	}
	
	public void run(){
		while(true){
			if(codes.size() > 0){
				Object[] args = arguments.get(0);
				int code = codes.get(0);
				codes.remove(0);
				arguments.remove(0);
				switch(code){
				case CONNECT:
					connect();
					break;
				case MUTE:
					((Channel)args[0]).setMute((Boolean)args[1]);
					break;
				case DELAY:
					((Channel)args[0]).setDelay((Integer)args[1]);
					break;
				}				
			}else{
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public int getNumber(){
		return number;
	}
	
	public Channel getInputChannel(int i){
		return inputchannels.get(i);
	}
	public Channel getOutputChannel(int i){
		return outputchannels.get(i);
	}
	public int getInputChannelCount(){
		return inputchannels.size();
	}
	public int getOutputChannelCount(){
		return outputchannels.size();
	}
	
	public void write(Command command){
		try {
			Client device = command.getDevice();
			Channel channel = command.getChannel();
			byte[] cmd = command.getCommand().getBytes("ascii");
			int dataValue = command.getData();
			
			
			
			ArrayList<Byte> bytes = new ArrayList<Byte>();
			bytes.add((byte) 0x01);
			bytes.add((byte) (command.getData()<0?0x52:0x57));
			bytes.add((byte) 0x7f);
			

			bytes.add((byte)(cmd.length-1));		
			for(int i = 0; i < cmd.length; i++){
				bytes.add(cmd[i]);
			}
			
			if(device != null){
				bytes.add((byte)0x08);
				bytes.add((byte)(0x20+device.getNumber()));
			}
			if(channel != null){
				bytes.add((byte)0x09);
				bytes.add((byte)(channel.getInput()?0x20:0x21));
				bytes.add((byte)0x0a);
				bytes.add((byte)(0x20+channel.getNumber()));
			}
			
			
			if(dataValue >= 0){
				int bytecount = 1;
				while(Math.pow(96,bytecount)< dataValue){
					bytecount++;
				}
				
				byte[] data = new byte[bytecount];
				
				for(int i = bytecount-1; i >= 0; i-- ){
					data[i] = (byte)(dataValue%96);
					dataValue -= data[i];
					data[i] += 0x20;
					dataValue /= 96;
				}
				
				bytes.add((byte)(0x10+data.length-1));		
				for(int i = 0; i < data.length; i++){
					bytes.add(data[i]);
				}
			}
			bytes.add((byte)0x1f);
			
			int checksum = 0x0; 		
			for(int i = 0; i < bytes.size(); i++){
				checksum += bytes.get(i);
			}
			checksum %= 256;
			checksum %= 0x60;
			checksum += 0x20;
			
			bytes.add((byte)checksum);
			bytes.add((byte)0x02);
			
			byte[] result = new byte[bytes.size()];
			for(int i = 0; i < result.length; i++){
				result[i] = bytes.get(i);
			}
			
			socket.getOutputStream().write(result);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	public Command read(){
		ArrayList<Byte> bytes = new ArrayList<Byte>();
		Command command = new Command();
		int i,i2;
		byte[] buffer = new byte[]{0x00};
		while(buffer[0] != 0x02){
			try {
				if(socket.getInputStream().read(buffer) > 0){
					bytes.add(buffer[0]);
				}
			} catch (Exception e) {
			}
		}
		
		//read command
		String cmd = "";
		i= find(bytes,(byte)0x03,(byte)0x07);
		if(i >= 0){
			byte b = bytes.get(i);
			for(int j = i+1; j < i+b+2; j++){
				cmd += (char)(byte)bytes.get(j);
			}
			command.setCommand(cmd);
		}
		
		//read device
		i = find(bytes,(byte)0x08)+1;
		if(i >= 0){
			//command.setDevice(bytes.get(i)-0x20);
			command.setDevice(this);
		}
		
		//read channel
		i = find(bytes,(byte)0x0a);
		i2 = find(bytes,(byte)0x09);
		if(i >= 0 && i2 >= 0 && getInputChannelCount() > 0){
			int channel = (bytes.get(i+1)-0x20);
			boolean input = (int)bytes.get(i2+1) == 0x20;
			command.setChannel(input?getInputChannel(channel):getOutputChannel(channel));
		}

		//read data
		int out = 0;
		i = find(bytes,(byte)0x10,(byte)0x18);
		if(i >= 0){
			int l = bytes.get(i++)-0x10+1;
			for(int j = i; j < i+l; j++){
				out += ((char)(byte)bytes.get(j)-(int)0x20 *Math.pow(96, j-i));
			}
			command.setData(out);
		}
		
		return command;
	}

	
	private int find(ArrayList<Byte> bytes, byte min, byte max){
		for(int i = 0; i < bytes.size(); i++){
			byte b = bytes.get(i);
			if(b >= min && b <= max){
				return i;
			}
		}
		return -1;
	}
	private int find(ArrayList<Byte> bytes, byte min){
		return find(bytes,min,min);
	}
	
	
	
	
}
