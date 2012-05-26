/**
 * Created with JetBrains WebStorm.
 * User: CMM
 * Date: 5/19/12
 * Time: 12:24 PM
 * To change this template use File | Settings | File Templates.
 */
jQuery.fn.extend({
    slideRightShow: function() {
        return this.each(function() {
            $(this).show('slide', {direction: 'right'}, 500);
        });
    },
    slideLeftHide: function() {
        return this.each(function() {
            $(this).hide('slide', {direction: 'left'}, 500);
        });
    },
    slideRightHide: function() {
        return this.each(function() {
            $(this).hide('slide', {direction: 'right'}, 500);
        });
    },
    slideLeftShow: function() {
        return this.each(function() {
            $(this).show('slide', {direction: 'left'}, 500);
        });
    }
});

(function($){

}(jQuery));
