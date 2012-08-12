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

                            var $rendered = ich['tireTemplate'](tirejson);
                            //$rendered.appendTo(context.$element().find('#results-tires'));
                            allTires.push({dataObject:tirejson, renderedObject:$rendered[0]});
                        });
                    });

                    var filterElems = function (arr, filterFunc, appliedSorts) {
                        var res = [];
                        if (arr != undefined) {
                            $.each(arr, function (i, loadedObj) {
                                if (filterFunc != null || filterFunc != undefined) {
                                    if (filterFunc(loadedObj.dataObject)) {
                                        var $rendered = loadedObj;
                                        res.push($rendered);
                                    }
                                } else {
                                    res.push(loadedObj);
                                }
                            });
                        }
                        return res;
                    }
                    var getRenderedElems = function (arr){
                        var res = [];
                        if (arr != undefined) {
                            $.each(arr, function (i, loadedObj) {
                                res.push(loadedObj.renderedObject);
                            });
                        }
                        return res;
                    }
                    /*    }).then(function () {*/
                    console.log("THEN 2");
                    //SHOW FILTER COLUMN
                    $('#filterContainer').addClass("span2");
                    $('#filterContainer').slideLeftShow();
                    $('#results-tires').removeClass("span12").addClass("span10");
//                    }).then(function () {
                    console.log("THEN 3");
                    //LOAD FILTERING COMPONENT

                    $(".filter-slider-range").slider({
                        range:true,
                        min:0,
                        max:500,
                        values:[ 0, 500 ],
                        slide:function (event, ui) {
                            $("#amount").val("" + ui.values[ 0 ] + "€ - " + ui.values[ 1 ]+"€");

                        },
                        change:function(event,ui){
                            $('#filterMarca input[name="price"]').attr("checked","");
                            $('#filterMarca input[name="price"][value="other"]').attr("checked","checked");

                            $('#filterMarca input[name="price"]').trigger('change');
                        }
                    });
                    $("#amount").val("" + $(".filter-slider-range").slider("values", 0) +
                        "€ - " + $(".filter-slider-range").slider("values", 1)+"€");
                    // bind radiobuttons in the form

                    var $filterBrand = $('#filterMarca input[name="brand"]');
                    var $filterPrice = $('#filterMarca input[name="price"]');

                    // get the first collection
                    var $applications = $('#results-tires');

                    //var $data = $applications.clone(true);

                    //FIRST QUICKSAND LOAD
                    var $filteredData = filterElems(allTires, null, null);
                    // attempt to call Quicksand on every form change
                    $applications.quicksand(getRenderedElems($filteredData), {
                        duration:800,
                        easing:'easeInOutQuad'
                    });


                    $filterBrand.add($filterPrice).change(function (e) {
                        var selectedBrand = $('#filterMarca input[name="brand"]:checked').val();
                        var selectedPrice = $('#filterMarca input[name="price"]:checked').val();

                        console.log(selectedBrand);
                        console.log(selectedPrice);
                        var $filteredData = filterElems(allTires, null, null);

                        if(selectedBrand!='all'){
                        $filteredData = filterElems(allTires, function (objToCompare) {
                            if (objToCompare.brand.name == selectedBrand)
                                return true;
                            return false;
                        }, null);
                        }



                        if (selectedPrice != 'all') {
                            var hasMax = false;
                            var minValue = 0;
                            var maxValue = 99999999;
                            if (selectedPrice.indexOf('+') >= 0) {
                                minValue = selectedPrice.split("+")[0];
                            } else if(selectedPrice=='other'){
                                hasMax = true;
                                minValue = $(".filter-slider-range").slider("values", 0);
                                maxValue = $(".filter-slider-range").slider("values", 1);
                                console.log("OTHER",minValue,maxValue);
                            }else{
                                hasMax = true;
                                minValue = selectedPrice.split("-")[0];
                                maxValue = selectedPrice.split("-")[1];
                            }

                            $filteredData = filterElems($filteredData, function (objToCompare) {
                                if (hasMax) {
                                    if (objToCompare.price >= minValue && objToCompare.price <= maxValue)
                                        return true
                                } else {
                                    if (objToCompare.price > minValue)
                                        return true;

                                }
                                return false;

                            }, null);
                        }


                        console.log('allElems', $filteredData);
                        // finally, call quicksand
                        $applications.quicksand(getRenderedElems($filteredData), {
                            duration:800,
                            easing:'easeInOutQuad'
                        });

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
