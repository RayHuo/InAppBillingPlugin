package com.intel.cordova.inAppBilling;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import android.content.Context;


public class InAppBillingPlugin extends CordovaPlugin {
	public static final String TAG = "InAppBillingPlugin";
	public static final String ACTION_SEND_BILLING = "sendBilling";
	public static final String ACTION_SEND_REFUND = "sendRefund";
	public static final String ACTION_REFUND_DETAIL = "refundDetail";
	public static final String ACTION_STATEMENT_ACCOUNT = "statementAccount";
	private Context context = null;
	private Activity thisActivity = null;
	private CallbackContext callbackContext = null;
	private JSONArray JSData = null;	// This is the product info from js.
	private JSONObject callbackjsb = null;
	
	private IWXAPI api = null;
	// The four major key to apply access_token and package data for bill
	private static final String APP_ID = "wxd930ea5d5a258f4f";
	private static final String APP_SECRET = "db426a9829e4b49a0dcac7b4162da6b6";
	private static final String APP_KEY = "L8LrMqqeGRxST5reouB0K66CaYAWpqhAVsq7ggKkxHCOastWksvuX1uvmvQclxaHoYd3ElNBrNO2DHnnzgfVG9Qs473M3DTOZug5er46FhuGofumV8H2FVR9qkjSlC5K";
	private static final String PARTNER_KEY = "8934e7d15453e97507ef794cf7b0519d";
											   
	private static enum LocalRetCode {
		ERR_OK, ERR_HTTP, ERR_JSON, ERR_OTHER
	}

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		context = this.cordova.getActivity().getApplicationContext();
		thisActivity = this.cordova.getActivity();
		
		// register the app to wechat with APP_ID
		api = WXAPIFactory.createWXAPI(context, APP_ID);
		// api.registerApp(APP_ID);
		boolean registerResult = api.registerApp(APP_ID);
		Toast.makeText(context, "Register result = " + registerResult, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		JSData = args;
		if(ACTION_SEND_BILLING.equals(action)) {
			new GetAccessTokenTask().execute();
			callbackContext.success("Send payment successfully");
			return true;
		}
		if(ACTION_SEND_REFUND.equals(action)) {
			new SendRefundRequestTask().execute();
			callbackContext.success(callbackjsb);
			return true;
		}
		if(ACTION_REFUND_DETAIL.equals(action)) {
			new GetRefundDetailTask().execute();
			callbackContext.success(callbackjsb);
			return true;
		}
		if(ACTION_STATEMENT_ACCOUNT.equals(action)) {
			new GetStatementAccountTask().execute();
			callbackContext.success(callbackjsb);
			return true;
		}
		return false;
	}


	/*
	 * Get access_token task and result
	 */
	private class GetAccessTokenTask extends AsyncTask<Void, Void, GetAccessTokenResult> {
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(thisActivity, "Tips", "Getting access_token");
		}

		@Override
		protected void onPostExecute(GetAccessTokenResult result) {
			if(dialog != null) {
				dialog.dismiss();
			}

			if(result.localRetCode == LocalRetCode.ERR_OK) {
				Toast.makeText(context, "Get access_token successfully", Toast.LENGTH_LONG).show();
				Log.d(TAG, "onPostExecute, access_token = " + result.accessToken);

				// here send the billing to wechatPay
				GetPrepayIdTask getPrepayId = new GetPrepayIdTask(result.accessToken);
				getPrepayId.execute();
			}
			else {
				Toast.makeText(context, "Get access_token fail " + result.localRetCode.name(), Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected GetAccessTokenResult doInBackground(Void... params) {
			GetAccessTokenResult result = new GetAccessTokenResult();

			String url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
						APP_ID, APP_SECRET);
			Log.d(TAG, "Get access_token, url = " + url);

			byte[] buf = Util.httpGet(url);
			if(buf == null || buf.length == 0) {
				result.localRetCode = LocalRetCode.ERR_HTTP;
				return result;
			}

			String content = new String(buf);
			result.parseFrom(content);
			return result;
		}
	}


	private static class GetAccessTokenResult {
		private static final String TAG = "Crosswalk-cordova.InAppBillingPlugin.GetAccessTokenResult";
		public LocalRetCode localRetCode = LocalRetCode.ERR_OTHER;
		public String accessToken;
		public int expiresIn;
		public int errCode;
		public String errMsg;

		public void parseFrom(String content) {
			if(content == null || content.length() <= 0) {
				Log.e(TAG, "parseFrom fail, content is null");
				localRetCode = LocalRetCode.ERR_JSON;
				return;
			}

			try {
				JSONObject json = new JSONObject(content);
				if(json.has("access_token")) {
					accessToken = json.getString("access_token");
					expiresIn = json.getInt("expires_in");
					localRetCode = LocalRetCode.ERR_OK;
				}
				else {
					errCode = json.getInt("errcode");
					errMsg = json.getString("errmsg");
					localRetCode = LocalRetCode.ERR_JSON;
				}
			} catch(Exception e) {
				localRetCode = LocalRetCode.ERR_JSON;
			}
		}
	}



	/*
	 * Get prepayId task and result
	 */
	private class GetPrepayIdTask extends AsyncTask<Void, Void, GetPrepayIdResult> {
		private ProgressDialog dialog;
		private String accessToken;

		public GetPrepayIdTask(String accessToken) {
			this.accessToken = accessToken;
		}

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(thisActivity, "Tips", "Getting prepayId");
		}

		@Override
		protected void onPostExecute(GetPrepayIdResult result) {
			if(dialog != null) {
				dialog.dismiss();
			}

			if(result.localRetCode == LocalRetCode.ERR_OK) {
				Toast.makeText(context, "Get prepayId successfully!", Toast.LENGTH_LONG).show();
				sendPayReq(result);
			}
			else {
				Toast.makeText(context, "Get prepayId fail! " + result.localRetCode.name(), Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected GetPrepayIdResult doInBackground(Void... params) {
			String url = String.format("https://api.weixin.qq.com/pay/genprepay?access_token=%s", accessToken);
			String entity = genProductArgs();

			Log.d(TAG, "doInBackground, url = " + url);
			Log.d(TAG, "doInBackground, entity = " + entity);

			GetPrepayIdResult result = new GetPrepayIdResult();

			byte[] buf = Util.httpPost(url, entity);
			if(buf == null || buf.length == 0) {
				result.localRetCode = LocalRetCode.ERR_HTTP;
				return result;
			}

			String content = new String(buf);
			Log.d(TAG, "doInBackground, content = " + content);
			result.parseFrom(content);
			return result;
		}
	}

	private static class GetPrepayIdResult {
		private static final String TAG = "Crosswalk-cordova.InAppBillingPlugin.GetPrepayIdResult";
		public LocalRetCode localRetCode = LocalRetCode.ERR_OTHER;
		public String prepayId;
		public int errCode;
		public String errMsg;

		public void parseFrom(String content) {
			if(content == null || content.length() <= 0) {
				Log.e(TAG, "parseFrom fail, content is null");
				localRetCode = LocalRetCode.ERR_JSON;
				return;
			}

			try {
				JSONObject json = new JSONObject(content);
				if(json.has("prepayid")) {
					prepayId = json.getString("prepayid");
					localRetCode = LocalRetCode.ERR_OK;
				}
				else {
					localRetCode = LocalRetCode.ERR_JSON;
				}
				errCode = json.getInt("errcode");
				errMsg = json.getString("errmsg");
			} catch(Exception e) {
				localRetCode = LocalRetCode.ERR_JSON;
			}
		}
	}

	
	
	/*
	 * The next functions for refund
	 */
	private class SendRefundRequestTask extends AsyncTask<Void, Void, SendRefundRequestResult> {
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(thisActivity, "Tips", "Sending refund request");
		}

		@Override
		protected void onPostExecute(SendRefundRequestResult result) {
			if(dialog != null) {
				dialog.dismiss();
			}

			if(result.localRetCode == LocalRetCode.ERR_OK) {
				Toast.makeText(context, "Refund success and retcode = " + result.retCode, Toast.LENGTH_LONG).show();
				// No need to send another request 
			}
			else {
				Toast.makeText(context, "Send refund request fail! " + result.localRetCode.name(), Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected SendRefundRequestResult doInBackground(Void... params) {
			SendRefundRequestResult result = new SendRefundRequestResult();

			String url = String.format("https://mch.tenpay.com/refundapi/gateway/refund.xml");
			String entity = genRefundEntity();		// detail parameter data is in JSONData
			Log.d(TAG, "doInBackground, url = " + url);
			Log.d(TAG, "doInBackground, entity = " + entity);

			// use url and entity to send http request and use post method to send data
			byte[] buf = Util.httpPost(url, entity);
			if(buf == null || buf.length == 0) {
				result.localRetCode = LocalRetCode.ERR_HTTP;
				return result;
			}

			String content = new String(buf);
			
			JSONObject jsobj;
			try {
				jsobj = new JSONObject(content);
				setcallbackjsb(jsobj);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Log.d(TAG, "doInBackground, content = " + content);
			result.parseFrom(content);
			return result;
		}
	}
	
	private static class SendRefundRequestResult {
		private static final String TAG = "Crosswalk-cordova.InAppBillingPlugin.SendRefundRequestResult";
		public LocalRetCode localRetCode = LocalRetCode.ERR_OTHER;
		public int retCode;
		public int errCode;
		public String errMsg;

		public void parseFrom(String content) {
			if(content == null || content.length() <= 0) {
				Log.e(TAG, "parseFrom fail, content is null");
				localRetCode = LocalRetCode.ERR_JSON;
				return;
			}

			try {
				JSONObject json = new JSONObject(content);
				if(json.has("retcode")) {
					retCode = json.getInt("retcode");
					localRetCode = LocalRetCode.ERR_OK;
				}
				else {
					localRetCode = LocalRetCode.ERR_JSON;
				}
				errCode = json.getInt("errcode");
				errMsg = json.getString("errmsg");
			} catch(Exception e) {
				localRetCode = LocalRetCode.ERR_JSON;
			}
		}
	}

	private String genRefundEntity() {
		JSONObject jsonObj = new JSONObject();

		try {
			jsonObj.put("sign_type", "MD5");
			jsonObj.put("input_charset", "utf-8");
			jsonObj.put("sign_key_index", 1);

			List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
			packageParams.add(new BasicNameValuePair("partner", JSData.getJSONObject(0).getString("partner")));
			packageParams.add(new BasicNameValuePair("out_trade_no", JSData.getJSONObject(0).getString("out_trade_no")));
			packageParams.add(new BasicNameValuePair("transaction_id", JSData.getJSONObject(0).getString("transaction_id")));
			packageParams.add(new BasicNameValuePair("out_refund_no", JSData.getJSONObject(0).getString("out_refund_no")));
			packageParams.add(new BasicNameValuePair("total_fee", JSData.getJSONObject(0).getString("total_fee")));
			packageParams.add(new BasicNameValuePair("refund_fee", JSData.getJSONObject(0).getString("refund_fee")));
			packageParams.add(new BasicNameValuePair("op_user_id", JSData.getJSONObject(0).getString("op_user_id")));
			packageParams.add(new BasicNameValuePair("op_user_passwd", JSData.getJSONObject(0).getString("op_user_passwd")));
			packageParams.add(new BasicNameValuePair("recv_user_id", JSData.getJSONObject(0).getString("recv_user_id")));
			packageParams.add(new BasicNameValuePair("recv_user_name", JSData.getJSONObject(0).getString("recv_user_name")));
			packageParams.add(new BasicNameValuePair("use_spbill_no_flag", JSData.getJSONObject(0).getString("use_spbill_no_flag")));
			packageParams.add(new BasicNameValuePair("refund_type", JSData.getJSONObject(0).getString("refund_type")));
			packageValue = genPackage(packageParams);

			jsonObj.put("sign", packageValue);
		} catch(Exception e) {
			Log.e(TAG, "genRefundEntity fail, e = " + e.getMessage());
			return null;
		}
		return jsonObj.toString();
	}
	
	
	
	/*
	 * The next functions for refund Detail request
	 */
	private class GetRefundDetailTask extends AsyncTask<Void, Void, GetRefundDetailResult> {
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(thisActivity, "Tips", "Asking refund detail");
		}

		@Override
		protected void onPostExecute(GetRefundDetailResult result) {
			if(dialog != null) {
				dialog.dismiss();
			}

			if(result.localRetCode == LocalRetCode.ERR_OK) {
				Toast.makeText(context, "Refund Detail success and retcode = " + result.retCode, Toast.LENGTH_LONG).show();
				// No need to send another request 
			}
			else {
				Toast.makeText(context, "Ask refund detail fail! " + result.localRetCode.name(), Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected GetRefundDetailResult doInBackground(Void... params) {
			GetRefundDetailResult result = new GetRefundDetailResult();

			String url = String.format("https://gw.tenpay.com/gateway/normalrefundquery.xml");
			String entity = genRefundDetail();		// detail parameter data is in JSONData
			Log.d(TAG, "doInBackground, url = " + url);
			Log.d(TAG, "doInBackground, entity = " + entity);

			// use url and entity to send http request and use post method to send data
			byte[] buf = Util.httpPost(url, entity);
			if(buf == null || buf.length == 0) {
				result.localRetCode = LocalRetCode.ERR_HTTP;
				return result;
			}

			String content = new String(buf);
			
			JSONObject jsobj;
			try {
				jsobj = new JSONObject(content);
				setcallbackjsb(jsobj);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Log.d(TAG, "doInBackground, content = " + content);
			result.parseFrom(content);
			return result;
		}
	}
	
	private static class GetRefundDetailResult {
		private static final String TAG = "Crosswalk-cordova.InAppBillingPlugin.GetRefundDetailResult";
		public LocalRetCode localRetCode = LocalRetCode.ERR_OTHER;
		public int retCode;
		public int errCode;
		public String errMsg;

		public void parseFrom(String content) {
			if(content == null || content.length() <= 0) {
				Log.e(TAG, "parseFrom fail, content is null");
				localRetCode = LocalRetCode.ERR_JSON;
				return;
			}

			try {
				JSONObject json = new JSONObject(content);
				if(json.has("retcode")) {
					retCode = json.getInt("retcode");
					localRetCode = LocalRetCode.ERR_OK;
				}
				else {
					localRetCode = LocalRetCode.ERR_JSON;
				}
				errCode = json.getInt("errcode");
				errMsg = json.getString("errmsg");
			} catch(Exception e) {
				localRetCode = LocalRetCode.ERR_JSON;
			}
		}
	}

	private String genRefundDetail() {
		JSONObject jsonObj = new JSONObject();

		try {
			jsonObj.put("sign_type", "MD5");
			jsonObj.put("input_charset", "utf-8");
			jsonObj.put("sign_key_index", 1);

			List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
			packageParams.add(new BasicNameValuePair("partner", JSData.getJSONObject(0).getString("partner")));
			packageParams.add(new BasicNameValuePair("out_trade_no", JSData.getJSONObject(0).getString("out_trade_no")));
			packageParams.add(new BasicNameValuePair("transaction_id", JSData.getJSONObject(0).getString("transaction_id")));
			packageParams.add(new BasicNameValuePair("out_refund_no", JSData.getJSONObject(0).getString("out_refund_no")));
			packageParams.add(new BasicNameValuePair("refund_id", JSData.getJSONObject(0).getString("refund_id")));			
			packageParams.add(new BasicNameValuePair("use_spbill_no_flag", JSData.getJSONObject(0).getString("use_spbill_no_flag")));
			packageValue = genPackage(packageParams);

			jsonObj.put("sign", packageValue);
		} catch(Exception e) {
			Log.e(TAG, "genRefundDetail fail, e = " + e.getMessage());
			return null;
		}
		return jsonObj.toString();
	}

	
	
	
	
	/*
	 * The next functions for statement account 
	 */
	private class GetStatementAccountTask extends AsyncTask<Void, Void, GetStatementAccountResult> {
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(thisActivity, "Tips", "Asking statement account detail");
		}

		@Override
		protected void onPostExecute(GetStatementAccountResult result) {
			if(dialog != null) {
				dialog.dismiss();
			}

			if(result.localRetCode == LocalRetCode.ERR_OK) {
				Toast.makeText(context, "Statement account success and retcode = " + result.retCode, Toast.LENGTH_LONG).show();
				// No need to send another request 
			}
			else {
				Toast.makeText(context, "Ask statement account fail! " + result.localRetCode.name(), Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected GetStatementAccountResult doInBackground(Void... params) {
			GetStatementAccountResult result = new GetStatementAccountResult();

			String url = String.format("http://mch.tenpay.com/cgi-bin/mchdown_real_new.cgi");
			String entity = genStatementAccount();		// detail parameter data is in JSONData
			Log.d(TAG, "doInBackground, url = " + url);
			Log.d(TAG, "doInBackground, entity = " + entity);

			// use url and entity to send http request and use post method to send data
			byte[] buf = Util.httpPost(url, entity);
			if(buf == null || buf.length == 0) {
				result.localRetCode = LocalRetCode.ERR_HTTP;
				return result;
			}

			String content = new String(buf);
			
			JSONObject jsobj;
			try {
				jsobj = new JSONObject(content);
				setcallbackjsb(jsobj);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Log.d(TAG, "doInBackground, content = " + content);
			result.parseFrom(content);
			return result;
		}
	}
	
	private static class GetStatementAccountResult {
		private static final String TAG = "Crosswalk-cordova.InAppBillingPlugin.GetStatementAccountResult";
		public LocalRetCode localRetCode = LocalRetCode.ERR_OTHER;
		public int retCode;
		public int errCode;
		public String errMsg;

		public void parseFrom(String content) {
			if(content == null || content.length() <= 0) {
				Log.e(TAG, "parseFrom fail, content is null");
				localRetCode = LocalRetCode.ERR_JSON;
				return;
			}

			try {
				JSONObject json = new JSONObject(content);
				if(json.has("retcode")) {
					retCode = json.getInt("retcode");
					localRetCode = LocalRetCode.ERR_OK;
				}
				else {
					localRetCode = LocalRetCode.ERR_JSON;
				}
				errCode = json.getInt("errcode");
				errMsg = json.getString("errmsg");
			} catch(Exception e) {
				localRetCode = LocalRetCode.ERR_JSON;
			}
		}
	}

	private String genStatementAccount() {
		JSONObject jsonObj = new JSONObject();

		try {
			jsonObj.put("sign_type", "MD5");
			jsonObj.put("input_charset", "utf-8");
			jsonObj.put("sign_key_index", 1);

			List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
			packageParams.add(new BasicNameValuePair("spid", JSData.getJSONObject(0).getString("spid")));
			packageParams.add(new BasicNameValuePair("trans_time", JSData.getJSONObject(0).getString("trans_time")));
			packageParams.add(new BasicNameValuePair("stamp", JSData.getJSONObject(0).getString("stamp")));
			packageParams.add(new BasicNameValuePair("cft_signtype", JSData.getJSONObject(0).getString("cft_signtype")));
			packageParams.add(new BasicNameValuePair("mchtype", JSData.getJSONObject(0).getString("mchtype")));
			packageValue = genPackage(packageParams);

			jsonObj.put("sign", packageValue);
		} catch(Exception e) {
			Log.e(TAG, "genRefundDetail fail, e = " + e.getMessage());
			return null;
		}
		return jsonObj.toString();
	}
	
	
	
	

	/*
	 * Functions to generate parameters for the billing package post to wechat server via http
	 */
	private String genNonceStr() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
	}

	private long genTimeStamp() {
		return System.currentTimeMillis() / 1000;	// Get timestamp in seconds
	}

	private String getTraceId() {
		return "crestxu_" + genTimeStamp();
	}

	private String genOutTradNo() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
	}

	private long timeStamp;
	private String nonceStr, packageValue;

	private String genSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		int i = 0;
		for(; i < params.size() - 1; i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append(params.get(i).getName());
		sb.append('=');
		sb.append(params.get(i).getValue());

		String sha1 = Util.sha1(sb.toString());
		Log.d(TAG, "genSign, sha1 = " + sha1);
		return sha1;
	}

	// Here is the Product info, need to change to get from the third party app
	// maybe there should only export an interface to the third party, and they need to complete it.
	private String genProductArgs() {
		JSONObject json = new JSONObject();

		try {
			json.put("appid", APP_ID);
			String traceId = getTraceId();
			json.put("traceid", traceId);
			nonceStr = genNonceStr();
			json.put("noncestr", nonceStr);

			List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
			packageParams.add(new BasicNameValuePair("bank_type", "WX"));
			packageParams.add(new BasicNameValuePair("body", JSData.getJSONObject(0).getString("body")));
			packageParams.add(new BasicNameValuePair("fee_type", "1"));	 // default currency type : RMB
			packageParams.add(new BasicNameValuePair("input_charset", "UTF-8"));
			packageParams.add(new BasicNameValuePair("notify_url", "http://weixin.qq.com"));
			packageParams.add(new BasicNameValuePair("out_trade_no", genOutTradNo()));
			packageParams.add(new BasicNameValuePair("partner", JSData.getJSONObject(0).getString("partner")));	
			packageParams.add(new BasicNameValuePair("spbill_create_ip", JSData.getJSONObject(0).getString("IP")));	// user pubilc internet ip
			packageParams.add(new BasicNameValuePair("total_fee", JSData.getJSONObject(0).getString("totalFee")));	// total fee 1 = 0.01 RMB.
			packageValue = genPackage(packageParams);

			json.put("package", packageValue);
			timeStamp = genTimeStamp();
			json.put("timestamp", timeStamp);

			List<NameValuePair> signParams = new LinkedList<NameValuePair>();
			signParams.add(new BasicNameValuePair("appid", APP_ID));
			signParams.add(new BasicNameValuePair("appkey", APP_KEY));
			signParams.add(new BasicNameValuePair("noncestr", nonceStr));
			signParams.add(new BasicNameValuePair("package", packageValue));
			signParams.add(new BasicNameValuePair("timestamp", String.valueOf(timeStamp)));
			signParams.add(new BasicNameValuePair("traceid", traceId));
			json.put("app_signature", genSign(signParams));
			
			json.put("sign_method", "sha1");
		} catch(Exception e) {
			Log.e(TAG, "genProductArgs fail, e = " + e.getMessage());
			return null;
		}

		return json.toString();
	}

	private String genPackage(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		sb.append(PARTNER_KEY);

		String packageSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();

		return URLEncodedUtils.format(params, "utf-8") + "&sign=" + packageSign;
	}
	
	private void sendPayReq(GetPrepayIdResult result) {
		PayReq req = new PayReq();
		req.appId = APP_ID;
		req.partnerId = PARTNER_KEY;
		req.prepayId = result.prepayId;
		req.nonceStr = nonceStr;
		req.timeStamp = String.valueOf(timeStamp);
		req.packageValue = "Sign=" + packageValue;

		List<NameValuePair> signParams = new LinkedList<NameValuePair>();
		signParams.add(new BasicNameValuePair("appid", req.appId));
		signParams.add(new BasicNameValuePair("appkey", APP_KEY));
		signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
		signParams.add(new BasicNameValuePair("package", req.packageValue));
		signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
		signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
		signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));
		req.sign = genSign(signParams);

		api.sendReq(req);
	}
	
	private void setcallbackjsb(JSONObject jsobj) {
		callbackjsb = jsobj;
	}
}