package hqr.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class SaveHtml {
	private String fileName;
	private String path;
	private String content;
	public SaveHtml(String fileName, String path, String content) {
		super();
		this.fileName = fileName;
		this.path = path;
		this.content = content;
	}
	
	public void execute() {
		//save magnet url in readme.txt
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path+System.getProperty("file.separator")+fileName)));) {
			bw.write(content);
			bw.flush();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
