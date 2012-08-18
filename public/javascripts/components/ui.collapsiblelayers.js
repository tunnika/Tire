(function ($) {
    var CollapsibleLayers = function (properties) {
        var self = this;
        var _default = {
            "data":[],
            "$container":undefined,
            "template": undefined,
            "header": {},
            "skip_header": false,
            "toggle_collapse": "collapse",
            "toggle_expand": "expand",
            "toggle_selector": "i",
            "row_selector": "[ui-id=row]",
            "template_helpers": {},
            "onDataLengthChange": function(){},
            "onBeforeSort": function(){},
            "onAfterSort": function(){},
            // want to enable a style on clicked sort and disable
            // on the others? these are the event's to hook to
            "onSortClickElement": function(){},
            "onSortClickForEachSortNotClicked": function(){},

            onBeforeFilter: function(){},
            onAfterFilter: function(){}
        };
        // widget state
        // ============
        // config
        var config = {};
        $.extend(config, _default, properties);
        // keep list o added sort behaviours (look at addSort)
        var sorts = [];
        // reference to data passed in config
        var data = config.data;
        // keep map between the generated rows and "original" data
        var map = {};
        // is any sort applied
        var sortOn = undefined;
        // var transient data
        var transientData = undefined;


        // private methods
        // ===============

        /**
         * Main method were most "magic" happens.
         * Loops a list of objects and renders each one with the result function of calling template(level).
         * Once it finds a _children property, it will "drill" a level (increase level and loop through the result, recursively),
         * and render the result using a different template returned by template(new_level)
         *
         * @param data     {Array}    data to be traversed
         * @param level    {Number}   level of recursion (0 is header, 1 is root and 2 is first _children of root)
         * @param $parent  {Object}   jQuery enriched DOM node. It is the parent element the object should be rendered to. Starts with
         *                            $container but every time we loop to a _children, parent gets updated with the last row being
         *                            processed
         * @param map      {Object}   To ease creating events in rows and retrieve the reference to original object, an _id is added
         *                            to every row that can be used in template to "mark" the row and in the event by calling map()
         *                            function(). map object holds an _id => object key map
         * @return {*}
         */
        var traverse = function (data, level, $parent, map) {
            var template
                , $row
                , uiid
                , count = arguments[4] || 0;

            template = config.template(level);
            for (var i = 0, ilen = data.length; i < ilen; i++) {
                uiid = level + '-' + count;
                count++;
                data[i]['_id'] = uiid;
                data[i] = $.extend(data[i], config.template_helpers());
                $row = template(data[i]);
                $parent.append($row);
                // persist
                map[uiid] = {};
                map[uiid].data = data[i];
                map[uiid].$dom = $row;
                if (data[i]._children && data[i]._children.length > 0) {
                    $row.find(config.toggle_selector).click(function ($row) {
                        return function () {
                            $row.find(config.toggle_selector).toggleClass(config.toggle_collapse).toggleClass(config.toggle_expand);
                            $row.find(config.row_selector).toggle();
                        }
                    }($row));
                    traverse(data[i]._children, level+1, $row, map, count);
                }
            }
            return $row;
        };
        var triggerOnDataLengthChange = function(data){
            data = data || self.data();
            config.onDataLengthChange.apply(self, [data.length]);
        };

        /**
         * Renders widget
         * @param data
         */
        var draw = function(data, skipHeader) {
            var $parent = config.$container;
            if (config.template(0)) {
                if (!skipHeader) {
                    $parent = traverse([config.header], 0, $parent, {});
                    self.$header = $parent;
                } else {
                    $parent = self.$header;
                }

            }
            traverse(data, 1, $parent, map);
        };

        /**
         * Checks if arguments include a behaviour function
         * @param config
         * @return {*}
         */
        var hasBehaviour = function(config){
            return config && config.behaviour && typeof config.behaviour == 'function';
        };


        // public methods and properties
        // =============================
        this.$container = config.$container;
        /**
         * Allows to retrieve object based on ui-id attribute in dom
         * @param uiid
         * @return {*}
         */
        this.getRowData = function(uiid){
            return map[uiid].data;
        };

        this.getRowElement = function(uiid){
            return map[uiid].$dom;
        };

        /**
         * Retrieve all data
         * @return {*}
         */
        this.data = function(){
            return data;
        };
        /**
         * refresh view
         * @param data
         */
        this.refresh = function(data){
            data = data || self.data();
            config.$container.find('[ui-id=content]').remove();
            // hack - avoid redraw header
            draw(data, true);
            if(hasBehaviour(config)){
                config.behaviour.apply(self, [data, config.$container, true]);
            }
            if (data.length != self.data().length) {
                triggerOnDataLengthChange(data);
            }
        };

        this.remove = function(uuid) {
            var data = self.data();
            for (var i = 0, ilen = data.length; i < ilen; i++) {
                if (data[i]._id === uuid) {
                    data.splice(i,1);
                    break;
                }
            }
            triggerOnDataLengthChange();
        };

        /**
         * Make an DOM element onClick event act as a sort event
         *
         * @param $elem            {Object}  jQuery enriched DOM element to act as sort trigger
         * @param property         {String}  property to be used as the sort (within the objects in data).
         *                                   For instance, "company" would cause the data[i].company to be
         *                                   used as sort
         * @param iconElemSelector {String}  jQuery selector for the element were the icon showing 'asc'
         *                                   or desc state should be placed
         */
        this.addSort = function($elem, property, iconElemSelector, sortFn){
            var sortFunction = sortFn;
            if(!sortFunction || typeof sortFunction !=='function'){
                sortFunction = function (a, b) {
                    if (a[property] < b[property]) {
                        return -1;
                    } else if (a[property] > b[property]) {
                        return 1;
                    }
                    return 0;
                };
            }

            // store element
            if($.inArray($elem, sorts) == -1){
                sorts.push($elem);
            }

            // add actual behaviour
            $elem.click(function () {
                var $this = $(this);
                // save state
                sortOn = $this;

                var data = config.onBeforeSort.apply(self,[$this, sorts]);
                transientData = data;
                if(!data) data = self.data();
                // sort
                data.sort(sortFunction);
                // check if it is asc or desc
                if ($this.attr('ui-data')==='desc') {
                    data.reverse();
                    $this.attr('ui-data', 'asc');
                    $this.find('i').removeClass('icon-arrow-up').addClass('icon-arrow-down');
                } else {
                    $this.attr('ui-data', 'desc');
                    $this.find('i').removeClass('icon-arrow-down').addClass('icon-arrow-up');
                }
                self.refresh(data);
                //trigger onElement event
                config.onSortClickElement.apply(self, [$this]);
                var selector = 'i';
                for (var i = 0, ilen = sorts.length; i < ilen; i++) {
                    if (sorts[i] !== $elem) {
                        if (iconElemSelector && iconElemSelector.length > 0) {
                            selector = iconElemSelector;
                        }
                        sorts[i].find(selector).removeClass('icon-arrow-up').removeClass('icon-arrow-down');
                        //trigger onOtherElements event
                        config.onSortClickForEachSortNotClicked.apply(self,[sorts[i]]);
                    }
                }
                config.onAfterSort.apply(self,[$this, sorts]);
            });
        };

        /**
         * filter data according by value.
         * TODO: escape value!
         * @param properties {Array|String}  array or string with the property name to be used as filter
         * @param value      {String}        the value to be used as the sort value
         * @param data       {Array}         use custom list instead of self.data()
         */
        this.filter = function (properties, value, data) {
            data = data || self.data();
            if(!value || value.length==0){
                self.refresh(data);
                return;
            }
            if (!$.isArray(properties)) {
                properties = [properties];
            }
            // create support metadata for recursion to don't go further then needed
            // meta.max = maximum number of levels
            // meta[level] = [ array, of, values, per, level ]
            var parts
                , meta = {max:1};
            for (var p = 0, plen = properties.length; p < plen; p++) {
                // get level
                parts = properties[p].split('.');
                if (!meta[parts.length]) {
                    meta[parts.length] = [];
                }
                meta[parts.length].push(parts[parts.length - 1]);
                // check if max should be increased
                if (parts.length > meta.max) {
                    meta.max = parts.length;
                }
            }
            // traverse levels and match value
            var traverse = function (data, level, results) {
                if(level > meta.max) return;

                var fn = ''
                    , properties = meta[level]
                    , i
                    , ilen
                    , added = false;

                for (i = 0, ilen = properties.length; i < ilen; i++) {
                    fn+='if(expression.test(data.'+properties[i]+')){return true}';
                }

                var match = new Function(['expression','data'], fn)
                    , regex = new RegExp(value,'i');

                for (i = 0, ilen = data.length; i < ilen; i++) {
                    if (match(regex, data[i])) {
                        results.push(data[i]);
                        added = true;
                    } else {
                        added = false;
                    }
                    if (data[i]._children && data[i]._children.length > 0) {
                        var children = [];
                        traverse(data[i]._children, level+1, children);
                        // check if parent should also be included (if it wasn't yet)
                        // to avoid orphans in view
                        if (children.length > 0) {
                            if (!added) {
                                results.push(data[i]);
                            }
                            results[results.length-1]._children = children;
                        }

                    }
                }
            };
            config.onBeforeFilter.apply(self,[]);
            var results = [];
            traverse(data, 1, results);
            self.refresh(results);
            config.onAfterFilter.apply(self,[]);
        };

        this.setTemplate = function(fn){
            config.template = fn;
        };

        this.$sortOn = function(){
            return sortOn;
        };

        this.getTransientData = function(){
            return transientData;
        };


        // start execution
        // ===============
        draw(data);
        if (hasBehaviour(config)) {
            config.behaviour.apply(this, [data, config.$container]);
        }
        triggerOnDataLengthChange();
    };


    var global = (function(){ return this || (0,eval)('this'); }());

    if (typeof module !== 'undefined' && module.exports) {
        module.exports = CollapsibleLayers;
    } else if (typeof define === 'function' && define.amd) {
        define(function(){return CollapsibleLayers;});
    } else {
        global.CollapsibleLayers = CollapsibleLayers;
    }

})(jQuery);
