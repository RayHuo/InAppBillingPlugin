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
    // var tableData = document.getElementById("food");
    // var jsonData = [];
    // var size = tableData.rows.length;
    // var i = 0;
    // for(i = 0; i < size; i++) {
    //     var data = {};
    //     data.name = tableData.rows[i].cells[0].innerHTML;
    //     data.value = tableData.rows[i].cells[1].innerHTML;
    //     jsonData.push(data);
    // }

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
