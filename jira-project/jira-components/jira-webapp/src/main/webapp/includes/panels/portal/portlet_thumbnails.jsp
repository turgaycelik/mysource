<script language="javascript">
//preload images
var aryImages = new Array();
<ww:iterator value="portlets">
aryImages[aryImages.length] = new Image();
aryImages[aryImages.length-1].src = '<%=request.getContextPath()%><ww:property value="thumbnailfile"/>';
var portletPreview;
</ww:iterator>
    function openWindow(imgNumber)
    {
        if (portletPreview && !portletPreview.closed)
        {
            //Resize
            portletPreview.resizeTo(aryImages[imgNumber-1].width + 30, aryImages[imgNumber-1].height + 60);
        }
        portletPreview = window.open(aryImages[imgNumber-1].src, 'PortletPreview', 'status=no,resizable=yes,top=100,left=200,width=' + (aryImages[imgNumber-1].width + 20) + ',height=' + (aryImages[imgNumber-1].height + 20) + ',scrollbars=no');
        portletPreview.opener = self;
        portletPreview.focus();
    }
</script>
