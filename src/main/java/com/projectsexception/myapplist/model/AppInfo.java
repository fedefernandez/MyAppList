package com.projectsexception.myapplist.model;

import java.text.Collator;

import android.os.Parcel;
import android.os.Parcelable;

import com.sun.istack.internal.NotNull;

public class AppInfo implements Comparable<AppInfo>, Parcelable {
    
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
    public int compareTo(@NotNull AppInfo another) {
        if (!installed && another.installed) {
            return -1;
        } else  if (installed && !another.installed) {
            return 1;
        }
        return sCollator.compare(getName(), another.getName());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(packageName);
        dest.writeString(Boolean.toString(installed));
    }
    
    public static final Parcelable.Creator<AppInfo> CREATOR = new Parcelable.Creator<AppInfo>() {

        @Override
        public AppInfo createFromParcel(Parcel source) {
            AppInfo appInfo = new AppInfo();
            appInfo.setName(source.readString());
            appInfo.setPackageName(source.readString());
            appInfo.setInstalled(Boolean.parseBoolean(source.readString()));
            return appInfo;
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
        
        
    };
    

}
