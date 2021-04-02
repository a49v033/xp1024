package hqr.action;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;

public class SendMsg {
	private CloseableHttpClient httpclient = null;
	private HttpClientContext httpClientContext = null;
	
	public SendMsg(CloseableHttpClient httpclient, HttpClientContext httpClientContext) {
		try {
			this.httpclient = httpclient;
			this.httpClientContext = httpClientContext;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void execute(String msg) {
		try {
			Http1GetToken h1 = new Http1GetToken(httpclient, httpClientContext);
			h1.execute();
			
			Http2SendMsg h2 = new Http2SendMsg(httpclient, httpClientContext, h1.getToken(), msg);
			h2.execute();
			
			httpclient.close();
		}
		catch (Exception e) {
			System.out.println("[!]无法推送微信消息 "+e);
		}
	}
	
}
