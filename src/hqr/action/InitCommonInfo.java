package hqr.action;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
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

public class InitCommonInfo {
	private String host;
	private String startUrl;
	private String lastRunDt;
	private String processDt;
	private String baseDir;
	private String imgBaseDir;
	private SimpleDateFormat yyyyMMddhhmm = new SimpleDateFormat("yyyy-MM-dd hh:mm");
	
	public InitCommonInfo(String host, String startUrl, String lastRunDt, String baseDir, String imgBaseDir) {
		super();
		this.host = host;
		this.startUrl = startUrl;
		this.lastRunDt = lastRunDt;
		this.baseDir = baseDir;
		this.imgBaseDir = imgBaseDir;
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
					cl.close();
					
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
									Grab gb = new Grab(topicUrl, subject, author, baseDir, imgBaseDir, httpclient, httpClientContext);
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
	 * File content:
	 * host->https://e1.a6def2ef910.pw/pw
	 * startUrl->https://e1.a6def2ef910.pw/pw/thread.php?fid=3&page=
	 * lastRunDt->2021-03-04 15:09
	 * baseDir->D:\\temp
	 * imgBaseDir->D:\\temp
	 */
	private void updateConfig() {
		try(
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("config.dat")));
		){
			bw.write("host->"+host+System.getProperty("line.separator"));
			bw.write("startUrl->"+startUrl+System.getProperty("line.separator"));
			bw.write("lastRunDt->"+processDt+System.getProperty("line.separator"));
			bw.write("baseDir->"+baseDir+System.getProperty("line.separator"));
			bw.write("imgBaseDir->"+imgBaseDir+System.getProperty("line.separator"));
			bw.flush();
		}
		catch (Exception e) {
			System.out.println("[!]can't find config.dat");
			System.exit(255);
		}
	}
	
}
