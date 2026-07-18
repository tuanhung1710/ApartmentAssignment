<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
    <div>
        <h2 class="h4 mb-1">Yêu cầu của tôi</h2>
        <p class="text-muted small mb-0">
            <c:if test="${not empty myApartment}">
                Căn hộ: <strong>${myApartment.apartmentCode}</strong>
                ·
            </c:if>
            Đang mở: <strong>${openCount}</strong>
        </p>
    </div>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/request?action=create">
        <i class="bi bi-plus-circle me-1"></i> Gửi yêu cầu
    </a>
</div>

<div class="card shadow-sm mb-3">
    <div class="card-body">
        <form method="get" action="${pageContext.request.contextPath}/request" class="row g-2 align-items-end">
            <input type="hidden" name="action" value="my"/>
            <div class="col-md-3">
                <label class="form-label small mb-1">Trạng thái</label>
                <select name="status" class="form-select form-select-sm">
                    <option value="">Tất cả</option>
                    <option value="PENDING" ${filterStatus == 'PENDING' ? 'selected' : ''}>PENDING</option>
                    <option value="APPROVED" ${filterStatus == 'APPROVED' ? 'selected' : ''}>APPROVED</option>
                    <option value="REJECTED" ${filterStatus == 'REJECTED' ? 'selected' : ''}>REJECTED</option>
                    <option value="ASSIGNED" ${filterStatus == 'ASSIGNED' ? 'selected' : ''}>ASSIGNED</option>
                    <option value="IN_PROGRESS" ${filterStatus == 'IN_PROGRESS' ? 'selected' : ''}>IN_PROGRESS</option>
                    <option value="COMPLETED" ${filterStatus == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                    <option value="CANCELLED" ${filterStatus == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                </select>
            </div>
            <div class="col-md-3">
                <label class="form-label small mb-1">Loại</label>
                <select name="type" class="form-select form-select-sm">
                    <option value="">Tất cả</option>
                    <option value="REPAIR" ${filterType == 'REPAIR' ? 'selected' : ''}>Sửa chữa</option>
                    <option value="PARKING" ${filterType == 'PARKING' ? 'selected' : ''}>Trông xe</option>
                    <option value="MOVE_IN" ${filterType == 'MOVE_IN' ? 'selected' : ''}>Chuyển đồ vào</option>
                    <option value="MOVE_OUT" ${filterType == 'MOVE_OUT' ? 'selected' : ''}>Chuyển đồ ra</option>
                    <option value="OTHER" ${filterType == 'OTHER' ? 'selected' : ''}>Khác</option>
                </select>
            </div>
            <div class="col-md-4">
                <label class="form-label small mb-1">Tìm kiếm</label>
                <input type="text" name="keyword" class="form-control form-control-sm"
                       value="${filterKeyword}" placeholder="Tiêu đề / mô tả..."/>
            </div>
            <div class="col-md-2 d-grid">
                <button type="submit" class="btn btn-sm btn-outline-primary">
                    <i class="bi bi-funnel"></i> Lọc
                </button>
            </div>
        </form>
    </div>
</div>

<div class="card shadow-sm">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr>
                <th style="width:70px">#</th>
                <th>Tiêu đề</th>
                <th>Loại</th>
                <th>Trạng thái</th>
                <th>Ngày gửi</th>
                <th style="width:100px"></th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty requests}">
                    <tr>
                        <td colspan="6" class="text-center text-muted py-4">
                            Chưa có yêu cầu nào.
                            <a href="${pageContext.request.contextPath}/request?action=create">Gửi yêu cầu đầu tiên</a>
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="r" items="${requests}">
                        <tr>
                            <td class="text-muted">${r.requestId}</td>
                            <td>
                                <a class="text-decoration-none fw-semibold"
                                   href="${pageContext.request.contextPath}/request?action=detail&amp;id=${r.requestId}">
                                    ${r.title}
                                </a>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${r.requestType == 'REPAIR'}"><span class="badge text-bg-warning">Sửa chữa</span></c:when>
                                    <c:when test="${r.requestType == 'PARKING'}"><span class="badge text-bg-info">Trông xe</span></c:when>
                                    <c:when test="${r.requestType == 'MOVE_IN'}"><span class="badge text-bg-success">Chuyển vào</span></c:when>
                                    <c:when test="${r.requestType == 'MOVE_OUT'}"><span class="badge text-bg-secondary">Chuyển ra</span></c:when>
                                    <c:otherwise><span class="badge text-bg-light text-dark">Khác</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${r.status == 'PENDING'}"><span class="badge text-bg-warning">PENDING</span></c:when>
                                    <c:when test="${r.status == 'APPROVED'}"><span class="badge text-bg-primary">APPROVED</span></c:when>
                                    <c:when test="${r.status == 'REJECTED'}"><span class="badge text-bg-danger">REJECTED</span></c:when>
                                    <c:when test="${r.status == 'ASSIGNED'}"><span class="badge text-bg-info">ASSIGNED</span></c:when>
                                    <c:when test="${r.status == 'IN_PROGRESS'}"><span class="badge text-bg-primary">IN_PROGRESS</span></c:when>
                                    <c:when test="${r.status == 'COMPLETED'}"><span class="badge text-bg-success">COMPLETED</span></c:when>
                                    <c:when test="${r.status == 'CANCELLED'}"><span class="badge text-bg-secondary">CANCELLED</span></c:when>
                                    <c:otherwise><span class="badge text-bg-light text-dark">${r.status}</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="small">
                                <%-- Realtime VN: hôm nay HH:mm · khác ngày dd/MM HH:mm --%>
                                <t:rt value="${r.createdAt}"/>
                            </td>
                            <td class="text-end">
                                <a class="btn btn-sm btn-outline-primary"
                                   href="${pageContext.request.contextPath}/request?action=detail&amp;id=${r.requestId}">
                                    Chi tiết
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>

    <c:if test="${totalPages > 1}">
        <div class="card-footer">
            <c:url value="/request" var="paginationUrl">
                <c:param name="action" value="my"/>
                <c:if test="${not empty filterStatus}">
                    <c:param name="status" value="${filterStatus}"/>
                </c:if>
                <c:if test="${not empty filterType}">
                    <c:param name="type" value="${filterType}"/>
                </c:if>
                <c:if test="${not empty filterKeyword}">
                    <c:param name="keyword" value="${filterKeyword}"/>
                </c:if>
            </c:url>
            <nav>
                <ul class="pagination pagination-sm mb-0 justify-content-end">
                    <c:forEach begin="1" end="${totalPages}" var="i">
                        <li class="page-item ${i == currentPage ? 'active' : ''}">
                            <a class="page-link" href="${paginationUrl}&amp;page=${i}">${i}</a>
                        </li>
                    </c:forEach>
                </ul>
            </nav>
        </div>
    </c:if>
</div>
