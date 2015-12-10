;(function() {
    var FormDialog = require('jira/dialog/form-dialog');
    var $ = require('jquery');

    $(function() {
        $('.actions a.delete').each(function() {
            new FormDialog({
                trigger: '#' + this.id,
                autoClose: true
            });
        });

        $('.runners .actions .show-details').click(function() {
            var $this = $(this);
            var $rows = $('.job-details[data-runner-id="' + $this.data('runner-id') + '"]');

            if ($rows.length > 0) {
                if ($($rows[0]).hasClass('hidden')) {
                    $rows.removeClass('hidden');
                    $this.text(AJS.I18n.getText('common.words.show.less'));
                } else {
                    $rows.addClass('hidden');
                    $this.text(AJS.I18n.getText('common.words.show.more'));
                }
            }
        });
    });
})();
