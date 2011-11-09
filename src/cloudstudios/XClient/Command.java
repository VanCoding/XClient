package cloudstudios.XClient;

import java.net.Socket;
import java.util.ArrayList;

public class Command {
	private ArrayList<Byte> bytes;
	boolean packed;
	
	public Command(boolean read, String cmd){
		try{
			packed = false;
			bytes = new ArrayList<Byte>();
			bytes.add((byte) 0x01);
			bytes.add((byte) (read?0x52:0x57));
			bytes.add((byte) 0x7f);
			
			byte[] command = cmd.getBytes("ascii");
			bytes.add((byte)(command.length-1));		
			for(int i = 0; i < command.length; i++){
				bytes.add(command[i]);
			}			
		}catch(Exception e){		
		}
	}
	
	public Command(boolean read, String cmd, int device){
		this(read,cmd);
		setDevice(device);
	}
	
	public Command(boolean read, String cmd,int device, boolean input, int channel){
		this(read,cmd);
		setDevice(device);
		setChannel(input,channel);
	}
	
	public Command(boolean read, String cmd, int device, boolean input, int channel, int data){
		this(read,cmd,device,input,channel);
		setData(data);
	}
	
	public Command(boolean read, String cmd, int device, int data){
		this(read,cmd);
		setData(data);
	}
	public Command(String cmd, int data){
		this(false,cmd);
		setData(data);
	}

	
	public Command(Socket s){
		bytes = new ArrayList<Byte>();
		byte[] buffer = new byte[]{0x00};
		while(buffer[0] != 0x02){
			try {
				if(s.getInputStream().read(buffer) > 0){
					bytes.add(buffer[0]);
				}
			} catch (Exception e) {
			}
		}
		
	}
	
	public String getCommand(){
		String cmd = "";
		int i = find((byte)0x03,(byte)0x07);
		byte b = bytes.get(i);
		for(int j = i+1; j < i+b+2; j++){
			cmd += (char)(byte)bytes.get(j);
		}
		return cmd;
	}
	
	public void setDevice(int device){
		bytes.add((byte)0x08);
		bytes.add((byte)(0x20+device));
	}
	public int getDevice(){
		return bytes.get(find((byte)0x08)+1)-0x20;
	}
	
	public void setChannel(boolean input,int i){		
		bytes.add((byte)0x09);
		bytes.add((byte)(input?0x20:0x21));
		bytes.add((byte)0x0a);
		bytes.add((byte)(0x20+i));		
	}
	public int getChannel(){
		return bytes.get(find((byte)0x0a)+1)-0x20 ;
	}
	public boolean getInput(){
		return (int)bytes.get(find((byte)0x09)+1) == 0x20;
	}
	
	public String getDataString(){
		String out = "";
		String map = "_ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz-<>?,./~!@#$%^&*()=+';";
		int i = find((byte)0x10,(byte)0x18);
		int l = bytes.get(i++)-0x10+1;
		for(int j = i; j < i+l; j++){
			out += map.charAt((int)bytes.get(j)-0x20);
		}
		return out;
	}
	
	public int getData(){
		int out = 0;
		int i = find((byte)0x10,(byte)0x18);
		int l = bytes.get(i++)-0x10+1;
		for(int j = i; j < i+l; j++){
			out += ((char)(byte)bytes.get(j)-(int)0x20 *Math.pow(96, j-i));
		}
		return out;
	}
	
	public void setData(int value){	
		
		int bytecount = 1;
		while(Math.pow(96,bytecount)< value){
			bytecount++;
		}
		
		byte[] data = new byte[bytecount];
		
		for(int i = bytecount-1; i >= 0; i-- ){
			data[i] = (byte)(value%96);
			value -= data[i];
			data[i] += 0x20;
			value /= 96;
		}
		
		bytes.add((byte)(0x10+data.length-1));		
		for(int i = 0; i < data.length; i++){
			bytes.add(data[i]);
		}
	}
	
	public byte[] getBytes(){
		pack();
		byte[] result = new byte[bytes.size()];
		for(int i = 0; i < result.length; i++){
			result[i] = bytes.get(i);
		}
		return result;
	}
	
	private int find(byte min, byte max){
		for(int i = 0; i < bytes.size(); i++){
			byte b = bytes.get(i);
			if(b >= min && b <= max){
				return i;
			}
		}
		return 0;
	}
	private int find(byte min){
		return find(min,min);
	}
	
	private void pack(){
		if(!packed){
			bytes.add((byte)0x1f);
			int checksum = 0x0; 		
			for(int i = 0; i < bytes.size(); i++){
				checksum += bytes.get(i);
			}
			checksum %= 256;
			checksum %= 0x60; // Modulo of <60> (Remainder when divided by <60>) 
			checksum += 0x20;  // Add <20> to become readable ascii 
			bytes.add((byte)checksum);
			bytes.add((byte)0x02);
			
			packed = true;
		}
	}
}
