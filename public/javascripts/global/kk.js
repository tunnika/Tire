var kk = {};


kk.model = {};
kk.model.create = function (properties) {
    var data = $.extend(true, {}, properties);
    var instance = {};
    var listeners = {};
    instance.addListener = function(property, callback){
        if(callback && typeof callback === 'function'){
            listeners[property] = listeners[property] || [];
            listeners[property].push(callback);
        }
    };
    instance.setProperty = function(property, value, notify){
        notify = notify || true;
        data[property] = value;
        if (listeners[property] && notify) {
            for (var i = 0, ilen = listeners[property].length; i < ilen; i++) {
                listeners[property][i].apply(data, [data[property], property, value]);
            }
        }
    };
    instance.getProperty = function (property) {
        return data[property];
    };

    return instance;
};

// util namespace
// ==============
kk.util = {};
kk.util.alert_success = function($parent, message){
    var $alert = ich['global-alert-success']({"message":message}).appendTo($parent);
    setTimeout(function(){
        $alert.fadeOut(2000);
    }, 1000)
};

kk.util.alert_error = function($parent, message){
    var $alert = ich['global-alert-error']({"message":message}).appendTo($parent);
    setTimeout(function(){
        $alert.fadeOut(2000);
    }, 1000)
};
