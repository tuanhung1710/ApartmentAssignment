<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%-- TV4 – Resident xem chi tiết + history (UC-REQ-07) + hủy (UC-REQ-08) --%>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
    <div>
        <h2 class="h4 mb-1">Yêu cầu #${reqItem.requestId}</h2>
        <p class="text-muted small mb-0">${reqItem.title}</p>
    </div>
    <div class="d-flex gap-2">
        <a class="btn btn-outline-secondary btn-sm" href="${pageContext.request.contextPath}/request?action=my">
            ← Danh sách
        </a>
        <c:if test="${canCancel}">
            <button type="button" class="btn btn-outline-danger btn-sm"
                    onclick="confirmCancel(${reqItem.requestId})">
                <i class="bi bi-x-circle me-1"></i> Hủy yêu cầu
            </button>
        </c:if>
    </div>
</div>

<div class="row g-3">
    <div class="col-lg-7">
        <div class="card shadow-sm mb-3">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span>Thông tin chung</span>
                <c:choose>
                    <c:when test="${reqItem.status == 'PENDING'}"><span class="badge text-bg-warning">PENDING</span></c:when>
                    <c:when test="${reqItem.status == 'APPROVED'}"><span class="badge text-bg-primary">APPROVED</span></c:when>
                    <c:when test="${reqItem.status == 'REJECTED'}"><span class="badge text-bg-danger">REJECTED</span></c:when>
                    <c:when test="${reqItem.status == 'ASSIGNED'}"><span class="badge text-bg-info">ASSIGNED</span></c:when>
                    <c:when test="${reqItem.status == 'IN_PROGRESS'}"><span class="badge text-bg-primary">IN_PROGRESS</span></c:when>
                    <c:when test="${reqItem.status == 'COMPLETED'}"><span class="badge text-bg-success">COMPLETED</span></c:when>
                    <c:when test="${reqItem.status == 'CANCELLED'}"><span class="badge text-bg-secondary">CANCELLED</span></c:when>
                    <c:otherwise><span class="badge text-bg-light text-dark">${reqItem.status}</span></c:otherwise>
                </c:choose>
            </div>
            <div class="card-body">
                <dl class="row mb-0">
                    <dt class="col-sm-4">Loại</dt>
                    <dd class="col-sm-8">
                        <c:choose>
                            <c:when test="${reqItem.requestType == 'REPAIR'}">Sửa chữa</c:when>
                            <c:when test="${reqItem.requestType == 'PARKING'}">Trông xe</c:when>
                            <c:when test="${reqItem.requestType == 'MOVE_IN'}">Chuyển đồ vào</c:when>
                            <c:when test="${reqItem.requestType == 'MOVE_OUT'}">Chuyển đồ ra</c:when>
                            <c:otherwise>Khác</c:otherwise>
                        </c:choose>
                        <span class="text-muted small">(${reqItem.requestType})</span>
                    </dd>

                    <dt class="col-sm-4">Căn hộ</dt>
                    <dd class="col-sm-8">${reqItem.apartmentCode}</dd>

                    <dt class="col-sm-4">Mô tả</dt>
                    <dd class="col-sm-8">${empty reqItem.description ? '—' : reqItem.description}</dd>

                    <dt class="col-sm-4">Ngày gửi</dt>
                    <dd class="col-sm-8">
                        <%-- Realtime VN: hôm nay HH:mm · khác ngày dd/MM HH:mm --%>
                        <t:rt value="${reqItem.createdAt}"/>
                    </dd>

                    <c:if test="${reqItem.requestType == 'REPAIR'}">
                        <dt class="col-sm-4">Vị trí</dt>
                        <dd class="col-sm-8">${empty reqItem.locationDetail ? '—' : reqItem.locationDetail}</dd>
                        <dt class="col-sm-4">Ưu tiên</dt>
                        <dd class="col-sm-8">
                            <c:choose>
                                <c:when test="${reqItem.urgency == 'HIGH'}"><span class="badge text-bg-danger">HIGH</span></c:when>
                                <c:when test="${reqItem.urgency == 'LOW'}"><span class="badge text-bg-secondary">LOW</span></c:when>
                                <c:otherwise><span class="badge text-bg-warning">MEDIUM</span></c:otherwise>
                            </c:choose>
                        </dd>
                    </c:if>

                    <c:if test="${reqItem.requestType == 'PARKING'}">
                        <dt class="col-sm-4">Loại xe</dt>
                        <dd class="col-sm-8">${empty reqItem.vehicleType ? '—' : reqItem.vehicleType}</dd>
                        <dt class="col-sm-4">Biển số</dt>
                        <dd class="col-sm-8"><code>${empty reqItem.plateNumber ? '—' : reqItem.plateNumber}</code></dd>
                    </c:if>

                    <c:if test="${reqItem.requestType == 'MOVE_IN' || reqItem.requestType == 'MOVE_OUT'}">
                        <dt class="col-sm-4">Giờ đăng ký</dt>
                        <dd class="col-sm-8">
                            <t:rt value="${reqItem.scheduledAt}" mode="full"/>
                        </dd>
                        <dt class="col-sm-4">Ghi chú</dt>
                        <dd class="col-sm-8">${empty reqItem.moveNote ? '—' : reqItem.moveNote}</dd>
                    </c:if>

                    <c:if test="${not empty reqItem.assignedToName}">
                        <dt class="col-sm-4">Nhân viên xử lý</dt>
                        <dd class="col-sm-8">${reqItem.assignedToName}</dd>
                    </c:if>

                    <c:if test="${reqItem.status == 'REJECTED'}">
                        <dt class="col-sm-4">Lý do từ chối</dt>
                        <dd class="col-sm-8 text-danger">${empty reqItem.rejectReason ? '—' : reqItem.rejectReason}</dd>
                    </c:if>

                    <c:if test="${not empty reqItem.completedAt}">
                        <dt class="col-sm-4">Hoàn thành lúc</dt>
                        <dd class="col-sm-8">
                            <t:rt value="${reqItem.completedAt}" mode="full"/>
                        </dd>
                    </c:if>
                </dl>
            </div>
        </div>
    </div>

    <div class="col-lg-5">
        <div class="card shadow-sm">
            <div class="card-header">
                <i class="bi bi-clock-history me-1"></i> Lịch sử tiến độ
            </div>
            <div class="card-body p-0">
                <c:choose>
                    <c:when test="${empty history}">
                        <p class="text-muted small p-3 mb-0">Chưa có lịch sử.</p>
                    </c:when>
                    <c:otherwise>
                        <ul class="list-group list-group-flush">
                            <c:forEach var="h" items="${history}">
                                <li class="list-group-item">
                                    <div class="d-flex justify-content-between">
                                        <div>
                                            <c:if test="${not empty h.oldStatus}">
                                                <span class="badge text-bg-light text-dark">${h.oldStatus}</span>
                                                <i class="bi bi-arrow-right small mx-1"></i>
                                            </c:if>
                                            <span class="badge text-bg-primary">${h.newStatus}</span>
                                        </div>
                                        <span class="small">
                                            <t:rt value="${h.createdAt}" mode="history"/>
                                        </span>
                                    </div>
                                    <div class="small mt-1">
                                        <c:if test="${not empty h.changedByName}">
                                            <strong>${h.changedByName}</strong>
                                            <span class="text-muted">·</span>
                                        </c:if>
                                        ${empty h.note ? '' : h.note}
                                    </div>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/request/_comments.jsp" %>

<script>
    function confirmCancel(id) {
        if (confirm('Bạn chắc chắn muốn hủy yêu cầu #' + id + '? Chỉ hủy được khi còn PENDING.')) {
            window.location.href = '<%= request.getContextPath() %>/request?action=cancel&id=' + id;
        }
    }
</script>
