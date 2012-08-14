(function ($) {
    var app = Sammy('#main-app', function () {
        // include a plugin
        //this.use('Mustache', 'ms');
        // define a 'route'
        this.get('#/', function (context) {
            context.app.swap('');
            //TODO: load directly from dashboard.scala.html - that would avoid a round trip to the server to get the "home" template

            ich['dashboardHomeTemplate']().appendTo(context.$element());
            context.log('Sammy started');
            //SHOULD LOAD ALL TIRES ON BG TO A LOCAL BD...
        });
        this.get('#/alltires', function (context) {
            var allTires = [];
            context.app.swap('');
            ich.grabTemplates();
            // TODO: avoid calling server everytime we need to get a template - this should be stored locally once
            ich['dashboardHomeTemplate']().appendTo(context.$element());
            /*context.render('Dashboard-home.ms', {}).then(function (home) {
             context.$element().html(home);*/
            //SHOULD LOAD TIRES FROM LOCAL BD
            this.load('/tires', {contentType:'application/json', dataType:'json'})
                .then(function (items) {

                    console.log("THEN 1");
                    $.each(items, function (i, tires) {
                        $.each(jQuery.parseJSON(tires), function (t, tirejson) {
                            allTires.push(tirejson);
                        });
                    });


                    /*    }).then(function () {*/
                    console.log("THEN 2");
                    //SHOW FILTER COLUMN
                    $('#filterContainer').addClass("span2");
                    $('#filterContainer').slideLeftShow();
                    $('#results-tires').removeClass("span12").addClass("span10");
                    FilterableDashboard({
                        id:"filterTires",
                        "$filterDOMTarget":$('#filterContainer'), //DIV TARGET where the Filters will be placed
                        "$dataDOMTarget":$('#results-tires'), //DIV TARGET where the data detail (subject of filtering) wil be displayed
                        "dataRenderingItemTemplate":ich['tireTemplate'], //Template for each rendered item
                        "filters":[ {
                            filterId:"nomeMarca",
                            filterType:'histogram',
                            uniqueSelection:true, //unique will be rendered as radios, non-unique checkboxes
                            title:"Marca",
                            matchProperty:"brand.name",
                            includeAllOption:true //Will provide a All option as first option (if non-unique, will deselect all others)
                             },
                            {
                                filterId:"price",
                                filterType:'ranges',
                                uniqueSelection:true, //unique will be rendered as radios, non-unique checkboxes
                                title:"Preço",
                                matchProperty:"price",
                                step:20,
                                includeAllOption:true //Will provide a All option as first option (if non-unique, will deselect all others)
                            }],
                        "data":allTires
                        });

//                    });
                });
        });


        this.after(function () {
            var href = '#' + this.app.getLocation().split('#')[1];
            $.each($('#main-menu').find('a'), function () {
                var $elem = $(this);
                if ($elem.attr('href') === href) {
                    $elem.parent().addClass('active');
                } else {
                    $elem.parent().removeClass('active');
                }
            });

        });

    });
    ich.grabTemplates();
    // start the application
    app.run('#/');


})(jQuery);
