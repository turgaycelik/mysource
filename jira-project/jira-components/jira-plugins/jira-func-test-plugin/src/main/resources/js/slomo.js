(function($){
    JIRA.Slomo = {};

    var ajax = $.ajax;

    var flash = function($element) {
        $element.stop()
                .css('background-color', "#90EE90")
                .animate({'background-color': "#FFFFFF"}, 1000, function() {
                    $(this).css('background-color', '');
                });
        return this;
    };

    var Slomo = function() {
        this.$el = $("#slomo-ui");
        this.def = new Default(this);
        this.table = new PatternTable(this);

        var that = this;
        this.$el.on('keydown', function(e) {
            if (e.which === jQuery.ui.keyCode.ESCAPE) {
                e.preventDefault();
                that.close();
            }
        });

        this.mouseup = $.proxy(this, 'mouseup');
    };

    Slomo.prototype = $.extend(Slomo.prototype, {
        open: function() {
            var update = this.update();
            update.done(function(data) {
                this.def.setValue(data.defaultDelay || 0);
                this.table.reset(data.patterns);
                this.$el.toggle(true);
                this.table.focus();
                $(document).mouseup(this.mouseup);
            });
            update.fail(function(xhr) {
                alert("Unable to read slomo config: " + xhr.status);
            });
            return this;
        },
        close: function() {
            $(document).unbind('mouseup', this.mouseup);
            this.$el.toggle(false);
            return this;
        },
        update: function() {
            return ajax({
                url:contextPath + "/rest/func-test/latest/slomo",
                type:"GET",
                dataType: "json",
                contentType: "application/json",
                context:this
            });
        },
        mouseup: function(e) {
            if ($(e.target).closest(this.$el).length == 0) {
                this.close();
            }
        }
    });

    var Default = function(slomo) {
        var form = $("#slomo-form"), range = $('#slomo-val'), reset = $('#slomo-reset'), button = form.find(":submit");

        var def = this;
        reset.click(function(){
            range.val(0);
            def.sendDefault();
        });

        form.submit(function (e) {
            e.preventDefault();
            def.sendDefault();
        });

        range.change(function() {
            def.setButton(range.val());
        });

        this.button = button;
        this.range = range;
        this.slomo = slomo;
    };

    Default.prototype = $.extend(Default.prototype, {
        sendDefault: function () {
            ajax({
                url:contextPath + "/rest/func-test/latest/slomo/default",
                type:"POST",
                data: this.range.val(),
                dataType: "json",
                contentType: "application/json"
            });
            this.slomo.close();
            return this;
        },
        setValue: function(val) {
            this.range.val(val);
            this.setButton(val);
            return this;
        },
        setButton: function(speed) {
            this.button.val("Set speed to " + speed + "ms");
            return this;
        }
    });

    var PatternTable = function(slomo) {
        this.slomo = slomo;
        this.$el = $("#slomo-pattern-table");
        this.$form = this.$el.closest("form");
        this.createRow = new PatternCreateRow(this);
        this.activeRow = this.createRow;
        this.rows = [];

        var that = this;
        this.$form.submit(function(e) {
            e.preventDefault();
            that.activeRow.submit();
        });
    };

    PatternTable.prototype = $.extend(PatternTable.prototype, {
        reset: function(data) {
            this.activeRow = this.createRow;
            this.activeRow.clearErrors();
            this.rows.forEach(function(row) {
                row.remove()
            });
            this.rows = [];

            data.forEach(function(pattern) {
                this.add(pattern);
            }, this);

            return this;
        },
        add: function(data) {
            var patternRow = new PatternRow(this, data);
            this.rows.push(patternRow);
            this.$el.find('tbody').append(patternRow.$el);
            return patternRow;
        },
        remove: function(row) {
            for (var i = 0; i < this.rows.length; i++) {
                if (this.rows[i] === row) {
                    this.rows.splice(i, 1);
                    var active = this.createRow;
                    if (i < this.rows.length) {
                        active = this.rows[i];
                    } else if (this.rows.length) {
                        active = this.rows[this.rows.length - 1];
                    }
                    this.activeRow = active.focus();
                    break;
                }
            }
            return this;
        },
        setActive: function(row) {
            this.activeRow = row;
            return this;
        },
        focus: function() {
            (this.activeRow || this.createRow).focus();
            return this;
        }
    });

    var ErrorCell = function($el) {
        this.$el = $el;
        this.$cell = $el.closest('td');
    };

    ErrorCell.prototype = $.extend(ErrorCell.prototype, {
        focus: function() {
            this.$el.focus();
            return this;
        },
        clearError: function() {
            this.setError(null);
            return this;
        },
        setError: function(msg) {
            if (msg) {
                if (!this.$error) {
                    this.$error = $('<div>').addClass('error');
                    this.$cell.append(this.$error);
                }
                this.$cell.addClass('error');
                this.$error.text(msg);
            } else {
                this.$cell.removeClass('error');
                if (this.$error) {
                    this.$error.remove();
                    delete this.$error;
                }
            }
            return this;
        },
        val: function() {
            return this.$el.val.apply(this.$el, arguments);
        },
        checked: function(val) {
            if (val !== undefined) {
                this.$el.prop('checked', !!val);
                return this;
            } else {
                return !!this.$el.prop('checked');
            }
        },
        hasError: function() {
            return !!this.$error;
        },
        flash: function() {
            flash(this.$cell);
        }
    });

    var AbstractPatternRow = function() {
    };

    AbstractPatternRow.prototype = $.extend(AbstractPatternRow.prototype, {
        init: function(table, $el, $pattern, $delay) {
            this.$el = $el;
            this.table = table;
            this.patternCell = new ErrorCell($pattern);
            this.delayCell = new ErrorCell($delay);

            var that = this;
            $el.find('input').focus(function() {
                that.table.setActive(that);
            });
        },
        wrap: function(d) {
            return d.fail(function(xhr) {
                try {
                    var parse = JSON.parse(xhr.responseText);
                    if (parse.patternError || parse.delayError) {
                        return this.renderErrors(parse);
                    } else {
                        alert("An unexpected error has occured: " + xhr.status);
                    }
                }
                catch (e){
                    alert("An unexpected error has occured: " + xhr.status);
                }
            });
        },
        renderErrors: function(data) {
            this.setPatternError(data.patternError);
            this.setDelayError(data.delayError);

            return this;
        },
        validate: function() {
            var delay = this.delay();
            var ret = true;
            if (isNaN(delay) || delay < 0) {
                this.setDelayError("Delay must be an Integer >= 0");
                ret = false;
            } else {
                this.setDelayError('');
            }
            var pattern = this.pattern();
            if (!pattern) {
                this.setPatternError("Pattern must not be blank.");
                ret = false;
            } else {
                this.setPatternError('');
            }
            return ret;
        },
        update: function(data) {
            this.setPattern(data.pattern);
            this.setDelay(data.delay);

            return this;
        },
        setPattern: function(value) {
            this.patternCell.val(value);
            return this;
        },
        setDelay: function(value) {
            this.delayCell.val(value);
            return this;
        },
        pattern: function() {
            return this.patternCell.val();
        },
        delay: function() {
            return parseInt(this.delayCell.val(), 10);
        },
        setDelayError: function(msg) {
            this.delayCell.setError(msg);
            return this;
        },
        setPatternError: function(msg) {
            this.patternCell.setError(msg);
            return this;
        },
        clearErrors: function() {
            this.patternCell.clearError();
            this.delayCell.clearError();

            return this;
        },
        focus: function() {
            var field = this.patternCell;
            if (!this.patternCell.hasError()) {
                if (this.delayCell.hasError()) {
                    field = this.delayCell;
                }
            }
            field.focus();

            return this;
        }
    });

    var PatternCreateRow = function(table) {
        var $pattern = $('#slomo-pattern');
        this.init(table, $pattern.closest('tr'), $pattern, $('#slomo-pattern-delay'));
    };

    PatternCreateRow.prototype = $.extend(new AbstractPatternRow(), {
        clear: function() {
            this.setPattern('');
            this.setDelay('');
            this.clearErrors();

            return this;
        },
        add: function() {
            if (!this.validate()) {
                return this.focus();
            }

            var delayed = this.wrap(ajax({
                url:contextPath + "/rest/func-test/latest/slomo/pattern",
                type:"PUT",
                dataType: "json",
                contentType: "application/json",
                context: this,
                data: JSON.stringify(this.data())
            }));

            delayed.done(function(data) {
                this.clear();
                this.table.add(data);
            });
            delayed.always(function(){
                this.focus();
            });

            return this;
        },
        data: function() {
            return {pattern: this.pattern(), delay: this.delay()};
        },
        submit: function() {
            this.add();
            return this;
        }
    });

    PatternCreateRow.prototype.constructor = PatternCreateRow;

    var PatternRow = function(table, data) {
        var $pattern = $("<input size='70' name='pattern'>");
        var $delay = $("<input size='30' name='delay'>");
        var $row = $("<tr>");

        var $enabled = $("<input type='checkbox' name='enabled'>");
        this.$remove = $("<input class='aui-button' type='submit' name='remove'>").val("Remove");

        $("<td>").append($enabled).appendTo($row);
        $("<td>").append($pattern).appendTo($row);
        $("<td>").append($delay).appendTo($row);
        $("<td>").append(this.$remove).appendTo($row);
        this.init(table, $row, $pattern, $delay);

        this.id = data.id;
        this.enabledCell = new ErrorCell($enabled);
        this.current = data;
        this.setPattern(data.pattern);
        this.setDelay(data.delay);
        this.setEnabled(data.enabled);

        var that = this;
        this.$remove.click(function(e) {
            e.preventDefault();
            that.destroy();
        });

        $enabled.click(function() {
            that.table.setActive(this);
            that.save();
        });

        this.$el.find('input').blur(function() {
            that.save();
        });
    };

    PatternRow.prototype = $.extend(new AbstractPatternRow(), {
        remove: function() {
            this.$el.remove();
            return this;
        },
        data: function() {
            var data = {id: this.id};
            var changed = false;
            if (this.current.pattern !== this.pattern()) {
                data.pattern = this.pattern();
                changed = true;
            }
            if (this.current.delay !== this.delay()) {
                data.delay = this.delay();
                changed = true;
            }
            if (this.current.enabled !== this.enabled()) {
                data.enabled = this.enabled();
                changed = true;
            }

            if (changed) {
                return data;
            } else {
                return null;
            }
        },
        save: function() {
            this.table.setActive(this);

            if (!this.validate()) {
                return this;
            }

            var data = this.data();
            if (!data) {
                this.clearErrors();
                return this;
            }

            var delayed =  this.wrap(ajax({
                url:contextPath + "/rest/func-test/latest/slomo/pattern/" + this.id,
                type:"POST",
                dataType: "json",
                contentType: "application/json",
                context: this,
                data: JSON.stringify(data)
            }));

            delayed.done(function(data) {
                this.update(data);
            });
            return this;
        },
        destroy: function() {
            var delayed = this.wrap(ajax({
                url:contextPath + "/rest/func-test/latest/slomo/pattern/" + this.id,
                type:"DELETE",
                dataType: "json",
                contentType: "application/json",
                context: this
            }));

            delayed.done(function() {
                this.table.remove(this);
                this.remove();
            });
            return this;
        },
        submit: function() {
            this.save();
            return this;
        },
        enabled: function() {
            return this.enabledCell.checked();
        },
        setEnabled: function(enabled) {
            this.enabledCell.checked(enabled);
            return this;
        },
        update: function(data) {
            var old = this.current;

            this.setPatternError();
            if (old.pattern !== data.pattern) {
                this.setPattern(data.pattern);
                this.patternCell.flash();
            }

            this.setDelayError();
            if (old.delay !== data.delay) {
                this.setDelay(data.delay);
                this.delayCell.flash();
            }

            if (old.enabled !== data.enabled) {
                this.setEnabled(data.enabled);
                this.enabledCell.flash();
            }
            this.current = data;
            return this;
        }
    });

    PatternRow.prototype.constructor = PatternRow;

    JIRA.Slomo.activate = function() {
        $(document.body).prepend(JIRA.Templates.Slomo.slomoUi());
        var def = new Slomo();
        def.open();
        JIRA.Slomo.activate = function() { // from now on just toggle
            def.open();
        };
    };
})(AJS.$);
