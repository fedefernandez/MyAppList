package com.projectsexception.myapplist.xml;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.projectsexception.myapplist.model.AppInfo;

public class AppXMLHandler extends DefaultHandler {
    
    private static final String PACKAGE_ELEMENT = "package";
    private static final String NAME_ELEMENT = "name";
    private static final String APP_ELEMENT = "app";
    
    private ArrayList<AppInfo> appInfoList;
    
    @Override
    public void startDocument() throws SAXException {
        appInfoList = new ArrayList<AppInfo>();
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (APP_ELEMENT.equals(getName(localName, qName))) {
            // Aplication
            String name = getAttribute(attributes, NAME_ELEMENT);
            String packageName = getAttribute(attributes, PACKAGE_ELEMENT);
            if (name != null && packageName != null) {
                AppInfo appInfo = new AppInfo();
                appInfo.setName(name);
                appInfo.setPackageName(packageName);
                appInfoList.add(appInfo);
            }
        }
    }
    
    public ArrayList<AppInfo> getAppInfoList() {
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
