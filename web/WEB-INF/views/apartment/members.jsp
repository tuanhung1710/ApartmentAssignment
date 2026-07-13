<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:url value="/apartment" var="membersBaseUrl">
    <c:param name="action" value="members"/>
    <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}"/></c:if>
    <c:if test="${not empty relationshipFilter}"><c:param name="relationship" value="${relationshipFilter}"/></c:if>
    <c:if test="${not empty statusFilter}"><c:param name="status" value="${statusFilter}"/></c:if>
    <c:if test="${not empty buildingFilter}"><c:param name="building" value="${buildingFilter}"/></c:if>
</c:url>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
    <div>
        <h2 class="h4 mb-1">Danh sách thành viên</h2>
        <p class="text-muted small mb-0">Search · Filter quan hệ/status · Pagination · Export Excel</p>
    </div>
    <div class="d-flex gap-2">
        <c:if test="${canManage}">
            <c:url value="/apartment" var="exportUrl">
                <c:param name="action" value="export-members"/>
                <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}"/></c:if>
                <c:if test="${not empty relationshipFilter}"><c:param name="relationship" value="${relationshipFilter}"/></c:if>
                <c:if test="${not empty statusFilter}"><c:param name="status" value="${statusFilter}"/></c:if>
                <c:if test="${not empty buildingFilter}"><c:param name="building" value="${buildingFilter}"/></c:if>
            </c:url>
            <a class="btn btn-sm btn-success" href="${exportUrl}">
                <i class="bi bi-file-earmark-excel me-1"></i> Export Excel
            </a>
        </c:if>
        <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/apartment?action=list">
            Căn hộ
        </a>
    </div>
</div>

<div class="card shadow-sm mb-3">
    <div class="card-body">
        <form method="get" action="${pageContext.request.contextPath}/apartment" class="row g-2 align-items-end"
              onsubmit="showMembersLoading()">
            <input type="hidden" name="action" value="members">
            <div class="col-md-3">
                <label class="form-label small mb-1">Tìm kiếm</label>
                <input type="text" class="form-control form-control-sm" name="keyword"
                       value="${keyword}" placeholder="Tên, SĐT, CCCD, mã căn...">
            </div>
            <div class="col-md-2">
                <label class="form-label small mb-1">Quan hệ</label>
                <select class="form-select form-select-sm" name="relationship">
                    <option value="" ${empty relationshipFilter ? 'selected' : ''}>Tất cả</option>
                    <c:forEach var="rel" items="${relationshipOptions}">
                        <option value="${rel}" ${relationshipFilter == rel ? 'selected' : ''}>${rel}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-2">
                <label class="form-label small mb-1">Status</label>
                <select class="form-select form-select-sm" name="status">
                    <option value="" ${empty statusFilter ? 'selected' : ''}>Tất cả</option>
                    <option value="ACTIVE" ${statusFilter == 'ACTIVE' ? 'selected' : ''}>ACTIVE (đang ở)</option>
                    <option value="INACTIVE" ${statusFilter == 'INACTIVE' ? 'selected' : ''}>INACTIVE (đã gỡ)</option>
                </select>
            </div>
            <div class="col-md-2">
                <label class="form-label small mb-1">Tòa nhà</label>
                <input type="text" class="form-control form-control-sm" name="building"
                       value="${buildingFilter}" placeholder="VD: Tòa A">
            </div>
            <div class="col-md-3 d-flex gap-2">
                <button type="submit" class="btn btn-sm btn-primary">
                    <i class="bi bi-search me-1"></i> Lọc
                </button>
                <a class="btn btn-sm btn-outline-secondary"
                   href="${pageContext.request.contextPath}/apartment?action=members"
                   onclick="showMembersLoading()">Xóa lọc</a>
            </div>
        </form>
    </div>
</div>

<div class="card shadow-sm position-relative" id="membersListCard">
    <div id="membersLoading" class="list-loading-overlay d-none">
        <div class="text-center text-primary">
            <div class="spinner-border mb-2" role="status"></div>
            <div class="small">Đang tải…</div>
        </div>
    </div>

    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover table-sm align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th>#</th>
                    <th>Họ tên</th>
                    <th>Quan hệ</th>
                    <th>CCCD</th>
                    <th>SĐT</th>
                    <th>Ngày sinh</th>
                    <th>Căn</th>
                    <th>Tòa</th>
                    <th>Status</th>
                    <c:if test="${canManage}"><th>Thao tác</th></c:if>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty members}">
                        <tr>
                            <td colspan="${canManage ? 10 : 9}" class="text-center text-muted py-5">
                                <i class="bi bi-inbox display-6 d-block mb-2"></i>
                                <c:choose>
                                    <c:when test="${hasFilter}">
                                        Không tìm thấy thành viên phù hợp.
                                        <div class="mt-2">
                                            <a href="${pageContext.request.contextPath}/apartment?action=members">Xóa bộ lọc</a>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        Chưa có thành viên nào trong hệ thống.
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="m" items="${members}" varStatus="st">
                            <tr class="${m.isActive ? '' : 'table-secondary'}">
                                <td class="text-muted">${fromIndex + st.index}</td>
                                <td class="fw-semibold"><c:out value="${m.fullName}"/></td>
                                <td><c:out value="${empty m.relationship ? '—' : m.relationship}"/></td>
                                <td><c:out value="${empty m.idNumber ? '—' : m.idNumber}"/></td>
                                <td><c:out value="${empty m.phone ? '—' : m.phone}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty m.dateOfBirth}">—</c:when>
                                        <c:otherwise>${m.dateOfBirth}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/apartment?action=detail&amp;id=${m.apartmentId}">
                                        <c:out value="${m.apartmentCode}"/>
                                    </a>
                                </td>
                                <td><c:out value="${empty m.building ? '—' : m.building}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${m.isActive}">
                                            <span class="badge text-bg-success">ACTIVE</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-secondary">INACTIVE</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <c:if test="${canManage}">
                                    <td>
                                        <a class="btn btn-sm btn-outline-primary"
                                           href="${pageContext.request.contextPath}/apartment?action=edit-member&amp;apartmentId=${m.apartmentId}&amp;memberId=${m.memberId}">
                                            Sửa
                                        </a>
                                    </td>
                                </c:if>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <c:if test="${totalItems > 0}">
        <div class="card-footer bg-white d-flex flex-wrap justify-content-between align-items-center gap-2">
            <div class="small text-muted">
                Hiển thị <strong>${fromIndex}</strong>–<strong>${toIndex}</strong>
                / <strong>${totalItems}</strong> thành viên
                · Trang ${currentPage}/${totalPages}
            </div>
            <nav>
                <ul class="pagination pagination-sm mb-0">
                    <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                        <a class="page-link" onclick="showMembersLoading()"
                           href="${membersBaseUrl}&amp;page=${currentPage - 1}">Trước</a>
                    </li>
                    <c:forEach begin="1" end="${totalPages}" var="i">
                        <c:if test="${i <= 3 || i > totalPages - 2 || (i >= currentPage - 1 && i <= currentPage + 1)}">
                            <li class="page-item ${i == currentPage ? 'active' : ''}">
                                <a class="page-link" onclick="showMembersLoading()"
                                   href="${membersBaseUrl}&amp;page=${i}">${i}</a>
                            </li>
                        </c:if>
                    </c:forEach>
                    <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                        <a class="page-link" onclick="showMembersLoading()"
                           href="${membersBaseUrl}&amp;page=${currentPage + 1}">Sau</a>
                    </li>
                </ul>
            </nav>
        </div>
    </c:if>
</div>

<style>
    .list-loading-overlay {
        position: absolute; inset: 0; background: rgba(255,255,255,.72); z-index: 5;
        display: flex; align-items: center; justify-content: center; border-radius: .375rem;
    }
    .list-loading-overlay.d-none { display: none !important; }
</style>
<script>
    function showMembersLoading() {
        var el = document.getElementById('membersLoading');
        if (el) el.classList.remove('d-none');
    }
</script>
