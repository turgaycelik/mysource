define('jira/ajs/ajax/smart-ajax', [
    'jquery',
    'aui/params'
], function (
    $,
    params
) {

    /**
     * The Ajax handling in JQuery is broken to some degree.  For example if the server is down (eg cant talk to it at all) then jQuery runs
     * through the success path, but with no data.  And if you timeout, and you touch the xhr object then you can cause
     * DOM exceptions because its not ready to be called and so forth.
     *
     * So this wrapper code allows us to make an AJAX call and go down the right handler paths and in a sensible manner
     *
     * This is intended to be similar to the jQuery.ajax() method but without the wrinkles.
     *
     * @namespace SmartAjax
     */
    var SmartAjax = {};

    /**
     * This object constructor creates an object with the following shape
     *
     * {
     *   successful  : Boolean,             // a quick indication on whether the result when down the success path or error path
     *   status      : Number,              // the HTTP status of the call.  0 can mean it didnt happen at all
     *   statusText  : String,              // the HTTP status text - can be errors such as 'timeout' and 'commserror' as well
     *   errorThrown : Object,              // the JavaScript error thrown in the case of error
     *   readyState  : Number,              // the ready state of the xhr object
     *   hasData     : Boolean,             // true if the data is present in the response
     *   data        : String,              // any data that may have come down
     *   xhr         : XmlHttpRequest       // the xhr object in play
     *   requestId   : Number               // the unique request id of this ajax call
     *   aborted     : Boolean               // whether or not the request has been aborted. Note, aborting can be caused by timeout.
     * }
     *
     *
     * Why is this object needed.  Well turns out its quite hard in jQuery to get a true handle on the state of the ajax request.  Lots of
     * inference and code path detection.  So we create this 'smart result' to that you can look in the one place regardless of whether
     * it is error handler, success handler or complete handler.
     *
     * When then throw this smart object back as the last parameter on all the jQuery handlers.  This way they can be more easily written
     * to detect and handle errors.  In fact if we just passed around this object to lower layers we could simplify the parameters
     * a fair bit and still be in a better position about how things when.  This guy is REALLY useful in the complete handler
     * to work out if the AJAX request was successful or not.
     *
     * In fact you could re-code many ajax calls as on complete() only like this.
     *
     * ajaxOptions = {
     *      complete : function(function (xhr, textStatus, smartAjaxResult) {
     *           if (smartAjaxResult.successful) {
     *               // good code path
     *              handleSuccess(smartAjaxResult);
     *           } else {
     *              // error code path
     *              handleFailure(smartAjaxResult);
     *           }
     *   }
     */
    SmartAjax.SmartAjaxResult = function(xhr, requestId, statusText, data, successful, errorThrown)
    {
        // in some browser you are NOT allowed to touch the xhr.status variable otherwise it throws a DOM exception
        // depending on the state of the xhr request.  So lets be super careful.
        var status = tryIt(function()
        {
            return xhr.status;
        }, 0);

        var result = {
            successful  : successful,
            status      : status,
            statusText  : statusText,
            errorThrown : errorThrown,
            readyState  : xhr.readyState,
            hasData     : data != null && data.length > 0,
            data        : data,
            xhr         : xhr,
            aborted     : xhr.aborted,
            requestId   : requestId,
            validationError: !!(xhr.status === 400 && data && data.errors)
        };
        result.toString = function()
        {
            return '{\n'
                    + 'successful  : ' + this.successful + ',\n'
                    + 'status      : ' + this.status + ',\n'
                    + 'statusText  : ' + this.statusText + ',\n'
                    + 'hasData     : ' + this.hasData + ',\n'
                    + 'readyState  : ' + this.readyState + ',\n'
                    + 'requestId   : ' + this.requestId + ',\n'
                    + 'aborted     : ' + this.aborted + ',\n'
                    + '}';
        };

        return result;
    };

    /**
     * These are the errors that can come back from an AJAX call according to the jQuery documentation
     */
    SmartAjax.SmartAjaxResult.ERROR = 'error';
    SmartAjax.SmartAjaxResult.TIMEOUT = 'timeout';
    SmartAjax.SmartAjaxResult.NOTMODIFIED = 'notmodified';
    SmartAjax.SmartAjaxResult.PARSEERROR = 'parseerror';

    /**
     * This wrapper for the jQuery ajax() method ensure that the error() path is taken for errors.  Remarkable!
     *
     * It also passes in a special JIRA.SmartAjax.SmartAjaxResult object that can be used to much like the xhr object but without
     * exceptions being thrown.
     *
     * @method makeRequest
     * @param {Object} ajaxOptions - the options to control the ajax call
     * @returns the xhr object just like jQuery.ajax() does
     */
    SmartAjax.makeRequest = function(ajaxOptions)
    {
        var _smartAjaxResult = {};

        var log = function(calltype, requestId, msg)
        {
            if (AJS.log)
            {
                var id = requestId ? '[' + requestId + '] ' : ' ';
                /* [logging] */
                AJS.log('ajax' + id + calltype + ' : ' + msg);
                /* [logging] end */
            }
        };

        var generateRequestId = function()
        {
            var now = new Date();
            var midnight = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0);
            var ms = (now.getTime() - midnight.getTime());
            return Math.max(Math.floor(ms), 1);
        };

        var errorHandler = function(xhr, statusText, errorThrown, smartAjaxResult)
        {
            if (! smartAjaxResult)
            {
                var data = tryIt(function()
                {
                    return xhr.responseText;
                }, '');
                smartAjaxResult = _smartAjaxResult = new SmartAjax.SmartAjaxResult(xhr, _requestId, statusText, data, false, errorThrown);
            }

            log('error', smartAjaxResult.requestId, smartAjaxResult);

            if ($.isFunction(ajaxOptions.error))
            {
                ajaxOptions.error(xhr, statusText, errorThrown, smartAjaxResult);
            }
        };

        var successHandler = function(data, statusText, xhr)
        {
            //
            // but was it a really a success .  It may have been aborted or never made it to
            // the server at all.  John Resig himself talks about it here :
            //
            // http://groups.google.com/group/jquery-dev/browse_thread/thread/3d8f7ac78c9b0117
            //
            if (xhr.status < 100)
            {
                _smartAjaxResult = new SmartAjax.SmartAjaxResult(xhr, _requestId, SmartAjax.SmartAjaxResult.ERROR, '', false);
                errorHandler(xhr, SmartAjax.SmartAjaxResult.ERROR, undefined, _smartAjaxResult);
                return;
            }

            // ok by this stage we consider the request Kosher!
            _smartAjaxResult = new SmartAjax.SmartAjaxResult(xhr, _requestId, statusText, data, true);

            //log('success', _smartAjaxResult.requestId, _smartAjaxResult);

            if ($.isFunction(ajaxOptions.success))
            {
                ajaxOptions.success(data, statusText, xhr, _smartAjaxResult);
            }
        };

        var completeHandler = function(xhr, textStatus)
        {
            if ($.isFunction(ajaxOptions.complete))
            {
                ajaxOptions.complete(xhr, textStatus, _smartAjaxResult);
            }
        };

        // the default options we'll apply to all AJAX requests
        var DEFAULT_AJAX_OPTS = {
            // the function used to perform the AJAX request (defaults to AJS.$.ajax)
            jqueryAjaxFn: $.ajax,

            // HTTP request headers
            headers: {
                // username of logged in user on page load. see JRADEV-6999
                'X-AUSERNAME': encodeURIComponent(params.loggedInUser)
            }
        };

        // recursively merge the default and user-provided options
        var ourAjaxOptions = $.extend(true, {}, DEFAULT_AJAX_OPTS, ajaxOptions);

        // but use our our handlers that delegate back to them
        ourAjaxOptions.error = errorHandler;
        ourAjaxOptions.success = successHandler;
        ourAjaxOptions.complete = completeHandler;

        var xhr = ourAjaxOptions.jqueryAjaxFn.apply(ourAjaxOptions, [ourAjaxOptions]);
        var _requestId = generateRequestId();

        /**
         * xhr.abort() is busted on IE7 and jQuery 1.4.1 and we need to create our XHR objects differently on this
         * browser.
         *
         * http://forum.jquery.com/topic/object-doesn-t-support-this-property-or-method-from-jquery-1-4-1-in-ie7-only
         *
         * http://stackoverflow.com/questions/2949179/jquery-error-when-aborting-an-ajax-call-only-in-internet-explorer
         *
         * The reason is that while IE7 does have a window.XMLHttpRequest function, it broken for abort.  But the ActiveX
         * version of the same thing is not broken.  Typical IE shittyness.
         */
        try {
            xhr.abort = function(oldabort) {
                return function() {
                    log('aborted', _requestId, '');
                    xhr.aborted = true;
                    if ($.isFunction(oldabort)) {
                        try {
                            oldabort.apply(xhr, arguments);
                        } catch (ex) {
                            // on IE7 we try our best with abort but it throws exceptions
                            // so we handle them but know that the request continues on regardless
                        }
                    }
                };
            }(xhr.abort);
        } catch (ex) {
            // Accessing or assigning to xhr.abort has failed. In this case, we'll assume the native
            // xhr.abort() method without injecting logging.
        }

        //log('started', _requestId, '' + ourAjaxOptions.url);
        return xhr;
    };
    /**
     * This function will build standardized error messages for handling AJAX errors.
     *
     * If the request has, then it will be treated as HTML and its content body stripped
     * and returned.
     *
     * If the AJAX call didnt get any data then it will generate an appropriate error message.
     *
     *
     * @method buildDialogErrorContent
     * @param {Object} smartAjaxResult - the JIRA.SmartAjax.SmartAjaxResult in play
     * @param {Boolean} noHeader - if this is true then no header will be placed on the error message
     */
    SmartAjax.buildDialogErrorContent = function(smartAjaxResult, noHeader) {
        //
        // If its 401 unauthorised then the data returned is most likely a Tomcat 401 generated HMTML message
        // which we dont want to use.  We want to be specific about how we handle these guys
        //
        var fourHundredClass = Math.floor(smartAjaxResult.status / 100);
        if (smartAjaxResult.hasData && fourHundredClass != 4) {
            return wrapDialogErrorContent(AJS.extractBodyFromResponse(smartAjaxResult.data));
        }
        else {
            var errMsg = buildRawHttpErrorMessage(smartAjaxResult);
            return buildDialogAjaxErrorMessage(errMsg, noHeader);
        }
    };

    /**
     * This method is called when you have an AJAX error but you are not a dialog that can contain
     * the 500 page or your simply dont want to display the error message that way.  Instead you
     * want a simple error message string and the caller takes on the responsibility of marking it up
     *
     * @method buildSimpleErrorContent
     * @param {Object} smartAjaxResult - the JIRA.SmartAjax.SmartAjaxResult in play
     * @param {Object} options - alert : true if the messages is to be displayed in an alert box
     */
    SmartAjax.buildSimpleErrorContent = function(smartAjaxResult, options) {
        return buildRawHttpErrorMessage(smartAjaxResult, options ? options : {});
    };

    //
    // The reason we have the alert box parameter is that that error messages may contain HTML. But the callers
    // are using alert().  Until we have a component that can easily make a rich alert box, we will stick with this work
    // around
    //
    function buildRawHttpErrorMessage(smartAjaxResult, options) {

        // We might be inside an <iframe>, e.g., gadgets, so use the top level AJS.
        var AJS = window.top.AJS;
        var errMsg;
        if (smartAjaxResult.statusText == SmartAjax.SmartAjaxResult.TIMEOUT) {
            errMsg = AJS.I18n.getText("common.forms.ajax.timeout");
        }
        else if (smartAjaxResult.status == 401) {
            errMsg = AJS.I18n.getText('common.forms.ajax.unauthorised.alert');
        }
        else if (smartAjaxResult.hasData) {
            errMsg = AJS.I18n.getText("common.forms.ajax.servererror");
        }
        else {
            errMsg = AJS.I18n.getText("common.forms.ajax.commserror");
        }
        return errMsg;
    }

    function buildDialogAjaxErrorMessage(errorMessage, noHeader) {
        var errorContent = '<div class="aui-message error"><span class="aui-icon icon-error"></span>' +
                '<p>' + errorMessage + '</p>' +
                '<p>' + AJS.I18n.getText("common.forms.ajax.error.dialog") + '</p>' +
                '</div>';
        if (! noHeader) {
            errorContent = '<h1>' + AJS.I18n.getText("common.forms.ajax.error.dialog.heading") + '</h1>' + errorContent;
        }
        return wrapDialogErrorContent(errorContent);
    }

    function wrapDialogErrorContent(content) {
        var $container = $('<div class="ajaxerror"/>');
        $container.append(content);
        return $container;
    }

    return SmartAjax;
});

/**
 * @namespace JIRA.SmartAjax
 */
AJS.namespace('JIRA.SmartAjax', null, require('jira/ajs/ajax/smart-ajax'));
/** Preserve legacy namespace
    @deprecated jira.ajax */
AJS.namespace("jira.ajax", null, require('jira/ajs/ajax/smart-ajax'));
