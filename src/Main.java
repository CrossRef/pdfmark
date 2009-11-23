import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class Main {

	public static void main(String[] args) {
		if (args[0].equals("-i")) {
		
			try {
				// Grab the XMP
				DataInputStream din = new DataInputStream(new FileInputStream(new File(args[1])));
			
				byte[] buff = new byte[1024], xmpData = new byte[0];
				int read = 0;
				while ((read = din.read(buff, 0, buff.length)) > 0) {
					byte[] tmp = new byte[xmpData.length + read];
					System.arraycopy(xmpData, 0, tmp, 0, xmpData.length);
					System.arraycopy(buff, 0, tmp, xmpData.length, read);
				}
				
				din.close();
				
				// Insert the XMP into the document as XMP data.
				PdfReader reader = new PdfReader(new FileInputStream(new File(args[2])));
				PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(args[3])));
				
				stamper.setXmpMetadata(xmpData);
				
				stamper.close();
				reader.close();
			} catch (IOException e ) {
				System.err.println(e);
			} catch (DocumentException e) {
				System.err.println(e);
			}
			
		} else if (args[0].equals("-x")) {
			
			try {
				// Grab XMP from a reader and dump it.
				PdfReader reader = new PdfReader(new FileInputStream(new File(args[1])));
				byte[] metadata = reader.getMetadata();
				reader.close();
				
				DataOutputStream dout = new DataOutputStream(new FileOutputStream(new File(args[2])));
				dout.write(metadata);
				dout.close();
			} catch (IOException e) {
				System.err.println(e);
			}
			
		}
	}

}
