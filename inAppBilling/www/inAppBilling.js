var cordova = require('cordova');

var InAppBilling = function() {  };

InAppBilling.prototype.sendBilling = function(success, error) {
	cordova.exec(success, error, 'InAppBillingPlugin', 'sendBilling', []);
};

var inAppBilling = new InAppBilling();
module.exports = inAppBilling;