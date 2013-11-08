package com.projectsexception.myapplist.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.projectsexception.util.CustomLog;

public class ParserUtil {

    private static final String TAG = "ParserUtil";

    public static void launchParser(File file, ContentHandler handler) throws ParserException {
        if (file != null && file.exists()) {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(file);
                SAXParserFactory spf = SAXParserFactory.newInstance(); 
                SAXParser sp = spf.newSAXParser();              
                XMLReader xr = sp.getXMLReader();                
                xr.setContentHandler(handler);              
                xr.parse(new InputSource(stream));
            } catch (SAXException e) {
                throw new ParserException(e);
            } catch (IOException e) {
                throw new ParserException(e);
            } catch (Exception e) {
                throw new ParserException(e);
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    CustomLog.getInstance().error(TAG, "Error closing stream");
                }                
            }
        }
    }

}
