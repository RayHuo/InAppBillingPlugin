var cordova = require('cordova');

var InAppBilling = function() {  };

InAppBilling.prototype.sendBilling = function(success, error, jsonData) {
	cordova.exec(success, error, 'InAppBillingPlugin', 'sendBilling', jsonData);
};

var inAppBilling = new InAppBilling();
module.exports = inAppBilling;
