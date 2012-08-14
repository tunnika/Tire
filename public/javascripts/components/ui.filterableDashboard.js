(function ($) {
    var FilterableDashboard = function (properties) {
        var props = {
            "id":undefined,
            "$filterDOMTarget":undefined, //DIV TARGET where the Filters will be placed
            "$dataDOMTarget":undefined, //DIV TARGET where the data detail (subject of filtering) wil be displayed
            "dataRenderingMode":"quicksand", //By default the data will be rendered with quicksand effect, other possible value 'template'
            "dataRenderingTemplate":undefined, //if "dataRenderingMode":"template" - the template
            "dataRenderingItemTemplate":undefined, //Template for each rendered item
            "filters":[], //Filters to apply
            //Filter Object
            //Histogram type (will render unique values)
            //{
            //filterId:undefined,
            // filterType:'histogram',
            // uniqueSelection:true, //unique will be rendered as radios, non-unique checkboxes
            // title:"",
            // matchProperty:"",
            // compareFunction:function(object){return true;},
            // matchProperty:"",
            // values:[], // If values are provided it won't calculate unique values and it's count
            // valueFormatter:undefined, //function(value)
            // valueDataFormatter:undefined, //function(objectFRomData)
            // includeAllOption:true //Will provide a All option as first option (if non-unique, will deselect all others)
            // }
            //Range type (will render elements in ranges)
            //{
            //filterId:undefined,
            // filterType:'ranges',
            // uniqueSelection:true, //unique will be rendered as radios, non-unique checkboxes
            // title:"",
            // compareFunction:function(object){return true;},
            // matchProperty:"",
            // values:[], // If values are provided it won't calculate max and min values
            // step:10, // The step to be applied from each range
            // min:0 //the minimum value
            // max: 100 //the maximum value
            // includeLessThan: undefined //if the defined as value, the first value will be less than
            // includeMoreThan: undefined //if the defined as values, the last value will be more than
            // includeAllOption:true, //Will provide a All option as first option (if non-unique, will deselect all others)
            // includeRefinementOption:false //will provide a refinement widget
            // }

            "data":[],
            "quicksandProps":{
                duration:800,
                easing:'easeInOutQuad'},
            "sortFunction":undefined //Sort function to be applied on data
        };
        var allToken = "c0mputedAll";
        var $renderFieldSet = function (id, title) {
            //CREATE THE ROOT ELEMENT -  FIELDSET
            var $fieldset = $('<fieldset id="' + id + 'fieldset"/>');
            //ADD THE TITLE
            $('<legend>' + title + '</legend>').appendTo($fieldset);
            return $fieldset;
        }

        var $renderInputRadioDom = function (props, $parent, name, value, label, checked) {
            var $inputRadio = $('<input type="radio" name="' + name + 'field" value="' + value + '" ' + (checked == true ? ' checked="checked"' : '') + '>');
            var $inputRadioLabel = $('<label></label>');
            $inputRadio.appendTo($inputRadioLabel);
            $inputRadioLabel.append(label);
            $inputRadio.change(function (evt) {
                $parent.trigger('changeFilter', {props:props, name:name, value:value, label:label});
            });
            return $inputRadioLabel;

        }

        var $renderHistogram = function ($container, globalProperties, histProperties) {
            //CREATE THE ROOT ELEMENT -  FIELDSET and the TITLE
            var $histogramDOM = $renderFieldSet(histProperties.filterId, histProperties.title);

            //IF IT HAS THE ALL OPTION ADD IT
            if (histProperties.includeAllOption) {
                $renderInputRadioDom(histProperties, $histogramDOM, histProperties.filterId, allToken, "Todos", true).appendTo($histogramDOM);
            }
            //IF THE LIST OF VALUES IS ARGUMENT
            if (histProperties.values && histProperties.values && typeof histProperties.values == 'array') {
                $.each(histProperties.values, function (i, value) {
                    var valueFormatted = value;
                    if (histProperties.valueFormatter && typeof histProperties.valueFormatter == 'function')
                        valueFormatted = histProperties.valueFormatter(value);
                    $renderInputRadioDom(histProperties, $histogramDOM, histProperties.filterId, value, valueFormatted, false).appendTo($histogramDOM);
                });
            } else {
                //NEED TO CALCULATE THE UNIQUE VALUES
                var uniqueValues = [];
                var uniqueValuesObjects = [];
                //PROPERTY TO MATCH CAN BE MULTI LEVEL e.g brand.name
                var matchPropertyArr = histProperties.matchProperty.split(".");
                $.each(globalProperties.data, function (i, valueObject) {
                    var currValue = "";
                    //if multilevel match property
                    if (matchPropertyArr.length > 1) {
                        currValue = valueObject;
                        //traverse all the object hierarchy
                        for (var i = 0; i < matchPropertyArr.length; i++) {
                            currValue = currValue[matchPropertyArr[i]];
                        }
                    } else {
                        //return immediate match
                        currValue = String(valueObject[histProperties.matchProperty]);
                    }
                    //store unique values and the whole object if there is a valueDataFormatter
                    if ($.inArray(currValue, uniqueValues) < 0) {
                        uniqueValues.push(currValue);
                        if (histProperties.valueDataFormatter && typeof histProperties.valueDataFormatter == 'function')
                            uniqueValuesObjects.push(valueObject);
                    }
                });
                //For each unique value render it's option
                $.each(uniqueValues.sort(), function (i, value) {
                    var valueFormatted = value;
                    if (histProperties.valueFormatter && typeof histProperties.valueFormatter == 'function')
                        valueFormatted = histProperties.valueFormatter(value);
                    else if (histProperties.valueDataFormatter && typeof histProperties.valueDataFormatter == 'function')
                        valueFormatted = histProperties.valueDataFormatter(uniqueValuesObjects[i]);
                    $renderInputRadioDom(histProperties, $histogramDOM, histProperties.filterId, value, valueFormatted, false).appendTo($histogramDOM);
                });
            }
            return $histogramDOM;
        }

        var $renderDataElement = function (objectToRender, template) {
            var $rendered = template(objectToRender);
            return $rendered[0];
        }

        var filterElems = function (arr, filterFunc, filterValue, appliedSorts) {
            var res = [];
            if (arr !== undefined) {
                $.each(arr, function (i, loadedObj) {
                    if (filterFunc !== null || filterFunc != undefined) {
                        if (filterFunc(loadedObj, filterValue)) {
                            res.push(loadedObj);
                        }
                    } else {
                        res.push(loadedObj);
                    }
                });
            }
            return res;
        }

        var renderElements = function (arr) {
            var res = [];
            if (arr !== undefined) {
                $.each(arr, function (i, loadedObj) {
                    var $rendered = $renderDataElement(loadedObj, props.dataRenderingItemTemplate);
                    res.push($rendered);
                });
            }
            return res;
        }

        $.extend(props, properties);
        var self = this;

        var draw = function () {
            var $parentFilter = props.$filterDOMTarget;
            var $parentData = props.$dataDOMTarget;

            var $rootFilterContent = $('<form id="' + props.id + 'form"/>');
            $rootFilterContent.appendTo($parentFilter);
            $.each(props.filters, function (i, filterProps) {
                if (filterProps.filterType == 'histogram') {
                    var $histogram = $renderHistogram($rootFilterContent, props, filterProps);
                    $histogram.appendTo($rootFilterContent)
                }
            });
            var filteredData = [];
            var currentFilters = {};//filterId => {eventHandledObject}
            var counterCurrentFilters = 0;
            filteredData = renderElements(props.data);
            $parentData.quicksand(filteredData, props.quicksandProps);
            $rootFilterContent.bind('changeFilter', function (evt, handledObject) {
                console.log('bind', 'rootFilterContent', 'changeFilter', handledObject);
                console.log(handledObject.props.filterId);

                if (handledObject.value == allToken) {
                    delete currentFilters[handledObject.props.filterId];
                    counterCurrentFilters--;
                } else {
                    if (!currentFilters.hasOwnProperty(handledObject.props.filterId))
                        counterCurrentFilters++;
                    currentFilters[handledObject.props.filterId] = handledObject;
                }

                //RESET TO ALL ELEMENTS
                //filteredData = filterElems(props.data, null, null, null);
                var currentData = props.data;
                //ITERATE AND APPLY CURRENT FILTERS
                if (counterCurrentFilters > 0) {
                    for (var filterId in currentFilters) {
                        var handledObject = currentFilters[filterId];
                        if (handledObject.props.compareFunction && typeof handledObject.props.compareFunction == "function") {
                            currentData = filterElems(currentData, handledObject.props.compareFunction, handledObject.value, null);
                        } else {
                            currentData = filterElems(currentData, function (objToCompare, value) {
                                var currValue = "";
                                var matchPropertyArr = handledObject.props.matchProperty.split(".");
                                //if multilevel match property
                                if (matchPropertyArr.length > 1) {
                                    currValue = objToCompare;
                                    //traverse all the object hierarchy
                                    for (var i = 0; i < matchPropertyArr.length; i++) {
                                        currValue = currValue[matchPropertyArr[i]];
                                    }
                                } else {
                                    //return immediate match
                                    currValue = String(objToCompare[handledObject.props.matchProperty]);
                                }
                                if (currValue == value)
                                    return true;
                                return false;
                            }, handledObject.value, null);
                        }
                    }
                }
                filteredData = renderElements(currentData);

                $parentData.quicksand(filteredData, props.quicksandProps);
            });
        };


        draw();

        this.refresh = function (data) {
            // props.$container.find('[ui-data=content]').empty();
            // props.data = data;
            // props.byPassHeader = true;
            draw();
        }
    };
    var global = (function () {
        return this || (0, eval)('this');
    }());

    if (typeof module !== 'undefined' && module.exports) {
        module.exports = FilterableDashboard;
    } else if (typeof define === 'function' && define.amd) {
        define(function () {
            return FilterableDashboard;
        });
    } else {
        global.FilterableDashboard = FilterableDashboard;
    }
})(jQuery);
