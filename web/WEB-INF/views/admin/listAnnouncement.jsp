<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
    <div>
        <h2 class="h4 mb-1">Danh sách thông báo</h2>
        <p class="text-muted small mb-0">
            UC-ADM-06 · Tổng <strong>${totalRecords}</strong> thông báo
        </p>
    </div>
    <%-- UC-ADM-06 Add Announcement --%>
    <a class="btn btn-sm btn-primary"
       href="${pageContext.request.contextPath}/admin?action=add-announcement">
        <i class="bi bi-plus-circle me-1"></i> Add Announcement
    </a>
</div>

<div class="card border-0 shadow-sm mb-3">
    <div class="card-body">
        <form method="get" action="${pageContext.request.contextPath}/admin" class="row g-2 align-items-end">
            <input type="hidden" name="action" value="announcements"/>
            <div class="col-md-4">
                <label for="status" class="form-label small mb-1">Status</label>
                <select class="form-select form-select-sm" id="status" name="status">
                    <option value="">-- Tất cả --</option>
                    <option value="published" ${status == 'published' ? 'selected' : ''}>Published</option>
                    <option value="draft" ${status == 'draft' ? 'selected' : ''}>Draft</option>
                </select>
            </div>
            <div class="col-md-4 d-flex gap-2">
                <button type="submit" class="btn btn-sm btn-primary">
                    <i class="bi bi-funnel me-1"></i> Lọc
                </button>
                <a class="btn btn-sm btn-outline-secondary"
                   href="${pageContext.request.contextPath}/admin?action=announcements">
                    Reset
                </a>
            </div>
        </form>
    </div>
</div>

<div class="card border-0 shadow-sm">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th scope="col" style="width: 4rem;">ID</th>
                    <th scope="col">Title</th>
                    <th scope="col">Content</th>
                    <th scope="col">Status</th>
                    <th scope="col">Created Date</th>
                    <th scope="col" class="text-center">Action</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty announcements}">
                        <tr>
                            <td colspan="6" class="text-center text-muted py-4">
                                <i class="bi bi-inbox me-1"></i>
                                Không có thông báo nào.
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="a" items="${announcements}">
                            <tr>
                                <td><span class="fw-semibold">#${a.announcementId}</span></td>
                                <td>
                                    <div class="fw-semibold"><c:out value="${a.title}"/></div>
                                    <c:if test="${not empty a.category}">
                                        <span class="badge text-bg-light text-dark">
                                            <c:out value="${a.category}"/>
                                        </span>
                                    </c:if>
                                </td>
                                <td style="max-width: 22rem;">
                                    <c:choose>
                                        <c:when test="${fn:length(a.content) > 120}">
                                            <c:out value="${fn:substring(a.content, 0, 120)}"/>...
                                        </c:when>
                                        <c:otherwise>
                                            <c:out value="${a.content}"/>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${a.isPublished}">
                                            <span class="badge text-bg-success">Published</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-secondary">Draft</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:if test="${not empty a.createdAt}">
                                        <t:rt value="${a.createdAt}" mode="full"/>
                                    </c:if>
                                </td>
                                <td class="text-center text-nowrap">
                                    <%-- Placeholder UC sau: Edit / Delete --%>
                                    <div class="btn-group btn-group-sm" role="group">
                                        <a class="btn btn-outline-primary"
                                           href="${pageContext.request.contextPath}/admin?action=edit-announcement&amp;id=${a.announcementId}"
                                           title="Edit Announcement">
                                            <i class="bi bi-pencil"></i>
                                        </a>
                                        <a class="btn btn-outline-danger"
                                           href="${pageContext.request.contextPath}/admin?action=delete-announcement&amp;id=${a.announcementId}"
                                           title="Delete Announcement"
                                           onclick="return confirm('Xóa vĩnh viễn thông báo #${a.announcementId}? Hành động không hoàn tác.');">
                                            <i class="bi bi-trash"></i>
                                        </a>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <c:if test="${totalPages > 1}">
        <div class="card-footer bg-white d-flex justify-content-end">
            <c:url value="/admin" var="paginationUrl">
                <c:param name="action" value="announcements"/>
                <c:if test="${not empty status}">
                    <c:param name="status" value="${status}"/>
                </c:if>
            </c:url>
            <c:set var="paginationLabel" value="Phân trang thông báo"/>
            <c:set var="paginationAlign" value="justify-content-end"/>
            <%@ include file="/WEB-INF/views/common/pagination.jsp" %>
        </div>
    </c:if>
</div>
