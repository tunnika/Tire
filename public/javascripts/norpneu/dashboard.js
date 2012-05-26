(function($){
        var availableBrands = [];
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
                              //if(!$.inArray(tirejson.brand.name,availableBrands)){
                                             availableBrands.push(tirejson.brand.name);
                              //}
                    		  context.render('Tire.ms',tirejson).appendTo(context.$element());
                    		  
                    	  });
                      });
                  }).then(function(){
                	  $('#filterContainer').addClass("span2");
                	  $('#filterContainer').slideLeftShow();
                	  $('#mainResults').removeClass("span12").addClass("span10");
                  }).then(function(){
                          //LOAD FILTERING COMPONENT
                          availableBrands = $.unique(availableBrands);
                          availableBrands.sort();
                          $( "#filter-slider-range" ).slider({
                              range: true,
                              min: 0,
                              max: 500,
                              values: [ 75, 300 ],
                              slide: function( event, ui ) {
                                  $( "#amount" ).val( "$" + ui.values[ 0 ] + " - $" + ui.values[ 1 ] );
                              }
                          });
                          $( "#amount" ).val( "$" + $( "#filter-slider-range" ).slider( "values", 0 ) +
                              " - $" + $( "#filter-slider-range" ).slider( "values", 1 ) );
                          console.log("availableBrands",availableBrands);
                      });
        		  
        	  });
        	});
     // start the application
        app.run('#/');
        $('#mainResults').quicksand($('.result-tire'));
})(jQuery)
