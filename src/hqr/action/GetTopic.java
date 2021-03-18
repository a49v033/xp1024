package hqr.action;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import hqr.util.Brower;

public class GetTopic {
	private String host;
	private String startUrl;
	private String lastRunDt;
	private String processDt;
	private String baseDir;
	private String imgBaseDir;
	private String aria2;
	private String token;
	private SimpleDateFormat yyyyMMddhhmm = new SimpleDateFormat("yyyy-MM-dd hh:mm");
	
	public GetTopic() {
		this.host = System.getProperty("host");
		this.startUrl = System.getProperty("startUrl");
		this.lastRunDt = System.getProperty("lastRunDt");
		this.baseDir = System.getProperty("baseDir");
		this.imgBaseDir = System.getProperty("imgBaseDir");
		this.aria2 = System.getProperty("aria2");
		this.token = System.getProperty("token");
	}
	
	public void execute() {
		try(			
			CloseableHttpClient httpclient = Brower.getCloseableHttpClient();
		) {
			HttpClientContext httpClientContext = Brower.getHttpClientContext();
			int firstTime = 0;
			//gogogo, util no new html
			for(int i=0;i<9999;i++) {
				System.out.println("Process "+startUrl+(i+1));
				HttpGet get = new HttpGet(startUrl+(i+1));
				CloseableHttpResponse cl = httpclient.execute(get, httpClientContext);
				String html = EntityUtils.toString(cl.getEntity(), "UTF-8");
				
				if(cl.getStatusLine().getStatusCode()==200) {
					Document bodys = Jsoup.parse(html);
					//class = tr3 , then select all td
					Elements trs = bodys.select(".tr3");
					
					for (Element element : trs) {
						//get all td 
						Elements tds = element.select("td");
						if(tds.size()==5) {
							String topicUrl = host+"/"+tds.get(0).select("a").attr("href");
							String subject = tds.get(1).select("a").html();
							String author = tds.get(2).select("a").html();
							String issueDt = tds.get(4).select("a").html();
							
							if(subject.indexOf("中文")>=0||subject.indexOf("中字")>=0) {
								if(compareDateTime(lastRunDt, issueDt)) {
									firstTime ++;
									if(firstTime==1) {
										//save the newest issue date to lastRunDt
										processDt = issueDt;
									}
									Grab gb = new Grab(topicUrl, subject, author, httpclient, httpClientContext);
									gb.execute();
								}
								else {
									if(processDt!=null) {
										updateConfig();
									}
									System.out.println("All update-to-date");
									System.exit(0);
								}
							}
							else {
								//not cn porn, skip it
							}
						}
						else {
							//not common topic, skip it
						}
					}
					
				}
				else {
					cl.close();
					System.out.println("[!]fail to connect ");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Covert the date from 2021-03-06 15:44 to 20210306
	 */
	private boolean compareDateTime(String lastRunDt, String issueDt) {
		try {
			Date d1 = yyyyMMddhhmm.parse(lastRunDt);
			Date d2 = yyyyMMddhhmm.parse(issueDt);
			
			return d1.before(d2);
			
		} catch (Exception e) {
			System.out.println("[!]Date format error(yyyy-MM-dd hh:mm), pls check the date string:"+lastRunDt+"|"+issueDt);
			System.exit(255);
			return false;
		}
	}
	
	/*	
	 * Save the property file again with the new lastRunDt
	 */
	private void updateConfig() {
		Properties newProp = new Properties();
		newProp.setProperty("host", host);
		newProp.setProperty("startUrl", startUrl);
		newProp.setProperty("lastRunDt", processDt);
		newProp.setProperty("baseDir", baseDir);
		newProp.setProperty("imgBaseDir", imgBaseDir);
		newProp.setProperty("aria2", aria2);
		newProp.setProperty("token", token);
		newProp.setProperty("skipIfExist", token);
		
		try {
			newProp.store(new FileWriter("config.dat"), "Update last run date");
		}
		catch (Exception e) {
			System.out.println("[!]can't write config.dat");
			System.exit(255);
		}
	}
	
}
