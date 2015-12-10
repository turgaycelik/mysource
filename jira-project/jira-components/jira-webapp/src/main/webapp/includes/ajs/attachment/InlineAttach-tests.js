AJS.test.require("jira.webresources:jira-global");
AJS.test.require("jira.webresources:dialogs");

(function($) {

    var contains = function(el) {
        //$.contains seems less the reliable so we use this.
        var elements = $(el).find('*').toArray();

        for (i = 1; i < arguments.length; i++) {
            var expected = arguments[i];
            if (expected && expected.get) {
                expected = expected.get(0) || expected;
            }
            if (!$.inArray(expected, elements) < 0) {
                return false;
            }
        }
        return true;
    };

    var MockFile = function(name, size) {
        this.name = name;
        this.size = size;

        this.toSimple = function() {
            return {name: name, size: size, file: this};
        }
    };

    var mockTr = function() {
        if (arguments.length == 0) {
            return undefined;
        } else if (arguments.length == 1) {
            return arguments[0];
        } else {
            return Array.prototype.slice.call(arguments).join(",");
        }
    };

    var MockText = {
        tr: function() {
            if (arguments.length == 0) {
                return undefined;
            } else if (arguments.length == 1) {
                return arguments[0];
            } else {
                var args = Array.prototype.slice.call(arguments);
                for (var i = 0; i < args.length; i++) {
                    if (typeof args[i] === "number") {
                        args[i] = args[i].toFixed(4);
                    }
                }
                return args.join(",");
            }
        },
        currentOutOfTotalSize: function(current, total) {
            return current + "/" + total;
        },
        time: function(time) {
            return time;
        },
        rate: function(rate) {
            return rate;
        },
        fileSize: function() {
            if (arguments.length == 0) {
                return undefined;
            } else if (arguments.length == 1) {
                return arguments[0];
            } else {
                return Array.prototype.slice.call(arguments);
            }
        }
    };

    var MockStaticProgress = function() {
        this.hidden = true;
        this.started = false;
        this.removed = false;

        this.onCancel = function(fn) {
            this.cancel = fn;
            return this;
        };
        
        this.triggerCancel = function() {
            this.cancel.call(this);
            return this;
        };

        this.toString = function() {
            return "MockStaticProgress[" + JSON.stringify(this) + "]";
        };
        this.show = function() {
            this.hidden = false;
            return this;
        };
        this.start = function() {
            this.started = true;
            return this;
        };
        this.remove = function() {
            this.removed = true;
            return this;
        }
    };

    var MockProgress = function(file) {
        this.file = file;
        this.hidden = true;
        this.value = Infinity;
        this.started = false;
        this.finished = false;
        this.removed = false;

        this.hide = function() {
            this.hidden = true;
            return this;
        };
        this.show = function() {
            this.hidden = false;
            return this;
        };
        this.start = function() {
            this.started = true;
            return this;
        };
        this.update = function(value) {
            this.value = value;
            return this;
        };
        this.onCancel = function(fn) {
            this.cancel = fn;
            return this;
        };
        this.triggerCancel = function() {
            this.cancel.call(this);
            return this;
        };
        this.finish = function() {
            this.finished = true;
            return this;
        };
        this.remove = function() {
            this.removed = true;
            return this;
        };
        this.toString = function() {
            return "MockProgress[" + JSON.stringify(this) + "]";
        }
    };

    var MockFileInput = function(element, multiple) {
        this.$element = this.element = element;
        this.multiple = multiple;
        this.onChange = function(change) {
            this.change = change;
        };
        this.focus = recordfn(this);
        this.clear = recordfn(this);
        this.cloneInput = recordfn(function() {
            return {count: this.cloneInput.count, cloneInput: true};
        });
    };

    var MockForm = function(input) {
        this.nextToken = 33737;
        this.formToken = "form-token"
        this.errors = [];
        this.fileNames = [];
        this.fileSelector = input;
        this.maxSize = 1000;
        this.disabled = false;
        this.progress = [];
        this.lastReplace = null;
        this.lastInput = null;

        this.onCancel = function(cancel) {
            this.cancel = cancel;
            return this;
        };
        this.addError = function(error, replace) {
            this.errors.push(error);
            this.lastReplace = replace;
            return this;
        };
        this.addErrorWithFileName = function(error, fileName, replaceObj) {
            this.errors.push(error + '|' + fileName);
            this.lastReplace = replaceObj;
        };
        this.clearErrors = recordfn(function() {
            this.errors = [];
            return this;
        });
        this.getAtlToken = function() {
            return this.nextToken++;
        };
        this.setAtlToken = function(token) {
            this.lastToken = token;
            return this;
        };
        this.getFormToken = function() {
            return this.formToken;
        };
        this.disable = function() {
            this.disabled = true;
            return this;
        };
        this.enable = function() {
            this.disabled = false;
            return this;
        },
        this.addProgress = function(file) {
            var prog = new MockProgress(file);
            this.progress.push(prog);
            return prog;
        };
        this.addStaticProgress = function(file) {
            var prog = new MockStaticProgress(file);
            this.progress.push(prog);
            return prog;
        };
        this.addTemporaryFileCheckbox = recordfn(this);
        this.cloneFileInput = function() {
            return this.fileSelector.cloneInput();
        };
    };

    var failfn = function(name) {
        return function() {
            ok(false, "Should not be calling " + name + ".");
        }
    };

    var recordfn = function(obj) {
        var func = function() {
            var me = arguments.callee;
            me.lastScope = this;
            me.lastArgs = Array.prototype.slice.call(arguments);
            me.count++;

            if (obj && obj.apply) {
                return obj.apply(this, arguments);
            } else {
                return obj;
            }
        };
        func.count = 0;
        return func
    };

    var KB = 1024;
    var MB = 1024 * KB;
    var GB = 1024 * MB;

    module("Text", {
        setup: function() {
            this.tr =  AJS.InlineAttach.Text.tr;
        },
        teardown: function() {
            AJS.InlineAttach.Text.tr = this.tr;
        }
    });
    test("Test Translate No Key", function() {
        equal(AJS.InlineAttach.Text.tr(), undefined, "Return undefined on no key.");
    });

    test("Test Translate No Args", function() {
        equal(AJS.InlineAttach.Text.tr("upload.cancel"), "common.words.cancel", "Return the translation.");
        equal(AJS.InlineAttach.Text.tr("doesnotexist.12627"), "doesnotexist.12627", "Return the key.");
    });

    test("Test Translate Args", function() {
        this.stub(AJS, "format");
        AJS.InlineAttach.Text.tr("upload.bytes.per.second", 5);

        sinon.assert.calledWith(AJS.format, 'upload.bytes.per.second', 5);
        equal(AJS.InlineAttach.Text.tr("doesnotexist.12627", 5), "doesnotexist.12627", "Return the key.");
    });

    test("Test for fileSize No args", function() {
        equal(AJS.InlineAttach.Text.fileSize(), undefined, "Return undefined on no arguments.");
    });

    test("Test for fileSize Single Arg", function() {
        AJS.InlineAttach.Text.tr = mockTr;
        var test = AJS.$.proxy(AJS.InlineAttach.Text.fileSize, AJS.InlineAttach.Text);
        equal(test(0), mockTr("upload.kilobyte", 0), "0 should return as kB with no decimal places.");
        equal(test(512), mockTr("upload.kilobyte", "0.50"),"Return a 1/2 kB");
        equal(test(1024), mockTr("upload.kilobyte", "1.00"),"Return a 1 kB");
        equal(test(10 * KB + KB / 4), mockTr("upload.kilobyte", "10.25"),"Return a 10.25 kB");
        equal(test(Math.floor(MB * 0.995)), mockTr("upload.kilobyte", "1018.88"),"Return a 1018.88 kB");
        equal(test(Math.ceil(MB * 0.995)), mockTr("upload.megabyte", "1.00"),"Return a 1 MB because of rounding.");
        equal(test(MB*2.5), mockTr("upload.megabyte", "2.50"),"Return a 2.5 MB.");
        equal(test(Math.floor(GB * 0.995)), mockTr("upload.megabyte", "1018.88"),"Return a 1018.88 MB.");
        equal(test(Math.ceil(GB * 0.995)), mockTr("upload.gigabyte", "1.00"),"Return a 1.0 GB.");
        equal(test(GB*3.5), mockTr("upload.gigabyte", "3.50"),"Return a 3.5 GB.");
        equal(test(GB*1024), mockTr("upload.gigabyte", "1024.00"),"Return a 1024 GB.");
    });

    test("Test for fileSize Multiple Arg", function() {
        AJS.InlineAttach.Text.tr = mockTr;
        var test = AJS.$.proxy(AJS.InlineAttach.Text.fileSize, AJS.InlineAttach.Text);
        deepEqual(test(0, MB), [mockTr("upload.kilobyte", "0"),
            mockTr("upload.kilobyte", "1024.00")], "[0, 1024kb] should be returned.");
        deepEqual(test(512, 2*MB),
                [mockTr("upload.kilobyte", "0.50"), mockTr("upload.kilobyte", "2048.00")],
                "[0.50kB, 2048.00kB]");
        deepEqual(test(1024, 2.5*MB),
                [mockTr("upload.kilobyte", "1.00"), mockTr("upload.kilobyte", "2560.00")],
                "[1kB, 2560kB]");
        deepEqual(test(10 * KB + KB / 4, 10 * KB, 5 * KB),
                [mockTr("upload.kilobyte", "10.25"),mockTr("upload.kilobyte", "10.00"),mockTr("upload.kilobyte", "5.00")],
                "[10.25kB, 10.00kB, 5.00kB]");
        deepEqual(test(Math.floor(MB * 0.995), 670 * KB, 56 * MB),
                [mockTr("upload.kilobyte", "1018.88"), mockTr("upload.kilobyte", "670.00"), mockTr("upload.kilobyte", "57344.00")],
                "[1018.88kB, 670kB, 57344kB]");
        deepEqual(test(Math.ceil(MB * 0.995), 512 * KB, 1),
                [mockTr("upload.megabyte", "1.00"), mockTr("upload.megabyte", "0.50"), mockTr("upload.megabyte", "0")],
                "[1.00 MB, 0.50 MB, 0]");
        deepEqual(test(MB*2.5, GB, 10 * KB),
                [mockTr("upload.megabyte", "2.50"), mockTr("upload.megabyte", "1024.00"), mockTr("upload.megabyte", "0.01")],
                "[2.50MB, 1024.00MB, 0.01MB]");
        deepEqual(test(Math.floor(GB * 0.995), GB, 0),
                [mockTr("upload.megabyte", "1018.88"), mockTr("upload.megabyte", "1024.00"),mockTr("upload.megabyte", "0")],
                "[1018.88 MB, 1024MB, 0]");
        deepEqual(test(Math.ceil(GB * 0.995), 0.5 * GB, 0.25 * GB),
                [mockTr("upload.gigabyte", "1.00"), mockTr("upload.gigabyte", "0.50"), mockTr("upload.gigabyte", "0.25")],
                "[1GB, 0.5GB, 0.25GB]");
        deepEqual(test(GB*3.5, 5 * MB, 6 * MB),
                [mockTr("upload.gigabyte", "3.50"), mockTr("upload.gigabyte", "0"), mockTr("upload.gigabyte", "0.01")],
                "[3.5GB, 0GB, 0.01GB]");
        deepEqual(test(GB*1024, 50 * MB, 57578 * GB, 57578 * MB),
                [mockTr("upload.gigabyte", "1024.00"), mockTr("upload.gigabyte", "0.05"), mockTr("upload.gigabyte", "57578.00"), mockTr("upload.gigabyte", "56.23")],
                "[1024GB, 0.05GB, 57578.00GB, 56.23GB]");
    });

    test("Test for currentOutOfTotalSize", function() {
        AJS.InlineAttach.Text.tr = mockTr;
        var test = AJS.$.proxy(AJS.InlineAttach.Text.currentOutOfTotalSize, AJS.InlineAttach.Text);
        equal(test(0, 0), mockTr("upload.kilobyte.part", "0", "0"), "0/0 kb.");
        equal(test(5, 0.5 * MB), mockTr("upload.kilobyte.part", "0", "512.00"), "0/512.00 kb.");
        equal(test(6, 0.5 * MB), mockTr("upload.kilobyte.part", "0.01", "512.00"), "0.01/512.00 kb.");
        equal(test(KB, Math.floor(0.995 * MB)), mockTr("upload.kilobyte.part", "1.00", "1018.88"), "0.01/1018.88 kb.");
        equal(test(5*KB, Math.ceil(0.995 * MB)), "upload.megabyte.part,0,1.00", "0/1.00 MB.");
        equal(test(6*KB, Math.ceil(0.995 * MB)), mockTr("upload.megabyte.part", "0.01", "1.00"), "0.01/1.00 MB.");
        equal(test(0.5 * MB, Math.floor(0.995 * GB)), mockTr("upload.megabyte.part", "0.50", "1018.88"), "0.5/1018.88 MB.");
        equal(test(5 * MB, Math.ceil(0.995 * GB)), "upload.gigabyte.part,0,1.00", "0/1GB.");
        equal(test(6 * MB, Math.ceil(0.995 * GB)), mockTr("upload.gigabyte.part", "0.01", "1.00"), "0.01/1GB.");
        equal(test(8.5*GB, 9*GB), mockTr("upload.gigabyte.part", "8.50", "9.00"), "8.5/9GB.");
        equal(test(8.5*GB, 1055*GB), mockTr("upload.gigabyte.part", "8.50", "1055.00"), "8.5/1055GB.");
    });

    test("Test for Text.rate", function() {
        AJS.InlineAttach.Text.tr = mockTr;
        var test = AJS.$.proxy(AJS.InlineAttach.Text.rate, AJS.InlineAttach.Text);
        equal(test(0), mockTr("upload.bytes.per.second", "0"), "0 B/sec");
        equal(test(512), mockTr("upload.bytes.per.second", "512.00"), "512 B/sec");
        equal(test(Math.floor(0.995 * KB)), mockTr("upload.bytes.per.second", "1018.00"), "1018.00 B/sec");
        equal(test(Math.ceil(0.995 * KB)), mockTr("upload.kilobytes.per.second", "1.00"), "1.00 kB/sec");
        equal(test(3.75 * KB), mockTr("upload.kilobytes.per.second", "3.75"), "3.75 kB/sec");
        equal(test(Math.floor(0.995 * MB)), mockTr("upload.kilobytes.per.second", "1018.88"), "1018.88 kB/sec");
        equal(test(Math.ceil(0.995 * MB)), mockTr("upload.megabytes.per.second", "1.00"), "1.00 MB/sec");
        equal(test(5.25 * MB), mockTr("upload.megabytes.per.second", "5.25"), "5.25 MB/sec");
        equal(test(1025 * MB), mockTr("upload.megabytes.per.second", "1025.00"), "1025.00 MB/sec");
    });

    test("Test for Text.time", function() {
        AJS.InlineAttach.Text.tr = mockTr;
        var test = AJS.$.proxy(AJS.InlineAttach.Text.time, AJS.InlineAttach.Text);

        equal(test(0), mockTr("upload.seconds", "0"), "0 sec");
        equal(test(45), mockTr("upload.seconds", "45"), "45 sec");
        equal(test(59), mockTr("upload.seconds", "59"), "59 sec");
        equal(test(60), mockTr("upload.minutes", "1"), "1 min");
        equal(test(60*30), mockTr("upload.minutes", "30"), "30 min");
        equal(test(60*59), mockTr("upload.minutes", "59"), "59 min");
        equal(test(60*60), mockTr("upload.hours", "1"), "1hr");
        equal(test(60*61), mockTr("upload.hours.minutes", "1", "1"), "1hr 1 min");
        equal(test(60*60.25), mockTr("upload.hours", "1"), "1.0004hr");
        equal(test(60*65), mockTr("upload.hours.minutes", "1", "5"), "1hr 5 min");
        equal(test(60*(60 + 59)), mockTr("upload.hours.minutes", "1", "59"), "1hr 59 min");
        equal(test(60*(60 + 60)), mockTr("upload.hours", "2"), "2hr");
        equal(test(60*(60 + 61)), mockTr("upload.hours.minutes", "2", "1"), "2hr 1 min");
        equal(test(60*(60 + 70.234)), mockTr("upload.hours.minutes", "2", "10"), "2hr 10min");
    });

    module("Timer");
    test("Test for Timer.schedule no scope", function() {
        var count = 0, fn, timeout, called = false, scope, cancelled = false;
        var _startTimeout = function(f, t) {
            fn = f;
            timeout = t;
            return ++count;
        };
        var cancel = function() {
            cancelled = true;
        };
        var callback = function() {
            called = true;
            scope = this;
        };

        var timer = new AJS.InlineAttach.Timer(callback);
        timer._startTimeout = _startTimeout;
        timer.cancel = cancel;

        ok(!timer.timeoutId, "No Timeout");
        ok(timer.schedule(16) === timer, "Return the timer");
        equal(16, timeout, "Correct Timeout");
        ok(timer.timeoutId == 1, "Correct Id");

        //Call the callback as if from setTimeout
        fn.call(window);
        ok(!timer.timeoutId, "Timer destroyed");
        ok(called, "Callback should be called.");
        ok(cancelled, "Cancel was called.");
        equal(timer, scope, "Is the scope correctly set.");
    });

    test("Test for Timer.schedule scope", function() {
        var count = 0, fn, timeout, called = false, actualScope, expectedScope = {};
        var _startTimeout = function(f, t) {
            fn = f;
            timeout = t;
            return ++count;
        };
        var callback = function() {
            called = true;
            actualScope = this;
        };

        var timer = new AJS.InlineAttach.Timer(callback, expectedScope);
        timer._startTimeout = _startTimeout;

        ok(!timer.timeoutId, "No Timeout");
        ok(timer.schedule(678) === timer, "Return the timer");
        equal(678, timeout, "Correct Timeout");
        ok(timer.timeoutId == 1, "Correct Id");

        //Call the callback as if from setTimeout
        fn.call(window);
        ok(!timer.timeoutId, "Timer destroyed");
        ok(called, "Callback should be called.");
        equal(expectedScope, actualScope, "Is the scope correctly set.");
    });

    test("Test for Timer.schedule double schedule", function() {
        var count = 0, called = false;
        var _startTimeout = function() {
            return ++count;
        };
        var _endTimeout = function(id) {
            equal(1, id, "Cancel called with the correct id.");
        };

        var timer = new AJS.InlineAttach.Timer();
        timer._startTimeout = _startTimeout;
        timer._endTimeout = _endTimeout;

        ok(!timer.timeoutId, "No Timeout");
        ok(timer.schedule(678) === timer, "Return the timer");
        ok(timer.timeoutId == 1, "Correct Id");
        ok(timer.schedule(56) === timer, "This should cancel the timer.");
        ok(timer.timeoutId == 2, "Correct Id");
    });

    module("AjaxUpload", {
        setup: function() {
            this.rescope = AJS.InlineAttach.rescope;
            this.xhr = AJS.InlineAttach.AjaxUpload.xhr;
            this._xhrJquery = AJS.InlineAttach.AjaxUpload._xhrJquery;
            this._xhrDirect = AJS.InlineAttach.AjaxUpload._xhrDirect;
            this.apisupport = AJS.InlineAttach.AjaxUpload._fileApiSupport;
            this.xhrsupport = AJS.InlineAttach.AjaxUpload._xhrSupport;
            this.text = AJS.InlineAttach.Text;

            AJS.InlineAttach.Text = MockText;
        },
        teardown: function() {
            AJS.InlineAttach.rescope = this.rescope;
            AJS.InlineAttach.AjaxUpload.xhr = this.xhr;
            AJS.InlineAttach.AjaxUpload._xhrJquery = this._xhrJquery;
            AJS.InlineAttach.AjaxUpload._xhrDirect = this._xhrDirect;
            AJS.InlineAttach.AjaxUpload._fileApiSupport = this.apisupport;
            AJS.InlineAttach.AjaxUpload._xhrSupport = this.xhrsupport;
            AJS.InlineAttach.Text = this.text;
        }
    });

    var MockXhr = Class.extend({
        init: function(th) {
            this.throwdata = th || null;
            this.headers = {};
            this.upload = {};
            this.aborted = false;
        },
        open: function(method, url, async) {
            this.method = method;
            this.url = url;
            this.async = async;
        },
        setRequestHeader: function(key, value) {
            this.headers[key] = value;
        },
        send: function(data) {
            this.data = data;
            if (this.throwdata) {
                throw this.throwdata;
            }
        },
        abort: function() {
            this.aborted = true;
        }
    });

    test("xhr constructor", function() {
        var testing = AJS.InlineAttach.AjaxUpload.xhr;
        AJS.InlineAttach.AjaxUpload._xhrJquery = function() { return null; };
        AJS.InlineAttach.AjaxUpload._xhrDirect = function() { return 1 };

        equal(1, testing(), "Should used direct");
        equal(AJS.InlineAttach.AjaxUpload._xhrDirect, AJS.InlineAttach.AjaxUpload.xhr, "Updated the function.");

        var jQueryConstructor = function() { return 2; };

        AJS.InlineAttach.AjaxUpload._xhrJquery = function() { return jQueryConstructor; };
        equal(2, testing(), "Should use jQuery");
        equal(jQueryConstructor, AJS.InlineAttach.AjaxUpload.xhr, "Updated the function.")
    });

    test("isSupported", function() {
        var testing = AJS.InlineAttach.AjaxUpload.isSupported;

        AJS.InlineAttach.AjaxUpload._fileApiSupport = recordfn(false);
        AJS.InlineAttach.AjaxUpload._xhrSupport = failfn("_xhrSupport");
        equal(false, testing(), "No File Api Support");

        AJS.InlineAttach.AjaxUpload._fileApiSupport = recordfn(true);
        AJS.InlineAttach.AjaxUpload._xhrSupport = recordfn(false);
        equal(false, testing(), "No xhr.upload.");

        AJS.InlineAttach.AjaxUpload._xhrSupport = recordfn(true);
        equal(true, testing(), "Has support");
    });

    test("_xhrSupport", function() {
         var testing = AJS.InlineAttach.AjaxUpload._xhrSupport;

        AJS.InlineAttach.AjaxUpload.xhr = function() { return null; };
        equal(false, testing(), "No xhr.");

        AJS.InlineAttach.AjaxUpload.xhr = function() { return {}; };
        equal(false, testing(), "No xhr.upload.");

        AJS.InlineAttach.AjaxUpload.xhr = function() { throw "Exception" };
        equal(false, testing(), "No xhr error is not supported.");

        AJS.InlineAttach.AjaxUpload.xhr = function() { return {upload: true}; };
        equal(true, testing(), "No xhr with xhr.upload is supported.");
    });

    test("test constructor", function() {
        var testing = AJS.InlineAttach.AjaxUpload;
        var file = "file", url = "url", params = {test: true}, scope = { scope: true };
        AJS.InlineAttach.rescope = function(fn, actualScope) {
            deepEqual(scope, actualScope, "Correct scope?");
            return fn + 1;
        };

        var upload = new testing({
            scope: scope,
            file: file,
            url: url,
            params: params,
            progress: "progress",
            error: "error",
            success: "success",
            abort: "abort",
            after: "after",
            before: "before"
        });

        equal(file, upload.file, "File correctly set.");
        equal(url, upload.url, "URL correctly set.");
        deepEqual(params, upload.params, "Params correctly set.");
        equal("progress1", upload.progresscb, "Progress correctly set.");
        equal("error1", upload.errorcb, "Error correctly set.");
        equal("success1", upload.successcb, "Success correctly set.");
        equal("abort1", upload.abortcb, "Abort correctly set.");
        equal("after1", upload.finalcb, "Final correctly set.");
        equal("before1", upload.beforecb, "Before correctly set.");
        equal(false, upload.aborted, "Aborted correctly set.");

        AJS.InlineAttach.rescope = function(fn, actualScope) {
            equal(null, actualScope, "Correct scope for " + fn + "?");
            return fn + 2;
        };

        upload = new testing({
            file: file,
            url: url,
            progress: "progress",
            error: "error",
            success: "success",
            abort: "abort",
            after: "after",
            before: "before"
        });

        equal(file, upload.file, "File correctly set.");
        equal(url, upload.url, "URL correctly set.");
        deepEqual({}, upload.params, "Params correctly set.");
        equal("progress2", upload.progresscb, "Progress correctly set.");
        equal("error2", upload.errorcb, "Error correctly set.");
        equal("success2", upload.successcb, "Success correctly set.");
        equal("abort2", upload.abortcb, "Abort correctly set.");
        equal("after2", upload.finalcb, "Final correctly set.");
        equal("before2", upload.beforecb, "Before correctly set.");
        equal(false, upload.aborted, "Aborted correctly set.");
    });

    test("upload not aborted", function() {

        var testing = AJS.InlineAttach.AjaxUpload;
        var xhr = new MockXhr();

        AJS.InlineAttach.AjaxUpload.xhr = function() { return xhr; };

        var beforeCalled = 0;
        var file = {type: "type"};
        var upload = new testing({
            url: "url",
            params: {test: true},
            file: file,
            before: function() {
                beforeCalled++;
            }
        });

        var statechangeCalled = 0;
        var change = upload._statechange = function() {
            ++statechangeCalled;
            equal(upload, this, "Correct scope for on state change?");
        };
        var uploadCalled = 0;
        var uploadcb = upload._upload = function() {
            uploadCalled++;
            equal(upload, this, "Correct scope for upload?");
        };
        upload.upload();

        equal(1, beforeCalled, "Before should have been called.");
        equal("POST", xhr.method, "Posting?");
        equal("url?test=true", xhr.url, "Correct url?");
        equal(true, xhr.async, "Asynchronous?");
        deepEqual({"Content-Type": "type"}, xhr.headers, "Content type set?");
        equal(file, xhr.data, "File being sent?");

        equal(0, statechangeCalled, "statechange not called yet.");
        xhr.onreadystatechange();
        equal(1, statechangeCalled, "statechange not called.");

        equal(0, uploadCalled, "upload not called yet.");
        xhr.upload.onprogress();
        equal(1, uploadCalled, "upload not called.");

        xhr = new MockXhr();
        AJS.InlineAttach.AjaxUpload.xhr = function() { return xhr; };
        file = {blarg: "type"};
        upload = new testing({
            url: "url",
            file: file
        });
        upload._statechange = change;
        upload._upload = uploadcb;

        upload.upload();

        equal("POST", xhr.method, "Posting?");
        equal("url", xhr.url, "Correct url?");
        equal(true, xhr.async, "Asynchronous?");
        deepEqual({"Content-Type": "application/octet-stream"}, xhr.headers, "Content type set?");
        equal(file, xhr.data, "File being sent?");

        equal(1, statechangeCalled, "statechange not called yet.");
        xhr.onreadystatechange();
        equal(2, statechangeCalled, "statechange not called.");

        equal(1, uploadCalled, "upload not called yet.");
        xhr.upload.onprogress();
        equal(2, uploadCalled, "upload not called.");
    });

    test("abort not uploading", function() {
        var testing = AJS.InlineAttach.AjaxUpload;

        AJS.InlineAttach.AjaxUpload.xhr = function() { ok(false, "Should not be creating the XHR if its aborted."); }

        var abortCalled = false;
        var finalCalled = false;

        var upload = new testing({
            url: "url",
            file: "file",
            abort: function() {
                ok(!abortCalled, "First call in abort?.");
                ok(!finalCalled, "Abort called before final.");
                abortCalled = true;
            },
            after: function() {
                ok(!finalCalled, "Final call in after?.");
                ok(abortCalled, "Abort not called before final.");
                finalCalled = true;
            },
            progress: failfn("progess"),
            error: failfn("error"),
            success: failfn("success"),
            before: failfn("before")
        });

        ok(!upload.aborted, "Not aborted initially.");
        upload.abort();
        ok(upload.aborted, "Aborted?.");
        ok(abortCalled && finalCalled, "Correct callbacks called.");
        upload.abort();
        ok(upload.aborted, "Still Aborted?.");
    });

    test("progess", function() {
        var testing = AJS.InlineAttach.AjaxUpload;

        var xhr = new MockXhr();
        AJS.InlineAttach.AjaxUpload.xhr = function() { return xhr; };

        var loadedCalled = 0;
        var lastLoaded = 0;
        var upload = new testing({
            url: "url",
            file: "file",
            abort: failfn("abort"),
            after: failfn("after"),
            progress: function(loaded) {
                loadedCalled++;
                lastLoaded = loaded;
            },
            error: failfn("error"),
            success: failfn("success"),
            before: failfn("before")
        });

        equal(0, loadedCalled, "Callback not called.");
        upload._upload({});
        equal(0, loadedCalled, "Callback still not called.");
        upload._upload({lengthComputable: true, loaded: 1});
        equal(1, loadedCalled, "Callback called.");
        equal(1, lastLoaded, "Loaded correctly passed?");
        upload._upload({loaded: 2});
        equal(1, loadedCalled, "Callback not called again.");
        upload._upload({lengthComputable: true, loaded: 78});
        equal(2, loadedCalled, "Callback called.");
        equal(78, lastLoaded, "Loaded correctly passed?");
    });

    test("statechange aborted", function() {
        var testing = AJS.InlineAttach.AjaxUpload;

        var xhr = new MockXhr();
        AJS.InlineAttach.AjaxUpload.xhr = function() { return xhr; };

        var abortCalled = 0;
        var afterCalled = 0;
        var beforeCalled = 0;
        var upload = new testing({
            url: "url",
            file: "file",
            abort: function() {
                abortCalled++;
            },
            after: function() {
                afterCalled++;
                equal(abortCalled, afterCalled, "After called after the abort.");
            },
            before: function() {
                beforeCalled++;
            },
            progress: failfn("progress"),
            error: failfn("error"),
            success: failfn("success")
        });

        testing = $.proxy(upload._statechange, upload);
        upload.upload();
        upload.aborted = true;

        equal(1, beforeCalled, "Before called.");
        equal(0, abortCalled, "Abort not called.");
        testing();
        equal(0, abortCalled, "Abort still not called.");
        xhr.readyState = 4;
        testing();
        equal(1, abortCalled, "Abort is called.");
        equal(1, afterCalled, "After is called.");
    });

    test("statechange errors", function() {
        var testing = AJS.InlineAttach.AjaxUpload;

        var xhr = new MockXhr();
        AJS.InlineAttach.AjaxUpload.xhr = function() { return xhr; };

        var name = "thisIsAFileName";
        var afterfn = recordfn("after");
        var errorfn = recordfn("error");
        var upload = new testing({
            url: "url",
            file: {name: name},
            abort: failfn("abort"),
            after: afterfn,
            progress: failfn("progress"),
            error: errorfn,
            success: failfn("success")
        });

        testing = $.proxy(upload._statechange, upload);
        upload.upload();

        equal(errorfn.count, 0, "Error not called.");
        testing();
        equal(errorfn.count, 0, "Error still not called.");

        xhr.readyState = 4;
        xhr.responseText = "Not JSON";
        xhr.status = 400012;

        testing();
        equal(errorfn.count, 1, "Error is called.");
        equal(afterfn.count, 1,  "After is called.");
        deepEqual(errorfn.lastArgs, [xhr.responseText, xhr.status, xhr], "Error passed the correct arguments?");
    });

    test("client errors", function() {
        var testing = AJS.InlineAttach.AjaxUpload;

        var name = "thisIsAFileName";
        var afterfn = recordfn("after");
        var errorfn = recordfn("error");
        var xhr;
        var createUpload = function(th) {
            xhr = new MockXhr(th);
            AJS.InlineAttach.AjaxUpload.xhr = function() { return xhr; };

            return new testing({
                url: "url",
                file: {name: name},
                abort: failfn("abort"),
                after: afterfn,
                progress: failfn("progress"),
                error: errorfn,
                success: failfn("success")})
        };

        var upload = createUpload({name: "NS_ERROR_FILE_ACCESS_DENIED"});
        upload.upload();
        equal(errorfn.count, 1, "Error is called.");
        equal(afterfn.count, 1,  "After is called.");
        deepEqual(errorfn.lastArgs, [mockTr("upload.error.no.access", name), -1, xhr], "Error passed the correct arguments?");

        upload = createUpload({name: "NS_ERROR_FILE_NOT_FOUND"});
        upload.upload();
        equal(errorfn.count, 2, "Error is called.");
        equal(afterfn.count, 2,  "After is called.");
        deepEqual(errorfn.lastArgs, [mockTr("upload.error.does.not.exist", name), -1, xhr], "Error passed the correct arguments?");

        var expectionStr = "Some kind of error";
        upload = createUpload(expectionStr);
        upload.upload();
        equal(errorfn.count, 3, "Error is called.");
        equal(afterfn.count, 3,  "After is called.");
        deepEqual(errorfn.lastArgs, [mockTr("upload.error.client.unknown", name, expectionStr), -1, xhr], "Error passed the correct arguments?");
    });

    test("statechange success", function() {
        var testing = AJS.InlineAttach.AjaxUpload;

        var xhr = new MockXhr();
        AJS.InlineAttach.AjaxUpload.xhr = function() { return xhr; };

        var response = {bad: "fase", caount: 574758};
        var beforefn = recordfn("before");
        var afterfn = recordfn("after");
        var successfn = recordfn("success");
        var upload = new testing({
            url: "url",
            file: {name: name},
            abort: failfn("abort"),
            progress: failfn("progress"),
            error: failfn("error"),
            after: afterfn,
            success: successfn,
            before: beforefn
        });

        testing = $.proxy(upload._statechange, upload);
        upload.upload();

        equal(beforefn.count, 1, "Before was called.");
        equal(successfn.count, 0, "Succcess not called.");
        testing();
        equal(successfn.count, 0, "Success still not called.");

        xhr.readyState = 4;
        xhr.responseText = JSON.stringify(response);
        xhr.status = 201;
        testing();
        equal(successfn.count, 1, "Success called.");
        equal(afterfn.count, 1, "After called after success");
        deepEqual(successfn.lastArgs, [response, xhr.status, xhr], "Is the correct result passed?");
    });

    test("getClientErrorMessage", function() {
        var testing = AJS.InlineAttach.AjaxUpload.getClientErrorMessage;
        var file = {name: "name"};
        var e = "error";

        equal(testing(e, file), AJS.InlineAttach.Text.tr("upload.error.client.unknown", file.name, e));

        e = {message: "Don't know"};
        equal(testing(e, file), AJS.InlineAttach.Text.tr("upload.error.client.unknown", file.name, e.message));

        e.name = "NS_ERROR_FILE_ACCESS_DENIED";
        equal(testing(e, file), AJS.InlineAttach.Text.tr("upload.error.no.access", file.name));

        e.name = "NS_ERROR_FILE_NOT_FOUND";
        equal(testing(e, file), AJS.InlineAttach.Text.tr("upload.error.does.not.exist", file.name));

        e.name = "NS_ERROR_FILE_TARGET_DOES_NOT_EXIST";
        equal(testing(e, file), AJS.InlineAttach.Text.tr("upload.error.does.not.exist", file.name));
    });

    module("FormUpload", {
        setup: function() {
            this.rescope = AJS.InlineAttach.rescope;

        },
        teardown: function() {
            AJS.InlineAttach.rescope = this.rescope;
        }
    });

    test("test constructor", function() {
        var testing = AJS.InlineAttach.FormUpload;
        var $input = "input", url = "url", params = {test: true}, scope = { scope: true };
        AJS.InlineAttach.rescope = function(fn, actualScope) {
            deepEqual(scope, actualScope, "Correct for " + fn + " scope?");
            return fn + 1;
        };

        var upload = new testing({
            scope: scope,
            $input: $input,
            url: url,
            params: params,
            before: "before",
            error: "error",
            success: "success",
            after: "after",
            abort: "abort"
        });

        equal($input, upload.$input, "$input correctly set.");
        equal(url, upload.url, "URL correctly set.");
        deepEqual(params, upload.params, "Params correctly set.");
        equal("error1", upload.errorcb, "Error correctly set.");
        equal("success1", upload.successcb, "Success correctly set.");
        equal("before1", upload.before, "Before correctly set.");
        equal("after1", upload.after, "Final correctly set.");
        equal("abort1", upload.abortcb, "Abort correctly set.");
        equal(upload.aborted, false, "Aborted set correctly?");
        equal(upload.$form, null, "No form should have been set.");
        equal(upload.xhr, null, "XHR not set?");

        AJS.InlineAttach.rescope = function(fn, actualScope) {
            equal(null, actualScope, "Correct scope for " + fn + "?");
            return fn + 2;
        };

        upload = new testing({
            $input: $input,
            url: url,
            before: "before",
            error: "error",
            success: "success",
            after: "after",
            abort: "abort"
        });

        equal($input, upload.$input, "$input correctly set.");
        equal(url, upload.url, "URL correctly set.");
        deepEqual({}, upload.params, "Params correctly set.");
        equal("error2", upload.errorcb, "Error correctly set.");
        equal("success2", upload.successcb, "Success correctly set.");
        equal("before2", upload.before, "Before correctly set.");
        equal("after2", upload.after, "Final correctly set.");
        equal("abort2", upload.abortcb, "Abort correctly set.");
        equal(upload.aborted, false, "Aborted set correctly?");
        equal(upload.$form, null, "No form should have been set.");
        equal(upload.xhr, null, "XHR not set?");
    });

    test("test upload", function() {
        var testing = AJS.InlineAttach.FormUpload;
        var $input = "<input type='file'>", url = "url", params = {test: true};

        var ajaxFromArg;
        var appendArg;
        var submitCalled = false;
        var removedCount = 0;

        var mockAjaxForm = {
            ajaxForm: function(argument) {
                ajaxFromArg = argument;
                return mockAjaxForm;
            },
            submit: function() {
                submitCalled = true;
                return mockAjaxForm;
            },
            append: function(argument) {
                appendArg = argument;
                return mockAjaxForm;
            },
            remove: function() {
                removedCount++;
                return mockAjaxForm;
            }
        };

        var beforeCount = 0;
        var errorCount = 0;
        var successCount = 0;
        var afterCount = 0;
        var lastError;
        var lastSuccess;
        var upload = new testing({
            $input: $input,
            url: url,
            params: params,
            before: function() {
                beforeCount++;
            },
            error: function(error) {
                errorCount++;
                lastError = error;
            },
            success: function(data) {
                lastSuccess = data;
                successCount++;
            },
            after: function() {
                afterCount++;
            }
        });

        upload._renders = {
            form: function(postUrl) {
                var queryParams = AJS.$.param(params);
                var urlWithQueryParams = url;
                if (queryParams) {
                    urlWithQueryParams = url + "?" + queryParams;
                }
                equal(urlWithQueryParams, postUrl, "Is the post url correctly set with parameters added to querystring?");
                return mockAjaxForm;
            }
        };
        upload._addToBody = function(form) {
            equal(form, mockAjaxForm, "Appending form to the body?");
        };

        upload.upload();
        equal("json", ajaxFromArg.dataType, "Is data correct?");
        deepEqual(params, ajaxFromArg.data, "Are the parameters correct?");
        equal(0, ajaxFromArg.timeout, "Is AJAX timeout zero?");
        equal(true, submitCalled, "Is the form submitted?");

        var xhr = {};
        ajaxFromArg.beforeSend(xhr);
        equal(upload.xhr, xhr, "XHR correctly set?");

        equal(0, beforeCount, "Before not called.");
        ajaxFromArg.beforeSubmit();
        equal(1, beforeCount, "Before called.");

        //Check success.
        equal(0, successCount, "Success not called.");
        equal(0, afterCount, "After not called.");
        equal(0, removedCount, "Removed not called.");
        var object = {data: "data"};
        ajaxFromArg.success(object);
        equal(1, successCount, "Before called.");

        //Check error with no arg.
        ajaxFromArg.error();
        equal(1, errorCount, "Before called.");
        equal("", lastError, "Correct errror passed.");

        //Test error with arg but no message.
        ajaxFromArg.error({});
        equal(2, errorCount, "Before called.");
        equal("", lastError, "Correct errror passed.");

        //Test error with message.
        ajaxFromArg.error({responseText: "error"});
        equal(3, errorCount, "Before called.");
        equal("error", lastError, "Correct errror passed.");

        //Test empty success.
        ajaxFromArg.success();
        equal(2, successCount, "Before called.");
        deepEqual(lastSuccess, {}, "Correct data passed.");

        //Test complete callback.
        equal(removedCount, 0, "Initial remove count correct?");
        equal(upload.$form, mockAjaxForm, "Initial form correct?");
        ajaxFromArg.complete();
        equal(1, removedCount, "Removed called.");
        equal(upload.$form, null, "Form removed?");
        equal(1, afterCount, "After called.");
        
        //Test complete callback when form already removed.
        ajaxFromArg.complete();
        equal(1, removedCount, "Removed called.");
        equal(2, afterCount, "After called.");
    });

    test("test abort", function() {
        var testing = AJS.InlineAttach.FormUpload;
        var $input = "<input type='file'>", url = "url", params = {test: true};

        var abortFn = recordfn("abort");
        var afterFn = recordfn("after");
        var upload = new testing({
            $input: $input,
            url: url,
            params: params,
            abort: abortFn,
            after: afterFn
        });

        equal(upload.aborted, false, "Initially not aborted");
        upload.abort();
        equal(abortFn.count, 1, "Abort called");
        equal(afterFn.count, 1, "After called");
        equal(upload.aborted, true, "Aborted after?");

        abortFn = recordfn("abort");
        var xhr = {
            abort: abortFn
        };

        upload = new testing({
            $input: $input,
            url: url,
            params: params,
            abort: failfn("abort"),
            after: failfn("after")
        });
        upload.xhr = xhr;

        equal(upload.aborted, false, "Initially not aborted");
        upload.abort();
        equal(abortFn.count, 1, "XHR abort called?");
        equal(upload.aborted, true, "Aborted after?");
    });

    module("Form", {
        setup: function() {
            this.progress = AJS.InlineAttach.UploadProgress;
            this.staticprogress = AJS.InlineAttach.UnknownProgress;
            this.tr = AJS.InlineAttach.Text.tr;
            AJS.InlineAttach.Text.tr = mockTr;
            var body = this.$body = $('#qunit-fixture');
            var form = this.$form = $("<form>");
            var input = this.$input = $("<input type='file'>");
            this.$id = $("<input name='id' value='10'>").appendTo(form);
            this.$pid = $("<input name='pid' value='20'>").appendTo(form);
            this.$maxSize = $("<div id='attach-max-size'>30</div>").appendTo(body);

            body.append(form.append(input));
        },
        teardown: function() {
            AJS.InlineAttach.Text.tr = this.tr;
            AJS.InlineAttach.UploadProgress = this.progress;
            AJS.InlineAttach.UnknownProgress = this.staticprogress;
        }
    });

    test("Form init", function() {
        this.$body.append("<input name='id' value='2272782'>");
        this.$body.append("<input name='pid' value='2828282'>");

        var testing = new AJS.InlineAttach.Form({$element: this.$input});
        ok(this.$form.get(0)==testing.$form.get(0), "Correct form found?");
        equal(testing.maxSize, 30, "Correct max size.");
        equal(testing.issueId, 10, "Correct issue id.");
        equal(testing.projectId, 20, "Correct issue pid.");

        this.$pid.remove();

        testing = new AJS.InlineAttach.Form({$element: this.$input});
        ok(this.$form.get(0) == testing.$form.get(0), "Correct form found?");
        equal(testing.maxSize, 30, "Correct max size.");
        equal(testing.issueId, 10, "Correct issue id.");
        equal(testing.projectId, undefined, "Correct issue pid.");

        this.$pid.appendTo(this.$form);
        this.$id.remove();

        testing = new AJS.InlineAttach.Form({$element: this.$input});
        ok(this.$form.get(0) == testing.$form.get(0), "Correct form found?");
        equal(testing.maxSize, 30, "Correct max size.");
        equal(testing.issueId, undefined, "Correct issue id.");
        equal(testing.projectId, 20, "Correct issue pid.");

        this.$maxSize.remove();
        try{
            testing = new AJS.InlineAttach.Form({$element: this.$input});
            ok(false, "Should not be able to create form wihtout maxSize");
        } catch (e) {
            //expected.
        }

        this.$maxSize.appendTo(this.$body);
        this.$pid.remove();

        try{
            testing = new AJS.InlineAttach.Form({$element: this.$input});
            ok(false, "Should not be able to create form wihtout pid or id.");
        } catch (e) {
            //expected.
        }
    });

    test("Form getAtlToken", function() {
        this.$form.append("<input name='atl_token' value='78'>");
        var testing = new AJS.InlineAttach.Form({$element: this.$input});
        equal("78", testing.getAtlToken(), "Do it get the current token.");
    });

    test("Form setAtlToken", function() {
        var value = "someTokenValue;";
        var tokenElement = $("<input name='atl_token' value='78'>");
        this.$form.append(tokenElement);
        var testing = new AJS.InlineAttach.Form({$element: this.$input});
        ok(testing.setAtlToken(value)== testing, "Returning itself?");
        ok(tokenElement.val() == value, "Is token value set correctly?");
    });

    test("Form disable/enable", function() {
        var submit1 = $("<input type='submit' name='atl_token' value='78'>");
        var submit2 = $("<input name='atl_token' value='78'>");
        this.$form.append(submit1).append(submit2);

        var testing = new AJS.InlineAttach.Form({$element: this.$input});
        testing.disable();

        equal(submit1.prop("disabled"), true, "Submit 1 disabled?");
        equal(submit2.prop("disabled"), false, "Submit 2 enabled?");

        submit2.attr("disabled", "disabled");
        testing.enable();

        equal(submit1.prop("disabled"), false, "Submit 1 enabled?");
        equal(submit2.prop("disabled"), true, "Submit 2 disabled?");
    });

    test("Add Progress", function() {
        var expectedFile = 327373.333;
        var testing = new AJS.InlineAttach.Form({$element: this.$input});
        var lastElement;
        testing._addElement = function(el) {
            lastElement = el;
        };

        AJS.InlineAttach.UploadProgress = function(file) {
            equal(file, expectedFile, "Input file is correct?");
            this.$element = file;
        };
        var prog = testing.addProgress(expectedFile);
        ok(prog, "Progress actuall returned?");
        equal(lastElement, expectedFile, "Element correct?");
    });

    test("Add Static Progress", function() {
        var expectedFile = 327373.333;
        var testing = new AJS.InlineAttach.Form({$element: this.$input});
        var lastElement;
        testing._addElement = function(el) {
            lastElement = el;
        };

        AJS.InlineAttach.UnknownProgress = function(file) {
            equal(file, expectedFile, "Input file is correct?");
            this.$element = file;
        };
        var prog = testing.addStaticProgress(expectedFile);
        ok(prog, "Progress actuall returned?");
        equal(lastElement, expectedFile, "Element correct?");
    });

    test("Add Temporary Checkbox", function() {
        var value = 327373, name = "name", replaceObj ={};
        var testing = new AJS.InlineAttach.Form({$element: this.$input});
        var lastElement, lastReplace;
        testing._replaceElement = function(el, replace) {
            lastElement = el;
            lastReplace = replace;
        };

        testing.addTemporaryFileCheckbox(value, name, replaceObj);
        ok(lastElement, "The checkox was actually created?");
        equal(lastReplace, replaceObj, "The replacement object was correct");

        var label = lastElement.children("label");
        equal(label.text(), name, "Label created with name.");

        var checkbox = lastElement.children("input[type=checkbox]");
        equal(checkbox.attr("value"), String(value), "Checkbox value correct.");
        equal(checkbox.attr("title"), mockTr("upload.checkbox.title"), "Checkbox value correct.");
    });

    test("Add Error", function() {
        var error = "name", replaceObj ={};
        var testing = new AJS.InlineAttach.Form({$element: this.$input});
        var lastElement, lastReplace;
        testing._replaceElement = function(el, replace) {
            lastElement = el;
            lastReplace = replace;
        };

        var that = testing.addError(error, replaceObj);
        ok(lastElement, "The checkox was actually created?");
        ok(lastReplace == replaceObj, "The replacement object was correct");

        var errorEl = lastElement.find("div");
        equal(errorEl.text(), error, "Error created.");
    });

    test("Clear Error", function() {
        var testing = new AJS.InlineAttach.Form({$element: this.$input});
        this.$form.append($('<div>').addClass("error").text("bad"));
        this.$form.append($('<div>').text("good"));
        this.$form.append($('<div>').addClass("error").text("bad"));

        var that = testing.clearErrors();

        ok(that == testing, "Returned self?");
        ok(this.$form.find("div.error").size() == 0, "All Errors removed.");
        ok(this.$form.find("div").size() == 1, "Left around non-errors");
    });

    test("Clone File Input", function() {
        var testing = new AJS.InlineAttach.Form({$element: this.$input});

        var oldInput = {};

        testing.fileSelector = {
            cloneInput: function() {
                return oldInput;
            }
        };

        var old = testing.cloneFileInput();
        equal(old, oldInput, "Old selector returned?");
    });

    test("On Cancel", function() {
        var link1 = $("<a>").addClass("cancel");
        var link2 = $("<a>").addClass("notCancel");

        this.$form.append(link1, link2);
        var testing = new AJS.InlineAttach.Form({$element: this.$input}), cancelCalled = 0, scope;
        var that = testing.onCancel(function(){
            scope = this;
            cancelCalled++;
        });
        ok(that == testing, "Returned self?");

        link2.click();
        equal(cancelCalled, 0, "Cancel not called.");

        link1.click();
        equal(cancelCalled, 1, "Cancel called.");
        ok(scope == testing, "Is scope correct for callback.");
    });

    test("Replace Element", function() {
        var testing = new AJS.InlineAttach.Form({$element: this.$input});
        var lastReplace, lastAdd, addCount = 0, replaceCount = 0;
        var replaceObject = {
            $element: {
                replaceWith: function($el) {
                    replaceCount++;
                    lastReplace = $el;
                }
            }
        };

        var elem = function (id) {
            return {
                id: id,
                fadeIn: function() {
                }
            }
        };

        testing._addElement = function($el) {
            addCount++;
            lastAdd = $el;
        };

        var e = elem(0  );
        testing._replaceElement(e);
        equal(replaceCount, 0, "Replace should not have been called.");
        equal(addCount, 1, "Add should have been called.");
        equal(lastAdd, e, "Added right element?");

        e = elem(1);
        testing._replaceElement(e, {});
        equal(replaceCount, 0, "Replace should not have been called.");
        equal(addCount, 2, "Add should have been called.");
        equal(lastAdd, e, "Added right element?");

        e = elem(2);
        testing._replaceElement(e, replaceObject);
        equal(replaceCount, 1, "Replace should not have been called.");
        equal(addCount, 2, "Add should have been called.");
        equal(lastReplace, e, "Replaced right element?");
    });

    module("UnknownProgress", {
        setup: function() {
            this.tr = AJS.InlineAttach.Text.tr;
            AJS.InlineAttach.Text.tr = mockTr;
        },
        teardown: function() {
            AJS.InlineAttach.Text.tr = this.tr;
        }
    });

    test("Constructor", function() {
        var name = "fileName";
        var testing = new AJS.InlineAttach.UnknownProgress(name);
        equal(testing.$element.attr("title"), mockTr("upload.progress.title.waiting"), "Title is set to waiting.");
        equal(testing.$content.text(), mockTr("upload.file.waiting", name), "Content set correctly to file name.");

        ok(contains(testing.$element, testing.$cancel), "Cancel added to element.");

        testing.start();
        equal(testing.$element.attr("title"), mockTr("upload.progress.title.running"), "Title is set to running.");
    });

    test("onCancel", function() {
        var name = "Name";
        var testing = new AJS.InlineAttach.UnknownProgress(name);

        var callbackCount = 0;
        var callback = function() {
            callbackCount++;
            ok(this == testing, "Callback called with correct scope?");
        };

        testing.onCancel(callback);
        testing.$cancel.click();
        equal(callbackCount, 1, "Callback should have been called.");
    });

    test("start", function() {
        var name = "fileName";
        var testing = new AJS.InlineAttach.UnknownProgress(name);

        testing.start();
        equal(testing.$element.attr("title"), mockTr("upload.progress.title.running"), "Title set to running.");
        equal(testing.$content.text(), name, "Title set to the name.");
    });

    module("UploadProgress", {
        setup: function() {
            this.text = AJS.InlineAttach.Text;
            this.pb = AJS.InlineAttach.ProgressBar;
            this.timer = AJS.InlineAttach.Timer;

            AJS.InlineAttach.Text = MockText;
        },
        teardown: function() {
            AJS.InlineAttach.Text = this.text;
            AJS.InlineAttach.ProgressBar = this.pb;
            AJS.InlineAttach.Timer = this.timer;
        }
    });

    test("Constructor", function() {
        var size = 500;
        var name = "Name";
        var file = {name: name, size: size};

        var timerFn, timerScope;
        AJS.InlineAttach.Timer = function(fn, scope) {
            timerFn = fn;
            timerScope = scope;
        };

        var testing = new AJS.InlineAttach.UploadProgress(file);

        equal(timerFn, testing._update, "Timer function correctly set?");
        ok(timerScope == testing, "Timer scope correctly set?");

        equal(testing.total, size, "Total Size set correctly?");
        equal(testing.current, 0, "Current Size set correctly?");
        equal(testing.rateNumerator, 0, "Rate setup?");
        equal(testing.rateDenominator, 0, "Rate setup?");
        equal(testing.name, name, "File name correctly set?");

        equal(testing.$element.attr("title"), mockTr("upload.progress.title.waiting"), "Correct Waiting title.");
        equal(testing.$cancel.text(), mockTr("upload.cancel"), "Cancel Link Name.");
        equal(testing.$content.text(), mockTr("upload.file.waiting", name), "Element text.");
        ok(contains(testing.$element, testing.$cancel), "Cancel added to element.");
        ok(contains(testing.$element, testing.$content), "Content added to element.");
        ok(contains(testing.$element, testing.progress.$element, "Progress added to element."));
    });

    test("Start", function() {
        var file = {name: "Name", size: 500};

        var timerFn, timerScope;
        AJS.InlineAttach.Timer = function(fn, scope) {
            timerFn = fn;
            timerScope = scope;
        };

        var now;
        var testing = new AJS.InlineAttach.UploadProgress(file);
        testing._now = function() {
            return now = new Date().getTime();
        };

        ok(testing.started === undefined, "Satrted should not be defined until started.");
        ok(testing.startedSize === undefined, "SatrtedSize should not be defined until started.");
        ok(testing.start() == testing, "Returning self?");

        equal(testing.started, now, "Started was not set correctly.");
        equal(testing.startedSize, 0, "StartedSize was not set to zero.");
    });

    test("Update", function() {
        var file = {name: "Name", size: 500};
        var cancelCalled = 0;
        var testing = new AJS.InlineAttach.UploadProgress(file);

        testing._update = function(arg) {
            current = arg;
            return testing;
        };

        testing.timer = {
            cancel: function() {
                cancelCalled++;
            }
        };

        ok(testing.update(15) == testing, "Returning self?");
        equal(current, 15, "_update called with value of 15?");
        equal(cancelCalled, 1, "Timer.cancel called?");
    });

    test("finish", function() {
        var file = {name: "Name", size: 500};
        var testing = new AJS.InlineAttach.UploadProgress(file);

        var cancelCalled = 0;
        testing.timer = {
            cancel: function() {
                cancelCalled++;
            }
        };

        var lastValue;
        testing.progress = {
            value: function(val) {
                lastValue = val;
            }
        };

        ok(testing.finish() == testing, "Returning self?");
        equal(cancelCalled, 1, "Timer cancelled?");
        equal(lastValue, 100, "Progress bar set to 100?");
    });

    test("onCancel", function() {
        var file = {name: "Name", size: 500};
        var testing = new AJS.InlineAttach.UploadProgress(file);

        var callbackCount = 0;
        var callback = function() {
            callbackCount++;
            ok(this  == testing, "Callback called with correct scope?");
        };

        testing.onCancel(callback);
        testing.$cancel.click();
        equal(callbackCount, 1, "Callback should have been called.");
    });

    test("_addRate", function() {
        var file = {name: "Name", size: 500};
        var testing = new AJS.InlineAttach.UploadProgress(file);

        var weight = AJS.InlineAttach.UploadProgress.WEIGHT;
        var rates = [0, 0, 0, 5,6,7,8,9,10,11, 0, 0, 68689];
        var num = 0, den = 0;
        for (var i = 0; i < rates.length; i++) {
            num = num * weight + rates[i];
            den = den * weight + 1;

            testing._addRate(rates[i]);
            ok(Math.abs(testing.rateNumerator - num) < 0.0001, "Numerator is the same? ("
                    + testing.rateNumerator + ", " + num + ").");
            ok(Math.abs(testing.rateDenominator - den) < 0.0001, "Denominator is the same? ("
                    + testing.rateDenominator + ", " + den + ").");
        }
    });

    test("_calcRate", function() {
        var file = {name: "Name", size: 500};
        var testing = new AJS.InlineAttach.UploadProgress(file);

        equal(testing._calcRate(), 0, "Return zero with no data?");
        testing.rateNumerator = 5;
        equal(testing._calcRate(), 0, "Return zero with no denominator?");
        testing.rateDenominator = 1000;
        equal(testing._calcRate(), 5/1000, "Return the fraction?");
        testing.rateNumerator = 4;
        equal(testing._calcRate(), 0, "Return zero with small fraction?");
    });

    test("_update", function() {
        var file = {name: "Name", size: 1024 * 1024};
        var testing = new AJS.InlineAttach.UploadProgress(file);
        var lastRate, lastPercentage, lastTitle, lastContent;
        var nextNow = 0, nextRate = 0, timerCount = 0;

        testing._now = function() {
            return nextNow;
        };

        testing._calcRate = function() {
            return nextRate;
        };

        testing._addRate = failfn("_addRate");

        testing._title = function(title) {
            lastTitle = title;
        };

        testing._content = function(content) {
            lastContent = content;
        };

        testing.progress.value = function(per) {
            lastPercentage = per;
        };

        testing.timer.schedule = function(timeout) {
            equal(timeout, AJS.InlineAttach.UploadProgress.UPLOAD_REFRESH, "Timer reset?");
            timerCount++;
        };


        var percentage = 0;
        var expectedTimer = 0;
        //Check to see that zerro percent is shown.
        var current = Math.floor(file.size * 0.005);
        ok(testing._update(current) == testing, "Returned self?");
        equal(lastPercentage, percentage, "Percentage is zero?");
        equal(timerCount, ++expectedTimer, "Timer reset correctly?");
        equal(lastTitle, MockText.currentOutOfTotalSize(current, file.size), "Title set correctly?");
        equal(lastContent, file.name, "Content set correctly?");

        testing.start();

        percentage = 1;
        //Check to see that 1% is shown.
        current = Math.ceil(file.size * 0.005);
        ok(testing._update(current) == testing, "Returned self?");
        equal(lastPercentage, 1, "Percentage is 1?");
        equal(timerCount,++expectedTimer , "Timer reset correctly?");
        equal(lastTitle, MockText.currentOutOfTotalSize(current, file.size), "Title set correctly?");
        equal(lastContent, file.name, "Content set correctly?");
        equal(testing.lastUpdate, nextNow, "Last update correct?");
        equal(testing.current, current, "Current set?");
        equal(testing.started, nextNow, "Started correct?");
        equal(testing.startedSize, 0, "Started size?");

        //Progress a single second. There is no rate to calculate because we have running less than 2 seconds. In
        //this state:
        // - Generic title with the amount uploaded.
        // - Generic content with only the filename.
        var lastUpdate = nextNow;

        nextNow += 1000;

        ok(testing._update(current) == testing, "Returned self?");
        equal(lastPercentage, percentage, "Percentage is correct?");
        equal(timerCount, ++expectedTimer, "Timer reset correctly?");
        equal(lastTitle, MockText.currentOutOfTotalSize(current, file.size), "Title set correctly?");
        equal(lastContent, file.name, "Content set correctly?");
        equal(testing.lastUpdate, lastUpdate, "Last update correct?");
        equal(testing.current, current, "Current set?");
        equal(testing.started, 0, "Started correct?");
        equal(testing.startedSize, 0, "Started size?");

        //Progress a single second. There is no rate to calculate because we ignore the first 2 seconds
        //of data to try and negate any OS buffers that make the upload seem super fast.
        // - Generic title with the amount uploaded.
        // - Generic content with only the filename.
        nextNow += 1000;
        var started = nextNow;
        var startedSize = current;

        ok(testing._update(current) == testing, "Returned self?");
        equal(lastPercentage, percentage, "Percentage is correct?");
        equal(timerCount, ++expectedTimer, "Timer reset correctly?");
        equal(lastTitle, MockText.currentOutOfTotalSize(current, file.size), "Title set correctly?");
        equal(lastContent, file.name, "Content set correctly?");
        equal(testing.lastUpdate, lastUpdate, "Last update correct?");
        equal(testing.current, current, "Current set?");
        equal(testing.started, started, "Started correct?");
        equal(testing.startedSize, startedSize, "Started size?");

        //We should be adding a new rate because we have progressed another two seconds. We wont be
        //displaying the rates/remaining time because we have not yet uploaded over 20kB.
        // - Generic title with the amount uploaded.
        // - Generic content with only the filename.
        nextNow += 2000;
        current = AJS.InlineAttach.UploadProgress.DATA_MIN - 1;
        percentage = Math.round(current / file.size * 100);
        var calcRate = 1000 * (current - startedSize) / (nextNow - started);
        started = nextNow;
        startedSize = current;
        lastUpdate = nextNow;

        testing._addRate = function(rate) {
            lastRate = rate;
        };

        ok(testing._update(current) == testing, "Returned self?");
        equal(lastPercentage, percentage, "Percentage is correct?");
        equal(timerCount, ++expectedTimer, "Timer reset correctly?");
        equal(lastTitle, MockText.currentOutOfTotalSize(current, file.size), "Title set correctly?");
        equal(lastContent, file.name, "Content set correctly?");
        equal(testing.lastUpdate, lastUpdate, "Last update correct?");
        equal(testing.current, current, "Current set?");
        equal(testing.started, started, "Started correct?");
        equal(testing.startedSize, startedSize, "Started size?");
        ok(Math.abs(lastRate - calcRate) < 0.005, "Rate correctly added?");

        //We are stalling the rate calculation before we know the rate. In this state:
        // - Generic stalled title.
        // - Generic stalled content.
        nextNow += AJS.InlineAttach.UploadProgress.STALLED_TIMEOUT;
        calcRate = 1000 * (current - startedSize) / (nextNow - started);
        started = nextNow;
        startedSize = current;
        nextRate = 0;

        ok(testing._update(current) == testing, "Returned self?");
        equal(lastPercentage, percentage, "Percentage is correct?");
        equal(timerCount, ++expectedTimer, "Timer reset correctly?");
        equal(lastTitle, MockText.tr("upload.progress.title.unknown.stalled",
                MockText.currentOutOfTotalSize(current, file.size)), "Title set correctly?");
        equal(lastContent, MockText.tr("upload.file.stalled", file.name), "Content set correctly?");
        equal(testing.lastUpdate, lastUpdate, "Last update correct?");
        equal(testing.current, current, "Current set?");
        equal(testing.started, started, "Started correct?");
        equal(testing.startedSize, startedSize, "Started size?");
        ok(Math.abs(lastRate - calcRate) < 0.005, "Rate correctly added?");


        //We are moving pased the 20kB calculation limit were response time will be calculated. Note that a rate
        //calculation will not be performed because it has not been 2 seconds since the last calculation.
        // - Title with rate.
        // - Content with rate.
        nextNow += 1000;
        current = AJS.InlineAttach.UploadProgress.DATA_MIN;
        nextRate = 25784.30;

        percentage = Math.round(current / file.size * 100);
        lastUpdate = nextNow;
        var remaining = (file.size - current) / nextRate;

        //We should now be displaying the rate.
        ok(testing._update(current) == testing, "Returned self?");
        equal(lastPercentage, percentage, "Percentage is correct?");
        equal(timerCount, ++expectedTimer, "Timer reset correctly?");
        equal(lastTitle, MockText.tr("upload.progress.title.known", nextRate,
                MockText.currentOutOfTotalSize(current, file.size), remaining),
                "Title set correctly?");
        equal(lastContent, MockText.tr("upload.file.remaining", file.name,remaining),
                "Content set correctly?");
        equal(testing.lastUpdate, lastUpdate, "Last update correct?");
        equal(testing.current, current, "Current set?");
        equal(testing.started, started, "Started correct?");
        equal(testing.startedSize, startedSize, "Started size?");

        //Calling update with no arguments. Should just use the old current value and update the rate.
        // - Title with rate.
        // - Content with rate.
        nextNow += 500;
        nextRate = 48484.25;
        remaining = (file.size - current) / nextRate;

        ok(testing._update() == testing, "Returned self?");
        equal(lastPercentage, percentage, "Percentage is correct?");
        equal(timerCount, ++expectedTimer, "Timer reset correctly?");
        equal(lastTitle, MockText.tr("upload.progress.title.known", nextRate,
                MockText.currentOutOfTotalSize(current, file.size), remaining),
                "Title set correctly?");
        equal(lastContent, MockText.tr("upload.file.remaining", file.name, remaining),
                "Content set correctly?");
        equal(testing.lastUpdate, lastUpdate, "Last update correct?");
        equal(testing.current, current, "Current set?");
        equal(testing.started, started, "Started correct?");
        equal(testing.startedSize, startedSize, "Started size?");

        //Stalling the progess calculation with known rate.
        // - Stalled title with rate.
        // - Stalled content.
        nextNow += AJS.InlineAttach.UploadProgress.STALLED_TIMEOUT;
        nextRate = 33738739.74748;

        calcRate = 1000 * (current - startedSize) / (nextNow - started);
        started = nextNow;
        startedSize = current;

        ok(testing._update(current) == testing, "Returned self?");
        equal(lastPercentage, percentage, "Percentage is correct?");
        equal(timerCount, ++expectedTimer, "Timer reset correctly?");
        equal(lastTitle, MockText.tr("upload.progress.title.known.stalled", nextRate,
                MockText.currentOutOfTotalSize(current, file.size)),
                "Title set correctly?");
        equal(lastContent, mockTr("upload.file.stalled", file.name),
                "Content set correctly?");
        equal(testing.lastUpdate, lastUpdate, "Last update correct?");
        equal(testing.current, current, "Current set?");
        equal(testing.started, started, "Started correct?");
        equal(testing.startedSize, startedSize, "Started size?");
        ok(Math.abs(lastRate - calcRate) < 0.005, "Rate correctly added?");


        //Move to the end of the file. In this state:
        // - No timer is re-started.
        // - Remaining hardcoded to 1 sec.
        // - Title with rate and remaining information.
        // - Content with remaining information.
        nextNow += 60000;
        nextRate = 473482743823.338383;
        current = file.size;

        percentage = 100;
        calcRate = 1000 * (current - startedSize) / (nextNow - started);
        started = nextNow;
        startedSize = current;
        remaining = 1;
        lastUpdate = nextNow;

        ok(testing._update(current) == testing, "Returned self?");
        equal(lastPercentage, percentage, "Percentage is correct?");
        equal(timerCount, expectedTimer, "Timer not reset correctly?");
        equal(lastTitle, MockText.tr("upload.progress.title.known", nextRate,
                MockText.currentOutOfTotalSize(current, file.size), remaining),
                "Title set correctly?");
        equal(lastContent, MockText.tr("upload.file.remaining", file.name,remaining),
                "Content set correctly?");
        equal(testing.lastUpdate, lastUpdate, "Last update correct?");
        equal(testing.current, current, "Current set?");
        equal(testing.started, started, "Started correct?");
        equal(testing.startedSize, startedSize, "Started size?");
        ok(Math.abs(lastRate - calcRate) < 0.005, "Rate correctly added?");
    });

    module("ProgressBar");

    test("Constructor", function() {
        var idRegex = /upload-progress-(\d+)/;

        var testing = new AJS.InlineAttach.ProgressBar();
        equal(testing.old , 0, "Old value is zero.");
        ok(testing.hidden, "The progress state is hidden?");
        ok(contains(testing.$element, testing.$progress), "The progress bar is in the element");
        var lastId = testing.$progress.attr("id");
        ok(idRegex.test(lastId), "Id Element set");

        testing = new AJS.InlineAttach.ProgressBar();
        equal(testing.old , 0, "Old value is zero.");
        ok(testing.hidden, "The progress state is hidden?");
        ok(contains(testing.$element, testing.$progress), "The progress bar is in the element");
        var id = testing.$progress.attr("id");
        ok(idRegex.test(id), "Id Element set");
        notEqual(id, lastId, "Ids should not have matched.");

    });

    test("Value", function() {
        var testing = new AJS.InlineAttach.ProgressBar();

        var lastValue, lastOptions, showCalled = 0, progressCalled = 0, fadeOut = 0;
        var expectedOptions = {showPercentage: false, height: "2px"};
        testing.$progress = {
            show: function() {
                showCalled++;
            },
            progressBar: function(value, options) {
                lastValue = value;
                lastOptions = options;
                progressCalled++;
            },
            fadeOut: function() {
                fadeOut++;
            }
        };

        testing.value(-1);
        equal(showCalled, 1, "Show was called.");
        equal(progressCalled, 0, "Progress should not have been called.");
        equal(fadeOut, 0, "Fade out should not have been called.");

        testing.value(0);
        equal(showCalled, 1, "Should only be called once.");
        equal(progressCalled, 0, "Progress should not have been called.");
        equal(fadeOut, 0, "Fade out should not have been called.");

        testing.value(1);
        equal(showCalled, 1, "Should only be called once.");
        equal(progressCalled, 1, "Progress called?.");
        deepEqual(lastOptions, expectedOptions, "Correct options?");
        equal(lastValue, 1, "Last value correctly updated?");
        equal(fadeOut, 0, "Fade out should not have been called.");

        testing.value(1);
        equal(showCalled, 1, "Should only be called once.");
        equal(progressCalled, 1, "Progress not called?.");
        equal(fadeOut, 0, "Fade out should not have been called.");

        testing.value(101);
        equal(showCalled, 1, "Should only be called once.");
        equal(progressCalled, 2, "Progress not called?.");
        deepEqual(lastOptions, expectedOptions, "Correct options?");
        equal(lastValue, 100, "Last value correctly updated?");
        equal(fadeOut, 1, "Fade should be called at 100%.");
    });

    module("FileInput");

    test("Contructor", function() {

        var parent = {};
        var parentFunc = function() { return parent; };

        var element = [{}];
        element.parent = parentFunc;
        var testing = new AJS.InlineAttach.FileInput(element, true);
        equal(testing.multiple, false, "Multiple set correctly?");

        element = [{
            files: {}
        }];
        
        var lastAttr, lastValue;

        element.attr = function(attr, value) {
            lastAttr = attr;
            lastValue = value;
        };
        element.parent = parentFunc;

        testing = new AJS.InlineAttach.FileInput(element);
        equal(lastAttr, undefined, "Attribute not set?");
        equal(lastValue, undefined, "Value not set?");
        equal(testing.multiple, false, "Multiple set correctly?");
        equal(testing.$container, parent, "Parent set correctly?");

        testing = new AJS.InlineAttach.FileInput(element, true);
        equal(lastAttr, "multiple", "Attribute set?");
        equal(lastValue, "multiple", "Value set?");
        equal(testing.multiple, true, "Multiple set correctly?");
        equal(testing.$container, parent, "Parent set correctly?");
    });

    test("onChange", function() {

        var element = {};
        element.parent = function() { return element; };

        var testing = new AJS.InlineAttach.FileInput(element);
        var fn;
        testing.$element.change = function(actualFn) {
            fn = actualFn;
        };
        testing.getFileName = function() {
            return "FileName";
        };

        var scope, argument;
        var returnValue = testing.onChange(function(a) {
            scope = this;
            argument = a;
        });

        equal(returnValue, testing, "Returned self?");

        //Call without files.
        fn.call({});
        equal(scope, testing, "Correct callback scope?");
        equal(argument, "FileName", "Correct argument?");

        //Call with but still in single mode.
        fn.call({files: "Files"});
        equal(scope, testing, "Correct callback scope?");
        equal(argument, "FileName", "Correct argument?");

        //Call when in multiple mode with files.
        testing.multiple = true;
        fn.call({files: "Files"});
        equal(scope, testing, "Correct callback scope?");
        equal(argument, "Files", "Correct argument?");
    });

    test("cloneInput", function(){
        var count = 0;
        var element = function() {
            this.count = count++;
            this.bound = true;

            this.clone = function() {
                return new element();
            };
            this.replaceWith = function(e) {
                this.replace = e;
            };
            this.parent = function() {
                return this;
            };
            this.unbind = function() {
                this.bound = false;
                return this;
            };
        };

        var elem = new element();
        var testing = new AJS.InlineAttach.FileInput(elem);

        var old = testing.cloneInput();
        equal(elem, old, "Should have returned the old element.");
        equal(testing.$element.count, elem.count + 1, "Created a new element");
        equal(old.replace, testing.$element, "Replaced correct element?");
        ok(!old.bound, "Should have been unbound.");
    });

    test("getFileName", function(){

        var nextVal, ie = false;
        var element = {
            val: function() {
                return nextVal;
            },
            parent: recordfn()
        };

        var testing = new AJS.InlineAttach.FileInput(element);
        testing._isIE = function() {
            return ie;
        };

        nextVal = "abc";
        equal(testing.getFileName(), nextVal, "abc not changed?");
        nextVal = "c:\\fakepATH\\abc";
        equal(testing.getFileName(), "abc", "c:\\fakepATH\\abc changed?");
        nextVal = "c:\\fakepath\\";
        equal(testing.getFileName(), "c:\\fakepath\\", "c:\\fakepath\\ not changed when last?");
        nextVal = "c:\\fakepath\\c\\drive.txt";
        equal(testing.getFileName(), "c\\drive.txt", "\\ not cut when when not IE");
        ie = true;
        equal(testing.getFileName(), "drive.txt", "\\ cut when when IE");
    });

    module("Test AjaxPresenter",{
        setup: function() {
            this.form = AJS.InlineAttach.Form;
            this.fileInput = AJS.InlineAttach.FileInput;
            this.text = AJS.InlineAttach.Text;
            this.t = AJS.InlineAttach.Timer;
            this.ajax = AJS.InlineAttach.AjaxUpload;
            this.max = AJS.InlineAttach.MAX_UPLOADS;

            AJS.InlineAttach.Text = MockText;
            AJS.InlineAttach.MAX_UPLOADS = 2;
            AJS.InlineAttach.FileInput = MockFileInput;
            AJS.InlineAttach.Form = MockForm;
        },
        teardown: function() {
            AJS.InlineAttach.Form = this.form;
            AJS.InlineAttach.FileInput = this.fileInput;
            AJS.InlineAttach.Text = this.text;
            AJS.InlineAttach.Timer = this.t;
            AJS.InlineAttach.AjaxUpload = this.ajax;
            AJS.InlineAttach.MAX_UPLOADS = this.max;
        }
    });

    test("isSupported", function() {
        AJS.InlineAttach.AjaxUpload.isSupported = failfn("isSupported");
        ok(!AJS.InlineAttach.AjaxPresenter.isSupported(), "No argument is not supported?");
        ok(!AJS.InlineAttach.AjaxPresenter.isSupported(null), "Null argument is not supported?");
        ok(!AJS.InlineAttach.AjaxPresenter.isSupported([]), "Empty not supported.");
        ok(!AJS.InlineAttach.AjaxPresenter.isSupported([{}]), "No files not supported.");

        var ausupport = false;
        AJS.InlineAttach.AjaxUpload.isSupported = function() {
            return ausupport;
        };

        ok(!AJS.InlineAttach.AjaxPresenter.isSupported([{files: "files"}]), "Not supported when AJAX upload is not supported.");

        ausupport = true;
        ok(AJS.InlineAttach.AjaxPresenter.isSupported([{files: "files"}]), "Supported?");
    });

    test("Constructor", function() {

        var element = {};
        var testing = new AJS.InlineAttach.AjaxPresenter(element);

        equal(testing.form.fileSelector.element, element, "File input correctly wrapped!");
        ok(testing.form.fileSelector.multiple, "Multiple files added?");
    });

    test("Cancel", function() {
        var element = {};
        var testing = new AJS.InlineAttach.AjaxPresenter(element);

        testing.form.disable();
        testing._cancel();
        ok(!testing.form.disabled, "Form should not be disabled.");
    });

    test("_attach", function() {
        var element = {};
        var testing = new AJS.InlineAttach.AjaxPresenter(element);
        var form = testing.form;
        var selector = form.fileSelector;

        var callCount = 0;
        var checkCalledState = function() {
            equal(form.clearErrors.count, ++callCount, "Called clear errors?");
            equal(selector.clear.count, callCount, "Called clear?");
            equal(selector.focus.count, callCount, "Called focus?");
        };

        var processfn = testing._uploadFiles = failfn("_uploadFiles");
        var filterfn = testing._checkAndFilterFiles = failfn("_checkAndFilterFiles");

        //Calling with nothing do just clear the current errors.
        testing._attach();
        checkCalledState();
        ok(!form.disabled, "Form still enabled?");

        //Calling wiith empty list should also do nothing.
        testing._attach([]);
        checkCalledState();

        //What happens when there all the files have errors?
        var files = [new MockFile("name", 3637)];
        filterfn = testing._checkAndFilterFiles = recordfn(null);
        testing._attach(files);
        checkCalledState();
        equal(filterfn.count, 1, "_checkAndFilterFiles called?");
        deepEqual(filterfn.lastArgs, [files], "_checkAndFilterFiles called with correct arguments?");

        //What when there is some good files.
        files = [new MockFile("name", 3637), new MockFile("name2", 38383829)];
        var goodFiles = files.slice(0, 1);
        processfn = testing._uploadFiles = recordfn(null);
        filterfn = testing._checkAndFilterFiles = recordfn(goodFiles);

        testing._attach(files);
        checkCalledState();
        equal(filterfn.count, 1, "_checkAndFilterFiles called?");
        equal(processfn.count, 1, "_uploadFiles called?");
        deepEqual(filterfn.lastArgs, [files], "_checkAndFilterFiles called with correct arguments?");
        deepEqual(processfn.lastArgs, [goodFiles], "_uploadFiles called with correct arguments?");
    });

    test("_checkAndFilterFiles", function() {
        var element = {};
        var testing = new AJS.InlineAttach.AjaxPresenter(element);
        var form = testing.form;
        var tooBig = form.maxSize + 1;

        //No valid files.
        var zeroFile = new MockFile("zero", 0);
        var tooBigFile = new MockFile("big", tooBig);
        var files = [zeroFile, tooBigFile];
        var newFiles = testing._checkAndFilterFiles(files);
        deepEqual(form.errors, ["upload.empty.file,zero", "upload.too.big,big," + tooBig.toFixed(4) + "," + form.maxSize.toFixed(4)], "Errors correctly added?");
        equal(newFiles, null, "Null returned for no good files?");

        //Some valid files.
        form.errors = [];
        var maxFile = new MockFile("size", form.maxSize);
        var smallFile = new MockFile("size", 1);
        var middleFile = new MockFile("size", form.maxSize / 2);
        files = [smallFile, zeroFile, middleFile, tooBigFile, maxFile];
        newFiles = testing._checkAndFilterFiles(files);
        deepEqual(form.errors, ["upload.empty.file,zero", "upload.too.big,big," + tooBig.toFixed(4) + "," + form.maxSize.toFixed(4)], "Errors correctly added?");
        deepEqual(newFiles, [smallFile.toSimple(), middleFile.toSimple(), maxFile.toSimple()], "Good files returned?");

        files = [];
        var expectedFiles = [];
        for (var i = 0; i < AJS.InlineAttach.AjaxPresenter.MAX_SELECTED_FILES; i++) {
            var newFile = new MockFile("File" + i, 56);
            files.push(newFile);
            expectedFiles.push(newFile.toSimple());
        }

        //Some lets test the maximum number of files to add.
        form.errors = [];
        newFiles = testing._checkAndFilterFiles(files);
        deepEqual(form.errors, [], "No errors added?");
        deepEqual(newFiles, expectedFiles, "Good files returned?");

        //Lets test when we try to attach too many files.
        files.push(new MockFile("FileThatBrokeTheCamelsBack", form.maxSize + 1000));
        newFiles = testing._checkAndFilterFiles(files);
        deepEqual(form.errors, [MockText.tr("upload.error.too.many.files", files.length, files.length - 1)], "Too many error added?");
        equal(newFiles, null, "No files returned?");
    });

    test("_createSubmitData", function() {
        var element = {};
        var testing = new AJS.InlineAttach.AjaxPresenter(element);
        var form = testing.form;

        form.projectId = 38383838302020;
        var expectedData = { projectId: form.projectId, atl_token: form.nextToken, formToken: form.formToken };
        deepEqual(testing._createSubmitData(), expectedData, "Generated correct data?");

        form.issueId = 5858658;
        expectedData = { issueId: form.issueId, atl_token: form.nextToken, formToken: form.formToken };
        deepEqual(testing._createSubmitData(), expectedData, "Generated correct data?");

        delete form.projectId;
        expectedData = { issueId: form.issueId, atl_token: form.nextToken, formToken: form.formToken };
        deepEqual(testing._createSubmitData(), expectedData, "Generated correct data?");

        //No issue or project.
        delete form.issueId;
        try {
            testing._createSubmitData();
            ok(false, "Did not get exception on bad state!");
        } catch (e) {
            //good.
        }
    });

    test("_checkStatus", function() {
        var element = {};
        var testing = new AJS.InlineAttach.AjaxPresenter(element);
        var file = {name: "name"};

        equal(testing._getErrorFromStatus(400, file), MockText.tr("upload.error.badrequest", file.name),
                "Error for bad request?");
        equal(testing._getErrorFromStatus(401, file), MockText.tr("upload.error.auth", file.name),
                "Error for bad auth?");
        equal(testing._getErrorFromStatus(0, file), MockText.tr("upload.error.server.no.reply", file.name),
                "Error for bad auth?");

        var status = 40383;
        equal(testing._getErrorFromStatus(status, file), MockText.tr("upload.error.unknown.status", file.name, status),
                "Error for unexpected status?");
        status = 475858;
        equal(testing._getErrorFromStatus(status, file), MockText.tr("upload.error.unknown.status", file.name, status),
                "Error for unexpected status?");
    });

    test("_uploadFiles", function() {
        var element = {};
        var testing = new AJS.InlineAttach.AjaxPresenter(element);
        var form = testing.form;
        var data = {data: true};
        var realOne = new MockFile("one", 1);
        var one = realOne.toSimple();
        var two = new MockFile("two", 2).toSimple();
        var three = new MockFile("three", 3).toSimple();

        testing._createSubmitData = recordfn(data);
        testing._getErrorFromStatus = failfn("_checkStatus");

        var addReturn = true;
        var uploadsAdded = [];
        testing._addUpload = function(upload) {
            uploadsAdded.push(upload);
            return addReturn;
        };

        var finishReturn = false;
        var uploadsFinished = [];
        testing._finishUpload = function(upload) {
            uploadsFinished.push(upload);
            return finishReturn;
        };

        var uploadsCreated = [];
        AJS.InlineAttach.AjaxUpload = function(obj) {
            this.running = false;

            uploadsCreated.push(this);

            var scope = obj.scope;
            equal(scope, testing, "Correct scope for AJAXUpload?");
            for (var key in obj) {
                //qunit does not deal with circular references when doing a toString. Scope needs to be removed.
                if (key !== "scope") {
                    var value = obj[key];
                    if (value.apply && value.call) {
                        this[key] = AJS.$.proxy(value, scope);
                    } else {
                        this[key] = value;
                    }
                }
            }
            this.upload = function() {
                this.running = true;
            },
            this.abort = function() {
                this.aborted = true;
            }
        };

        var timersCreated = [];
        AJS.InlineAttach.Timer = function(fn) {
            this.fn = fn;
            this.cancel = recordfn(this);
            this.schedule = recordfn(this);
            this.trigger = function() {
                this.fn.call(this);
            };
            timersCreated.push(this);
        };

        var getNewUploads = function() {
            var size = timersCreated.length;
            var uploads = [];
            equal(uploadsCreated.length, size, "Created Uploads?");
            equal(form.progress.length, size, "Created progress bars?");

            for (var i = 0; i < size; i++) {
                uploads.push({
                    timer: timersCreated[i],
                    upload: uploadsCreated[i],
                    progress: form.progress[i]
                });
            }
            timersCreated = [];
            uploadsCreated = [];
            form.progress = [];

            return uploads;
        };

        var assertInitialUploadState = function(uploads, files, successful) {
            for (var i = 0; i < uploads.length; i++) {
                var u = uploads[i];
                var f = files[i];
                equal(u.progress.hidden, true, "Is Progress Hidden?");
                equal(u.progress.started, false, "Progress not started!");
                equal(u.progress.value, Infinity, "No value?");
                equal(u.upload.file, f.file, "Correct File?");
                deepEqual(u.upload.params, AJS.$.extend({filename: f.name, size: f.size}, data), "Same params?");
                equal(u.upload.url, AJS.InlineAttach.AjaxPresenter.DEFAULT_URL, "Correct URL?");

                if (successful === undefined || successful)
                {
                    equal(u.timer.schedule.count, 1, "Timer called?");
                    ok(!u.timer.cancel.count, "Timer not cancelled called?");
                    deepEqual(u.timer.schedule.lastArgs, [AJS.InlineAttach.DISPLAY_WAIT],
                            "Timer started with correct timeout?");
                }
            }
        };

        var assertAdded = function(uploads) {
            var running = [];
            for (var i = 0; i < uploads.length; i++) {
                var up = uploads[i].upload;
                running.push(up);
            }
            deepEqual(uploadsAdded, running, "Added expected uploads?");
            uploadsAdded = [];
        };

        var assertFinished = function(uploads) {
            var finished = [];
            for (var i = 0; i < uploads.length; i++) {
                var up = uploads[i].upload;
                finished.push(up);
            }
            deepEqual(uploadsFinished, finished, "Expected uploads finished?");
            uploadsFinished = [];
        };

        //
        // Add one file. It should be immediately uploaded and the form disabled.
        //

        var newFiles = [one];
        testing._uploadFiles(newFiles);
        var uploads = getNewUploads();

        equal(uploads.length, 1, "Added the upload?");
        var up = uploads[0];
        assertInitialUploadState(uploads, newFiles);
        assertAdded(uploads);
        assertFinished([]);
        ok(form.disabled, "Is form disabled?");

        //Triggering the timer should display the progress bar.
        up.timer.trigger();
        ok(!up.progress.hidden, "Progress bar shown after timeout.");
        //Does the progress get displayed?
        up.upload.before();
        ok(up.progress.started, "Progress should have started.");
        up.upload.progress(5);
        equal(up.progress.value, 5, "Progress reported?");

        //Check a successful AJAX request with server errors.
        up.upload.success({errorMessage: "Error"}, 304);
        deepEqual(form.errors, ["Error|" + one.name], "Error correctly reported?");
        equal(form.lastReplace, up.progress, "Progress bar replaced with error?");

        //Check a successful AJAX request with server errors + XHR token.
        form.errors = [];
        up.upload.success({errorMessage: "Token Error", token: "someKindOfToken"}, 304);
        deepEqual(form.errors, ["Token Error|" + one.name], "Error correctly reported?");
        equal(form.lastReplace, up.progress, "Progress bar replaced with error?");
        equal(form.lastToken, "someKindOfToken", "Was the token replaced?");

        //Check a successful AJAX request with unexpected JSON. Get error from status.
        var status = testing._getErrorFromStatus = recordfn("Status Error");
        form.errors = [];
        up.upload.success({error: "Error"}, 405);
        deepEqual(form.errors, ["Status Error"], "Error correctly reported?");
        equal(form.lastReplace, up.progress, "Progress bar replaced with error?");
        deepEqual(status.lastArgs, [405, one], "Error from status?");
        equal(status.count, 1, "Error from status?");

        //Check a successful AJAX good return.
        form.errors = [];
        up.upload.success({id: 1, name: "name"}, 201);
        deepEqual(form.errors, [], "No errors to report?");
        deepEqual(form.addTemporaryFileCheckbox.lastArgs,
                [1, "name", up.progress, realOne], "Added temporary checkbox?");

        //Check successful AJAX with unknown response.
        form.errors = [];
        up.upload.success({id: 1234}, 201);
        deepEqual(form.errors, [MockText.tr("upload.error.bad.response", one.name)], "Error correctly reported?");
        equal(form.lastReplace, up.progress, "Progress bar replaced with error?");

        form.errors = [];
        up.upload.success({name: "name"}, 201);
        deepEqual(form.errors, [MockText.tr("upload.error.bad.response", one.name)], "Error correctly reported?");
        equal(form.lastReplace, up.progress, "Progress bar replaced with error?");

        //Check bad AJAX with status error
        status = testing._getErrorFromStatus = recordfn("Bad Status Found");
        form.errors = [];
        up.upload.error("Bad Ajax Request", 204);
        deepEqual(form.errors, ["Bad Status Found"], "Error correctly reported?");
        equal(form.lastReplace, up.progress, "Progress bar replaced with error?");
        deepEqual(status.lastArgs, [204, one], "Error from status?");
        equal(status.count, 1, "Error from status?");

        //Check bad client error
        status = testing._getErrorFromStatus = failfn("_getErrorFromStatus");
        form.errors = [];
        up.upload.error("Client Error", -1);
        deepEqual(form.errors, ["Client Error"], "Error from client correctly reported?");
        equal(form.lastReplace, up.progress, "Progress bar replaced with error?");

        //Check that the cancel aborts the upload.
        up.progress.triggerCancel();
        ok(up.upload.aborted, "Was aborted?");

        //Check what happens when the upload finishes but there are still other uploads.
        finishReturn = true;
        up.upload.after();
        equal(up.timer.cancel.count, 1, "Timer cancelled?");
        ok(up.progress.finished, "Progress finished?");
        ok(up.progress.removed, "Progress removed?");
        assertFinished(uploads);
        ok(form.disabled, "Form not enabled becasue there are still running builds.");

        //Check what happens when the upload finishes and there are no running uploads.
        finishReturn = false;
        up.upload.after();
        equal(up.timer.cancel.count, 2, "Timer cancelled?");
        ok(up.progress.finished, "Progress finished?");
        ok(up.progress.removed, "Progress removed?");
        assertFinished(uploads);
        ok(!form.disabled, "Form enabled after completion.");

        //
        //Make sure the form is not enabled when uploads cannot be added.
        //
        addReturn = false;
        form.disabled = false;
        newFiles = [one, two, three];
        testing._uploadFiles(newFiles);
        uploads = getNewUploads();

        equal(uploads.length, 3, "Added the upload?");
        assertInitialUploadState(uploads, newFiles, false);
        assertAdded(uploads);
        assertFinished([]);
        ok(!form.disabled, "Is form enabled?");

        //
        // Make sure that cancelling works.
        //
        addReturn = true;
        finishReturn = false;
        form.errors = [];
        form.addTemporaryFileCheckbox.count = 0;
        newFiles = [one];
        testing._uploadFiles(newFiles);
        uploads = getNewUploads();

        equal(uploads.length, 1, "Added the upload?");
        assertInitialUploadState(uploads, newFiles);
        assertAdded(uploads);
        assertFinished([]);
        up = uploads[0];
        ok(form.disabled, "Is form disabled?");

        testing.cancelled = true;

        up.timer.trigger();
        //Progress bar should not display when cancelled.
        ok(up.progress.hidden, "Don't display progress bar when hidden?");
        up.upload.before();
        //Progress bar should not be called when cancelled.
        ok(!up.progress.started, "Progress should not have started.");
        up.upload.progress(5);
        equal(up.progress.value, Infinity, "Progress ignored reported?");

        //All JSON returned is ignored when cancelled
        up.upload.success({id: 1, name: "name"});
        equal(form.addTemporaryFileCheckbox.count, 0, "Didn't add temporary checkbox?");

        //Errors ignored when cancelled.
        up.upload.error("Bad Ajax Request");
        deepEqual(form.errors, [], "Didn't add error?");

        //Finished upload should cleanup the UI and nothing else.
        up.upload.after();
        equal(up.timer.cancel.count, 1, "Timer cancelled?");
        ok(up.progress.finished, "Progress finished?");
        ok(up.progress.removed, "Progress removed?");
        assertFinished([]);
    });

    module("Test FormPresenter",{
        setup: function() {
            this.form = AJS.InlineAttach.Form;
            this.fileInput = AJS.InlineAttach.FileInput;
            this.text = AJS.InlineAttach.Text;
            this.t = AJS.InlineAttach.Timer;
            this.upload = AJS.InlineAttach.FormUpload;
            this.max = AJS.InlineAttach.MAX_UPLOADS;

            AJS.InlineAttach.Text = MockText;
            AJS.InlineAttach.MAX_UPLOADS = 2;
            AJS.InlineAttach.FileInput = MockFileInput;
            AJS.InlineAttach.Form = MockForm;
        },
        teardown: function() {
            AJS.InlineAttach.Form = this.form;
            AJS.InlineAttach.FileInput = this.fileInput;
            AJS.InlineAttach.Text = this.text;
            AJS.InlineAttach.Timer = this.t;
            AJS.InlineAttach.FormUpload = this.upload;
            AJS.InlineAttach.MAX_UPLOADS = this.max;
        }
    });

    test("Constructor", function() {
        var element = {};
        var testing = new AJS.InlineAttach.FormPresenter(element);

        equal(testing.form.fileSelector.element, element, "File input correctly wrapped!");
        ok(!testing.form.fileSelector.multiple, "Simple files only?");
    });

    test("Cancel", function() {
        var element = {};
        var testing = new AJS.InlineAttach.AjaxPresenter(element);

        testing.form.disable();
        testing._cancel();
        ok(!testing.form.disabled, "Form should not be disabled.");
    });

    test("_createSubmitData", function() {
        var element = {};
        var testing = new AJS.InlineAttach.FormPresenter(element);
        var form = testing.form;

        form.projectId = 38383838302020;
        var expectedData = { projectId: form.projectId, atl_token: form.nextToken, create: true, formToken: form.formToken};
        deepEqual(testing._createSubmitData(), expectedData, "Generated correct data?");

        form.issueId = 5858658;
        expectedData = { id: form.issueId, atl_token: form.nextToken, formToken: form.formToken};
        deepEqual(testing._createSubmitData(), expectedData, "Generated correct data?");

        delete form.projectId;
        expectedData = { id: form.issueId, atl_token: form.nextToken, formToken: form.formToken};
        deepEqual(testing._createSubmitData(), expectedData, "Generated correct data?");

        //No issue or project.
        delete form.issueId;
        try {
            testing._createSubmitData();
            ok(false, "Did not get exception on bad state!");
        } catch (e) {
            //good.
        }
    });

    test("_attach", function() {
        var element = {$element: {}};
        var testing = new AJS.InlineAttach.FormPresenter(element);
        var form = testing.form;
        var data = {data: true};

        testing._createSubmitData = recordfn(data);

        var uploadsCreated = [];
        AJS.InlineAttach.FormUpload = function(obj) {
            uploadsCreated.push(this);

            var scope = obj.scope;
            equal(scope, testing, "Correct scope for FormUpload?");
            for (var key in obj) {
                //qunit does not deal with circular references when doing a toString. Scope needs to be removed.
                if (key !== "scope") {
                    var value = obj[key];
                    if (value && value.apply && value.call) {
                        this[key] = AJS.$.proxy(value, scope);
                    } else {
                        this[key] = value;
                    }
                }
            }

            this.abort = recordfn("abort");
        };

        var uploadsAdded = [];
        testing._addUpload = function(upload) {
            uploadsAdded.push(upload);
            return arguments.callee.returnValue;
        };
        testing._addUpload.returnValue = true;

        var uploadsFinished = [];
        testing._finishUpload = function(upload) {
            uploadsFinished.push(upload);
            return arguments.callee.returnValue;
        };
        testing._finishUpload.returnValue = false;

        var assertAdded = function(uploads) {
            var running = [];
            for (var i = 0; i < uploads.length; i++) {
                var up = uploads[i].upload;
                running.push(up);
            }
            deepEqual(uploadsAdded, running, "Added expected uploads?");
            uploadsAdded = [];
        };

        var assertFinished = function(uploads) {
            var finished = [];
            for (var i = 0; i < uploads.length; i++) {
                var up = uploads[i].upload;
                finished.push(up);
            }
            deepEqual(uploadsFinished, finished, "Expected uploads finished?");
            uploadsFinished = [];
        };

        var timersCreated = [];
        AJS.InlineAttach.Timer = function(fn, scope) {
            equal(scope, testing, "Correct timer scope?");
            this.fn = fn;
            this.cancel = recordfn(this);
            this.schedule = recordfn(this);
            this.trigger = function() {
                this.fn.call(scope, this);
            };
            timersCreated.push(this);
        };

        var getNewUploads = function() {
            var size = timersCreated.length;
            var uploads = [];
            equal(uploadsCreated.length, size, "Created Uploads?");
            equal(form.progress.length, size, "Created progress bars?");

            for (var i = 0; i < size; i++) {
                uploads.push({
                    timer: timersCreated[i],
                    upload: uploadsCreated[i],
                    progress: form.progress[i]
                });
            }
            timersCreated = [];
            uploadsCreated = [];
            form.progress = [];

            return uploads;
        };

        var addCount = 0;
        var addAndAssertFile = function(fileName, successful) {
            successful = successful === undefined || successful;

            addCount++;
            var errorCount = form.clearErrors.count + 1 || 1;

            testing._attach(fileName);
            var uploads = getNewUploads();
            assertAdded(uploads);
            equal(uploads.length, 1, "Created one upload?");
            var up = uploads[0];
            equal(form.clearErrors.count, errorCount, "Cleared the errors?");
            equal(form.fileSelector.focus.count, addCount, "Focused the element?");

            equal(up.progress.hidden, true, "Is Progress Hidden?");
            equal(up.progress.started, false, "Is Progress started?");
            deepEqual(up.upload.$input, {count: addCount, cloneInput: true}, "Correct input?");
            deepEqual(up.upload.params, data, "Same params?");
            equal(up.upload.url, AJS.InlineAttach.FormPresenter.DEFAULT_URL, "Correct URL?");

            if (successful) {
                equal(up.timer.schedule.count, 1, "Timer called?");
                ok(!up.timer.cancel.count, "Timer not cancelled called?");
                deepEqual(up.timer.schedule.lastArgs, [AJS.InlineAttach.DISPLAY_WAIT],
                        "Timer started with correct timeout?");
            }
            equal(form.disabled, successful, "Form in correct state?");
            return up
        };

        //Add one upload.
        var fileName = "file";
        var up = addAndAssertFile(fileName);

        //Make sure the progress is shown when the timer tiggers.
        up.timer.trigger();
        equal(up.progress.hidden, false, "Progress shown?");

        //Make sure the progress is started on a call to before.
        up.upload.before();
        equal(up.progress.started, true, "Progress started?");

        //Check a successful AJAX request with server errors.
        up.upload.success({errorMsg: "Error"});
        deepEqual(form.errors, ["Error|" + fileName], "Error correctly reported?");
        equal(form.lastReplace, up.progress, "Progress bar replaced with error?");

        //Check a successful AJAX request with unexpected JSON.
        form.errors = [];
        up.upload.success({error: "Error"});
        deepEqual(form.errors, [AJS.InlineAttach.Text.tr("upload.error.bad.response", fileName)], "Error correctly reported?");
        equal(form.lastReplace, up.progress, "Progress bar replaced with error?");

        //Check a successful AJAX good return.
        form.errors = [];
        up.upload.success({id: 1, name: "name"});
        deepEqual(form.errors, [], "No errors to report?");
        deepEqual(form.addTemporaryFileCheckbox.lastArgs,
                [1, "name", up.progress], "Added temporary checkbox?");

        //Check unknown error from server.
        form.errors = [];
        //Check bad AJAX.
        up.upload.error("Bad Ajax Request");
        deepEqual(form.errors, [AJS.InlineAttach.Text.tr("upload.error.unknown", fileName)], "Error correctly reported?");
        equal(form.lastReplace, up.progress, "Progress bar replaced with error?");

        //Check XSRF error.
        form.errors = [];
        up.upload.error("ssssssSecurityTokenMissingssss");
        deepEqual(form.errors, [AJS.InlineAttach.Text.tr("upload.xsrf.timeout", fileName)], "Error correctly reported?");
        equal(form.lastReplace, up.progress, "Progress bar replaced with error?");

        //Check what happens when the upload finishes.
        up.upload.after();
        assertFinished([up]);
        equal(up.timer.cancel.count, 1, "Timer cancelled?");
        ok(up.progress.removed, "Progress removed?");
        ok(!form.disabled, "Form enabled after completion.");

        //
        //Does the cancel link work?
        //
        fileName = "file2.txt";
        up = addAndAssertFile(fileName);
        up.progress.triggerCancel();
        equal(up.upload.abort.count, 1, "Abort was called?");

        //
        // Check what happens when adding the upload does not work. Basically
        //
        testing._addUpload.returnValue = false;
        testing.form.enable();
        fileName = "WontAttachMe";
        up = addAndAssertFile(fileName, false);
        up = addAndAssertFile(fileName, false);
        testing._addUpload.returnValue = true;

        //Check what happens when the upload finishes but there are still other uploads.
        up = addAndAssertFile(fileName, true);
        testing._finishUpload.returnValue = true;
        up.upload.after();
        assertFinished([up]);
        ok(form.disabled, "Form not enabled because there are still running uploads.");
        testing._finishUpload.returnValue = false;

        //
        // Make sure that cancelling works.
        //
        form.addTemporaryFileCheckbox.count = 0;
        form.errors = [];
        up = addAndAssertFile("cancel");
        testing.cancelled = true;

        up.timer.trigger();
        //Progress bar should not display when cancelled.
        ok(up.progress.hidden, "Don't display progress bar when cancelled?");
        up.upload.before();
        ok(!up.progress.started, "Don't start progress bar when cancelled?");

        //All JSON returned is ignored when cancelled
        up.upload.success({id: 1, name: "name"});
        equal(form.addTemporaryFileCheckbox.count, 0, "Didn't add temporary checkbox?");

        //Errors ignored when cancelled.
        up.upload.error("Bad Ajax Request");
        deepEqual(form.errors, [], "Didn't add error?");

        //Finished upload should cleanup the UI and nothing else.
        up.upload.after();
        equal(up.timer.cancel.count, 1, "Timer cancelled?");
        ok(up.progress.removed, "Progress removed?");
    });

    module("Presenter", {
        setup: function() {
            this.max = AJS.InlineAttach.MAX_UPLOADS;
            AJS.InlineAttach.MAX_UPLOADS = 2;
        },
        teardown: function() {
            AJS.InlineAttach.MAX_UPLOADS = this.max;
        }
    });

    test("removeFromArray", function() {
        var testing = AJS.InlineAttach.Presenter.removeFromArray;
        var elems = [1, 2, 3, 4];

        equal(testing(elems, 0), null, "Remove nothing?");
        deepEqual(elems, [1, 2, 3, 4], "Remove nothing?");
        equal(testing(elems, 1), 1, "Remove 1?");
        deepEqual(elems, [2, 3, 4], "Array correctly altered?");
        equal(testing(elems, 3), 3, "Remove 3?");
        deepEqual(elems, [2, 4], "Array correctly altered?");
        equal(testing(elems, 4), 4, "Remove 4?");
        deepEqual(elems, [2], "Array correctly altered?");
        equal(testing(elems, 2), 2, "Remove 2?");
        deepEqual(elems, [], "Array correctly altered?");
        equal(testing(elems, 2), null, "Remove from empty does nothing?");
        deepEqual(elems, [], "Array not changed?");
    });

    test("_addUpload & _finishUpload", function() {

        var MockUpload = function(){
            return {
                upload: recordfn("upload"),
                abort: failfn("abort")
            };
        };

        var presenter = new AJS.InlineAttach.Presenter();
        var upload1 = new MockUpload(), upload2 = new MockUpload(), upload3 = new MockUpload(),
                upload4 = new MockUpload();

        //Add upload
        equal(presenter._addUpload(upload1), true, "Correct return?");
        deepEqual(presenter.waiting, [], "No Waiting uploads.");
        deepEqual(presenter.running, [upload1], "Running uploads?");
        equal(upload1.upload.count, 1, "Upload called?");

        //Add upload.
        equal(presenter._addUpload(upload2), true, "Correct return?");
        deepEqual(presenter.waiting, [], "No Waiting uploads.");
        deepEqual(presenter.running, [upload1, upload2], "Running uploads?");
        equal(upload1.upload.count, 1, "Upload called?");
        equal(upload2.upload.count, 1, "Upload called?");

        //Add Upload but this time queue.
        equal(presenter._addUpload(upload3), true, "Correct return?");
        deepEqual(presenter.waiting, [upload3], "Waiting uploads?");
        deepEqual(presenter.running, [upload1, upload2], "Running uploads?");
        equal(upload1.upload.count, 1, "Upload called?");
        equal(upload2.upload.count, 1, "Upload called?");
        equal(upload3.upload.count, 0, "Upload not called?");

        //Remove running upload. A queued one should be triggered.
        equal(presenter._finishUpload(upload1), true, "Correct return?");
        deepEqual(presenter.waiting, [], "No Waiting uploads?");
        deepEqual(presenter.running, [upload2, upload3], "Running uploads?");
        equal(upload1.upload.count, 1, "Upload called?");
        equal(upload2.upload.count, 1, "Upload called?");
        equal(upload3.upload.count, 1, "Upload called?");

        //Add another upload. It should be queued.
        equal(presenter._addUpload(upload4), true, "Correct return?");
        deepEqual(presenter.waiting, [upload4], "Waiting uploads?");
        deepEqual(presenter.running, [upload2, upload3], "Running uploads?");
        equal(upload2.upload.count, 1, "Upload called?");
        equal(upload3.upload.count, 1, "Upload called?");
        equal(upload4.upload.count, 0, "Upload not called?");

        //Removing an unrelated upload should do nothing.
        equal(presenter._finishUpload(new MockUpload()), true, "Correct return?");
        deepEqual(presenter.waiting, [upload4], "Waiting uploads not touched?");
        deepEqual(presenter.running, [upload2, upload3], "Running uploads not touched?");
        equal(upload2.upload.count, 1, "Upload called?");
        equal(upload3.upload.count, 1, "Upload called?");
        equal(upload4.upload.count, 0, "Upload not called?");

        //Removing a queued upload should not affect the state of running uploads.
        equal(presenter._finishUpload(upload4), true, "Correct return?");
        deepEqual(presenter.waiting, [], "No waiting uploads");
        deepEqual(presenter.running, [upload2, upload3], "Running uploads?");
        equal(upload2.upload.count, 1, "Upload called?");
        equal(upload3.upload.count, 1, "Upload called?");

        //Removing a running upload.
        equal(presenter._finishUpload(upload2), true, "Correct return?");
        deepEqual(presenter.waiting, [], "No waiting uploads");
        deepEqual(presenter.running, [upload3], "Running uploads?");
        equal(upload2.upload.count, 1, "Upload called?");
        equal(upload3.upload.count, 1, "Upload called?");

        //Removing last upload.
        equal(presenter._finishUpload(upload3), false, "Correct return?");
        deepEqual(presenter.waiting, [], "Not waiting uploads");
        deepEqual(presenter.running, [], "No running uploads");
        equal(upload2.upload.count, 1, "Upload called?");
        equal(upload3.upload.count, 1, "Upload called?");
    });

    test("_cancel", function() {

        var MockUpload = function(){
           return {
               upload: failfn("upload"),
               abort: recordfn("abort")
           };
        };

        var presenter = new AJS.InlineAttach.Presenter();
        var upload1 = new MockUpload(), upload2 = new MockUpload(), upload3 = new MockUpload(),
               upload4 = new MockUpload();

        presenter.running = [upload1, upload3];
        presenter.waiting = [upload4, upload2];

        equal(presenter.cancelled, false, "Not cancelled?");
        presenter._cancel();
        equal(presenter.cancelled, true, "Cancelled now?")
        deepEqual(presenter.running, [], "Nothing running?");
        deepEqual(presenter.waiting, [], "Nothing waiting?");

        equal(upload1.abort.count, 1, "Abort called?");
        equal(upload2.abort.count, 1, "Abort called?");
        equal(upload3.abort.count, 1, "Abort called?");
        equal(upload4.abort.count, 1, "Abort called?");
    });

    module("InlineAttach", {
        setup: function() {
            this.ajax = AJS.InlineAttach.AjaxPresenter;
            this.form = AJS.InlineAttach.FormPresenter;
            this.text = AJS.InlineAttach.Text;

            AJS.InlineAttach.Text = MockText;

        },
        teardown: function() {
            AJS.InlineAttach.AjaxPresenter = this.ajax;
            AJS.InlineAttach.FormPresenter = this.form;
            AJS.InlineAttach.Text = this.text;
        }
    });

    test("rescope", function() {
        var testing = AJS.InlineAttach.rescope;
        equal(testing(), AJS.$.noop, "No arg returns noop function.");
        equal(testing(null), AJS.$.noop, "Null arg return noop function.");

        var result = {};
        var scope = {};
        var fn = recordfn(result);
        var proxy = testing(fn);

        equal(proxy.call(scope), result, "Correct result?");
        equal(fn.count, 1, "Called once?");
        equal(fn.lastScope, scope, "Correct scope?");

        proxy = testing(fn, scope);
        equal(proxy.call({"never the same": 'ssj'}), result, "Correct result?");
        equal(fn.count, 2, "Called again?");
        equal(fn.lastScope, scope, "Correct scope?");        
    });

    test("copyArrayLike", function() {
        var testing = AJS.InlineAttach.copyArrayLike;
        var ArrayLike = function() {
            for (var i = 0; i < arguments.length; i++) {
                this[i] = arguments[i];
            }
            this.length = arguments.length;
        };

        deepEqual(testing(new ArrayLike()), [], "Empty works?");
        deepEqual(testing(new ArrayLike(1)), [1], "One element works?");
        deepEqual(testing(new ArrayLike(1, true, "hello")), [1, true, "hello"], "Multiple elements works?");
    });

    test("Constructor", function() {
        AJS.InlineAttach.AjaxPresenter = recordfn("ajax");
        AJS.InlineAttach.FormPresenter = recordfn("form");
        AJS.InlineAttach.AjaxPresenter.isSupported = recordfn(true);

        new AJS.InlineAttach("<div/>");
        equal(AJS.InlineAttach.AjaxPresenter.count, 1, "Ajax presenter created?");
        equal(AJS.InlineAttach.FormPresenter.count, 0, "Form presenter not created?");

        AJS.InlineAttach.AjaxPresenter.isSupported = recordfn(false);
        new AJS.InlineAttach("<div/>");
        equal(AJS.InlineAttach.AjaxPresenter.count, 1, "Ajax presenter not created?");
        equal(AJS.InlineAttach.FormPresenter.count, 1, "Form presenter created?");
    });
})(AJS.$);
