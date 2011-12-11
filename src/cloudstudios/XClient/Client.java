package cloudstudios.XClient;

import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;

public class Client {
	private Channel[] inputchannels = new Channel[0];
	private Channel[] outputchannels = new Channel[0];
	private ArrayList<DataField> programs = new ArrayList<DataField>();
	private ClientEventReceiver eventreceiver = null;

	private String ip;
	private Socket socket;
	private Client instance = this;
	private int number;
	
	DataField[] fields = new DataField[]{
		new DataField(CommandType.Company),
		new DataField(CommandType.ProductName),
		new DataField(CommandType.Version),
		new DataField(CommandType.DeviceName),
		new DataField(CommandType.DeviceNumber)
	};

	
	private Reader reader;
	private Writer writer;
	
	public Client(String ip, int number) {
		this.ip = ip;
		this.number = number;
		reader = new Reader();
	}
	
	public int getNumber(){
		return number;
	}
	
	public void disconnect(){
		try {
			reader.destroy();
			writer.destroy();
			socket.close();
			reader.destroy();
		} catch (Exception e) {
		}
	}	
	public void sync(){
		write(new Command(this,CommandType.UpSync));
	}

	
	public void write(Command cmd){
		if(writer == null || writer.hasFinished()){
			writer = new Writer(cmd);
		}else{
			writer.write(cmd);
		}
	}
	
	
	public Channel[] getInputChannels(){
		return inputchannels;
	}
	public Channel[] getOutputChannels(){
		return outputchannels;
	}
	public String[] getPrograms(){
		String[] p = new String[programs.size()];
		for(int i = 0; i < p.length; i++){
			p[i] = programs.get(i).getString();
		}
		return p;
	}
	
	public void saveProgram(int program){
		write(new Command(this,CommandType.ProgramStore,program));
	}
	public void loadProgram(int program){
		write(new Command(this,CommandType.ProgramRecall,program));
		sync();
	}
		
	private DataField getField(CommandType type){
		for(int i = 0; i< fields.length; i++){
			if(fields[i].getType() == type){
				return fields[i];
			}
		}
		return null;
	}
	
	public String getCompany(){
		return getField(CommandType.Company).getDataString();
	}
	public String getProduct(){
		return getField(CommandType.ProductName).getDataString();
	}
	public String getVersion(){
		return getField(CommandType.Version).getDataString();
	}
	public String getName(){
		return getField(CommandType.DeviceName).getString();
	}
	public void setName(){
		//todo
	}
	
	public void setEventReceiver(ClientEventReceiver r){
		eventreceiver = r;
	}

	

	private class Reader extends Thread{		
		public Reader(){
			start();
		}
		public void run(){
			try{
				socket = new Socket(ip,10001);
				while(true){
					Command c = new Command(socket);
					boolean createdprogram = false;					
					for(int i = 0; i < c.length(); i++){
						c.setPosition(i);
						try{
							CommandType cmd = c.getCommand();
							//Log.d("abc",cmd.toString());
							if(cmd.ordinal() <= CommandType.ChannelName.ordinal()){
								//Log.d("abc", "a");
								//Log.d("abc",c.getInput()+"/"+c.getChannel()+"/"+c.getAux()+"/"+c.getData());
								try{
									(c.getInput()?inputchannels:outputchannels)[c.getChannel()].getField(cmd).setValue(c.getAux(), c.getData());
								}catch(Exception e){
									
								}
								//Log.d("abc", "b");
							}else{
								switch(cmd){
								case UpSync:
									if(eventreceiver != null){
										eventreceiver.onSyncCompleted();
									}
									break;
								case ChannelInNumber:
									inputchannels = new Channel[c.getData()];
									for(int j = 0; j < inputchannels.length; j++){
										inputchannels[j] = new Channel(instance,true,j);
									}
									break;
								case ChannelOutNumber:
									outputchannels = new Channel[c.getData()];
									
									for(int j = 0; j < outputchannels.length; j++){
										Log.d("abc",j+"");
										outputchannels[j] = new Channel(instance,false,j);
									}
									break;
								case ProgramName:
									if(!createdprogram){
										programs.add(new DataField(CommandType.ProgramName));
										createdprogram = true;
									}
									programs.get(programs.size()-1).setValue(c.getAux(),c.getData());
									break;
									
								case Company:
								case ProductName:
								case Version:
								case DeviceName:
								case DeviceNumber:
									getField(cmd).setValue(c.getAux(),c.getData());
									break;
									
								}
								
							}
						}catch(Exception e){
							Log.d("abc",e.getMessage());
						}
					}
				}
			}catch(Exception e){
				Log.d("abc",e.getMessage());
			}
		}
	}
	private class Writer extends Thread{

		private ArrayList<Command> commands = new ArrayList<Command>();
		private boolean finished = false;
		
		public Writer(Command c){
			write(c);
			start();
		}
		
		
		public void run(){
			while(commands.size() > 0){
				try{
					socket.getOutputStream().write(commands.remove(0).compile());
				}catch(Exception e){					
				}
			}
			finished = true;
		}
		public void write(Command cmd){
			commands.add(cmd);
		}
		public boolean hasFinished(){
			return finished;
		}
	}
}
