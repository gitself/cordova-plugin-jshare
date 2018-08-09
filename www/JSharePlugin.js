var JSharePlugin = function() {};


JSharePlugin.prototype.isPlatformIOS = function() {
  return (
    device.platform === "iPhone" ||
    device.platform === "iPad" ||
    device.platform === "iPod touch" ||
    device.platform === "iOS"
  );
};

JSharePlugin.prototype.errorCallback = function(msg) {
  console.log("JShare Callback Error: " + msg);
};

JSharePlugin.prototype.callNative = function(
  name,
  args,
  successCallback,
  errorCallback
) {
  if (errorCallback) {
    cordova.exec(successCallback, errorCallback, "JSharePlugin", name, args);
  } else {
    cordova.exec(
      successCallback,
      this.errorCallback,
      "JSharePlugin",
      name,
      args
    );
  }
};

// Common methods
JSharePlugin.prototype.init = function() {
  if (this.isPlatformIOS()) {
    //this.callNative("initial", [], null);
  } else {
    this.callNative("init", [], null);
  }
};

JSharePlugin.prototype.setDebugMode = function(mode) {
  if (device.platform === "Android") {
    this.callNative("setDebugMode", [mode], null);
  } else {
    if (mode === true) {
      this.setDebugModeFromIos();
    } else {
      this.setLogOFF();
    }
  }
};

/**
 * 设置微信平台信息。
 *  参数说明
 *  appId 微信平台appId
 *  appSecret 微信平台appSecret
 *
 * 设置qq平台信息。
 *  参数说明
 *  appId qq平台appId
 *  appKey qq平台appKey

 * 设置微博平台信息。
 *  参数说明
 *  appKey 微博平台appKey
 *  appSecret 微博平台appSecret
 *  redirectUrl 新浪微博平台的回调url 
 * @param
 * {
 * Wechat:{ 'appId': string, 'appSecret':string }
 * SinaWeibo:{ 'appKey': string, 'appSecret':string,'redirectUrl':string }
 * QQ:{ 'appId': string, 'appKey':string }
 * }
 */ 
JSharePlugin.prototype.configPlatform=function(params){
    this.callNative("configPlatform", [params], successCallback);
};

/**
 * 获取已经正确配置的平台 
 * 
 */ 
JSharePlugin.prototype.getPlatformList=function(successCallback){
    this.callNative("getPlatformList",[], successCallback);
};

/**
 * 判断某个平台是否有效
 * @param name 平台名称，值可选 
 * "Wechat"、
 * "SinaWeibo"、
 * "QQ"
 * */

JSharePlugin.prototype.isClientValid=function(name,successCallback){
    this.callNative("isClientValid", [name], successCallback);
};

/**
 * 判断某个平台是否支持授权
 * @param name 平台名称，值可选 
 * "Wechat"、
 * "SinaWeibo"、
 * "QQ"
 * */
JSharePlugin.prototype.isSupportAuthorize = function(name,successCallback) {
  this.callNative("isSupportAuthorize", [name], successCallback);
};

/**
 * 授权
 * @param name 平台名称，值可选 
 * "Wechat"、
 * "SinaWeibo"、
 * "QQ"
 * */
JSharePlugin.prototype.authorize = function(name,successCallback) {
  this.callNative("authorize", [name],successCallback);
};

/**
 * 判断某个平台是否已经授权
 * @param name 平台名称，值可选 
 * "Wechat"、
 * "SinaWeibo"、
 * "QQ"
 * */
JSharePlugin.prototype.isAuthorize = function(name,successCallback) {
  this.callNative("isAuthorize", [name], successCallback);
};

/**
 * 移除某个平台授权
 * @param name 平台名称，值可选 
 * "Wechat"、
 * "SinaWeibo"、
 * "QQ"
 * */
JSharePlugin.prototype.removeAuthorize = function(name,successCallback) {
  this.callNative("removeAuthorize", [name], successCallback);
};

/**
 * 获取某个平台用户信息
 * @param name 平台名称，值可选 
 * "Wechat"、
 * "SinaWeibo"、
 * "QQ"
 * */
JSharePlugin.prototype.getUserInfo = function(name,successCallback) {
    this.callNative("getUserInfo", [name], successCallback);
};

// iOS methods

JSharePlugin.prototype.startJShareSDK = function() {
  this.callNative("startJShareSDK", [], null);
};

JSharePlugin.prototype.setDebugModeFromIos = function() {
  if (this.isPlatformIOS()) {
    this.callNative("setDebugModeFromIos", [], null);
  }
};

JSharePlugin.prototype.setLogOFF = function() {
  if (this.isPlatformIOS()) {
    this.callNative("setLogOFF", [], null);
  }
};

JSharePlugin.prototype.setCrashLogON = function() {
  if (this.isPlatformIOS()) {
    this.callNative("crashLogON", [], null);
  }
};



/**
 * 用于在 Android 6.0 及以上系统，申请一些权限
 * 具体可看：http://docs.JShare.io/client/android_api/#android-60
 */
JSharePlugin.prototype.requestPermission = function() {
  if (device.platform === "Android") {
    this.callNative("requestPermission", [], null);
  }
};



if (!window.plugins) {
  window.plugins = {};
}

if (!window.plugins.JSharePlugin) {
  window.plugins.JSharePlugin = new JSharePlugin();
}

module.exports = new JSharePlugin();
