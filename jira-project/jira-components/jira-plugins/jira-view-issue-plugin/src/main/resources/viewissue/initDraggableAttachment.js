(function($) {
    $.event.props.push("dataTransfer");

    $('[draggable=true]').live("dragstart", function(evt) {
        var $this = AJS.$(this);
        var dataTransfer = evt.dataTransfer;
        dataTransfer.effectAllowed = 'copy';
        dataTransfer.dropEffect = 'copy';
        dataTransfer.setData("DownloadURL", $this.data('downloadurl'));
        if (!$this.find('img').length) {
            dataTransfer.setDragImage($this.closest('li').find('img:visible')[0], 8, 8);
        }
    });
})(AJS.$);