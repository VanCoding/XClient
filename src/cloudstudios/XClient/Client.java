package cloudstudios.XClient;

import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;

public class Client {
	private ClientEventReceiver eventreceiver;
	private ArrayList<Channel> inputchannels = new ArrayList<Channel>();
	private ArrayList<Channel> outputchannels = new ArrayList<Channel>();
	private ArrayList<String> programs = new ArrayList<String>();
	private Worker worker = null;

	private int number;
	private String ip;
	private Socket socket;
	private String name;
	
	public enum AsyncAction{
		Connect,
		Mute,
		Level,
		Gain,
		Delay		
	}
	
	public Client(String ip, int number) {
		this.ip = ip;
		this.number = number;
	}
	
	public void setEventReceiver(ClientEventReceiver eventreceiver){
		this.eventreceiver = eventreceiver;
	}
	
	public void connect(){
		try {
			socket = new Socket(ip,10001);
			int in = 0,out = 0,programcount = 0;
			
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
				}else if(c.equals("%ND0")){
					name = mapDecode(cmd.getAux());
					Log.d("abc", name);
				}else if(c.equals("%PN0")){
					int p = cmd.getData();
					programcount++;
					
					int add = 0;
					if(programs.size() < programcount){
						add = programcount-programs.size();
					}
					if(programs.size() < p+1){
						add = (p+1)-programs.size();
					}
					for(int i = 0; i < add; i++){
						programs.add("");
					}
					programs.set(p, mapDecode(cmd.getAux()));
				}
			}
			
			
			for(int i = 0; i < in; i++){
				inputchannels.add(new Channel(this,true,i));
			}

			for(int i = 0; i < out; i++){
				outputchannels.add(new Channel(this,false,i));
			}
			
			for(int i = 0; i < getProgramCount(); i++){
				Log.d("abc",getProgram(i));
			}
			
			eventreceiver.OnConnect();
		} catch (Exception e) {
			eventreceiver.OnError(e.getMessage());
		}		
	}
	
	public void connectAsync(){
		async(AsyncAction.Connect);
	}
	
	public void disconnect(){
		try {
			socket.close();
		} catch (Exception e) {
		}
	}

	
	
	
	
	public int getNumber(){
		return number;
	}
	
	public Channel getChannel(int i){
		if(i < inputchannels.size()){
			return getInputChannel(i);
		}else{
			return getOutputChannel(i-inputchannels.size());
		}
	}
	
	public Channel getInputChannel(int i){
		return inputchannels.get(i);
	}
	public Channel getOutputChannel(int i){
		return outputchannels.get(i);
	}
	
	public int getChannelCount(){
		return getInputChannelCount()+getOutputChannelCount();
	}
	public int getInputChannelCount(){
		return inputchannels.size();
	}
	public int getOutputChannelCount(){
		return outputchannels.size();
	}
	
	public int getProgramCount(){
		return programs.size();
	}
	public String getProgram(int i){
		return programs.get(i);
	}
	public void saveProgram(int program){
		write(new Command(this,"%PS0",program));
		read();
	}
	public void loadProgram(int program){
		write(new Command(this,"%PR0",program));
		read();
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
				out += ((int)(char)(byte)bytes.get(j)-(int)0x20) *Math.pow(96, l-(j-i)-1);
			}
			command.setData(out);
		}
		
		//read aux
		i = 0;
		String aux = "";
		while(true){
			i = find(bytes,(byte)0x0b,i+1);
			if(i > 0){
				aux += ((char)((byte)bytes.get(i+3)-(byte)0x20));
			}else{
				break;
			}
		}
		if(aux.length() > 0){
			command.setAux(aux);
		}
		
		return command;
	}

	
	private int find(ArrayList<Byte> bytes, byte min, byte max, int start){
		for(int i = start; i < bytes.size(); i++){
			byte b = bytes.get(i);
			if(b >= min && b <= max){
				return i;
			}
		}
		return -1;
	}
	private int find(ArrayList<Byte> bytes, byte min, byte max){
		return find(bytes,min,max,0);
	}
	private int find(ArrayList<Byte> bytes, byte min){
		return find(bytes,min,min,0);
	}
	private int find(ArrayList<Byte> bytes, byte min, int start){
		return find(bytes,min,min,start);
	}
	
	private String mapDecode(String s){
		String result = "";
		String map = "_ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz-<>?,./~!@#$%^&*()=+';";
		for(int i = 0; i < s.length(); i++){
			result += map.charAt(s.charAt(i));
		}
		return result.replace("_", " ");
	}
	
	
	public void async(AsyncAction code, Object...args){
		if(worker == null || !worker.isAlive()){
			worker = new Worker(code,args);
		}else{
			worker.add(code,args);
		}
	}
	
	private class Worker extends Thread{
		private ArrayList<AsyncAction> codes = new ArrayList<AsyncAction>();
		private ArrayList<Object[]> arguments = new ArrayList<Object[]>();
		
		public Worker(AsyncAction code, Object[] args){
			add(code,args);
			start();
		}
		public void add(AsyncAction code, Object[] args){
			codes.add(code);
			arguments.add(args);	
		}
		public void run(){
			while(codes.size() > 0){
				Object[] args = arguments.get(0);
				AsyncAction code = codes.get(0);
				codes.remove(0);
				arguments.remove(0);
				switch(code){
					case Connect:
						connect();
						break;
					case Mute:
						((Channel)args[0]).setMute((Boolean)args[1]);
						break;
					case Level:
						((Channel)args[0]).setLevel((Integer)args[1]);
						break;
					case Delay:
						((Channel)args[0]).setDelay((Integer)args[1]);
						break;
					case Gain:
						((Channel)args[0]).setGain((Integer)args[1]);
						break;
					
				}
			}
		}
	}
	
	
}
