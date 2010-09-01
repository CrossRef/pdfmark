/*
 * Copyright 2009 CrossRef.org (email: support@crossref.org)
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
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
			
			ni.data = xmpData;
			
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
