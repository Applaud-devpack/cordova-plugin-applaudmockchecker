package bosowa.hris.cordova;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings.Secure;
import android.provider.Settings.Global;
import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MockChecker extends CordovaPlugin {

    private JSONObject objGPS = new JSONObject();
    private final String TAG = "MOCKLOCATION";
    private bosowa.hris.cordova.MockChecker mContext;

    @Override
    public boolean execute(String action, JSONArray data, final CallbackContext callbackContext) throws JSONException {
        mContext = this;
        if (action.equals("check")) {
            objGPS = new JSONObject();
            objGPS.put("osVersion",android.os.Build.VERSION.SDK_INT);
            if (android.os.Build.VERSION.SDK_INT <= 22) {
                if (Secure.getString(this.cordova.getActivity().getContentResolver(), Secure.ALLOW_MOCK_LOCATION)
                        .equals("0")) {
                    objGPS.put("isMock", false);
                } else {
                    objGPS.put("isMock", true);
                    objGPS.put("title", "GPS spoofing detected");
                    objGPS.put("messages", "Please turn off Allow Mock locations option in developer options.");
                }

            } else {
                objGPS.put("isMock", isDevModeEnabled(mContext.cordova.getActivity()));
                if (objGPS.getBoolean("isMock")) {
                    objGPS.put("title", "Developer options enabled");
                    objGPS.put("messages",
                            "You have enabled Developer options on your device. You are unable to use this feature until Developer options are disabled.");

                } 
                // else {
                //     objGPS.put("isMock", isMockPermissionGranted(mContext.cordova.getActivity()));
                //     if (objGPS.getBoolean("isMock")) {
                //         objGPS.put("title", "GPS spoofing detected");
                //         objGPS.put("messages",
                //                 "You have one or more GPS spoofing apps installed on your device. You are unable to use this feature until those apps are uninstalled.");
                //     }
                // }
            }
            // Log.i(TAG,"Location", "isMock: " + objGPS.get("isMock"));
            callbackContext.success(objGPS);
            return true;
        } else {
            return false;
        }

    }

    public static boolean isMockPermissionGranted(Context context) {
        int count = 0;

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName,
                        PackageManager.GET_PERMISSIONS);

                // Get Permissions
                String[] requestedPermissions = packageInfo.requestedPermissions;

                if (requestedPermissions != null) {
                    for (int i = 0; i < requestedPermissions.length; i++) {
                        // Check for System App //
                        if (!((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1)) {
                            if (requestedPermissions[i]
                                    .equals("android.permission.ACCESS_MOCK_LOCATION")
                                    && !applicationInfo.packageName.equals(context.getPackageName())) {
                                count++;
                            }
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("Got exception ", e.getMessage());
            }
        }

        if (count > 0)
            return true;
        return false;
    }

    public static boolean isDevModeEnabled(Context context) {
        if (Secure.getString(
                context.getContentResolver(),
                Global.DEVELOPMENT_SETTINGS_ENABLED)
                .equals("0")) {
            return false;
        } else {
            return true;
        }

    }

}
