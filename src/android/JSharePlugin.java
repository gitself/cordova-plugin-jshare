package cn.jiguang.cordova.share;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.jiguang.share.android.api.AuthListener;
import cn.jiguang.share.android.api.JShareInterface;
import cn.jiguang.share.android.api.Platform;
import cn.jiguang.share.android.api.PlatformConfig;
import cn.jiguang.share.android.model.AccessTokenInfo;
import cn.jiguang.share.android.model.BaseResponseInfo;
import cn.jiguang.share.android.model.UserInfo;
import cn.jiguang.share.android.utils.Logger;
import cn.jiguang.share.qqmodel.QQ;
import cn.jiguang.share.wechat.Wechat;
import cn.jiguang.share.weibo.SinaWeibo;

public class JSharePlugin extends CordovaPlugin {

    private static final String TAG = JSharePlugin.class.getSimpleName();

    private Context mContext;

    private static JSharePlugin instance;
    private static Activity cordovaActivity;

    public JSharePlugin() {
        instance = this;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        mContext = cordova.getActivity().getApplicationContext();
        cordovaActivity = cordova.getActivity();
    }

    public void onResume(boolean multitasking) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cordovaActivity = null;
        instance = null;
    }

    @Override
    public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext)
            throws JSONException {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Method method = JSharePlugin.class.getDeclaredMethod(action, JSONArray.class, CallbackContext.class);
                    method.invoke(JSharePlugin.this, data, callbackContext);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });
        return true;
    }

    void init(JSONArray data, CallbackContext callbackContext) {
        PlatformConfig conf=new PlatformConfig();
        JSONObject confData=data.getJSONObject(0);
        if(confData==null){
            callbackContext.error("config params must not be null");
            return;
        }
        this.configPlatform(confData,callbackContext);
        JShareInterface.init(mContext);
        callbackContext.success("jshare sdk init finish");
    }

    void setDebugMode(JSONArray data, CallbackContext callbackContext) {
        boolean mode;
        try {
            mode = data.getBoolean(0);
            JShareInterface.setDebugMode(mode);
            callbackContext.success();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private configPlatform(JSONObject confData, CallbackContext callbackContext) {
        JSONObject wechat=confData.optJSONObject(Wechat.Name);
        JSONObject qq=confData.optJSONObject(QQ.Name);
        JSONObject weibo=confData.optJSONObject(SinaWeibo.Name);
        if(wechat!=null){
            try{
                String appId = wechat.getString("appId");
                String appSecret = wechat.getString("appSecret");
                conf.setWechat(appId, appSecret);
            }catch (JSONException e){
                e.printStackTrace();
                callbackContext.error("config "+Wechat.Name+" parameters error.");
             }
        }
        if(qq!=null){
            try{
                String appId = qq.getString("appId");
                String appKey = qq.getString("appKey");
                conf.setQQ(appId, appKey);
            }catch (JSONException e){
                e.printStackTrace();
                callbackContext.error("config "+QQ.Name+" parameters error.");
            }
        }
        if(weibo!=null){
            try{
                String appKey = weibo.getString("appKey");
                String appSecret = weibo.getString("appSecret");
                String redirectUrl = weibo.getString("redirectUrl");
                conf.setSinaWeibo(appKey,appSecret,redirectUrl);
            }catch (JSONException e){
                e.printStackTrace();
                callbackContext.error("config "+SinaWeibo.Name+" parameters error.");
            }
        }
    }

    void getPlatformList(JSONArray data, final CallbackContext callback) {
        List<String> plats=JShareInterface.getPlatformList();
        JSONArray res = new JSONArray(plats);
        callback.success(res);
    }

    void isClientValid(JSONArray data, CallbackContext callbackContext) {
        String platform=null;
        try {
            platform = data.getString(0);
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error("Parameters error.");
        }
        boolean res=JShareInterface.isClientValid(platform);
        if(res){
            callbackContext.success(1);
        }else{
            callbackContext.success(0);
        }


    }

    void isSupportAuthorize(JSONArray data, CallbackContext callbackContext) {
        String platform=null;
        try {
            platform = data.getString(0);
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error("platform name parameters error.");
        }
        boolean res=JShareInterface.isSupportAuthorize(platform);
        if(res){
            callbackContext.success(1);
        }else{
            callbackContext.success(0);
        }
    }

    void authorize(JSONArray data, CallbackContext callbackContext) {
        String platform = null;
        try {
            platform = data.getString(0);
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error("Parameters error.");
        }
        JShareInterface.authorize(platform,new ThirdPartyAuthListener(callbackContext));
    }

    void isAuthorize(JSONArray data, CallbackContext callbackContext) {
        String platform = null;
        try {
            platform = data.getString(0);
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error("Parameters error.");
        }
        boolean res=JShareInterface.isAuthorize(platform);
        if(res){
            callbackContext.success(1);
        }else{
            callbackContext.success(0);
        }
    }

    void removeAuthorize(JSONArray data, CallbackContext callbackContext) {
        String platform = null;
        try {
            platform = data.getString(0);
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error("Parameters error.");
        }
        JShareInterface.removeAuthorize(platform,new ThirdPartyAuthListener(callbackContext));
    }

    void getUserInfo(JSONArray data, CallbackContext callbackContext) {
        String platform = null;
        try {
            platform = data.getString(0);
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error("Parameters error.");
        }
        JShareInterface.getUserInfo(platform,new ThirdPartyAuthListener(callbackContext));
    }

    /**
     * 用于 Android 6.0 以上系统申请权限，具体可参考：
     * http://docs.Push.io/client/android_api/#android-60
     */
    void requestPermission(JSONArray data, CallbackContext callbackContext) {
        //JShareInterface.requestPermission(this.cordova.getActivity());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean hasPermission(String appOpsServiceId) {
        Context context = cordova.getActivity().getApplicationContext();
        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();

        String pkg = context.getPackageName();
        int uid = appInfo.uid;
        Class appOpsClazz;

        try {
            appOpsClazz = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClazz.getMethod("checkOpNoThrow", Integer.TYPE, Integer.TYPE,
                    String.class);
            Field opValue = appOpsClazz.getDeclaredField(appOpsServiceId);
            int value = opValue.getInt(Integer.class);
            Object result = checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg);
            return Integer.parseInt(result.toString()) == AppOpsManager.MODE_ALLOWED;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }


    private  class ThirdPartyAuthListener implements AuthListener{
        private CallbackContext  callbackContext;

        ThirdPartyAuthListener(CallbackContext cbContext){
            this.callbackContext=cbContext;
        }

        @Override
        public void onComplete(Platform platform, int action, BaseResponseInfo data) {
            Logger.dd(TAG, "onComplete:" + platform + ",action:" + action + ",data:" + data);
            String toastMsg = null;
            switch (action) {
                case Platform.ACTION_AUTHORIZING:
                    if (data instanceof AccessTokenInfo) {        //授权信息
                        String token = ((AccessTokenInfo) data).getToken();//token
                        long expiration = ((AccessTokenInfo) data).getExpiresIn();//token有效时间，时间戳
                        String refresh_token = ((AccessTokenInfo) data).getRefeshToken();//refresh_token
                        String openid = ((AccessTokenInfo) data).getOpenid();//openid
                        //授权原始数据，开发者可自行处理
                        String originData = data.getOriginData();
                        toastMsg = "授权成功:" + data.toString();
                        Logger.dd(TAG, "openid:" + openid + ",token:" + token + ",expiration:" + expiration + ",refresh_token:" + refresh_token);
                        Logger.dd(TAG, "originData:" + originData);

                        JSONObject res=new JSONObject();
                        try {
                            res.put("token",token);
                            res.put("expiration",expiration);
                            res.put("refresh_token",refresh_token);
                            res.put("openid",openid);
                            res.put("originData",originData);
                            this.callbackContext.success(res);
                        }catch (JSONException e){
                            e.printStackTrace();
                            callbackContext.error("warp data to jsonobject error.");
                        }

                    }
                    break;
                case Platform.ACTION_REMOVE_AUTHORIZING:
                    this.callbackContext.success();
                    toastMsg = "删除授权成功";
                    break;
                case Platform.ACTION_USER_INFO:
                    if (data instanceof UserInfo) {      //第三方个人信息
                        String openid = ((UserInfo) data).getOpenid();  //openid
                        String name = ((UserInfo) data).getName();  //昵称
                        String imageUrl = ((UserInfo) data).getImageUrl();  //头像url
                        int gender = ((UserInfo) data).getGender();//性别, 1表示男性；2表示女性
                        //个人信息原始数据，开发者可自行处理
                        String originData = data.getOriginData();
                        toastMsg = "获取个人信息成功:" + data.toString();
                        Logger.dd(TAG, "openid:" + openid + ",name:" + name + ",gender:" + gender + ",imageUrl:" + imageUrl);
                        Logger.dd(TAG, "originData:" + originData);
                        Logger.dd(TAG, toastMsg);

                        JSONObject res=new JSONObject();
                        try {
                            res.put("openid",openid);
                            res.put("name",name);
                            res.put("imageUrl",imageUrl);
                            res.put("gender",gender);
                            res.put("originData",originData);
                            this.callbackContext.success(res);
                        }catch (JSONException e){
                            e.printStackTrace();
                            this.callbackContext.error("warp userdata to jsonobject error");
                        }
                    }
                    break;
            }
        }

        @Override
        public void onError(Platform platform, int action, int errorCode, Throwable error) {
            Logger.dd(TAG, "onError:" + platform + ",action:" + action + ",error:" + error);
            String toastMsg = null;
            switch (action) {
                case Platform.ACTION_AUTHORIZING:
                    toastMsg = "授权失败";
                    this.callbackContext.error(error.getMessage());
                    break;
                case Platform.ACTION_REMOVE_AUTHORIZING:
                    toastMsg = "删除授权失败";
                    this.callbackContext.error(error.getMessage());
                    break;
                case Platform.ACTION_USER_INFO:
                    toastMsg = "获取个人信息失败";
                    this.callbackContext.error(error.getMessage());
                    break;
            }
            Logger.dd(TAG,toastMsg);
        }

        @Override
        public void onCancel(Platform platform, int action) {
            Logger.dd(JSharePlugin.TAG, "onCancel:" + platform + ",action:" + action);
            String toastMsg = null;
            switch (action) {
                case Platform.ACTION_AUTHORIZING:
                    toastMsg = "取消授权";
                    this.callbackContext.error("authorization canceled");
                    break;
                // TODO: 2017/6/23 删除授权不存在取消
                case Platform.ACTION_REMOVE_AUTHORIZING:
                    break;
                case Platform.ACTION_USER_INFO:
                    toastMsg = "取消获取个人信息";
                    this.callbackContext.error("getUserInfo canceled");
                    break;
            }
            Logger.dd(TAG,toastMsg);
        }

    }
}



