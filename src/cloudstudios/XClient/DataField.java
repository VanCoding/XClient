package cloudstudios.XClient;

import java.util.ArrayList;


public class DataField {
	private CommandType type;
	private ArrayList<Integer> values;
	private static String map = " ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz-<>?,./~!@#$%^&*()=+';";
	public DataField(CommandType t){
		type = t;
		values = new ArrayList<Integer>();
	}
	public CommandType getType(){
		return type;
	}
	public void setValue(int index,int value){
		while(index >= values.size()){
			values.add(0);
		}
		values.set(index, value);
	}
	public void setValue(int value){
		setValue(0,value);
	}
	
	public int getValue(int index){
		try{
			return values.get(index);
		}catch(Exception e){
			return 0;
		}
	}
	public int getValue(){
		return getValue(0);
	}
	public String getString(){
		String out = "";
		for(int i = 0; i < values.size(); i++){
			try{
				out += map.charAt(values.get(i));
			}catch(Exception e){
			}
		}
		return out;
	}
	public String getDataString(){
		String out = "";
		for(int i = 0; i < values.size(); i++){
			out += (char)(int)values.get(i);
		}
		return out;
	}

}
