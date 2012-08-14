(function ($) {
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
            var $layout = ich['members-layout']().appendTo(context.$element());


            // member requests
            this.load('/admin/user-requests', {contentType:'application/json', dataType:'json'})
                .then(function (response) {
                    var $container = $layout.find('[ui-id=member-requests]');
                    var widget = new CollapsibleLayers({
                        data:response.data,
                        header:{"search_hint": "procurar por nome ou empresa" },
                        $container:$container,
                        template:function (level) {
                            switch (level) {
                                // level 0 = header
                                case 0:
                                    return ich['requests-header'];
                                    break;
                                case 1:
                                    return ich['request-garage'];
                                    break;
                                case 2:
                                    return ich['request-contacts'];
                                    break;
                            }
                        },
                        behaviour:function (data, $container, refresh) {
                            var self = this;
                            // Approve
                            $container.find('.btn').click(function () {
                                var uiid = $(this).attr('ui-data');
                                var record = self.getRowData(uiid);
                                $.ajax({
                                    url:"/admin/approve-account",
                                    data:{email:record.email},
                                    context:document.body
                                }).done(function (response) {
                                        if (!response.status) {
                                            kk.util.alert_error($layout.find('[ui-id=alert-messages]'), response.message)
                                        } else {
                                            var $element = self.getRowElement(uiid);
                                            $element.addClass('alert-success');
                                            $element.fadeOut(1500, function () {
                                                self.remove(uiid);
                                                self.refresh();
                                            });
                                        }
                                    });
                            });
                            // decline
                            $container.find('[ui-id=reject]').click(function () {
                                var uiid = $(this).attr('ui-data');
                                var record = self.getRowData(uiid);
                                $.ajax({
                                    url:"/admin/decline-account",
                                    data:{email:record.email},
                                    context:document.body
                                }).done(function (response) {
                                        if (!response.status) {
                                            kk.util.alert_error($layout.find('[ui-id=alert-messages]'), response.message)
                                        } else {
                                            var $element = self.getRowElement(uiid);
                                            $element.addClass('alert-success');
                                            $element.fadeOut(1500, function () {
                                                self.remove(uiid);
                                                self.refresh();
                                            });
                                        }
                                    });
                            });
                        },
                        onDataLengthChange:function (length) {
                            // update number of records
                            $container.find('[ui-id=header]').find('[ui-id=number-of-requests]').html(length);
                        },
                        template_helpers:function () {
                            return {
                                requested_timeago:function () {
                                    return moment(new Date(this.date)).fromNow();
                                }
                            }
                        }
                    });

                    widget.addSort($container.find('[ui-id=sort-by-name]'), 'name');
                    widget.addSort($container.find('[ui-id=sort-by-company]'), 'company');
                    var $input = $container.find('[ui-id=find]');
                    $input.keyup(function (e) {
                        widget.filter(['company'], $input.val());
                    });
                });


            // active members
            this.load('/admin/users', {contentType:'application/json', dataType:'json'})
                .then(function (response) {
                    var $container = $layout.find('[ui-id=active-members]');

                    var grabTemplate = function(level){
                        switch (level) {
                            // level 0 = header
                            case 0:
                                return ich['members-header'];
                                break;
                            case 1:
                                return ich['members-garage'];
                                break;
                            case 2:
                                return ich['members-user'];
                                break;
                        }
                    };


                    var widget = new CollapsibleLayers({
                        data:response.data,
                        header:{"search_hint": "procurar por nome ou empresa" },
                        $container:$container,
                        template: grabTemplate,
                        template_helpers: function(){
                            return {
                                active_timeago: function(){
                                    return moment(new Date(this.activeDate)).fromNow();
                                }
                            }
                        },
                        onDataLengthChange: function(length){
                            // update number of records
                            var data = this.data()
                                , total =0
                                , children;
                            for (var d = 0, dlen = data.length; d < dlen; d++) {
                                if(data[d]._children){
                                    total += data[d]._children.length;
                                }
                            }
                            $container.find('[ui-id=header]').find('[ui-id=number-of-requests]').html(length + " empresas, "+total + " utilizadores");
                        },
                        onBeforeSort: function($element){
                            if($element.attr('ui-id')!=='sort-by-name') {
                                //override templates, nevertheless
                                this.setTemplate(grabTemplate);
                                return undefined;
                            }
                            var data = this.data();
                            // refactor
                            var result = []
                                , user;
                            for (var data_idx = 0, data_len = data.length; data_idx < data_len; data_idx++) {
                                if (!data[data_idx]._children) continue;
                                for (var children_idx = 0, children_len = data[data_idx]._children.length; children_idx < children_len; children_idx++) {
                                    user = data[data_idx]._children[children_idx];
                                    user.company = data[data_idx].company;
                                    result.push(user);
                                }
                            }
                            // update templates before finish (only one level, so, is ok)
                            this.setTemplate(function(level){
                               if(level==0) return undefined;
                               return ich['members-sorted-by-name']
                            });
                            return result;
                        },
                        onAfterSort: function(){
                            //this.setTemplate(grabTemplate);
                        }
                    });
                    widget.addSort($container.find('[ui-id=sort-by-name]'), 'name');
                    widget.addSort($container.find('[ui-id=sort-by-company]'), 'company');
                    var $input = $container.find('[ui-id=find]');
                    $input.keyup(function (e) {
                        widget.filter(['company', '_children.name'], $input.val());
                    });
                });
        });
    });
    // force templates
    ich.grabTemplates();
    // start the application
    app.run('#/');
})(jQuery);