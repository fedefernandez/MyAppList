package com.projectsexception.myapplist.model;

import java.text.Collator;

public class AppInfo implements Comparable<AppInfo> {
    
    private final Collator sCollator = Collator.getInstance();
    
    private String packageName;
    private String name;
    private boolean installed;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    @Override
    public int compareTo(AppInfo another) {
        if (!installed && another.installed) {
            return -1;
        } else  if (installed && !another.installed) {
            return 1;
        }
        return sCollator.compare(getName(), another.getName());
    }
    

}
