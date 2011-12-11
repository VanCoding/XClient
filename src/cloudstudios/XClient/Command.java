package cloudstudios.XClient;

import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;


public class Command {
	boolean read = true;
	int sender = 0x7f;
	ArrayList<CommandType> command = new ArrayList<CommandType>();
	ArrayList<Integer> device = new ArrayList<Integer>();
	ArrayList<Integer> input = new ArrayList<Integer>();
	ArrayList<Integer> channel = new ArrayList<Integer>();
	ArrayList<Integer> aux = new ArrayList<Integer>();
	ArrayList<Integer> column = new ArrayList<Integer>();
	ArrayList<Integer> data = new ArrayList<Integer>();	
	
	int pos = 0;
	
	static String[] commands = new String[]{
		/* Channel Commands */
		"MTR0", //Meter
		"MUT0", //Mute
		"MIC0", //Mix Gain
		"LVL0", //Signal Level
		"POL0", //Signal Polarity
		"DLY3", //Signal Delay
		"EQT0", //EQ Type
		"EQF0", //EQ Frequency
		"EQB0", //EQ Bandwidth
		"EQL0", //EQ Level
		"EQb0", //EQ Bypass
		"EQL1", //GEQ Level
		"EQb1", //GEQ Bypass
		"XRT0", //Crossover Type
		"XRF0", //Crossover Frequency
		"XRS0", //Crossover Slope
		"XFE0", //FIR Type
		"XFF0", //FIR Frequency
		"CMT0", //Compressor Threshold
		"CMA0", //Compressor Attack
		"CMR0", //Compressor Release
		"CMX0", //Compressor Ratio
		"LMT0", //Limiter Threshold
		"LMA0", //Limiter Attack
		"LMR0", //Limiter Release
		"MIX0", //Mixer
		"NCH0", //Channel Name
		
		/* Special Channel Commands */			
		"#LVL", //Level Increment / Decrement
		
		/* Device Commands */
		"%SD0", //Down Sync
		"%SU0", //Up Sync
		"%LP0", //Lock Password
		"%LK0", //Lock Key
		"%nI0", //Channel In Number
		"%nO0", //Channel Out Number
		"%NC0", //Company
		"%nS0", //Sampling Frequency
		"%NP0", //Product Name
		"%NV0", //Version
		"%ND0", //Device Name
		"%nD0", //Device Number
		"%Pn0", //Program Number
		"%PN0", //Program Name
		"%PR0", //Program Recall
		"%PS0", //Program Store
		"%PD0", //Program Download
		"%PU0", //Program Upload
		"%RS0", //Reset
		"%EN0", //Ethernet Info
		"%XP0" 	//XPanel Info
	};
	
	public Command(){
		setPosition(0);
	}
	public Command(Client device){
		this();
		setDevice(device.getNumber());
	}
	public Command(Client device, CommandType command){
		this(device);
		setCommand(command);
	}
	public Command(Client device, Channel channel, CommandType command){
		this(device,command);
		setInput(channel.getInput());
		setChannel(channel.getNumber());		
	}
	public Command(Client device, Channel channel, CommandType command, int data){
		this(device,channel,command);
		setData(data);
	}
	public Command(Client device, CommandType command, int data){
		this(device,command);
		setData(data);
	}
	
	public Command(Socket s){
		ArrayList<Byte> bytes = new ArrayList<Byte>();
		byte[] buffer = new byte[]{0x00};
		while(buffer[0] != 0x02){
			try {
				if(s.getInputStream().read(buffer) > 0){
					bytes.add(buffer[0]);
				}
			} catch (Exception e) {
			}
		}
		init(bytes);
	}
	public Command(ArrayList<Byte> bytes){
		init(bytes);	
	}
	
	
	
	public void setPosition(int i){
		while(i >= command.size()){
			command.add(null);
			device.add(-1);
			input.add(-1);
			channel.add(-1);
			aux.add(-1);
			column.add(-1);
			data.add(-1);
		}
		if(i < 0){
			i = 0;
		}
		pos = i;
	}
	public void next(){
		setPosition(pos+1);
	}
	public void prev(){
		setPosition(pos-1);
	}	
	public boolean getRead(){
		return read;
	}
	public void setRead(boolean v){
		read = v;
	}
	public int getSender(){
		return sender;
	}
	public void setSender(int s){
		sender = s+0x20;
	}
	public CommandType getCommand(int i){
		for(;i >= 0; i--){
			if(command.get(i) != null){
				return command.get(i);
			}
		}
		return null;
	}
	public CommandType getCommand(){
		return getCommand(pos);
	}
	public void setCommand(int i,CommandType cmd){
		command.set(i,cmd);
	}
	public void setCommand(CommandType cmd){
		setCommand(pos,cmd);		
	}
	public int getDevice(int i){
		return getInt(device,i);
	}
	public int getDevice(){
		return getDevice(pos);
	}
	public void setDevice(int i, int v){
		device.set(i,v);
	}
	public void setDevice(int v){
		setDevice(v,pos);
	}
	public boolean getInput(int i){
		return getInt(input,i)==0;
	}
	public boolean getInput(){
		return getInput(pos);
	}
	public void setInput(int i, boolean v){
		input.set(i,v?0:1);
	}
	public void setInput(boolean v){
		setInput(pos,v);
	}
	public int getChannel(int i){
		return getInt(channel,i);
	}
	public int getChannel(){
		return getChannel(pos);
	}
	public void setChannel(int i, int v){
		channel.set(i,v);
	}
	public void setChannel(int v){
		setChannel(v,pos);
	}
	public int getAux(int i){
		return getInt(aux,i);
	}
	public int getAux(){
		int v = getAux(pos);
		return v<0?0:v;
	}
	public void setAux(int i, int v){
		aux.set(i,v);
	}
	public void setAux(int v){
		setAux(v,pos);
	}
	public int getColumn(int i){
		return getInt(column,i);
	}
	public int getColumn(){
		return getColumn(pos);
	}
	public void setColumn(int i, int v){
		column.set(i,v);
	}
	public void setColumn(int v){
		setColumn(v,pos);
	}
	public int getData(int i){
		return getInt(data,i);
	}
	public int getData(){
		return getData(pos);
	}
	public void setData(int i, int v){
		data.set(i,v);
		read = false;
	}
	public void setData(int v){
		setData(pos,v);
	}
	private void init(ArrayList<Byte> bytes){
		setPosition(0);
		read = bytes.get(1) == (byte)0x52;
		sender = bytes.get(2);
		
		for(int i = 3; i < bytes.size()-3; i++){
			switch(bytes.get(i)){			
			case 0x03:
			case 0x04:
			case 0x05:
			case 0x06:
			case 0x07:
				String str = "";
				int m = i+bytes.get(i)+2;
				i++;
				for(; i < m; i++){
					str += (char)(byte)bytes.get(i);
				}
				i--;
				command.set(pos,toCommandType(str));
				break;
			case 0x08:
				device.set(pos, getValue(bytes,i));
				i++;
				break;
			case 0x09:
				input.set(pos, getValue(bytes,i));
				i++;
				break;
			case 0x0a:
				channel.set(pos, getValue(bytes,i));
				i++;
				break;
			case 0x0b:
				aux.set(pos, getValue(bytes,i));
				i++;
				break;
			case 0x0c:
				column.set(pos, getValue(bytes,i));
				i++;
				break;
			case 0x10:
			case 0x11:
			case 0x12:
			case 0x13:
			case 0x14:
			case 0x15:
			case 0x16:
			case 0x17:
			case 0x18:
				data.set(pos, getValue(bytes,i,bytes.get(i)-0x0f));
				i += bytes.get(i)-0x0f;
				break;
			case 0x1f:
				next();
				break;
			}
		}
	}
	public byte[] compile(){
		ArrayList<Byte> bytes = new ArrayList<Byte>();
		bytes.add((byte)0x01);
		bytes.add((byte)(read?0x52:0x57));
		bytes.add((byte)sender);
		
		for(int i = 0; i < command.size(); i++){
			if(command.get(i) != null){
				String cmd = toString(command.get(i));
				bytes.add((byte)(cmd.length()-1));
				for(int j = 0; j < cmd.length(); j++){
					bytes.add((byte)cmd.charAt(j));
				}
			}
			compileField(0x08,device,bytes,i);
			compileField(0x09,input,bytes,i);
			compileField(0x0a,channel,bytes,i);
			compileField(0x0b,aux,bytes,i);
			compileField(0x0c,column,bytes,i);
			if(!read){
				compileField(0x10,data,bytes,i);
			}
			bytes.add((byte)0x1f);
		}
		
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
		return result;
	}
	
	public int length(){
		return command.size();
	}
	
	private void compileField(int code,ArrayList<Integer> list, ArrayList<Byte> bytes, int pos){
		int dataValue = list.get(pos);
		if(dataValue != -1){
			int bytecount = 1;
			while(Math.pow(96,bytecount)< dataValue){
				bytecount++;
			}
			bytes.add((byte)(code+bytecount-1));

			byte[] data = new byte[bytecount];
			
			for(int i = bytecount-1; i >= 0; i-- ){
				data[i] = (byte)(dataValue%96);
				dataValue -= data[i];
				data[i] += 0x20;
				dataValue /= 96;
			}
			for(int i = 0; i < data.length; i++){
				bytes.add(data[i]);
			}
		}
	}
	
	private int getInt(ArrayList<Integer> list,int i){
		for(;i >= 0; i--){
			if(list.get(i) != -1){
				return list.get(i);
			}
		}
		return -1;
	}	
	private int getValue(ArrayList<Byte> bytes,int i,int l){
		i++;
		int out = 0;
		if(i > 0){
			for(int j = i; j < i+l; j++){
				out += ((int)(char)(byte)bytes.get(j)-(int)0x20) *Math.pow(96, l-(j-i)-1);
			}		
			return out;
		}
		return -1;
	}
	private int getValue(ArrayList<Byte>bytes,int i){
		return getValue(bytes,i,1);
	}
	private static CommandType toCommandType(String command){
		for(int i = 0; i < commands.length; i++){
			if(command.equals(commands[i])){
				return CommandType.values()[i];
			}
		}
		Log.d("abc","Command "+command+" is not supported!");
		return CommandType.Unknown;
		
	}
	private static String toString(CommandType command){
		return commands[command.ordinal()];
	}
	public void addAux(int from, int to){
		for(;from <= to; from++){
			setAux(from);
			next();
		}
	}
}
