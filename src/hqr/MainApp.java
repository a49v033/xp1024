package hqr;

import hqr.action.GetTopic;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Properties;

public class MainApp {

	public static void main(String[] args) {
		Properties prop = new Properties();
		
		try{
			prop.load(new FileReader("config.dat"));
			Enumeration<?> enumeration = prop.propertyNames();
			while(enumeration.hasMoreElements()){
				String value = (String) enumeration.nextElement();
				System.setProperty(value, prop.getProperty(value));
			}
			
			GetTopic grab = new GetTopic();
			grab.execute();
			
		} catch (Exception e) {
			System.out.println("[!]can't find config.dat");
			System.exit(255);
		}
	}
}