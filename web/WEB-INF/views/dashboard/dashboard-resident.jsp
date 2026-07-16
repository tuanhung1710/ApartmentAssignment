<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="mb-3">
    <h2 class="h4">Xin chào, ${sessionScope.currentUser.fullName}!</h2>
    <p class="text-muted mb-0">
        Vai trò: <span class="badge text-bg-primary">RESIDENT</span>
        · Căn hộ, phí &amp; yêu cầu của bạn
    </p>
</div>

<div class="row g-3">
    <div class="col-md-3">
        <div class="card stat-card h-100">
            <div class="card-body">
                <div class="text-muted small"><i class="bi bi-house-heart me-1"></i> Căn hộ của tôi</div>
                <div class="h5 mt-2 mb-0 text-primary">${empty myApartment ? 'Chưa gán căn hộ' : myApartment}</div>
                <a class="btn btn-sm btn-outline-primary mt-2"
                   href="${pageContext.request.contextPath}/apartment?action=my">Chi tiết</a>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card stat-card h-100">
            <div class="card-body">
                <div class="text-muted small"><i class="bi bi-receipt me-1"></i> Phí tháng gần nhất</div>
                <div class="small mt-2 mb-0 fw-semibold">${empty latestFee ? 'Chưa có phí' : latestFee}</div>
                <a class="btn btn-sm btn-outline-primary mt-2"
                   href="${pageContext.request.contextPath}/fee?action=my">Xem phí</a>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card stat-card h-100">
            <div class="card-body">
                <div class="text-muted small"><i class="bi bi-send me-1"></i> Request đang mở</div>
                <div class="stat-value text-warning">${empty openRequests ? 0 : openRequests}</div>
                <a class="btn btn-sm btn-outline-primary mt-2"
                   href="${pageContext.request.contextPath}/request?action=my">Yêu cầu của tôi</a>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card stat-card h-100">
            <div class="card-body">
                <div class="text-muted small"><i class="bi bi-megaphone me-1"></i> Thông báo mới</div>
                <div class="stat-value text-success">${empty newAnnouncements ? 0 : newAnnouncements}</div>
                <p class="small text-muted mb-0 mt-1">30 ngày gần đây</p>
            </div>
        </div>
    </div>
</div>
