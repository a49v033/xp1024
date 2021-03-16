package hqr.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import hqr.util.SaveHtml;
import hqr.util.SaveImgAndMagnet;

public class Grab {
	private String topicUrl;
	private String subject;
	private String author;
	private String baseDir;
	private String imgBaseDir;
	private String aria2;
	private String token;
	private CloseableHttpClient httpclient;
	private HttpClientContext httpClientContext;
	
	private StringBuilder html = new StringBuilder();
	
	private SimpleDateFormat yyyymm = new SimpleDateFormat("yyyyMM");
	private String strYYYYMM = "999901";
	
	private String js = "	<script lang=\"javascript\" type=\"text/javascript\">\r\n" + 
			"		function download(e) {\r\n" + 
			"			var magneturl = e.getAttribute(\"magnet-url\")\r\n" + 
			"			var data1 = {\r\n" + 
			"				\"jsonrpc\":2,\r\n" + 
			"				\"id\":\"webui\",\r\n" + 
			"				\"method\":\"system.multicall\",\r\n" + 
			"				\"params\":[\r\n" + 
			"					[\r\n" + 
			"						{\r\n" + 
			"							\"methodName\":\"aria2.addUri\",\r\n" + 
			"							\"params\":[\"token:${token}\",[magneturl],{}]\r\n" + 
			"						}\r\n" + 
			"					]\r\n" + 
			"				]\r\n" + 
			"			};\r\n" + 
			"			$.ajax({\r\n" + 
			"				type : \"post\",\r\n" + 
			"				url : \"${jsonrpc}\",\r\n" + 
			"				dataType:\"json\",\r\n" + 
			"				//contentType: \"application/json\",\r\n" + 
			"				data : JSON.stringify(data1),\r\n" + 
			"				success : function(result) {\r\n" + 
			"					alert(\"任务已成功添加，任务ID \"+result.result);\r\n" + 
			"				},\r\n" + 
			"				error : function(){\r\n" + 
			"					alert(\"无法添加任务，请确认aria2设置是否正确\");\r\n" + 
			"				}\r\n" + 
			"			});\r\n" + 
			"			\r\n" + 
			"		}\r\n" + 
			"	</script>";
	
	
	public Grab(String topicUrl, String subject, String author, CloseableHttpClient httpclient, HttpClientContext httpClientContext) {
		super();
		this.topicUrl = topicUrl;
		this.subject = subject;
		this.author = author;
		this.baseDir = System.getProperty("baseDir");
		this.imgBaseDir = System.getProperty("imgBaseDir");
		this.aria2 = System.getProperty("aria2");
		this.token = System.getProperty("token");
		this.httpclient = httpclient;
		this.httpClientContext = httpClientContext;
		
		Date dd = new Date();
		strYYYYMM = yyyymm.format(dd);
		
		writeHtmlStart();
		writeHeaderStart();
	}

	public void execute() {
		handle2();
	}
	
	private void handle2() {
		System.out.print(topicUrl+"|"+subject+"|"+author);
		HttpGet get = new HttpGet(topicUrl);
		String html = "";
		try(CloseableHttpResponse cl = httpclient.execute(get, httpClientContext);) {
			html = EntityUtils.toString(cl.getEntity(), "UTF-8");
		}
		catch (Exception e) {
			e.printStackTrace();
			callOps("fail to process "+e);
		}
			
		Document bodys = Jsoup.parse(html);
		Elements content = bodys.select("#read_tpc");
		
		//save all the torrent uri -> uri maybe more than porns
		ArrayList<String> uris = new ArrayList<String>();
		Elements hrefs = content.select("a");
		//get the torrent url and split by the url
		String torrentUrl = getTorrentUrl(hrefs);
		if("".equals(torrentUrl)) {
			callOps("can't find the split torrent uri");
		}
		else {
			writeTitleStart(subject);
			writeTitleEnd();
			writeHeaderEnd();
			writeBodyStart();
			writeBodyContent("<div><b>原始网页:</b> <a href=\""+topicUrl+"\" target=\"_blank\">"+topicUrl+"</a></div><br>");
			
			for (Element href : hrefs) {
				String uri = href.attr("href");
				if(uri.indexOf(torrentUrl)>=0) {
					uris.add(uri);
				}
			}
			
			System.out.println("|size:"+uris.size()+"\nSplit:"+torrentUrl);
			
			//get picture
			String str = content.toString();
			String arr[] = str.split("<a href=\""+torrentUrl);
			
			for (int i=0; i<arr.length; i++) {
				Document piece = Jsoup.parse(arr[i]);
				Elements imgs = piece.select("img");
				
				if(imgs.size()!=0) {
					String magnetUri = "";
					/* 
					 * i<uris.size()
					 * Add this condition is due 1 Page's href is different like below
					 * 
					 * <a href="http://www3.downsx.rocks/torrent/625855B7B5DA071FB03977725482349A91C9CADF" target="_blank">http://www3.downsx.rocks/torrent/625855B7B5DA071FB03977725482349A91C9CADF</a>
					 * <a href="http://www.158file.com/file/OTM1MA==.html" target="_blank">http://www3.downsx.rocks/torrent/A327ACD28C91EAB0B6C9D008828BE79D27F2FFF2</a>
					 * 
					 * Skip them, it's invalid
					 */
					if(i<=arr.length-1&&i<uris.size()) {
						magnetUri = getMagnetInfo(uris.get(i));
						System.out.println(magnetUri);
						//magnet:?xt=urn:btih:C22CDAC324D56DA894DF6D98950E06BDB9966B8A&dn=ABW007C&tr=http://ya.97ro.org/
						String []params = magnetUri.split("&");
						String []kv = params[1].split("=");
						String folder = kv[1];
						writeBodyContent("<div><b>番号：  <font color=\"red\">"+folder+"</font></b><br>");
						for (Element img : imgs) {
							if("".equals(magnetUri)) {
								callOps("fail to get the magnet url");
							}
							else {
								String fullPath = baseDir+System.getProperty("file.separator")+strYYYYMM+System.getProperty("file.separator")+"resource"+System.getProperty("file.separator")+folder;
								
								SaveImgAndMagnet save = new SaveImgAndMagnet(img.attr("src"), fullPath, magnetUri, httpclient, httpClientContext);
								save.execute();
								
								String []arr2 = img.attr("src").split("/");
								String fileName = arr2[arr2.length-1];
								
								writeBodyContent("<img src=\""+imgBaseDir+"/"+strYYYYMM+"/resource/"+folder+"/"+fileName+"\" border=\"0\"><br>");
							}
						}
						writeBodyContent("<br>下载 <a href=\""+magnetUri+"\" target=\"_blank\">"+magnetUri+"</a> <input type=\"button\" magnet-url=\""+magnetUri+"\" onclick=\"download(this);\" value=\"aria2\" />");
						writeBodyContent("</div><br><br>");
					}
				}
				else {
					//System.out.println("No img, discard it");
				}

			}
			
			js = js.replaceAll("\\$\\{token\\}", token);
			js = js.replaceAll("\\$\\{jsonrpc\\}", aria2);
			writeBodyContent(js);
			writeBodyEnd();
			writeHtmlEnd();
			
			String []hns = topicUrl.split("/");
			String htmlName = hns[hns.length-1];
			SaveHtml sh = new SaveHtml(htmlName, baseDir+System.getProperty("file.separator")+strYYYYMM, this.html.toString());
			sh.execute();
			System.out.println("Saved Html file at "+baseDir+System.getProperty("file.separator")+htmlName);
		}
	}
	
	private String getTorrentUrl(Elements hrefs) {
		String torrentUrl = "";
		
		for (Element element : hrefs) {
			String uri = element.attr("href");
			
			//https://www3.downsx.rocks/torrent/668187376D5CF0B656FB0A31718C1649D558A0CC
			if(uri.indexOf("/torrent/")>=0) {
				String arr[] = uri.split("/torrent/");
				torrentUrl = arr[0]+"/torrent/";
				return torrentUrl;
			}
		}
		
		return "";
	}
	
	private String getMagnetInfo(String url) {
		HttpGet get = new HttpGet(url);
		try(CloseableHttpResponse cl = httpclient.execute(get, httpClientContext);) {
			String html = EntityUtils.toString(cl.getEntity(), "UTF-8");
			
			Document bodys = Jsoup.parse(html);
			Elements eles = bodys.select(".uk-button");
			for (Element element : eles) {
				String links = element.attr("href");
				if(links.indexOf("magnet")>=0) {
					return links;
				}
			}
			return "";
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	private void writeHtmlStart() {
		html.append("<html>");
	}
	
	private void writeHeaderStart() {
		html.append("<head>");
		html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
	}
	
	private void writeTitleStart(String subject) {
		html.append("<title>"+subject);
	}
	
	private void writeTitleEnd() {
		html.append("</title>");
	}
	
	private void writeHeaderEnd() {
		html.append("<script src=\"http://libs.baidu.com/jquery/2.0.0/jquery.min.js\"></script></head>");
	}
	
	private void writeBodyStart() {
		html.append("<body>");
	}
	
	private void writeBodyContent(String val) {
		html.append(val);
	}
	
	private void writeBodyEnd() {
		html.append("</body>");
	}
	
	private void writeHtmlEnd() {
		html.append("</html>");
	}
	
	private void callOps(String reason) {
		System.out.println("Missing something:"+ reason);
	}
	
}
