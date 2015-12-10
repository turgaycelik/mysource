AJS.$.fn.fancybox.defaults.titleFormat = function(title) {
    if (title && title.length) {
        title = AJS.escapeHtml(title);
        if (this.titlePosition == 'float') {
            return '<table id="fancybox-title-float-wrap" cellpadding="0" cellspacing="0"><tr><td id="fancybox-title-float-left"></td><td id="fancybox-title-float-main">' + title + '</td><td id="fancybox-title-float-right"></td></tr></table>';
        }
        return '<div id="fancybox-title-' + this.titlePosition + '">' + title + '</div>';
    }
};
