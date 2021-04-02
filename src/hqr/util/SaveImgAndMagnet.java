package hqr.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;

public class SaveImgAndMagnet {
	private String imgUrl;
	private String path;
	private String magnet;
	private CloseableHttpClient httpclient;
	private HttpClientContext httpClientContext;
	private String skipIfExist = "Y";
	
	public SaveImgAndMagnet(String imgUrl, String path, String magnet, CloseableHttpClient httpclient, HttpClientContext httpClientContext) {
		super();
		this.imgUrl = imgUrl;
		this.path = path;
		this.magnet = magnet;
		this.httpclient = httpclient;
		this.httpClientContext = httpClientContext;
		this.skipIfExist = System.getProperty("skipIfExist");

	}
	
	public void execute() throws Exception{
		File folder = new File(path);
		if(!folder.exists()) {
			folder.mkdirs();
		}
		
		//save magnet url in readme.txt
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(folder+System.getProperty("file.separator")+"readme.txt")));
		bw.write(magnet);
		bw.flush();
		bw.close();

		//save img
		String []arr = imgUrl.split("/");
		String fileName = arr[arr.length-1];
		HttpGet get = new HttpGet(imgUrl);
		
		File f = new File(folder+System.getProperty("file.separator")+fileName);
		if(!f.exists()||(f.exists()&&"N".equals(skipIfExist))) {
			CloseableHttpResponse cl = httpclient.execute(get, httpClientContext);
			FileOutputStream fos = new FileOutputStream((new File(folder+System.getProperty("file.separator")+fileName)));
			cl.getEntity().writeTo(fos);
			fos.flush();
			fos.close();
			cl.close();
		}
		else {
			System.out.println(folder+System.getProperty("file.separator")+fileName+" exist, skip it");
		}
	}
	
}
