<%@ taglib uri="webwork" prefix="ww" %>
<thead>
    <tr>
        <ww:if test="parameters['sort'] != null && parameters['sort'] == true">
            <ww:if test="parameters['name'] != false">
                <th class="<ww:property value="parameters['viewHelper']/generateSortCssClass('name')"/>" >
                    <a id="page_sort_name" href="<ww:property value="parameters['viewHelper']/generateSortUrl('name')"/>"><ww:text name="'common.words.name'"/>&nbsp;<ww:property value="parameters['viewHelper']/generateSortIcon('name')" escape="false"/></a>
                </th>
            </ww:if>
            <ww:if test="parameters['portalPageList'] != null && parameters['portalPageList']/size > 0">
                <ww:if test="parameters['owner'] != 'false'">
                    <th class="<ww:property value="parameters['viewHelper']/generateSortCssClass('owner')"/>" style="width:20%">
                        <a id="page_sort_owner" href="<ww:property value="parameters['viewHelper']/generateSortUrl('owner')"/>"><ww:text name="'common.concepts.owner'"/>&nbsp;<ww:property value="parameters['viewHelper']/generateSortIcon('owner')" escape="false"/></a>
                    </th>
                </ww:if>
                <ww:if test="parameters['shares'] != 'false'">
                    <th><ww:text name="'common.concepts.shared.with'"/></th>
                </ww:if>
                <ww:if test="parameters['favcount'] != 'false'">
                    <th class="<ww:property value="parameters['viewHelper']/generateSortCssClass('favcount')"/>" style="width:5%" title="<ww:text name="'portalpage.favourite.count.desc'"/>">
                        <a id="page_sort_popularity" href="<ww:property value="parameters['viewHelper']/generateSortUrl('favcount')"/>"><ww:text name="'common.concepts.popularity'"/>&nbsp;<ww:property value="parameters['viewHelper']/generateSortIcon('favcount')" escape="false"/></a>
                    </th>
                </ww:if>
                <ww:if test="parameters['ordering'] != 'false'">
                    <th></th>
                </ww:if>
                <ww:if test="parameters['operations'] != 'false'">
                    <th></th>
                </ww:if>
            </ww:if>
        </ww:if>
        <ww:else>
            <ww:if test="parameters['name'] != false">
                <th><ww:text name="'common.words.name'"/></th>
            </ww:if>
            <ww:if test="parameters['portalPageList'] != null && parameters['portalPageList']/size > 0">
                <ww:if test="parameters['owner'] != 'false'">
                    <th><ww:text name="'common.concepts.owner'"/></th>
                </ww:if>
                <ww:if test="parameters['shares'] != 'false'">
                    <th><ww:text name="'common.concepts.shared.with'"/></th>
                </ww:if>
                <ww:if test="parameters['favcount'] != 'false'">
                    <th title="<ww:text name="'portalpage.favourite.count.desc'"/>"><ww:text name="'common.concepts.popularity'"/></th>
                </ww:if>
                <ww:if test="parameters['ordering'] != 'false'">
                    <th></th>
                </ww:if>
                <ww:if test="parameters['operations'] != 'false'">
                    <th></th>
                </ww:if>
            </ww:if>
        </ww:else>
    </tr>
</thead>
