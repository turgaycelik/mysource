<%@ taglib uri="webwork" prefix="ww" %>
<ww:if test="parameters['portalPageList'] != null && parameters['portalPageList']/size > 0 && parameters['paging'] != null && parameters['paging'] == true">
    <ww:if test="parameters['pagingNextUrl'] != null || parameters['pagingPrevUrl'] != null">
        <div class="aui-group count-pagination">
            <div class="pagination aui-item">
            <ww:if test="parameters['pagingPrevUrl'] != null">
                <a class="icon icon-previous" href="<ww:property value="parameters['pagingPrevUrl']"/>"><span><ww:text name="'common.forms.previous'"/></span></a>
            </ww:if>
            <ww:if test="parameters['pagingPrevUrl'] == null && parameters['pagingNextUrl'] == null">
                &nbsp;
            </ww:if>
            <ww:if test="parameters['pagingMessage'] != null">
                <span><ww:property value="parameters['pagingMessage']"/></span>
            </ww:if>
            <ww:if test="parameters['pagingNextUrl'] != null">
                <a class="icon icon-next" href="<ww:property value="parameters['pagingNextUrl']"/>"><span><ww:text name="'common.forms.next'"/></span></a>
            </ww:if>
            </div>
        </div>
    </ww:if>
</ww:if> 