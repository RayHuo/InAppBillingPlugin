public class InAppBillingPlugin extends CordovaPlugin {
	public static final String TAG = "InAppBillingPlugin";
	public static final String ACTION_SEND_BILLING = "sendBilling";
	public static final String ACTION_SEND_REFUND = "sendRefund";
	private Context context = null;
	private Activity thisActivity = null;
	private CallbackContext callbackContext = null;
	private JSONArray JSData = null;	// This is the product info from js.
	
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
		api = WXAPIFactory.createWXAPI(context, APP_ID);	// register the app to wechat with APP_ID
		boolean registerResult = api.registerApp(APP_ID);
		Toast.makeText(context, "Register result = " + registerResult, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		JSData = args;		// get the 
		if(ACTION_SEND_BILLING.equals(action)) {
			new GetAccessTokenTask().execute();
			callbackContext.success("Send Pay successfully");
			return true;
		}
		if(ACTION_SEND_REFUND.equals(action)) {
			new SendRefundRequestTask().execute();
			callbackContext.success("Send Refund successfully");
			return true;
		}
		return false;
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
				Toast.makeText(context, "Send refund request successfully!", Toast.LENGTH_LONG).show();
				sendRefundReq(result);
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
			Log.d(TAG, "doInBackground, content = ", content);
			result.parseFrom(content);
			return result;
		}
	}

	private String genRefundEntity() {
		JSONObject jsonObj = new JSONObject();

		try {
			jsonObj.put("sign_type", "MD5");
			jsonObj.put("input_charset", "utf-8");
			jsonObj.put("sign_key_index", 1);

			List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
			packageParams.add(new BasicNameValuePair("partner", JSONData.getJSONObject(0).getString("partner")));
			packageParams.add(new BasicNameValuePair("out_trade_no", JSONData.getJSONObject(0).getString("out_trade_no")));
			packageParams.add(new BasicNameValuePair("transaction_id", JSONData.getJSONObject(0).getString("transaction_id")));
			packageParams.add(new BasicNameValuePair("out_refund_no", JSONData.getJSONObject(0).getString("out_refund_no")));
			packageParams.add(new BasicNameValuePair("total_fee", JSONData.getJSONObject(0).getString("total_fee")));
			packageParams.add(new BasicNameValuePair("refund_fee", JSONData.getJSONObject(0).getString("refund_fee")));
			packageParams.add(new BasicNameValuePair("op_user_id", JSONData.getJSONObject(0).getString("op_user_id")));
			packageParams.add(new BasicNameValuePair("op_user_passwd", JSONData.getJSONObject(0).getString("op_user_passwd")));
			packageParams.add(new BasicNameValuePair("recv_user_id", JSONData.getJSONObject(0).getString("recv_user_id")));
			packageParams.add(new BasicNameValuePair("recv_user_name", JSONData.getJSONObject(0).getString("recv_user_name")));
			packageParams.add(new BasicNameValuePair("use_spbill_no_flag", JSONData.getJSONObject(0).getString("use_spbill_no_flag")));
			packageParams.add(new BasicNameValuePair("refund_type", JSONData.getJSONObject(0).getString("refund_type")));
			packageValue = genPackage(packageParams);

			jsonObj.put("package", packageValue);
		} catch(Exception e) {
			Log.e(TAG, "genRefundEntity fail, e = " + e.getMessage());
			return null;
		}
		return jsonObj.toString();
	}

	private void sendRefundReq(SendRefundRequestResult result) {
		
	}

}