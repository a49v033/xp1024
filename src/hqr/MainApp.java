package hqr;

import hqr.action.InitCommonInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


public class MainApp {

	public static void main(String[] args) {
		try(BufferedReader br = new BufferedReader(new FileReader(new File("config.dat")));) {
			String content = "";
			String host = "";
			String startUrl = "";
			String lastRunDt = "";
			String baseDir = "";
			String imgBaseDir = "";
			String aria2 = "";
			String token = "";
			while((content=br.readLine())!=null) {
				String []arr = content.split("->");
				if("host".equals(arr[0])) {
					host = arr[1];
				}
				else if("startUrl".equals(arr[0])) {
					startUrl = arr[1];
				}
				else if("lastRunDt".equals(arr[0])) {
					lastRunDt = arr[1];
				}
				else if("baseDir".equals(arr[0])) {
					baseDir = arr[1];
				}				
				else if("imgBaseDir".equals(arr[0])) {
					imgBaseDir = arr[1];
				}
				else if("aria2".equals(arr[0])) {
					aria2 = arr[1];
				}
				else if("token".equals(arr[0])) {
					token = arr[1];
				}
			}
			
			br.close();
			
			InitCommonInfo grab = new InitCommonInfo(host, startUrl, lastRunDt, baseDir, imgBaseDir, aria2, token);
			grab.execute();
			
		} catch (Exception e) {
			System.out.println("[!]can't find config.dat");
			System.exit(255);
		}
	}
}