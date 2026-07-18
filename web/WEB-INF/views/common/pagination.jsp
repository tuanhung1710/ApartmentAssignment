<%--
  Compact pagination: Prev · 1 … nearby … last · Next
  Required request/page attributes (set before include):
    - paginationUrl   : base URL with query params (no page param)
    - currentPage     : int, 1-based
    - totalPages      : int
  Optional:
    - pageParam       : query param name, default "page"
    - paginationLabel : aria-label, default "Phân trang"
    - paginationAlign : extra ul classes, e.g. "justify-content-center"
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="pageParamName" value="${empty pageParam ? 'page' : pageParam}"/>
<c:set var="ariaLabel" value="${empty paginationLabel ? 'Phân trang' : paginationLabel}"/>
<c:set var="alignClass" value="${empty paginationAlign ? '' : paginationAlign}"/>
<c:set var="delta" value="1"/>

<c:if test="${totalPages > 1}">
    <c:set var="sep" value="${fn:contains(paginationUrl, '?') ? '&' : '?'}"/>
    <nav aria-label="${ariaLabel}">
        <ul class="pagination pagination-sm mb-0 flex-wrap ${alignClass}">
            <%-- Previous --%>
            <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                <c:choose>
                    <c:when test="${currentPage <= 1}">
                        <span class="page-link">Trước</span>
                    </c:when>
                    <c:otherwise>
                        <a class="page-link" href="${paginationUrl}${sep}${pageParamName}=${currentPage - 1}">Trước</a>
                    </c:otherwise>
                </c:choose>
            </li>

            <c:forEach begin="1" end="${totalPages}" var="i">
                <c:set var="showPage" value="false"/>
                <c:if test="${i == 1 || i == totalPages}">
                    <c:set var="showPage" value="true"/>
                </c:if>
                <c:if test="${i >= currentPage - delta && i <= currentPage + delta}">
                    <c:set var="showPage" value="true"/>
                </c:if>

                <c:choose>
                    <c:when test="${showPage}">
                        <li class="page-item ${i == currentPage ? 'active' : ''}">
                            <c:choose>
                                <c:when test="${i == currentPage}">
                                    <span class="page-link">${i}</span>
                                </c:when>
                                <c:otherwise>
                                    <a class="page-link" href="${paginationUrl}${sep}${pageParamName}=${i}">${i}</a>
                                </c:otherwise>
                            </c:choose>
                        </li>
                    </c:when>
                    <c:when test="${i == 2 && currentPage - delta > 2}">
                        <li class="page-item disabled" aria-hidden="true">
                            <span class="page-link">…</span>
                        </li>
                    </c:when>
                    <c:when test="${i == totalPages - 1 && currentPage + delta < totalPages - 1}">
                        <li class="page-item disabled" aria-hidden="true">
                            <span class="page-link">…</span>
                        </li>
                    </c:when>
                </c:choose>
            </c:forEach>

            <%-- Next --%>
            <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                <c:choose>
                    <c:when test="${currentPage >= totalPages}">
                        <span class="page-link">Sau</span>
                    </c:when>
                    <c:otherwise>
                        <a class="page-link" href="${paginationUrl}${sep}${pageParamName}=${currentPage + 1}">Sau</a>
                    </c:otherwise>
                </c:choose>
            </li>
        </ul>
    </nav>
</c:if>
