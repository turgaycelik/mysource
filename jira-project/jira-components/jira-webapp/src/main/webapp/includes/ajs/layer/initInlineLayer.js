AJS.namespace('AJS.InlineLayer', null, require('jira/ajs/layer/inline-layer'));
AJS.namespace('AJS.InlineLayer.create', null, require('jira/ajs/layer/inline-layer-factory').createInlineLayers);

AJS.namespace('AJS.InlineLayer.OptionsDescriptor', null, require('jira/ajs/layer/inline-layer/options-descriptor'));

AJS.namespace('AJS.InlineLayer.StandardPositioning', null, require('jira/ajs/layer/inline-layer/standard-positioning'));
AJS.namespace('AJS.InlineLayer.WindowPositioning', null, require('jira/ajs/layer/inline-layer/window-positioning'));
AJS.namespace('AJS.InlineLayer.IframePositioning', null, require('jira/ajs/layer/inline-layer/iframe-positioning'));

JIRA.bind(AJS.InlineLayer.EVENTS.beforeHide, function(e) {
    if (typeof Calendar !== "undefined" && Calendar.current) {
        e.preventDefault();
    }
});
