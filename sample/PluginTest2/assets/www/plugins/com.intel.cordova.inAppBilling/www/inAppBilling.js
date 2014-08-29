cordova.define("com.intel.cordova.inAppBilling.inAppBilling", function(require, exports, module) { var cordova = require('cordova');

var InAppBilling = function() {  };

InAppBilling.prototype.sendBilling = function(success, error, jsonData) {
	cordova.exec(success, error, 'InAppBillingPlugin', 'sendBilling', jsonData);
};

InAppBilling.prototype.sendRefund = function(success, error, jsonData) {
	cordova.exec(success, error, 'InAppBillingPlugin', 'sendRefund', jsonData);
};

var inAppBilling = new InAppBilling();
module.exports = inAppBilling;
});
