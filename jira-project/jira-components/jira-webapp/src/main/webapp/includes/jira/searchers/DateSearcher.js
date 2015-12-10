(function ($) {
    /**
     * https://jira.atlassian.com/browse/JRA-30741
     *
     * List of class definitions used by the date searcher, which we don't want to
     * 'labelfy'
     *
     * @type {Array}
     * @private
     */
    var _excludeList = ["js-dp-type-toggle", "js-val", "js-measurement", "js-clause",
                        "js-start-date", "js-end-date", "js-start-range", "js-end-range"]

    /**
     * https://jira.atlassian.com/browse/JRA-30741
     *
     * This function 'labelfy' raw html string.
     * The purpose of this is to automatically select the radio element when a labelfied element is clicked.
     *
     * This should only labelfy text element. Any non-text element, such as a dropdown list/text field should
     * be handled through javascript instead of abusing the label tag
     *
     * @param content {String} Raw html string of the field
     * @param radioKey {String} ID string of the radio element that will be selected when a labelfied element is clicked
     * @return {String} Raw html string that has been labelfied
     * @private
     */
    var _labelfyContent = function (content, radioKey) {
        var $content = jQuery("<div></div>").html(content).contents();
        var transformed = [];

        $content.each(function(index, element) {
            var $element = jQuery(element);
            var value = jQuery("<div/>").html(element).html();

            if (!_.contains(_excludeList, $element.attr("class"))) {
                /*
                 This is to ensure white spaces coming from the template are correctly displayed in html
                 */
                value = value.replace(/^\s+|\s+$/g, '&nbsp;');
                transformed.push( "<label for='" + radioKey + "'>" + value + "</label>");
            } else {
                transformed.push(value);
            }
        });

        return transformed.join("");
    };

    /**
     * This function takes the labelfied raw html and attach handlers to non-text element, such that
     * clicking on them will automatically select the radio element.
     *
     * Why don't we just inline the label element and put everything inside it you say?
     * Well, in the ideal world, there should only be one language, everybody should use chrome and the cake is NOT a lie.
     * But unfortunately, interactions between non-text elements inside a label tag behaves different
     * between browsers. In firefox, dropdown list and text field immediately lose their focus when you click on them,
     * and who knows what might happen in IE.
     *
     * See https://jira.atlassian.com/browse/JRA-30741
     *
     * @param data
     * @return {*}
     * @private
     */
    var _generateSearcherAndBindHandler = function(data) {
        var radioKey = data.radioKey + "Radio";
        var outputContent = data.$el.html(_labelfyContent(data.rawTemplate, radioKey));
        outputContent.find(":not(.js-dp-type-toggle):not(select):input").mousedown(function() {
            /*
             WHAT? why are you clicking on the label?
             3 characters: IE8
             Clicking on radio button doesn't work in IE8 because it doesn't unselect other radio buttons

             When using 'input', Firefox also matches the <select> elements, so we need to add a special clause for that
             */
            var $label = outputContent.find("label");
            if ($label.length > 0) {
                $label[0].click();
            }
        });
        return outputContent;
    }

    var NowOverdue = Backbone.View.extend({
        className: "field-group",
        attributes: {"data-date-type" : "nowOverdue"},
        template: JIRA.Templates.DateSearcher.nowOverdue,
        getValues: function () {
            return {to: 0};
        },
        updateFromValues: function (values) {
            if (!values.from && values.to === "0") {
                return true;
            }
        },
        render: function () {
            return this.$el.html(this.template());
        }
    });


    var WithinTheLast = Backbone.View.extend({
        className: "field-group",
        attributes: {"data-date-type" : "withinTheLast"},
        template: JIRA.Templates.DateSearcher.withinTheLast,
        getValues: function () {
            var val = this.$(".js-val").val();
            if (val) {
                return {
                    from: "-" + val + this.$el.find(".js-measurement").val()
                };
            }
        },
        updateFromValues: function (values) {
            if (!values.to && values.from && values.from.charAt(0) == "-") {
                // gets the character that represents the measurement
                var vals = this.model.parseRelativeStr(values.from);
                this.$(".js-val").val(vals.val.slice(1));
                if (vals.measurement) {
                    this.$(".js-measurement").val(vals.measurement);
                }
                return true;
            }
        },
        render: function (verb) {
            return _generateSearcherAndBindHandler({
                $el: this.$el,
                rawTemplate: this.template(),
                radioKey: this.attributes["data-date-type"]
            });
        }
    });


    var MoreThan = Backbone.View.extend({
        className: "field-group",
        attributes: {"data-date-type" : "moreThan"},
        template: JIRA.Templates.DateSearcher.moreThan,
        getValues: function () {
            var val = this.$(".js-val").val();
            if (val) {
                return {
                    to: "-" + val + this.$el.find(".js-measurement").val()
                };
            }
        },
        updateFromValues: function (values) {
            if (!values.from && values.to && values.to.charAt(0) == "-") {
                // gets the character that represents the measurement
                var vals = this.model.parseRelativeStr(values.to);
                this.$(".js-val").val(vals.val.slice(1));
                if (vals.measurement) {
                    this.$(".js-measurement").val(vals.measurement);
                }
                return true;
            }
        },
        render: function (verb) {
            return _generateSearcherAndBindHandler({
                $el: this.$el,
                rawTemplate: this.template(),
                radioKey: this.attributes["data-date-type"]
            });
        }
    });

    var DueMoreThan = MoreThan.extend({
        className: "field-group",
        template: JIRA.Templates.DateSearcher.dueMoreThan
    });

    var DueInNext = Backbone.View.extend({
        className: "field-group",
        attributes: {"data-date-type" : "dueInNext"},
        template: JIRA.Templates.DateSearcher.dueInNext,
        getValues: function () {
            var val = this.$el.find(".js-val").val();
            if (val) {
                return {
                    from: this.$el.find(".js-clause").val() === "andNot" ?  "0" : null,
                    to: val + this.$(".js-measurement").val()
                };
            }
        },
        updateFromValues: function (values) {
            if ((!values.from || values.from === "0") && values.to) {
                // gets the character that represents the measurement
                var vals = this.model.parseRelativeStr(values.to);
                var clause = values.from  === "0" ? "andNot" : "orIs";
                this.$(".js-val").val(vals.val);
                this.$(".js-clause").val(clause);
                if (vals.measurement) {
                    this.$(".js-measurement").val(vals.measurement);
                }
                return true;
            }
        },
        render: function () {
            return _generateSearcherAndBindHandler({
                $el: this.$el,
                rawTemplate: this.template(),
                radioKey: this.attributes["data-date-type"]
            });
        }
    });

    var DatesBetween = Backbone.View.extend({
        className: "field-group",
        attributes: {"data-date-type" : "dateBetween"},
        template: JIRA.Templates.DateSearcher.dateBetween,
        getValues: function () {
            return {
                startDate: this.$el.find(".js-start-date").val(),
                endDate: this.$el.find(".js-end-date").val()
            };
        },
        updateFromValues: function (values) {
            if (values.startDate || values.endDate) {
                this.$el.find(".js-start-date").val(values.startDate);
                this.$el.find(".js-end-date").val(values.endDate);
                return true;
            }
        },
        bindCalendar: function (inputClass, triggerClass) {
            var trigger = this.$el.find("." + triggerClass)[0];
            var input = this.$el.find("." + inputClass)[0];
            var calendarParams = _.extend({}, this.model.calendarConfig, {
                inputField: input,
                button: trigger
            });
            Calendar.setup(calendarParams);
        },
        render: function () {
            _generateSearcherAndBindHandler({
                $el: this.$el,
                rawTemplate: this.template(),
                radioKey: this.attributes["data-date-type"]
            });
            this.bindCalendar("js-end-date", "js-end-date-trigger");
            this.bindCalendar("js-start-date", "js-start-date-trigger");
            return this.$el;
        }
    });

    var InRange = Backbone.View.extend({
        className: "field-group",
        attributes: {"data-date-type" : "inRange"},
        template: JIRA.Templates.DateSearcher.inRange,
        getValues: function () {
            return {
                from: this.$el.find(".js-start-range").val(),
                to: this.$el.find(".js-end-range").val()
            };
        },
        updateFromValues: function (values) {
            if (values.from || values.to) {
                this.$el.find(".js-start-range").val(values.from);
                this.$el.find(".js-end-range").val(values.to);
                return true;
            }
        },
        render: function () {
            return _generateSearcherAndBindHandler({
                $el: this.$el,
                rawTemplate: this.template(),
                radioKey: this.attributes["data-date-type"]
            });
        }
    });


    /**
     * This model is a wrapper around 4 hidden input fields. The values of these fields is what actually gets sent to the
     * server.
     */
    var DateSeacherModel = Backbone.Model.extend({

        /**
         * @constructor
         */
        initialize: function (options) {
            this.from$El = options.$el.find(".js-date-picker-from").hide();
            this.to$El = options.$el.find(".js-date-picker-to").hide();
            this.startDate$El = options.$el.find(".js-date-picker-start-date").hide();
            this.endDate$El = options.$el.find(".js-date-picker-end-date").hide();
            this.errors = JIRA.parseOptionsFromFieldset(options.$el.find('fieldset.js-date-picker-errors'));
            this.calendarConfig = JIRA.parseOptionsFromFieldset(options.$el.find('fieldset.datepicker-params'));
        },

        parseRelativeStr: function (str) {
            var measurement = str.slice(str.length-1);
            var val = str.slice(0, str.length-1);
            return {
                val: val,
                measurement: measurement
            }
        },

        update: function (updateParams) {
            this.clear();
            if (updateParams) {
                if (typeof updateParams.from !== "undefined") {
                    this.from$El.val(updateParams.from)
                }
                if (typeof updateParams.to !== "undefined") {
                    this.to$El.val(updateParams.to)
                }
                if (typeof updateParams.startDate !== "undefined") {
                    this.startDate$El.val(updateParams.startDate)
                }
                if (typeof updateParams.endDate !== "undefined") {
                    this.endDate$El.val(updateParams.endDate)
                }
            }
        },

        toJSON: function () {
            return {
                from: this.from$El.val(),
                to: this.to$El.val(),
                startDate: this.startDate$El.val(),
                endDate: this.endDate$El.val(),
                errors: this.errors
            }
        },

        /**
         * Clears all the hidden input values
         */
        clear: function () {
            this.from$El.val("");
            this.to$El.val("");
            this.startDate$El.val("");
            this.endDate$El.val("");
        }
    });

    /**
     * Renders the date searcher ui.
     */
    JIRA.DateSearcher = Backbone.View.extend({

        /**
         * @param {Object} options
         * ... {jQuery) el - Container element
         * ... {jQuery) dateTypes - Container element
         * @constructor
         */
        initialize: function (options) {
            var instance = this;
            this.verb = options.verb;
            this.model = new DateSeacherModel({$el: this.$el});
            this.setElement($("<div />").addClass("js-picker-ui").appendTo(this.$el));
            this.views = [];

            /*
             * JRADEV-17691: IE10 doesn't implement the input event very well it incorrectly fires when you have placeholder text and you tab to it.
             * So in the case of IE10 we use a keydown and keyup to compare the values to validate input instead.
             */
            var useInputEvent = !jQuery.browser.msie || parseInt(jQuery.browser.version, 10) < 10;

            _.each(options.dateTypes, function (dateType) {
                var view = new dateType({model: instance.model});
                instance.views.push(view);

                if (useInputEvent) {
                    view.$el.delegate(":text", "input", function () {
                        instance.update(view);
                    });
                } else {
                    view.$el.delegate(":text", "keydown", function () {
                        var $this = jQuery(this);
                        var currentVal = $this.val();
                        $this.one("keyup", function () {
                            if ($this.val() !== currentVal) {
                                instance.update(view);
                            }
                        });
                    });
                    // for copy and paste
                    view.$el.delegate(":text", "change", function () {
                        instance.update(view);
                    });
                }

                view.$el.delegate(":radio,select,input", "change", function () {
                    instance.update(view);
                }).delegate(".icon-date", "click", function () {
                    instance.update(view);
                });
            });
        },

        update: function (selectedView, error) {
            this.model.update(selectedView.getValues());
            _.each(this.views, function (view) {
                if (view === selectedView) {
                    view.$(":radio").prop("checked", true);
                } else {
                    view.$(":radio").prop("checked", false);
                }
            });
        },

        render: function () {
            var instance = this;
            var values = this.model.toJSON();
            _.each(this.views, function (view) {
                instance.$el.append(view.render(instance.verb));
            });

            $.each(this.views, function (i, view) {
                if (this.updateFromValues(values)) {
                    instance.update(this);
                    if (values.errors) {
                        $.each(values.errors, function (i, error) {
                            $("<div />").addClass("error").text(error).appendTo(view.$el);
                            return false;
                        });
                    }
                    return false;
                }
            });

            // kickass default focuses first field. We don't want this unless we have nothing selected
            window.setTimeout(function () {
                instance.$el.find(":radio:checked").focus();
            }, 30)
        }
    });

    JIRA.DateSearcher.createDueDateSearcher = function (el) {
        return new JIRA.DateSearcher({
            el: el,
            dateTypes: [NowOverdue, DueMoreThan, DueInNext, DatesBetween, InRange]
        }).render();
    };

    JIRA.DateSearcher.createResolvedDateSearcher = function (el) {
        return new JIRA.DateSearcher({
            el: el,
            dateTypes: [WithinTheLast, MoreThan, DatesBetween, InRange]
        }).render();
    };

    JIRA.DateSearcher.createCreatedDateSearcher = function (el) {
        return new JIRA.DateSearcher({
            el: el,
            dateTypes: [WithinTheLast, MoreThan, DatesBetween, InRange]
        }).render();
    };

    JIRA.DateSearcher.createUpdatedDateSearcher = function (el) {
        return new JIRA.DateSearcher({
            el: el,
            dateTypes: [WithinTheLast, MoreThan, DatesBetween, InRange]
        }).render();
    };

    JIRA.DateSearcher.createCustomDateSearcher = function (el) {
        return new JIRA.DateSearcher({
            el: el,
            dateTypes: [WithinTheLast, MoreThan, DatesBetween, InRange]
        }).render();
    };


})(AJS.$);


