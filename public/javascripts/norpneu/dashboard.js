(function($){
        //make sure search for does not get submitted with an empty string
        $('#submit-search').click(function(){
            if($('[name="searchQuery"]').val().length==0)
                return false;
        });
        
        var app = Sammy('#mainResults', function() {
        	  // include a plugin
        	this.use('Mustache', 'ms');
        	  // define a 'route'
        	 this.get('#/', function(context) {
        		 context.app.swap('');
        		 context.log('Sammy started');
        		 //SHOULD LOAD ALL TIRES ON BG TO A LOCAL BD...
        	 });
        	  this.get('#/alltires', function(context) {
        		  context.app.swap('');
        		  //SHOULD LOAD TIRES FROM LOCAL BD
        		  this.load('/tires',{contentType: 'application/json',dataType: 'json'})
                  .then(function(items) {
                      $.each(items, function(i, tires) {
                    	  $.each(jQuery.parseJSON(tires), function(t, tirejson) {
                    		  context.log(tirejson);
                    		  context.render('Tire.ms',tirejson).appendTo(context.$element());
                    		  
                    	  });
                      });
                  }).then(function(){
                	  console.log("AFTER RENDER");
                	  $('#filterContainer').addClass("span2");
                	  $('#filterContainer').slideLeftShow();
                	  $('#mainResults').removeClass("span12").addClass("span10");
                  });
        		  
        	  });
        	});
     // start the application
        app.run('#/');
        $('#mainResults').masonry({
			  itemSelector: '.result-tire',
			  columnWidth: 150
			});
})(jQuery)

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