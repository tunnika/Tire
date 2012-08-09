(function ($) {
    var collapsibleLayers = function (properties) {
        var props = {
            "data":[],
            "$container":undefined,
            "template": undefined
        };
        $.extend(props, properties);
        var self = this;

        // create a map between the generated rows and its original data
        var map = {};
        var traverse = function (data, level, $parent, map) {
            var template
                , $row
                , uiid;

            template = props.template(level);
            for (var i = 0, ilen = data.length; i < ilen; i++) {
                uiid = level + '-' + i;
                data[i]['_id'] = uiid;
                map[uiid] = data[i];
                $row = template(data[i]);
                $parent.append($row);
                if (data[i]._children && data[i]._children.length > 0) {
                    $row.find('i').click(function ($row) {
                        return function () {
                            $row.find('i').toggleClass('collapse').toggleClass('expand');
                            $row.find('.line-marker').toggle();
                        }
                    }($row));
                    traverse(data[i]._children, level+1, $row, map);
                }
            }
            return $row;
        };

        var draw = function() {
            var $parent = props.$container;
            if (props.header) {
                if (!props.byPassHeader) {
                    $parent = traverse([props.header], 0, $parent, {});
                    self.$header = $parent;
                } else {
                    $parent = self.$header;
                }

            }
            traverse(props.data, 1, $parent, map);
        };

        var hasBehaviour = function(props){
            return props && props.behaviour && typeof props.behaviour == 'function';
        };

        draw();
        if (hasBehaviour(props)) {
            props.behaviour.apply(this, [props.data, props.$container]);
        }

        // public available methods
        this.$container = props.$container;
        this.map = function(uuid){
            return map[uuid];
        };
        this.data = function(){
            return props.data;
        };
        this.refresh = function(data){
            props.$container.find('[ui-data=content]').empty();
            props.data = data;
            props.byPassHeader = true;
            draw();
            if(hasBehaviour()){
                props.behaviour.apply(self, [props.data, props.$container, true]);
            }
        }
    };
})(jQuery);
