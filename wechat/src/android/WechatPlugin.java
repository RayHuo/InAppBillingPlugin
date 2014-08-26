package com.intel.cordova.wechat;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class WechatPlugin extends CordovaPlugin {
	public static final String TAG = "WechatPlugin";
	public static final String ACTION_GET_CARRIER_CODE = "sendTextAPI";
	private Context context = null;
	private CallbackContext callbackContext = null;
	private IWXAPI api = null;  	// here is a problem is there any method to load the lib of wechat automatically
	private static final String APP_ID = "wx3fd0e864160cedf4";

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		context = this.cordova.getActivity().getApplicationContext();
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
		if(ACTION_GET_CARRIER_CODE.equals(action)) {
			api = WXAPIFactory.createWXAPI(context, APP_ID);
			api.registerApp(APP_ID);

			String text = "Wechat Text";
			WXTextObject textObj = new WXTextObject();
			textObj.text = text;

			WXMediaMessage msg = new WXMediaMessage();
			msg.mediaObject = textObj;
			msg.description = text;

			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = String.valueOf(System.currentTimeMillis());
			req.message = msg;
			req.scene = SendMessageToWX.Req.WXSceneSession;
			// req.openId = getOpenId();
			boolean sendResult = api.sendReq(req);
			Toast.makeText(context, "Send Result = " + sendResult, Toast.LENGTH_LONG).show();
//			callbackContext.success("Send success!");
			return true;
		}
		else {
			callbackContext.error("Send fail!");
			return false;
		}
		
	}

	
}
