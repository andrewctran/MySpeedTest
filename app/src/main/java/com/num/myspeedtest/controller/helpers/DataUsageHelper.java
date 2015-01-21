package com.num.myspeedtest.controller.helpers;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;

import com.num.myspeedtest.model.Application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class to retrieve application data usage information.
 */
public class DataUsageHelper {
    private static long totalUsage;
    private static long maxUsage;

    public static Application[] getApplications(Context context){
        PackageManager pm = context.getPackageManager();
        totalUsage = 0;
        maxUsage = 0;
        List<Application> appList = new ArrayList<Application>();
        List<ApplicationInfo> appInfo = pm.getInstalledApplications(0);
        Set<Integer> uids = new HashSet<Integer>();
        for (ApplicationInfo info:appInfo) { // Loop through all installed apps
            Integer uid = info.uid;
            if(uids.contains(uid)) continue; // Skip if an application with same uid is found
            uids.add(uid);
            long recv = TrafficStats.getUidRxBytes(uid);
            long sent = TrafficStats.getUidTxBytes(uid);
            if(recv > 0 || sent > 0) { // Only get apps that used sent or received data
                String appName = info.loadLabel(pm).toString();
                String pkgName = info.packageName;
                Drawable appIcon = info.loadIcon(pm);
                Application app = new Application(appName, pkgName, appIcon);
                app.setTotalRecv(recv);
                app.setTotalSent(sent);
                totalUsage += recv + sent;
                if(recv + sent > maxUsage) maxUsage = recv + sent;
                appList.add(app);
            }
        }
        //Convert to array and sort in descending order of usage
        Application[] appArray = appList.toArray(new Application[appList.size()]);
        Arrays.sort(appArray);
        return appArray;
    }

    public static long getTotalUsage() {
        return totalUsage;
    }

    public static long getMaxUsage() {
        return maxUsage;
    }
}