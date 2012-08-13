var kk = {};

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
