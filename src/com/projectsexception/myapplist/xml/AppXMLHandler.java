package com.projectsexception.myapplist.xml;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.projectsexception.myapplist.model.AppInfo;

public class AppXMLHandler extends DefaultHandler {
    
    private List<AppInfo> appInfoList;
    
    @Override
    public void startDocument() throws SAXException {
        appInfoList = new ArrayList<AppInfo>();
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("app".equals(getName(localName, qName))) {
            // Aplicacion
            AppInfo appInfo = new AppInfo();
            appInfo.setName(getAttribute(attributes, "name"));
            appInfo.setPackageName(getAttribute(attributes, "package"));
            appInfoList.add(appInfo);
        }
    }
    
    public List<AppInfo> getAppInfoList() {
        return appInfoList;
    }

    private String getName(String localName, String qName) {
        if (localName == null || localName.length() == 0) {
            return qName;
        }
        return localName;
    }
    
    private String getAttribute(Attributes attributes, String name) {
        String value = null;
        if (attributes != null) {
            value = attributes.getValue(name);
        }
        return value;
    }

}
