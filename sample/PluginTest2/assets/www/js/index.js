/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        app.receivedEvent('deviceready');
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    }
};


function sendInfo() {
    document.addEventListener('deviceready', function() {
        var sendWechatTextAPI = cordova.require('com.intel.cordova.wechat.wechat');

        sendWechatTextAPI.sendTextAPI(function(success) {
            alert("Success : " + success);
        }, function(error) {
            alert("Error : " + error);
        });
    });
}

function sendPayReq() {
    var jsonData = [];
    var data = {};
    data.body = "千足金箍棒";
    data.partner = "1900000109";
    data.IP = "196.168.1.1"; 
    data.totalFee = 100;    // 1 yuan of RMB
    jsonData.push(data);

    document.addEventListener('deviceready', function() {
        var sendPayReqAPI = cordova.require('com.intel.cordova.inAppBilling.inAppBilling');

        sendPayReqAPI.sendBilling(function(success) {
            // alert("Success : " + success);
        }, function(error) {
            // alert("Error : " + error);
        }, jsonData);
    });
}


function refund() {
    var jsonData = [];
    var data = {};
    data.partner = "1900000109";
    data.out_trade_no = "";
    data.transaction_id = "";
    data.out_refund_no = "";
    data.total_fee = 100;
    data.refund_fee = 100;
    data.op_user_id = "";
    data.op_user_passwd = "";
    data.recv_user_id = "";
    data.recv_user_name = "";
    data.use_spbill_no_flag = "";
    data.refund_type = 1;

    jsonData.push(data);

    document.addEventListener('deviceready', function() {
        var sendRefundAPI = cordova.require('com.intel.cordova.inAppBilling.inAppBilling');

        sendRefundAPI.sendRefund(function(success) {
            // actions here. So far, no need
        }, function(error) {
            // actions here. So far, no need
        }, jsonData);
    });
}


function refundDetail() {
    var jsonData = [];
    var data = {};
    data.partner = "1900000109";
    data.out_trade_no = "";
    data.transaction_id = "";
    data.out_refund_no = "";
    data.refund_id = "";
    data.use_spbill_no_flag = "";

    jsonData.push(data);

    document.addEventListener('deviceready', function() {
        var refundDetailAPI = cordova.require('com.intel.cordova.inAppBilling.inAppBilling');

        refundDetailAPI.refundDetail(function(success) {
            // actions here. So far, no need
        }, function(error) {
            // actions here. So far, no need
        }, jsonData);
    });
}


function statementAccount() {
    var jsonData = [];
    var data = {};
    data.spid = "1900000109";
    data.trans_time = "";
    data.stamp = "";
    data.cft_signtype = "";
    data.mchtype = "";

    jsonData.push(data);

    document.addEventListener('deviceready', function() {
        var statementAccountAPI = cordova.require('com.intel.cordova.inAppBilling.inAppBilling');

        statementAccountAPI.statementAccount(function(success) {
            // actions here. So far, no need
        }, function(error) {
            // actions here. So far, no need
        }, jsonData);
    });
}