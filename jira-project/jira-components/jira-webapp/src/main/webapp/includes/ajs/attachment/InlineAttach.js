define('jira/attachment/inline-attach', [
    'jira/lib/class',
    'jira/xsrf',
    'jira/util/navigator',
    'jquery'
], function(
    Class,
    XSRF,
    Navigator,
    jQuery
) {
    /**
     * Convert a file input element into an inline file upload control.
     *
     * If possible it will use the <a href="http://www.w3.org/TR/FileAPI/">FileApi</a> to perform the uploads. This allows
     * the user to see progress and cancel individual uploads mid way through.
     *
     * If the browser does not have the FileApi it will submit uploads in the background using a form. In this mode
     * the user will not see progress.
     *
     * <h4>Use </h4>
     *
     * <h5>Markup:</h5>
     *
     * <pre>
     *   <input class="upfile " name="tempFilename" type="file">
     * </pre>
     *
     * <h5>JavaScript</h5>
     *
     * <pre>
     *
     * new InlineAttach("input:file");
     *
     *   or
     *
     * new InlineAttach(jQuery("#id"));
     *
     * </pre>
     *
     * @class InlineAttach
     * @extends Class
     */
    var InlineAttach = Class.extend({
        /**
         * Creates an inline file attach control
         *
         * @constructor
         * @param element the file input to use for uploading.
         *
         * <dl>
         *  <dt>{String, jQuery} element</dt>
         *      <dd>Element defining the file input HTML element</dd>
         *      <dd><strong>Default: <em>undefined</em></strong><dd>
         * </dl>
         *
         */
        init: function (element) {
            var $element = jQuery(element);
            if (InlineAttach.AjaxPresenter.isSupported($element)) {
                new InlineAttach.AjaxPresenter($element);
            } else {
                new InlineAttach.FormPresenter($element);
            }
        }
    });

    jQuery.extend(InlineAttach, {
        /**
         * The maxium number of uploads that can occur concurrently.
         */
        MAX_UPLOADS: 2,
        /**
         * The amount of time to wait to see if the upload finishes before displaying the progress. We do this so
         * because the progress bar is not very useful for small files and infact may introduce a flicker as it is
         * displayed and quickly removed.
         */
        DISPLAY_WAIT: 600,
        /**
         * Wraps the passed function in such a way that ensures it always runs in the passed scope. If no scope is passed
         * the the function is returned unmodified. If no function is passed, a no-operation function is returned.
         *
         * @param fn the function to wrap.
         * @param scope the scope to run the function under.
         * @return the wrapped function.
         */
        rescope: function(fn, scope) {
            if (fn) {
                if (scope) {
                    return jQuery.proxy(fn, scope);
                } else {
                    return fn;
                }
            } else {
                return jQuery.noop;
            }
        },
        /**
         * Copy the passed array-like object.
         *
         * @param array the array like object to copy.
         */
        copyArrayLike: function(array) {
            return jQuery.makeArray(array);
        },
        /**
         * Some global render helpers.
         */
        Renderers: {
            container: function() {
                return jQuery("<div class='field-group'/>");
            }
        }
    });

    /**
     * A class to helper with the impleation of upload logic. It manages the queing of uploads to ensure that only a
     * certain number of them are active at one time.
     */
    InlineAttach.Presenter = Class.extend({
        init: function() {
            /**
             * Has the user cancelled the attach?
             */
            this.cancelled = false;
            /**
             * The upload that are currently running.
             */
            this.running = [];
            /**
             * The uploads that are currently waiting to run..
             */
            this.waiting = [];
        },
        /**
         * Add an upload. The upload will be started if there are not too many currently running uploads or otherwise
         * it will be queued waiting for a call to _finishUpload.
         *
         * @param upload the upload to start or queue.
         * @return true if there are currently running uploads, false otherwise.
         */
        _addUpload: function(upload) {
            //Only start the current upload if we have not reached the concurrent upload limit.
            if (!this.cancelled) {
                if (this.running.length >= InlineAttach.MAX_UPLOADS) {
                    this.waiting.push(upload);
                } else {
                    this.running.push(upload);
                    upload.upload();
                }
            }
            return this.running.length > 0;
        },
        /**
         * Call to indicate that the passed upload has finished running. If there are queued uploads one will be
         * selected and started to replaced the one just finished.
         *
         * @param upload that has finished.
         * @return true if there are currently running uploads, false otherwise.
         */
        _finishUpload: function(upload) {
            if (!this.cancelled) {
                InlineAttach.Presenter.removeFromArray(this.waiting, upload);
                if (InlineAttach.Presenter.removeFromArray(this.running, upload)) {
                    if (this.waiting.length > 0 ) {
                        var next = this.waiting.shift();
                        this.running.push(next);
                        next.upload();
                    }
                }
            }
            return this.running.length > 0;
        },
        /**
         * Called when the user 'cancels' all uploading. It aborts all running and queued uploads.
         */
        _cancel: function() {
            this.cancelled = true;

            var i;

            //Make a copy of waiting (in case an upload finishes while we are aborting) and abort them.
            var wait = InlineAttach.copyArrayLike(this.waiting);
            for (i = 0; i < wait.length; i++) {
                wait[i].abort();
            }

            //Make a copy of running (in case the upload finishes while we are aborting) and abort them.
            var run = InlineAttach.copyArrayLike(this.running);
            for (i = 0; i < run.length; i++) {
                run[i].abort();
            }

            this.waiting = [];
            this.running = [];
        }
    });

    jQuery.extend(InlineAttach.Presenter, {
        /**
         * Removes the first occurance of the passed element from the passed array.
         *
         * @param array the array to process.
         * @param element the element to remove.
         * @return the element removed or null if no such element is found.
         */
        removeFromArray: function(array, element) {
            var index = jQuery.inArray(element, array);
            if (index >= 0) {
                return array.splice(index, 1)
            } else {
                return null;
            }
        }
    });

    /**
     * The overall control logic when file uploads are to occur using form submission in the background.
     */
    InlineAttach.FormPresenter = InlineAttach.Presenter.extend({
        /**
         * @constructor
         * @param $element the file input to used for uploading.
         */
        init: function($element) {
            this._super();
            /**
             * The UI form where the user is requesting the upload.
             */
            this.form = new InlineAttach.Form(new InlineAttach.FileInput($element, false));
            this.form.fileSelector.onChange(jQuery.proxy(this._attach, this));
            this.form.onCancel(jQuery.proxy(this._cancel, this));
        },
        /**
         * Called when the file input changes to start uploading to the server.
         *
         * @param fileName in the file input.
         */
        _attach: function(fileName) {
            this.form.clearErrors();
            if (this.cancelled) {
                return;
            }

            var form = this.form, data = this._createSubmitData();

            //Add a new "File Input" to the form. We use the old input as part of a hidden form that we can submit to the
            //server in the background.
            var $oldInput = form.cloneFileInput();
            form.fileSelector.clear();

            var progress = form.addStaticProgress(fileName);

            //We only show progress after we are sure the upload will take longer than InlineAttach.DISPLAY_WAIT.
            var timer = new InlineAttach.Timer(function() {
                !this.cancelled && progress.show();
            }, this);

            var upload = new InlineAttach.FormUpload({
                $input: $oldInput,
                url: InlineAttach.FormPresenter.DEFAULT_URL,
                params: data,
                scope: this,
                before: function() {
                    !this.cancelled && progress.start();
                },
                success: function(val) {
                    if (this.cancelled) {
                        return;
                    }

                    if (val.id && val.name) {
                        form.addTemporaryFileCheckbox(val.id, val.name, progress)
                    } else if (val.errorMsg) {
                        form.addErrorWithFileName(val.errorMsg, fileName, progress);
                    } else {
                        form.addError(InlineAttach.Text.tr("upload.error.bad.response", fileName), progress);
                    }
                },
                error: function(text) {
                    if (this.cancelled) {
                        return;
                    }

                    if (text.indexOf("SecurityTokenMissing") >= 0) {
                        form.addError(InlineAttach.Text.tr("upload.xsrf.timeout", fileName), progress);
                    } else {
                        form.addError(InlineAttach.Text.tr("upload.error.unknown", fileName), progress);
                    }
                },
                after: function() {
                    timer.cancel();
                    progress.remove();

                    if (!this.cancelled && !this._finishUpload(upload)) {
                        form.enable();
                    }
                }
            });

            progress.onCancel(function() {
                upload.abort();
            });

            if (this._addUpload(upload)) {
                timer.schedule(InlineAttach.DISPLAY_WAIT);
                form.disable();
            }

            form.fileSelector.focus();
        },
        /**
         * Called when the user 'cancels' the upload form, that is, when the user stops the upload.
         */
        _cancel: function() {
            this._super();
            this.form.enable();
        },
        _createSubmitData: function() {
            var data = { atl_token: this.form.getAtlToken(), formToken: this.form.getFormToken() };
            if(this.form.issueId) {
                data.id = this.form.issueId;
            } else if (this.form.projectId) {
                data.create = true;
                data.projectId = this.form.projectId;
            } else {
                throw "Unable to find either an issueId or projectId to submit the attachment to.";
            }
            return data;
        }
    });

    /**
     * Default location to add temporary attachments using multi-part request and forms.
     */
    InlineAttach.FormPresenter.DEFAULT_URL = contextPath + "/secure/AttachTemporaryFile.jspa?decorator=none";

    /**
     * The overall control logic when file uploads are to occur using direct AJAX and XHR.
     */
    InlineAttach.AjaxPresenter = InlineAttach.Presenter.extend({
        /**
         * @constructor
         * @param $element the file input used for uploading.
         */
        init: function($element) {
            this._super();

            this.form = new InlineAttach.Form(new InlineAttach.FileInput($element, true));
            this.form.fileSelector.onChange(jQuery.proxy(this._attach, this));
            this.form.onCancel(jQuery.proxy(this._cancel, this));
        },
        /**
         * Called to attach the passed File objects to the current issue.
         *
         * @param files the files to attach to the issue.
         */
        _attach: function(files) {
            this.form.clearErrors();
            if (this.cancelled) {
                return;
            }
            if (files && files.length > 0) {
                files = this._checkAndFilterFiles(files);
                if (files) {
                    this._uploadFiles(files);
                }
            }

            this.form.fileSelector.clear().focus();
        },
        /**
         * Called to check the passed files to ensure they can be uploaded. Returns an array of files that can be
         * uploaded. Null will be returned if no files can be uploaded.
         *
         * @param files the files that we want to filter.
         * @return Returns an array of files that can be uploaded. Null will be returned if no files can be uploaded.
         */
        _checkAndFilterFiles: function(files) {
            if (files.length > InlineAttach.AjaxPresenter.MAX_SELECTED_FILES) {
                this.form.addError(InlineAttach.Text.tr("upload.error.too.many.files",
                        files.length, InlineAttach.AjaxPresenter.MAX_SELECTED_FILES));
                return null;
            }

            var maxSize = this.form.maxSize;
            var newFiles = [];
            for (var i = 0; i < files.length; i++) {
                try {
                    var file = files[i];
                    if (file.size == 0) {
                        this.form.addError(InlineAttach.Text.tr("upload.empty.file", file.name));
                    } else if(maxSize > 0 && file.size > maxSize) {
                        //Note the order of this call is important. We want the size of the file to be based on max file
                        //(i.e. if maxSize is in MB than file.size should be in MB).
                        var sizes = InlineAttach.Text.fileSize(maxSize, file.size);
                        this.form.addError(InlineAttach.Text.tr("upload.too.big", file.name, sizes[1], sizes[0]));
                    } else {
                        //JRADEV-5679:
                        //Firefox throws exceptions on some I/O edge cases with its implementation of the FileAPI.
                        // For example, reading the File.size can throw an exception if the file no longer exists.
                        // So we don't have to add try...catch statements around everything we copy the attributes we
                        // need.
                        newFiles.push({name: file.name, size: file.size, file: file});
                    }
                } catch (e) {
                    this.form.addError(InlineAttach.AjaxUpload.getClientErrorMessage(e, file))
                }
            }
            return newFiles.length == 0 ? null : newFiles;
        },
        /**
         * Create and return the data to be submitted with the upload.
         *
         * @return the data to be submitted with the upload.
         */
        _createSubmitData: function() {
            var data = { atl_token: this.form.getAtlToken(), formToken: this.form.getFormToken() };
            if(this.form.issueId) {
                data.issueId = this.form.issueId;
            } else if (this.form.projectId) {
                data.projectId = this.form.projectId;
            } else {
                throw "Unable to find either an issueId or projectId to submit the attachment to.";
            }
            return data;
        },
        /**
         * Actually start uploading the files. The passed files have been checked and are valid.
         *
         * @param files the files to upload.
         */
        _uploadFiles: function(files)
        {
            var form = this.form, data = this._createSubmitData(), that = this, running = false;
            jQuery.each(files, function() {
                var progress = form.addProgress(this), file = this;

                //We only show progress after we are sure the upload will take longer than InlineAttach.DISPLAY_WAIT.
                var timer = new InlineAttach.Timer(function() {
                    if (!that.cancelled) {
                        progress.show();
                    }
                });
                var upload = new InlineAttach.AjaxUpload({
                    file: file.file,
                    params: jQuery.extend({filename: file.name, size: file.size}, data),
                    scope: that,
                    url: InlineAttach.AjaxPresenter.DEFAULT_URL,
                    before: function() {
                        !this.cancelled && progress.start();
                    },
                    progress: function(val) {
                        !this.cancelled && progress.update(val);
                    },
                    success: function(val, status) {
                        if (this.cancelled) {
                            return;
                        }

                        if (status === 201) {
                            if (val.id !== undefined && val.name !== undefined) {
                                form.addTemporaryFileCheckbox(val.id, val.name, progress, file.file);
                            } else {
                                form.addError(InlineAttach.Text.tr("upload.error.bad.response", file.name), progress);
                            }
                        } else {
                            if (val.token) {
                                form.setAtlToken(val.token);
                            }

                            if (val.errorMessage) {
                                form.addErrorWithFileName(val.errorMessage, file.name, progress);
                            } else {
                                form.addError(this._getErrorFromStatus(status, file), progress);
                            }
                        }
                    },
                    error: function(text, status) {
                        if (this.cancelled) {
                            return;
                        }
                        if (status < 0) {
                            //This is a client error so just render it.
                            form.addError(text, progress);
                        } else {
                            var statusError = this._getErrorFromStatus(status, file);
                            if (statusError) {
                                form.addError(statusError, progress);
                            } else {
                                form.addError(InlineAttach.Text.tr("upload.error.unknown", file.name), progress)
                            }
                        }
                    },
                    after: function() {
                        timer.cancel();
                        progress.finish().remove();

                        if (!this.cancelled && !this._finishUpload(upload)) {
                            form.enable();
                        }
                    }
                });

                progress.onCancel(function() {
                    upload.abort();
                });

                if (that._addUpload(upload)) {
                    running = true;
                    timer.schedule(InlineAttach.DISPLAY_WAIT);
                }
            });

            //Disable the form if there are any running uploads. The last running upload will enable the form.
            if (running) {
                this.form.disable();
            }
        },
        _getErrorFromStatus: function(status, file) {
            var error;
            if (status === 0) {
                error = InlineAttach.Text.tr("upload.error.server.no.reply", file.name);
            } else if (status === 400) {
                error = InlineAttach.Text.tr("upload.error.badrequest", file.name);
            } else if (status === 401) {
                error = InlineAttach.Text.tr("upload.error.auth", file.name);
            } else {
                error = InlineAttach.Text.tr("upload.error.unknown.status", file.name, status);
            }
            return error;
        },
        /**
         * Called when the user clicks cancel on the from they are using to attach files (i.e. the user does not want
         * to attach any files).
         */
        _cancel: function() {
            this._super();
            this.form.enable();
        }
    });

    jQuery.extend(InlineAttach.AjaxPresenter, {
        /**
         * The default location to attach temporary files using AJAX.
         */
        DEFAULT_URL: contextPath + "/rest/internal/1.0/AttachTemporaryFile",
        /**
         * The number of files that can be attached at one time.
         */
        MAX_SELECTED_FILES: 100,
        /**
         * Check to see if AJAX uploads are supported.
         *
         * @param $element the input element that will be used for attachments.
         */
        isSupported: function($element) {
            if (!$element || !$element[0] || !$element[0].files){
                return false;
            } else {
                return InlineAttach.AjaxUpload.isSupported();
            }
        }
    });

    /**
     * Simple wrapper around a HTML file input.
     */
    InlineAttach.FileInput = Class.extend({
        /**
         * @constructor
         * @param $fileInput the file input to wrap.
         * @param testMultiple tries to make the wrapped file input accept multiple files when set to true.
         */
        init: function($fileInput, testMultiple) {
            this.$element = $fileInput;
            this.$container = $fileInput.parent();

            if (testMultiple && this.$element[0].files !== undefined) {
                this.$element.attr("multiple", "multiple");
                this.multiple = true;
            } else {
                this.multiple = false;
            }
        },
        clear: function() {
            this.$element.val('');
            return this;
        },
        getFiles: function() {
            return this.$element[0].files;
        },
        hasFiles: function() {
            return this.getFiles().length > 0;
        },
        /**
         * Call the passed function when the wrapped file input changes. The callback will have "this" assigned to the
         * FileInput and not the wrapped HTML element.
         *
         * @param callback the function to call when the file input changes. It will be run with the FileInput assigned
         * to "this". If the FileApi is supported, the first argument will be the FileApi files from the HTML input.
         * If the FileApi is *not* supported it will be the string value from the HTML input.
         */
        onChange: function(callback) {
            var that = this;
            this.$element.change(function() {
                if (that.multiple) {
                    callback.call(that, this.files);
                } else {
                    callback.call(that, that.getFileName());
                }
            });
            return this;
        },
        focus: function() {
            if(this._isIE()) {
                var $e = this.$element;
                //IE being the usual pain that it is wont focus unless there's this timeout.
                setTimeout(function() {$e.focus();}, 0);
            } else {
                this.$element.focus();
            }
            return this;
        },
        /**
         * Clone the current HTML input and replace it with a new one.
         *
         * @return the old file input.
         */
        cloneInput: function(){
            var oldElement = this.$element;
            oldElement.replaceWith(this.$element = oldElement.clone(true));
            oldElement.unbind();
            return oldElement;
        },
        /**
         * Return the filename from the wrapped HTML element. We strip out some common garbage that some
         * browsers add to the name.
         *
         * See: http://dev.w3.org/html5/spec/number-state.html#concept-input-type-file-selected
         *
         * @return the filename currently in the wrapped input element.
         */
        getFileName: (function() {
            //Match the "c:\fakepath\" from the start of the string provided its not the entire string.
            var fakepath = /^c:\\fakepath\\(?!$)/i;
            return function() {
                var fileName = this.$element.val();
                //Remove "c:\fakepath\" from the string if there is stuff after it.
                // SEE: http://dev.w3.org/html5/spec/number-state.html#concept-input-type-file-selected
                fileName = fileName.replace(fakepath, "");

                if(this._isIE() && fileName.indexOf("\\") >= 0) {
                    //IE returns an absolute path for the selected file, we however only want to display the
                    //filename.
                    fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
                }
                return fileName;
            }
        })(),
        _isIE: function() {
            return Navigator.isIE() && Navigator.majorVersion() < 11;
        },
        before: function(el) {
            if (el) {
                if (el.$element) {
                    el = el.$element;
                }
                this.$container.before(el);
            }
        }
    });

    /**
     * Represents a simple progress bar.
     */
    InlineAttach.ProgressBar = (function() {
        var options = {showPercentage: false, height: "2px"}, count = 0;
        return Class.extend({
            init: function() {
                var $container = this.$element = this._renderers.container();
                this.$progress = this._renderers.progress().appendTo($container);
                this.$progress.progressBar(0, options);
                this.hidden = true;
                this.old = 0;
            },
            value: function(value) {
                if (value > 100) {
                    value = 100;
                } else if (value < 0) {
                    value = 0;
                }
                if (this.hidden) {
                    this.$progress.show();
                    this.hidden = false;
                }

                if (this.old !== value) {
                    this.$progress.progressBar(value, options);
                    if (value >= 100) {
                        this.$progress.fadeOut();
                    }
                    this.old = value;
                }
            },
            _renderers: {
                container: function() {
                    return jQuery("<div>").addClass("file-progress");
                },
                progress: function() {
                    return jQuery("<div>").attr("id", "upload-progress-" + (count++)).hide();
                }
            }
        })
    })();

    /**
     * Represents the user's view of a progressing upload whose status (i.e. % complete) is known. This includes:
     *
     * <ol>
     *     <li>Progress Bar</li>
     *     <li>File Name and Stats (upload rate, remaining time, etc)</li>
     *     <li>Cancel Link</li>
     * <ol>
     */
    InlineAttach.UploadProgress = Class.extend({
        /**
         * @constructor
         * @param file the file whose upload this object represents.
         */
        init: function(file) {
            var $container = this.$element = InlineAttach.Renderers.container().hide();
            var progress = this.progress = new InlineAttach.ProgressBar();
            var content = this._renders.content(file.name);

            //The content is the place for the text (filename, size, ...)
            this.$content = content.$content;
            this.$cancel = content.$cancel;

            $container.append(content.$element).append(progress.$element);

            this.total = file.size;
            this.current = 0;
            this.name = file.name;
            this.timer = new InlineAttach.Timer(this._update, this);
            this.rateNumerator = 0;
            this.rateDenominator = 0;
            this._title(InlineAttach.Text.tr('upload.progress.title.waiting'));
        },
        start: function() {
            this.started = this._now();
            this.startedSize = 0;
            return this;
        },
        update: function(current) {
            this.timer.cancel();
            return this._update(current);
        },
        /**
         * Called when the current status of the file upload needs to be updated.
         *
         * @param current the current amount uploaded in bytes. Can be undefined (i.e. not passed) when asked to do
         * and update with the last known value.
         */
        _update: function(current) {

            var now = this._now();

            //If current is not passed then use the last known value.
            if (current === undefined) {
                current = this.current;
            } else if (current !== this.current) {
                this.lastUpdate = now;
                this.current = current;
            }

            var text = InlineAttach.Text;
            var percentage = Math.min(100, Math.round(current / this.total * 100));
            var partSize = text.currentOutOfTotalSize(current, this.total);
            var rateDisplay, remainingDisplay;

            this.progress.value(percentage);

            var timeDiff = (now - this.started) / 1000; //seconds
            //Calculate the rate every 2 seconds.
            if (timeDiff >= 2) {
                //Don't use the first calculation. The OS buffers will still be filling and the
                //upload will appear to be super quick.
                if (this.startedSize > 0) {
                    this._addRate((current - this.startedSize) / timeDiff);
                }
                this.started = now;
                this.startedSize = current;
            }
            var rate = this._calcRate();
            //Only start outputting stats after some data has been sent to stabilise the numbers.
            if (current >= InlineAttach.UploadProgress.DATA_MIN && rate > 0) {
                var remaining = Math.max(1, (this.total - current) / rate);  //seconds.
                rateDisplay = text.rate(rate);
                remainingDisplay = text.time(remaining);
            }

            if (now - this.lastUpdate >= InlineAttach.UploadProgress.STALLED_TIMEOUT) {
                //If we have not seen movement in a while then assume we have stalled.
                this._content(text.tr("upload.file.stalled", this.name));
                if (rateDisplay) {
                    this._title(text.tr("upload.progress.title.known.stalled", rateDisplay, partSize));
                } else {
                    this._title(text.tr("upload.progress.title.unknown.stalled", partSize));
                }
            } else {
                if (rateDisplay) {
                    this._title(text.tr("upload.progress.title.known", rateDisplay, partSize, remainingDisplay));
                    this._content(text.tr("upload.file.remaining", this.name, remainingDisplay));
                } else {
                    this._title(partSize);
                    this._content(this.name);
                }
            }
            if (current < this.total) {
                //Schedule an update in 1 second if we don't get one from the XHR before than.
                this.timer.schedule(InlineAttach.UploadProgress.UPLOAD_REFRESH);
            }
            return this;
        },
        finish: function() {
            this.progress.value(100);
            this.timer.cancel();
            return this;
        },
        /**
         * Call the passed function when the user clicks the cancel link associated with an uploads progress. The "this"
         * variable in the callback is assigned to the current UploadProgress.
         *
         * @param callback the function to call when the user clicks the cancel link.
         */
        onCancel: function(callback) {
            var that = this;
            this.$cancel.click(function(e) {
                e.preventDefault();
                callback.call(that);
            });
            return this;
        },
        remove: function() {
            this.$element.remove();
            return this;
        },
        hide: function() {
            this.$element.hide();
            return this;
        },
        show: function() {
            this.$element.fadeIn();
            return this;
        },
        _title: function(title) {
            this.$element.attr("title", title);
            return this;
        },
        _content: function(rem) {
            this.$content.text(rem);
            return this;
        },
        _addRate: function(rate) {
            var weight = InlineAttach.UploadProgress.WEIGHT;
            this.rateNumerator = this.rateNumerator * weight + rate;
            this.rateDenominator = this.rateDenominator * weight + 1;
        },
        _calcRate: function() {
            if (this.rateDenominator == 0) {
                return 0;
            }
            var value = this.rateNumerator / this.rateDenominator;
            if (Math.abs(value) < 0.005) {
                return 0;
            } else {
                return value;
            }
        },
        _now: function() {
            return new Date().getTime();
        },
        _renders: {
            content: function(fileName) {
                var text = InlineAttach.Text.tr("upload.file.waiting", fileName);
                var $container = jQuery("<div class='loading file'>");
                var $content = jQuery("<span>").text(text);
                var $cancel = jQuery("<a href='#'/>").text(InlineAttach.Text.tr("upload.cancel"));
                $container.append($content).append(" ").append($cancel);
                return {$element: $container, $content: $content, $cancel: $cancel};
            }
        }
    });

    jQuery.extend(InlineAttach.UploadProgress, {
        /**
         * If we don't get AJAX progress within this timeout we think things have stalled.
         */
        STALLED_TIMEOUT: 10000,
        /**
         * We force the refresh of the upload stats using this interval.
         */
        UPLOAD_REFRESH: 2000,
        /**
         * The minimum amount of data to send before we start calculating stats.
         */
        DATA_MIN: 20 * 1024,
        /**
         * The decaying factor to use in the weighted average calculation.
         */
        WEIGHT: 0.7
    });

    /**
     * Represents the user's view of a progressing upload whose status (i.e. % complete) is unknown. This basically
     * includes a name and an animated throbber.
     */
    InlineAttach.UnknownProgress = Class.extend({
        /**
         * @constructor
         * @param fileName the name of the file this progress is meant to represent.
         */
        init: function(fileName) {
            var content = this._renders.content(fileName);
            this.$element = content.$element;
            this.$cancel = content.$cancel;
            this.$content = content.$content;
            this.fileName = fileName;
            this._title(InlineAttach.Text.tr("upload.progress.title.waiting"));
        },
        remove: function() {
            this.$element.remove();
            return this;
        },
        hide: function() {
            this.$element.hide();
            return this;
        },
        show: function() {
            this.$element.fadeIn();
            return this;
        },
        start: function() {
            this._title(InlineAttach.Text.tr("upload.progress.title.running"));
            this._content(this.fileName);
            return this;
        },
        /**
         * Call the passed function when the user clicks the cancel link associated with an uploads progress. The "this"
         * variable in the callback is assigned to the current UploadProgress.
         *
         * @param callback the function to call when the user clicks the cancel link.
         */
        onCancel: function(callback) {
            var that = this;
            this.$cancel.click(function(e) {
                e.preventDefault();
                callback.call(that);
            });
            return this;
        },
        _title: function(title) {
            this.$element.attr("title", title);
            return this;
        },
        _content: function(text) {
            this.$content.text(text);
            return this;
        },
        _renders: {
            content: function(fileName) {
                var text = InlineAttach.Text.tr("upload.file.waiting", fileName);
                var $cancel = jQuery("<a href='#'/>").text(InlineAttach.Text.tr("upload.cancel"));
                var $loading = jQuery("<div class='loading file'>");
                var $content = jQuery("<span>").text(text);

                $loading.append($content).append(" ").append($cancel);
                var $container = InlineAttach.Renderers.container().append($loading);
                return {$element: $container, $cancel: $cancel, $content: $content};
            }
        }
    });

    /**
     * Represents the HTML form that contains a FileInput used for file uploads to JIRA.
     */
    InlineAttach.Form = Class.extend({
        /**
         * @constructor
         *
         * @param fileInput the FileInput whose associated HTML form we are going to wrap.
         */
        init: function(fileInput) {
            this.fileSelector = fileInput;
            this.$form = fileInput.$element.closest("form");

            this.maxSize = parseInt(this.$form.find('#attach-max-size').text() || jQuery('#attach-max-size').text());
            if (isNaN(this.maxSize)) {
                throw "Unable to find maximum upload size on form.";
            }


            var assigned = false;
            var val = parseInt(this.$form.find(":input[name=id]").val());
            if (!isNaN(val)) {
                assigned = true;
                this.issueId = val;
            }
            val = parseInt(this.$form.find(":input[name=pid]").val());
            if (!isNaN(val)) {
                assigned = true;
                this.projectId = val;
            }
            if (!assigned) {
                throw "Unable to find either an issueId or projectId to submit the attachment to.";
            }
        },
        getAtlToken: function(){
            var $atlToken = this.$form.find("input[name='atl_token']");
            if ($atlToken.length > 0){
                return $atlToken.val();
            } else {
                return atl_token();
            }
        },
        setAtlToken: function(token) {
            var $token = this.$form.find("input[name='atl_token']");
            if ($token.length > 0) {
                $token.val(token);
            } else {
                XSRF.updateTokenOnPage(token);
            }
            return this;
        },
        getFormToken: function(){
            var $formToken = this.$form.find("input[name='formToken']");
            if ($formToken.length > 0){
                return $formToken.val();
            } else {
                return null;
            }
        },

        disable: function() {
            this._getFormSubmits().attr("disabled", "disabled");
            return this;
        },
        enable: function() {
            this._getFormSubmits().removeAttr("disabled");
            return this;
        },
        /**
         * Creates and adds an UploadProgress to the current form for the passed file.
         *
         * @param file the file to create the UploadProgress for.
         * @return the newly created UploadProgress.
         */
        addProgress: function(file){
            var prog = new InlineAttach.UploadProgress(file);
            this._addElement(prog.$element);
            return prog;
        },
        /**
         * Creates and adds an UnknownProgress to the current form for the passed file name.
         *
         * @param fileName the fileName to create the UnknownProgress for.
         * @return the newly created UnknownProgress.
         */
        addStaticProgress: function(fileName) {
            var prog = new InlineAttach.UnknownProgress(fileName);
            this._addElement(prog.$element);
            return prog;
        },
        /**
         * Adds the checkbox to the form with the passed value and name.
         *
         * @param value the submit value for the new checkbox.
         * @param name the name of the checkbox (i.e. what the user sees)
         * @param replaceObj if non false, the checkbox will replace the passed object
         * @param file optionally the FileAPI file object that was uploaded
         */
        addTemporaryFileCheckbox: function(value, name, replaceObj, file) {

            var $thumbNail = this.addLocalThumbnailImage(name,file);
            var $element = InlineAttach.Renderers.container();
            var $label = jQuery('<label>').attr('for', 'filetoconvert-' + value).text(name);
            var $check = jQuery("<input type='checkbox' class='checkbox' name='filetoconvert' checked='checked'>").attr({
                "id": "filetoconvert-" + value,
                "value": value,
                "title": InlineAttach.Text.tr("upload.checkbox.title")
            });
            $element.append($check).append($label);
            if ($thumbNail) {
                $element.append(jQuery("<br/>")).append($thumbNail);
            }
            $element.hide();
            this._replaceElement($element, replaceObj);
            return this;
        },

        /**
         * If the browsers supports it, this create a local thumbnail image of the upload if its an image
         * @param name the name of the attachment
         * @param file the FileAPI file object which may be null
         */
        addLocalThumbnailImage: function(name, file) {
            var $thumbNail = null;

            window.URL = window.webkitURL || window.URL; // Vendor prefixed in Chrome.

            if (file && window.URL && window.URL.createObjectURL) {
                var imageType = /image.*/;

                if (file.type.match(imageType)) {
                    var that  =  this;
                    var title = name + " - " + InlineAttach.Text.fileSize(file.size) + " - " + file.type;
                    var img = document.createElement('img');
                    img.title = title;
                    img.alt = title;
                    img.onload = function(e) {
                         var aspectRatio = that.getAspectRatio(100,100,img.width,img.height);
                        img.width = Math.round(img.width / aspectRatio);
                        img.height = Math.round(img.height / aspectRatio);
                        $thumbNail.show();
                        window.URL.revokeObjectURL(img.src); // Clean up after yourself.
                    };
                    img.src = window.URL.createObjectURL(file);
                    $thumbNail = jQuery(img).hide();
                }
            }
            return $thumbNail;
        },


        getAspectRatio : function(maxWidth,maxHeight, origWidth, origHeight) {

                if (origWidth > maxWidth) {
                    return Math.round(origWidth / maxWidth);
                } else if( origHeight > maxHeight) {
                    return Math.round(origHeight / maxHeight);
                } else {
                    return 1;
                }
        },

        /**
         * Add an error message to the form ensuring that the passed fileName is mentioned. The passed error message will
         * be updated to include the fileName if it is not already present.
         *
         * @param error the error message.
         * @param fileName the fileName that must be mentioned.
         * @param replaceObj if non falsy, the error will replace the passed object
         */
        addErrorWithFileName: function(error, fileName, replaceObj) {
            if (error.indexOf(fileName) == -1) {
                error = InlineAttach.Text.tr("upload.error.server", fileName, error);
            }
            return this.addError(error, replaceObj);
        },
        /**
         * Adds an error to the form.
         *
         * @param error the error message to display.
         * @param replaceObj if non falsy, the error will replace the passed object
         */
        addError: function(error, replaceObj) {
            var $element = InlineAttach.Renderers.container();
            $element.addClass("error").append(jQuery("<div>").text(error)).hide();
            this._replaceElement($element, replaceObj);
            return $element;
        },
        clearErrors: function() {
            this.$form.find("div.error").remove();
            return this;
        },
        /**
         * Clones the HTML File input and replaces it on the form. The old  HTML file input will be returned.
         *
         * @return the old file input.
         */
        cloneFileInput: function() {
            return this.fileSelector.cloneInput();
        },
        /**
         * Call the passed function when the user clicks the cancel on the form. The "this"
         * variable in the callback is assigned to the current Form.
         *
         * @param callback the function to call when the user clicks the cancel link.
         */
        onCancel: function(callback) {
            var $cancel = this.$form.find("a.cancel");
            $cancel.click(jQuery.proxy(callback, this));
            return this;
        },
        _getFormSubmits: function() {
            return this.$form.find("input[type=submit]");
        },
        _addElement: function(el) {
            this.fileSelector.before(el);
        },
        _replaceElement: function($element, replaceObj) {
            if (replaceObj && replaceObj.$element) {
                replaceObj.$element.replaceWith($element);
            } else {
                this._addElement($element);
            }
            //Love the animation. Maybe too much.
            $element.fadeIn();
        }
    });

    /**
     * Object used to upload an attachment via a form submission. We do this in the background using the jQuery Form plugin.
     * The file to be attached will be taken from the passed HTML element and uploaded as a multi-part POST. The HTML
     * element passed will be moved into a hidden form so make you it is not visible.
     */
    InlineAttach.FormUpload = Class.extend({
        /**
         * @constructor
         * @param options the configuration for the upload.
         *
         *  <dl>
         *      <dt>{jQuery} input</dt>
         *      <dd>Element defining the file input HTML element</dd>
         *      <dt>{String} url</dt>
         *      <dd>The url to POST the attachment to</dd>
         *      <dt>{Object} params
         *      <dd>Set of HTML parameters to send along with the file</dd>
         *      <dt>{Object} scope</dt>
         *      <dd>The scope will be assigned to "this" when a callback is executed. Defaults to the FormUpload.</dd>
         *      <dt>{Function} success.</dt>
         *      <dd>Function called when the upload succeeds. The first argument will be the data returned from the upload.</dd>
         *      <dt>{Function} error</dt>
         *      <dd>Function called when the fails. The first argument will be any text received from the server during the upload.</dd>
         *      <dt>{Function} before</dt>
         *      <dd>Function called just before the upload starts.</dd>
         *      <dt>{Function} after</dt>
         *      <dd>Function called after the upload has finished. It will be called always, even if the upload failed or is aborted<dd>
         *      <dt>{Function} abort</dt>
         *      <dd>Function called once the request is aborted.
         * </dl>
         */
        init: function(options) {
            var scope = options.scope || null;
            var rescope = InlineAttach.rescope;

            this.$input = options.$input;
            this.url = options.url;
            this.params = options.params || {};
            this.successcb = rescope(options.success, scope);
            this.errorcb = rescope(options.error, scope);
            this.before = rescope(options.before, scope);
            this.after = rescope(options.after, scope);
            this.abortcb = rescope(options.abort, scope);
            this.aborted = false;
            this.$form = null;
            this.xhr = null;
        },
        upload: function() {
            if (this.aborted) {
                return;
            }

            var url = this.url;
            var params = jQuery.param(this.params);
            if (params) {
                url = url + "?" + params;
            }

            var $attachForm = this.$form = this._renders.form(url), that = this;
            this._addToBody($attachForm.append(this.$input));

            $attachForm.ajaxForm({
                dataType: "json",
                data: this.params,
                //set the timeout to infinity since it could take a long time to upload large files (JRADEV-3455)
                timeout:0,
                beforeSend: function(xhr) {
                    that.xhr = xhr;
                },
                beforeSubmit: function() {
                    that.before();
                },
                error: function(xhr) {
                    if (that.aborted) {
                        that.abortcb();
                    } else {
                        var text = (xhr && xhr.responseText) || "";
                        that.errorcb(text);
                    }
                },
                success: function(data) {
                    //JRADEV-5919: The AjaxForm plugin can detect pass data === undefined here inder IE.
                    // If there is no content then assume {} is the result.
                    that.successcb(data || {});
                },
                complete: function() {
                    if (that.$form) {
                        that.$form.remove();
                        that.$form = null;
                    }
                    that.after();
                }
            });
            $attachForm.submit();
        },
        abort: function() {
            if (!this.aborted) {
                this.aborted = true;

                if (this.xhr) {
                    //Abort the upload. This triggers an error which will fire the abort callback.
                    this.xhr.abort();
                    this.xhr = null;
                } else {
                    //Not currently running. No Form to remove so lets just invoke the callbacks.
                    this.abortcb();
                    this.after();
                }
            }
        },
        _addToBody: function($form) {
            jQuery("body").append($form);
        },
        _renders: {
            form: function(postUrl) {
                return jQuery("<form method='post' enctype='multipart/form-data'/>").attr("action", postUrl).hide();
            }
        }
    });

    /**
     * Object used to upload an attachment via a AJAX submission. This only works if the browser supports the FileApi and
     * the XHR.upload callback set of functions (e.g. NOT IE7, IE8).
     */
    InlineAttach.AjaxUpload = Class.extend({
        /**
         * @constructor
         * @param options the configuration for the upload.
         *
         *  <dl>
         *      <dt>{File} file</dt>
         *      <dd>The file to upload.</dd>
         *      <dt>{String} url</dt>
         *      <dd>The url to POST the attachment to</dd>
         *      <dt>{Object} params
         *      <dd>Set of HTML parameters to send along with the file.</dd>
         *      <dt>{Object} scope</dt>
         *      <dd>The scope will be assigned to "this" when a callback is executed. Defaults to the AjaxUpload.</dd>
         *      <dt>{Function} success.</dt>
         *      <dd>Function called when the upload succeeds. The first argument will be the data returned from the upload.</dd>
         *      <dt>{Function} error</dt>
         *      <dd>Function called when the fails. The first argument will be any text received from the server during the upload.</dd>
         *      <dt>{Function} abort</dt>
         *      <dd>Function called after the upload is aborted.</dd>
         *      <dt>{Function} after</dt>
         *      <dd>Function called after the upload has finished. It will be called always, even if the upload failed or was aborted.<dd>
         *      <dt>{Function} progress</dt>
         *      <dd>Function called as the upload progresses with the amount of data currently uploaded. Always passed as bytes.<dd>
         * </dl>
         */
        init: function(options) {
            var scope = options.scope || null;
            var rescope = InlineAttach.rescope;

            this.file = options.file;
            this.url = options.url;
            this.params = options.params || {};
            this.beforecb = rescope(options.before, scope);
            this.progresscb = rescope(options.progress, scope);
            this.errorcb = rescope(options.error, scope);
            this.successcb = rescope(options.success, scope);
            this.abortcb = rescope(options.abort, scope);
            this.finalcb = rescope(options.after, scope);
            this.aborted = false;
        },
        upload: function() {
            if (this.aborted || this.xhr) {
                return;
            }
            var xhr = this.xhr = InlineAttach.AjaxUpload.xhr();
            xhr.upload.onprogress = jQuery.proxy(this._upload, this);
            xhr.onreadystatechange = jQuery.proxy(this._statechange, this);

            var url = this.url;
            var params = jQuery.param(this.params);
            if (params) {
                url = url + "?" + params;
            }

            this.beforecb();

            try {
                xhr.open("POST", url, true);
                xhr.setRequestHeader("Content-Type", this.file.type || "application/octet-stream");
                xhr.send(this.file);
            } catch (e) {
                //JRADEV-5679:
                //This can happen in Firefox 3.6+ when some I/O error occurs while reading this file. It returns an
                //XPCOM exception with the details of the error.
                this._clienterror(e, this.file);
            }
        },
        /**
         * Abort the upload. This will cancel the upload if it is currently running.
         */
        abort: function() {
            if (!this.aborted) {
                this.aborted = true;

                if (this.xhr) {
                    //The XHR _statechage will take care of all the callbacks.
                    this.xhr.abort();
                } else {
                    //We have not started YET. Still need to call the callbacks.
                    this.abortcb();
                    this.finalcb();
                }
            }
        },
        /**
         * XHR callback function.
         */
        _statechange: function() {
            if (this.xhr.readyState === 4) {
                if (this.aborted) {
                    this.abortcb();
                } else {
                    try {
                        this.successcb(JSON.parse(this.xhr.responseText), this.xhr.status, this.xhr);
                    } catch (e) {
                        this.errorcb(this.xhr.responseText, this.xhr.status, this.xhr);
                    }

                    this.xhr.upload.onprogress = this.xhr.statechange = null;
                }
                this.finalcb();
            }
        },
        _clienterror: function(e, file) {
            this.errorcb(InlineAttach.AjaxUpload.getClientErrorMessage(e, file), -1, this.xhr);
            this.finalcb();
        },
        /**
         * The xhr.upload callback.
         *
         * @param event the xhr.upload event.
         */
        _upload: function(event){
            if (event.lengthComputable)  {
                this.progresscb(event.loaded);
            }
        }
    });

    jQuery.extend(InlineAttach.AjaxUpload, {
        /**
         * Return true iff the browser supports XHR progress.
         */
        isSupported: function() {
            return InlineAttach.AjaxUpload._fileApiSupport(window) &&
                InlineAttach.AjaxUpload._xhrSupport();
        },
        /**
         * Create a new XHR request. We need to use XHR directly because we want to control the xhr.upload.
         *
         * @return an XHR request or a falsy if we could not create one.
         */
        xhr: function() {
            //Lets try jQuery's xhr constructor.
            var fn = InlineAttach.AjaxUpload._xhrJquery();
            if (!fn) {
                fn = InlineAttach.AjaxUpload._xhrDirect;
            }
            InlineAttach.AjaxUpload.xhr = fn;
            return fn();
        },
        getClientErrorMessage: function(e, file) {
            //JRADEV-5679:
            //Firefox throws exceptions on some I/O edge cases with its implementation of the FileAPI. For example, reading
            //the File.size can throw an exception if the file no longer exists. This method is designed to give some
            //nice error messages when such exceptions occur.
            var safeName;
            try {
                safeName = file.name;
            } catch (ignored) {
                //Reading the file.name can throw exceptions. In this case lets just do the best that we can.
                safeName = "<unknown>";
            }

            //These are Firefox XPCOM errors that it seems to throw.
            if (e.name === "NS_ERROR_FILE_ACCESS_DENIED") {
                return InlineAttach.Text.tr("upload.error.no.access", safeName);
            } else if (e.name === "NS_ERROR_FILE_NOT_FOUND" || e.name === "NS_ERROR_FILE_TARGET_DOES_NOT_EXIST") {
                return InlineAttach.Text.tr("upload.error.does.not.exist", safeName);
            } else {
                return InlineAttach.Text.tr("upload.error.client.unknown", safeName, e.message || e);
            }
        },
        _xhrJquery: function() {
            var settings = jQuery.ajaxSettings;
            return (settings && settings.xhr) || null;
        },
        _xhrDirect: function() {
            try {
                return new XMLHttpRequest();
            } catch (e) {
                return null;
            }
        },
        _xhrSupport: function() {
            try {
                var xhr = InlineAttach.AjaxUpload.xhr();
                if (xhr && xhr.upload) {
                    return true;
                }
            } catch (e) {
                //fall through because we need to use XHR directly.
            }
            return false;
        },
        _fileApiSupport: function(window) {
            //We need to check for the FileReader even though we don't use it because Firefox 3.5 has both
            //File and FileList classes but no way to read the file.
            return window.File && window.FileList && (!Navigator.isMozilla() || window.FileReader);
        }
    });

    /**
     * That will execute a function after a timeout.
     */
    InlineAttach.Timer = Class.extend({
        init: function(callback, scope) {
            this.callback = InlineAttach.rescope(callback, scope || this);
            this._callback = jQuery.proxy(this._callback, this);
            this.timeoutId = null;
        },
        cancel: function() {
            if (this.timeoutId !== null) {
                this._endTimeout(this.timeoutId);
                this.timeoutId = null;
            }
            return this;
        },
        /**
         * Schedule the timeout to occur after the passed number of milliseconds. The currently outstanding schedule will
         * be cancelled.
         *
         * @param timeout the amount of time to wait before triggering the callback in milliseconds.
         */
        schedule: function(timeout) {
            this.cancel();
            this.timeoutId = this._startTimeout(this._callback, timeout);
            return this;
        },
        _callback: function() {
            this.timeoutId = null;
            this.callback();
        },
        _startTimeout: function(fn, timeout) {
            return window.setTimeout(fn, timeout);
        },
        _endTimeout: function(id) {
            window.clearTimeout(id);
        }
    });

    /**
     * Lots of static text utilties.
     */
    InlineAttach.Text = (function() {
        /**
         * Kilobyte in bytes.
         */
        var kB = 1024;

        /**
         * Megabyte in bytes.
         */
        var MB = 1024 * kB;

        /**
         * Gigabyte in bytes.
         */
        var GB = 1024 * MB;

        /**
         * The maximum number of B we can display before rounding takes us up to
         * 1024 B (i.e. 1kB).
         */
        var bMax = Math.floor(kB * 0.995);

        /**
         * The maximum number of kB we can display before rounding takes us up to
         * 1024kb (i.e. 1MB).
         */
        var kBMax = Math.floor(MB * 0.995);

        /**
         * The maximum number of MB we can display before rounding takes us up to
         * 1024MB (i.e. 1GB).
         */
        var MBMax = Math.floor(GB * 0.995);

        return {
            'upload.empty.file': AJS.I18n.getText("upload.empty.file"),
            'upload.too.big': AJS.I18n.getText("upload.too.big"),
            'upload.error.bad.response': AJS.I18n.getText("upload.error.bad.response"),
            'upload.error.unknown.status': AJS.I18n.getText("upload.error.unknown.status"),
            'upload.error.auth': AJS.I18n.getText("upload.error.auth"),
            'upload.error.badrequest': AJS.I18n.getText("upload.error.badrequest"),
            'upload.error.server': AJS.I18n.getText("upload.error.server"),
            'upload.cancel': AJS.I18n.getText("common.words.cancel"),
            //Jack.png - 2 secs left.
            'upload.file.remaining': AJS.I18n.getText("upload.file.remaining"),
            //Jack.png - waiting.
            'upload.file.waiting': AJS.I18n.getText("upload.file.waiting"),
            //Jack.png - stalled.
            'upload.file.stalled': AJS.I18n.getText("upload.file.stalled"),
            'upload.checkbox.title': AJS.I18n.getText("attachfile.attachment.checkbox.title"),
            'upload.xsrf.timeout': AJS.I18n.getText("attachment.error.xsrf.expired"),
            'upload.error.unknown': AJS.I18n.getText("attachment.error.unknown"),
            'upload.progress.title.waiting': AJS.I18n.getText("upload.progress.title.waiting"),
            'upload.progress.title.running': AJS.I18n.getText("upload.progress.title.running"),
            // 16.3/81.1MB, stalled.
            'upload.progress.title.unknown.stalled': AJS.I18n.getText("upload.progress.title.unknown.stalled"),
            //108 kB/s - 16.3/81.1 MB, 10 mins left
            'upload.progress.title.known': AJS.I18n.getText("upload.progress.title.known"),
            //108 kB/s - 16.3/81.1 MB, stalled.
            'upload.progress.title.known.stalled': AJS.I18n.getText("upload.progress.title.known.stalled"),
            'upload.kilobyte': AJS.I18n.getText("upload.kilobyte"),
            'upload.kilobyte.part': AJS.I18n.getText("upload.kilobyte.part"),
            'upload.megabyte': AJS.I18n.getText("upload.megabyte"),
            'upload.megabyte.part': AJS.I18n.getText("upload.megabyte.part"),
            'upload.gigabyte': AJS.I18n.getText("upload.gigabyte"),
            'upload.gigabyte.part': AJS.I18n.getText("upload.gigabyte.part"),
            'upload.seconds': AJS.I18n.getText("upload.seconds"),
            'upload.minutes': AJS.I18n.getText("upload.minutes"),
            'upload.hours': AJS.I18n.getText("upload.hours"),
            'upload.hours.minutes': AJS.I18n.getText("upload.hours.minutes"),
            'upload.bytes.per.second': AJS.I18n.getText("upload.bytes.per.second"),
            'upload.kilobytes.per.second': AJS.I18n.getText("upload.kilobytes.per.second"),
            'upload.megabytes.per.second': AJS.I18n.getText("upload.megabytes.per.second"),
            'upload.error.no.access': AJS.I18n.getText("upload.error.no.access"),
            'upload.error.does.not.exist': AJS.I18n.getText("upload.error.does.not.exist"),
            'upload.error.client.unknown': AJS.I18n.getText("upload.error.client.unknown"),
            'upload.error.too.many.files': AJS.I18n.getText("upload.error.too.many.files"),
            'upload.error.server.no.reply': AJS.I18n.getText("upload.error.server.no.reply"),

            /**
             * Translate the passed key into its associated text. Arguments to the translation can passed as arguments
             * after the key.
             *
             * @param key the key to translate.
             */
            tr: function(key) {
                if (arguments.length == 0) {
                    return undefined;
                } else if (arguments.length == 1) {
                    return this[key] || key;
                } else if (this[key]){
                    var args = InlineAttach.copyArrayLike(arguments);
                    args[0] = this[key];
                    return AJS.format.apply(AJS, args);
                } else {
                    return key;
                }
            },
            /**
             * Return a nice string formatting of the passed file sizes. The size unit (i.e. KB, MB, GB) for all the
             * conversions will be based on the first argument. For example, if you pass three arguments of
             * [1024, 1024*1024, 1024*1024*1024] you will get [1 KB, 1024 KB, 1048576 KB].
             *
             * A single argument will return the translated string. Multiple arguments will return an array of translated
             * strings.
             */
            fileSize: function() {
                if (arguments.length == 0) {
                    return undefined;
                } else {
                    var key, b = InlineAttach.Text._classifySize(arguments[0]);
                    if (b.unit === kB) {
                        key = 'upload.kilobyte';
                    } else if (b.unit === MB) {
                        key = 'upload.megabyte';
                    } else {
                        key = 'upload.gigabyte';
                    }

                    if (arguments.length == 1) {
                        return this.tr(key, b.convert(arguments[0]));
                    } else {
                        var result = new Array(arguments.length);
                        for (var i = 0; i < arguments.length; i++) {
                            result[i] = this.tr(key, b.convert(arguments[i]));
                        }
                        return result;
                    }
                }
            },
            /**
             * Return a string of the form '1.5MB/30 MB' for the passed current and total. The total is used to determine
             * the unit (i.e. kB, MB, GB) that the returned string will be based in.
             */
            currentOutOfTotalSize: function(current, total) {
                var b = this._classifySize(total), key;
                if (b.unit === kB) {
                    key = 'upload.kilobyte.part';
                } else if (b.unit === MB) {
                    key = 'upload.megabyte.part';
                } else {
                    key = 'upload.gigabyte.part';
                }
                return this.tr(key, b.convert(current), b.convert(total));
            },
            _classifySize: function(size) {
                var base;
                if (size <= kBMax) {
                    base = kB;
                } else if (size <= MBMax) {
                    base = MB;
                } else {
                    base = GB;
                }
                return {
                    unit: base,
                    convert: function(s) {
                        return InlineAttach.Text._toDisplay(s / base);
                    }
                };
            },
            /**
             * Return a nice string formatting of the passed time.
             *
             * @param seconds the time in seconds to format.
             */
            time: function(seconds) {
                if (seconds < 60) {
                    return this.tr("upload.seconds", Math.floor(seconds));
                } else {
                    var minutes = seconds / 60;
                    if (minutes < 60) {
                        return this.tr("upload.minutes", Math.floor(minutes));
                    } else {
                        var hours = Math.floor(minutes / 60);
                        minutes = Math.floor(minutes % 60);
                        if (minutes > 0) {
                            return this.tr("upload.hours.minutes", hours, minutes);
                        } else {
                            return this.tr("upload.hours", hours);
                        }
                    }
                }
            },
            /**
             * Return a nice string formatting of the passed rate in B/sec.
             *
             * @param bytesPerSecond the rate to convert.
             */
            rate: function(bytesPerSecond) {
                if (bytesPerSecond <= bMax) {
                    return this.tr("upload.bytes.per.second", InlineAttach.Text._toDisplay(bytesPerSecond));
                } else if (bytesPerSecond <= kBMax) {
                    return this.tr("upload.kilobytes.per.second", InlineAttach.Text._toDisplay(bytesPerSecond / kB));
                } else {
                    return this.tr("upload.megabytes.per.second", InlineAttach.Text._toDisplay(bytesPerSecond / MB));
                }
            },
            _toDisplay: function(number) {

                //Round to two decimal places. IE does not do this correctly so we do it here for all browsers
                //because it ain't that slow.
                if (number < 0.005) {
                    return "0";
                } else {
                    return (Math.round(number * 100) / 100).toFixed(2);
                }
            }
        }
    })();

    return InlineAttach;
});

AJS.namespace('AJS.InlineAttach', null, require('jira/attachment/inline-attach'));
