var cordova = require('cordova');

var InAppBilling = function() {  };

InAppBilling.prototype.sendBilling = function(success, error, jsonData) {
	cordova.exec(success, error, 'InAppBillingPlugin', 'sendBilling', jsonData);
};

InAppBilling.prototype.sendRefund = function(success, error, jsonData) {
	cordova.exec(success, error, 'InAppBillingPlugin', 'sendRefund', jsonData);
};

InAppBilling.prototype.refundDetail = function(success, error, jsonData) {
	cordova.exec(success, error, 'InAppBillingPlugin', 'refundDetail', jsonData);
};

InAppBilling.prototype.statementAccount = function(success, error, jsonData) {
	cordova.exec(success, error, 'InAppBillingPlugin', 'statementAccount', jsonData);
};

var inAppBilling = new InAppBilling();
module.exports = inAppBilling;
