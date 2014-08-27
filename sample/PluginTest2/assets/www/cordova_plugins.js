cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "file": "plugins/com.intel.cordova.wechat/www/wechat.js",
        "id": "com.intel.cordova.wechat.wechat",
        "clobbers": [
            "Wechat"
        ]
    },
    {
        "file": "plugins/com.intel.cordova.inAppBilling/www/inAppBilling.js",
        "id": "com.intel.cordova.inAppBilling.inAppBilling",
        "clobbers": [
            "InAppBilling"
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "com.intel.cordova.wechat": "0.0.1",
    "com.intel.cordova.inAppBilling": "0.0.1"
}
// BOTTOM OF METADATA
});