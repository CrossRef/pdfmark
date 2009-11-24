package org.crossref.pdfmark;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class FileInfo {
	public String path;
	public byte[] data;
	public boolean missing;
	public IOException error;
	
	public static FileInfo readFileFully(String filePath) {
		FileInfo ni = new FileInfo();
		ni.path = filePath;
		
		try {
			FileInputStream fileIn = new FileInputStream(filePath);
			DataInputStream din = new DataInputStream(fileIn);
			
			byte[] buff = new byte[1024], xmpData = new byte[0];
			int read = 0;
			while ((read = din.read(buff, 0, buff.length)) > 0) {
				byte[] tmp = new byte[xmpData.length + read];
				System.arraycopy(xmpData, 0, tmp, 0, xmpData.length);
				System.arraycopy(buff, 0, tmp, xmpData.length, read);
				xmpData = tmp;
			}
			
			din.close();
		} catch (FileNotFoundException e) {
			ni.missing = true;
			ni.error = e;
		} catch (IOException e) {
			ni.error = e;
		}
		
		return ni;
	}
}
