<%@ taglib uri="webwork" prefix="ww" %>
<ww:if test="filter/start > 0">
    <a class="icon icon-previous" href="javascript:moveToPage(<ww:property value="filter/previousStart" />)"><span><ww:text name="'common.words.previous'" /></span></a>
</ww:if>
<ww:property value = "pager/pages(/browsableItems)">
<ww:if test="size > 1">
    <ww:iterator value="." status="'pagerStatus'">
        <ww:if test="currentPage == true"><strong><ww:property value="pageNumber" /></strong></ww:if>
        <ww:else>
            <a href="javascript:moveToPage(<ww:property value="start" />)"><ww:property value="pageNumber" /></a>
        </ww:else>
    </ww:iterator>
</ww:if>
</ww:property>
<ww:if test="filter/end < /browsableItems/size">
    <a class="icon icon-next" href="javascript:moveToPage(<ww:property value="filter/nextStart" />)"><span><ww:text name="'common.words.next'" /></span></a>
</ww:if>
