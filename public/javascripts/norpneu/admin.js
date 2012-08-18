(function ($) {
    // helper method to make remote request
    var remoteCall = function (operation, args, callback) {
        $.ajax({
            url:operation,
            data:args,
            contentType:'application/json',
            dataType:'json',
            type:  arguments[3] || "GET"

        }).success(function (response) {
                if(response.hasOwnProperty('status') && !response['status']) {
                    kk.util.alert_error($('#body'), response.message || response);
                } else {
                    callback(response);
                }
            }).error(function (response) {
                kk.util.alert_error($('#body'), response.message || response);
            });
    };

    // update row: visual feedback
    var updateRowVisualFeedback = function ($row, uiid, instance) {
        var deferred = $.Deferred();
        $row.addClass('alert-success');
        $row.fadeOut(800, function () {
            instance.remove(uiid);
            deferred.resolve();
        });
        return deferred;
    };


    var model;

    var MembersRequestForm = function (data, $layout) {
        var $container = $layout.find('[ui-id=member-requests]');
        $container.empty();
        var widget = new CollapsibleLayers({
            data: data,
            header:{"search_hint":"procurar por nome ou empresa", "header":"Pedidos de Adesão" },
            $container:$container,
            template:function (level) {
                switch (level) {
                    // level 0 = header
                    case 0:
                        return ich['members-header'];
                        break;
                    case 1:
                        return ich['request-garage'];
                        break;
                    case 2:
                        return ich['request-contacts'];
                        break;
                }
            },
            template_helpers:function () {
                return {
                    requested_timeago:function () {
                        return moment(new Date(this.date)).fromNow();
                    }
                }
            },
            toggle_selector:'[ui-id=expand-collapse]',
            behaviour:function (data, $container) {
                var self = this;
                // Approve
                $container.find('.btn').click(function () {
                    var uiid = $(this).attr('ui-data');
                    var $row = self.getRowElement(uiid);
                    var record = self.getRowData(uiid);
                    remoteCall('/admin/approve-account', {email:record.email}, function(){
                        // success: give some visual feedback and update members list
                        $.when(updateRowVisualFeedback($row, uiid, self)).then(function () {
                            remoteCall('/admin/users', {}, function (response) {
                                model.setProperty('members', response.data);
                            });
                        });
                    });
                });
                // decline
                $container.find('[ui-id=reject]').click(function () {
                    var uiid = $(this).attr('ui-data');
                    var $row = self.getRowElement(uiid);
                    var record = self.getRowData(uiid);
                    remoteCall('/admin/decline-account', {email:record.email}, function(){
                        // declined... just give visual feedback
                        updateRowVisualFeedback($row, uiid, self);
                    });
                });
            },
            onDataLengthChange:function (length) {
                // update number of records
                $container.find('[ui-id=header]').find('[ui-id=totals]').html(length);
            },
            onSortClickElement:function ($elem) {
                $elem.parent().addClass('active');
            },
            onSortClickForEachSortNotClicked:function ($elem) {
                $elem.parent().removeClass('active');
            }
        });

        widget.addSort($container.find('[ui-id=sort-by-name]'), 'name');
        widget.addSort($container.find('[ui-id=sort-by-company]'), 'company');
        var $input = $container.find('[ui-id=find]');
        $input.keyup(function (e) {
            widget.filter(['company', 'name'], $input.val());
        });

        this.form = widget;
    };

    var MembersForm = function (data, $layout) {
        var $container = $layout.find('[ui-id=active-members]');
        $container.empty();

        var grabTemplate = function (level) {
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
            data: data,
            header:{"search_hint":"procurar por nome ou empresa", "header":"Aderentes" },
            $container:$container,
            template:grabTemplate,
            template_helpers:function () {
                return {
                    active_timeago:function () {
                        if (this.activeDate) {
                            return 'Membro desde ' + moment(this.activeDate).format('MMM, YYYY');
                        } else {
                            return '&nbsp;';
                        }
                    }
                }
            },
            behaviour:function () {
                var self = this;
                $container.find('[ui-id=revoke-member-access]').click(function () {
                    var uiid = $(this).attr('ui-data');
                    var $row = self.getRowElement(uiid);
                    var record = self.getRowData(uiid);
                    remoteCall('/admin/revoke-account', JSON.stringify({email:record.email}), function(){
                        // success: give some visual feedback and update members list
                        $.when(updateRowVisualFeedback($row, uiid, self)).then(function () {
                            remoteCall('/admin/users', {}, function (response) {
                                model.setProperty('members', response.data);
                            });
                        });
                    }, 'POST');
                });


                $container.find('[ui-id=revoke-company-members]').click(function () {
                    var uiid = $(this).attr('ui-data');
                    var $row = self.getRowElement(uiid);
                    var record = self.getRowData(uiid);
                    var emails = [];
                    for (var i = 0, ilen = record._children.length; i < ilen; i++) {
                        emails.push(record._children[i].email);
                    }
                    remoteCall('/admin/revoke-account', JSON.stringify({listOfEmails: emails}), function(){
                        // success: give some visual feedback and update members list
                        $.when(updateRowVisualFeedback($row, uiid, self)).then(function () {
                            remoteCall('/admin/users', {}, function (response) {
                                model.setProperty('members', response.data);
                            });
                        });
                    }, 'POST');
                });


                $container.find('[ui-id=reset-password]').click(function(){
                    var uiid = $(this).attr('ui-data');
                    var $row = self.getRowElement(uiid);
                    var record = self.getRowData(uiid);
                    remoteCall('/admin/reset-password', {email:record.email}, function () {
                        // success: give some visual feedback and update members list
                        $row.addClass('alert-success', 10);
                        setTimeout(function(){
                            $row.removeClass('alert-success', 100);
                        }, 800)
                    });
                });

            },
            toggle_selector:'[ui-id=expand-collapse]',
            onDataLengthChange:function (length) {
                // update number of records
                var data = this.data()
                    , total = 0
                    , children;
                for (var d = 0, dlen = data.length; d < dlen; d++) {
                    if (data[d]._children) {
                        total += data[d]._children.length;
                    }
                }
                $container.find('[ui-id=header]').find('[ui-id=totals]').html(length + " empresas, " + total + " utilizadores");
            },
            onBeforeSort:function ($element) {
                if ($element.attr('ui-id') !== 'sort-by-name') {
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
                this.setTemplate(function (level) {
                    if (level == 0) return undefined;
                    return ich['members-sorted-by-name']
                });
                return result;
            },
            onAfterSort:function () {
                //this.setTemplate(grabTemplate);
            },
            onSortClickElement:function ($elem) {
                $elem.parent().addClass('active');
            },
            onSortClickForEachSortNotClicked:function ($elem) {
                $elem.parent().removeClass('active');
            },
            onAfterFilter:function () {
                $container.find('[ui-id=row]').show();
            }
        });

        widget.addSort($container.find('[ui-id=sort-by-name]'), 'name');
        widget.addSort($container.find('[ui-id=sort-by-company]'), 'company');
        var $input = $container.find('[ui-id=find]');
        $input.keyup(function (e) {
            if (widget.$sortOn() && widget.$sortOn().attr('ui-id') === 'sort-by-name') {
                widget.filter(['name'], $input.val(), widget.getTransientData());
            } else {
                widget.filter(['company', '_children.name'], $input.val(), undefined);
            }
        });

        this.form = widget;

    };

    var router = Sammy('#main-app', function () {
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

            model = kk.model.create({
                members: [],
                requests:[]
            });

            var members
                , membersRequests ;

            // member requests
            //================
            remoteCall('/admin/user-requests', {}, function (response) {
                membersRequests = new MembersRequestForm(response.data, $layout);
                //init model
                model.setProperty('requests', response.data);
                model.addListener('requests', membersRequests.form.refresh);
            });

            // active members
            // ==============
            remoteCall('/admin/users', {}, function (response) {
                members = new MembersForm(response.data, $layout);
                // init model
                model.setProperty('members', response.data);
                model.addListener('members', members.form.refresh);
            });

        });
    });
    // force templates
    ich.grabTemplates();
    // start the application
    router.run('#/');
})(jQuery);