jQuery(function () {

    function initSparklers($ctx) {
        $ctx.find(".js-default-checkboxmultiselect").each(function () {
            var $select = AJS.$(this);
            if (!$select.data("checkboxmultiselect")) {
                $select.data("checkboxmultiselect",  new AJS.CheckboxMultiSelect({
                    element: this
                }));
            } else {
                $select.data("checkboxmultiselect").render();
            }
        });
    }

    function initStatusLozengeSparklers($ctx) {
        $ctx.find(".js-default-checkboxmultiselectstatuslozenge").each(function () {
            var $select = AJS.$(this);
            if (!$select.data("checkboxmultiselectstatuslozenge")) {
                $select.data("checkboxmultiselectstatuslozenge",  new AJS.CheckboxMultiSelectStatusLozenge({
                    element: this
                }));
            } else {
                $select.data("checkboxmultiselectstatuslozenge").render();
            }
        });
    }

    function initLabelSparkler($ctx) {
        $ctx.find(".js-label-checkboxmultiselect").each(function () {
            var cms = new AJS.CheckboxMultiSelect({
                element: this,
                ajaxOptions: {
                    url: contextPath + "/rest/api/1.0/labels/suggest",
                    query: true,
                    minQueryLength: 0,
                    formatResponse: function (response) {
                        var selectedValues = cms.model.getSelectedValues();
                        return [new AJS.GroupDescriptor({
                            items: _.map(_.sortBy(_.reject(response.suggestions, function (suggestion) {
                                return _.contains(selectedValues, suggestion.label);
                            }), "label"), function (suggestion) {
                                return new AJS.ItemDescriptor({
                                    highlighted: true,
                                    html: suggestion.html,
                                    label: suggestion.label,
                                    value: suggestion.label,
                                    title: suggestion.label
                                });
                            })
                        })];
                    }
                }
            });
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, $context, reason) {
        if (reason === JIRA.CONTENT_ADDED_REASON.criteriaPanelRefreshed) {
            initSparklers($context);
            initStatusLozengeSparklers($context);
            initLabelSparkler($context);
        }
    });

});
