AJS.$.deactivateLinkedMenu = function () {

};

AJS.$.linkedMenuInstances = [];

AJS.$.fn.linkedMenu = function (opts) {

    var
	idx,
	that = this,
    onDisable,
	enabled = false,

	focusElement = function (elem) {
		elem = AJS.$(elem);
        that.blur();
        elem.trigger("click","focus","mousedown");
	},

	keyHandler = function (e) {
		var targ;
		if (e.keyCode === 37 || e.keyCode === 39 || e.keyCode === 27) {
			if (e.keyCode === 37) {
				targ = idx - 1;
				if (idx - 1 >= 0) {
					if (isNotActive(that[targ])) {
						idx = targ;
						focusElement(that[idx]);
					}
				}
				else {
					targ = that.length - 1;
					if (isNotActive(that[targ])) {
						idx = targ;
						focusElement(that[idx]);
					}
				}
			} else if (e.keyCode === 39) {
				targ = idx + 1;
				if (targ < that.length) {
					if (isNotActive(that[targ])) {
						idx = targ;
						focusElement(that[idx]);
					}
				}
				else {
					targ = 0;
					if (isNotActive(that[targ])) {
						idx = targ;
						focusElement(that[idx]);
					}
				}
			} else {
				that.disableLinkedMenu(e);
			}
			e.preventDefault();
		}
	},

	isNotActive = function (elem) {
		if (elem !== that[idx]) {
			return true;
		}
	},

	focusBridge = function () {
		if (isNotActive(this)) {
			idx = AJS.$.inArray(this, that);
			focusElement(this);
		}
	},

    reflectionBridge = function () {
        var targ = AJS.$.inArray(this, AJS.$(opts.reflectFocus));
        if (isNotActive(that[targ])) {
            idx = targ;
            focusElement(that[idx]);
        }
    },

	enable = function () {
        var elem, clss;
        if (!enabled) {

            AJS.$.currentLinkedMenu = that;

            if (opts.onFocusRemoveClass) {
                elem = AJS.$(opts.onFocusRemoveClass);
                clss = opts.onFocusRemoveClass.match(/\.([a-z]*)$/);
                if (clss && clss[1] && elem.length > 0) {
                    AJS.$(opts.onFocusRemoveClass).removeClass(clss[1]);
                    onDisable = function () {
                        AJS.$(elem).addClass(clss[1]);
                    };
                }
            }
            enabled = true;
            idx = AJS.$.inArray(this, that);

            that.mouseover(focusBridge);

            if (AJS.$.browser.mozilla) {
                AJS.$(document).keypress(keyHandler);
            } else {
                AJS.$(document).keydown(keyHandler);
            }
            AJS.$(document).mousedown(that.disableLinkedMenu);

            if (opts.reflectFocus) {
                AJS.$(opts.reflectFocus).mouseover(reflectionBridge);
            }
        }
	};

	that.disableLinkedMenu = function (e) {

        AJS.$(document).unbind("keypress", keyHandler);
        AJS.$(document).unbind("keydown", keyHandler);
        that.unbind("mouseover", focusBridge);
        AJS.$(document).unbind("mousedown", arguments.callee);
        if (opts.reflectFocus) {
            AJS.$(opts.reflectFocus).unbind("mouseover", reflectionBridge);
        }

        if (onDisable) {
            onDisable();
        }

        that.blur();

        delete AJS.$.currentLinkedMenu;

        window.setTimeout(function (){
            enabled = false;
        }, 200);
	};

    opts = opts || {};

    focusElement = opts.focusElement || focusElement;

	that.click(enable);

    return that;
};