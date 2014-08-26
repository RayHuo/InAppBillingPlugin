var cordova = require('cordova');

var Wechat = function() {  };

Wechat.prototype.sendTextAPI = function(onSuccess, onError) {
	cordova.exec(onSuccess, onError, 'WechatPlugin', 'sendTextAPI', []);
};

var wechat = new Wechat();
module.exports = wechat;
