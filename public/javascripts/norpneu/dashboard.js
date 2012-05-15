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
        		 //SHOULD LOAD ALL TIRES ON BG...
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
                  });
        	  });
        	});
     // start the application
        app.run('#/');
})(jQuery)