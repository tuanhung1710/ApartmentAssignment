<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h2 class="h4 mb-1">
            <c:choose>
                <c:when test="${staffViewOnly}">Việc được giao</c:when>
                <c:otherwise>Danh sách yêu cầu</c:otherwise>
            </c:choose>
        </h2>
        <p class="text-muted small mb-0">
            <c:choose>
                <c:when test="${staffViewOnly}">
                    Các yêu cầu được giao cho bạn · Tổng <strong>${totalRecords}</strong>
                </c:when>
                <c:otherwise>
                    Tổng <strong>${totalRecords}</strong> yêu cầu
                </c:otherwise>
            </c:choose>
        </p>
    </div>
</div>

<div class="card border-0 shadow-sm mb-3">
    <div class="card-body">
        <form method="get" action="${pageContext.request.contextPath}/request" class="row g-2 align-items-end">
            <input type="hidden" name="action" value="manage"/>

            <div class="col-md-4">
                <label for="status" class="form-label small mb-1">Status</label>
                <select class="form-select form-select-sm" id="status" name="status">
                    <option value="">-- Tất cả --</option>
                    <option value="PENDING" ${status == 'PENDING' ? 'selected' : ''}>PENDING</option>
                    <option value="APPROVED" ${status == 'APPROVED' ? 'selected' : ''}>APPROVED</option>
                    <option value="REJECTED" ${status == 'REJECTED' ? 'selected' : ''}>REJECTED</option>
                    <option value="ASSIGNED" ${status == 'ASSIGNED' ? 'selected' : ''}>ASSIGNED</option>
                    <option value="IN_PROGRESS" ${status == 'IN_PROGRESS' ? 'selected' : ''}>IN_PROGRESS</option>
                    <option value="COMPLETED" ${status == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                    <option value="CANCELLED" ${status == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                </select>
            </div>

            <div class="col-md-4">
                <label for="requestType" class="form-label small mb-1">Request Type</label>
                <select class="form-select form-select-sm" id="requestType" name="requestType">
                    <option value="">-- Tất cả --</option>
                    <option value="REPAIR" ${requestType == 'REPAIR' ? 'selected' : ''}>REPAIR</option>
                    <option value="PARKING" ${requestType == 'PARKING' ? 'selected' : ''}>PARKING</option>
                    <option value="MOVE_IN" ${requestType == 'MOVE_IN' ? 'selected' : ''}>MOVE_IN</option>
                    <option value="MOVE_OUT" ${requestType == 'MOVE_OUT' ? 'selected' : ''}>MOVE_OUT</option>
                    <option value="OTHER" ${requestType == 'OTHER' ? 'selected' : ''}>OTHER</option>
                </select>
            </div>

            <div class="col-md-4 d-flex gap-2">
                <button type="submit" class="btn btn-sm btn-primary">
                    <i class="bi bi-funnel me-1"></i> Lọc
                </button>
                <a class="btn btn-sm btn-outline-secondary"
                   href="${pageContext.request.contextPath}/request?action=manage">
                    <i class="bi bi-arrow-counterclockwise me-1"></i> Reset
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
                    <th scope="col">Request ID</th>
                    <th scope="col">Apartment</th>
                    <th scope="col">Resident</th>
                    <th scope="col">Request Type</th>
                    <th scope="col">Created Date</th>
                    <th scope="col">Status</th>
                    <th scope="col">Assigned Staff</th>
                    <th scope="col" class="text-center">Action</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty requests}">
                        <tr>
                            <td colspan="8" class="text-center text-muted py-4">
                                <i class="bi bi-inbox me-1"></i>
                                Không có yêu cầu nào phù hợp bộ lọc.
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="r" items="${requests}">
                            <tr>
                                <td><span class="fw-semibold">#${r.requestId}</span></td>
                                <td><c:out value="${r.apartmentCode}"/></td>
                                <td><c:out value="${r.createdByName}"/></td>
                                <td>
                                    <span class="badge text-bg-secondary">
                                        <c:out value="${r.requestType}"/>
                                    </span>
                                </td>
                                <td>
                                    <c:if test="${not empty r.createdAt}">
                                        <t:rt value="${r.createdAt}"/>
                                    </c:if>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${r.status == 'PENDING'}">
                                            <span class="badge text-bg-warning">PENDING</span>
                                        </c:when>
                                        <c:when test="${r.status == 'APPROVED'}">
                                            <span class="badge text-bg-info">APPROVED</span>
                                        </c:when>
                                        <c:when test="${r.status == 'REJECTED'}">
                                            <span class="badge text-bg-danger">REJECTED</span>
                                        </c:when>
                                        <c:when test="${r.status == 'ASSIGNED'}">
                                            <span class="badge text-bg-primary">ASSIGNED</span>
                                        </c:when>
                                        <c:when test="${r.status == 'IN_PROGRESS'}">
                                            <span class="badge text-bg-primary">IN_PROGRESS</span>
                                        </c:when>
                                        <c:when test="${r.status == 'COMPLETED'}">
                                            <span class="badge text-bg-success">COMPLETED</span>
                                        </c:when>
                                        <c:when test="${r.status == 'CANCELLED'}">
                                            <span class="badge text-bg-dark">CANCELLED</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-light text-dark">
                                                <c:out value="${r.status}"/>
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty r.assignedToName}">
                                            <c:out value="${r.assignedToName}"/>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-center">
                                    <a class="btn btn-sm btn-outline-primary"
                                       href="${pageContext.request.contextPath}/request?action=detail&amp;id=${r.requestId}"
                                       title="Xem chi tiết">
                                        <i class="bi bi-eye"></i> Xem
                                    </a>
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
            <c:url value="/request" var="paginationUrl">
                <c:param name="action" value="manage"/>
                <c:if test="${not empty status}">
                    <c:param name="status" value="${status}"/>
                </c:if>
                <c:if test="${not empty requestType}">
                    <c:param name="requestType" value="${requestType}"/>
                </c:if>
            </c:url>
            <c:set var="paginationLabel" value="Phân trang xử lý yêu cầu"/>
            <c:set var="paginationAlign" value="justify-content-end"/>
            <%@ include file="/WEB-INF/views/common/pagination.jsp" %>
        </div>
    </c:if>
</div>
