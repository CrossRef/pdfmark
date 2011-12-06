package org.crossref.pdfmark;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.itextpdf.text.pdf.PdfReader;

/**
 * Utility that dumps XMP data of a PDF to standard out.
 */
public class DumperMain {

    public static void main(String[] args) {
        for (String filename : args) {
            
            File f = new File(filename);
            FileInputStream fileIn;
            PdfReader reader;
            
            try {
                fileIn = new FileInputStream(f);
                reader = new PdfReader(fileIn);
                byte[] merged = reader.getMetadata();
                ByteArrayInputStream bIn = new ByteArrayInputStream(merged);
                BufferedReader bR = new BufferedReader(new InputStreamReader(bIn));
                String line;
                while ((line = bR.readLine()) != null) {
                    System.out.println(line);
                }
                
                reader.close();
                fileIn.close();
            } catch (IOException e) {
                System.err.println("Couldn't read file '" + filename + "'.");
                System.err.println(e);
            }
        }
    }
    
}
