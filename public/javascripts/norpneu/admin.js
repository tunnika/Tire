(function ($) {
    var collapsibleWidget = function (properties) {
        var last = undefined
            , header
            , row
            , $parent = undefined
            , $title = undefined;
        var props = {
            "data": [],
            "group": "parent",
            "fnTitleTemplate": undefined,
            "fnHeaderTemplate": undefined,
            "fnChildTemplate": undefined,
            "$container": undefined
        };
        $.extend(props, properties);

        $title = props.fnTitleTemplate(props.data);
        $title = $title.appendTo(props.$container);

        for (var i = 0, ilen = props.data.length; i < ilen; i++) {
            if (last != props.data[i][props.group]) {
                if (props.fnHeaderTemplate && typeof props.fnHeaderTemplate == 'function') {
                    header = props.fnHeaderTemplate(props.data[i]);
                    $parent = header.appendTo($title);

                    $parent.find('i').click(function($parent){return function(){
                        $parent.find('i').toggleClass('collapse').toggleClass('expand');
                        $parent.find('.line-marker').toggle();
                    }}($parent));

                    last = props.data[i][props.group];
                    i-=1;
                }
            } else {
                if (props.fnChildTemplate && typeof props.fnChildTemplate == 'function') {
                    row = props.fnChildTemplate(props.data[i]);
                    $parent.append(row);
                }
            }
        }
    };


    var collapsibleBox = function (properties) {
        var id = 0
            , $box
            , $toggle;
        var props = {
            "header": undefined,
            "body": undefined,
            "$container": undefined
        };
        $.extend(props, properties);
        var global = (function(){ return this || (0,eval)('this'); }());
        if (!global.ui_id) {
            global.ui_id = 0;
        }
        id = global.ui_id++;
        $box = ich['box-widget']({'id':id});
        $box.find('#'+id+'-header').append(props.header);
        $box.find('#'+id+'-container').append(props.body);
        $toggle = $box.find('#'+id+'-toggle');
        $toggle.click(function(){
            $toggle.toggleClass('collapse').toggleClass('expand');
        });
    };

    var app = Sammy('#main-app', function () {
        this.get('#/encomendas', function (context) {
            context.app.swap('');
            console.log('encomendas');
        });
        this.get('#/stock', function (context) {
            context.app.swap('');
            console.log('stock');
        });
        this.get('#/membros', function (context) {
            context.app.swap('');
            this.load('/admin/users', {contentType:'application/json', dataType:'json'})
                .then(function (data) {
                    collapsibleWidget({
                        data: data.users,
                        group: "company",
                        fnTitleTemplate: ich['garage-members-header'],
                        fnHeaderTemplate: ich['garage-row'],
                        fnChildTemplate: ich['garage-user'],
                        $container: context.$element()
                    })
                });
        });
    });
    // start the application
    app.run('#/');
})(jQuery);