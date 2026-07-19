<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h2 class="h4 mb-1">Chi tiết phí căn hộ</h2>
        <p class="text-muted small mb-0">${assignment.apartmentCode} · ${assignment.feeTitle}</p>
    </div>
    <c:choose>
        <c:when test="${sessionScope.currentUser.role == 'RESIDENT'}">
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/fee?action=my">Quay lại</a>
        </c:when>
        <c:otherwise>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/fee?action=detail&amp;id=${assignment.feeId}">Quay lại đợt phí</a>
        </c:otherwise>
    </c:choose>
</div>

<div class="card shadow-sm">
    <div class="card-body">
        <dl class="row mb-0">
            <dt class="col-sm-4">Khoản phí</dt>
            <dd class="col-sm-8">${assignment.feeTitle}</dd>
            <dt class="col-sm-4">Loại phí</dt>
            <dd class="col-sm-8">
                <c:choose>
                    <c:when test="${assignment.feeType == 'ONE_TIME'}">
                        <span class="badge text-bg-warning">Phát sinh</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge text-bg-info">Hàng tháng</span>
                    </c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${not empty assignment.feeMonth && not empty assignment.feeYear}">
                        · ${assignment.feeMonth}/${assignment.feeYear}
                    </c:when>
                    <c:when test="${not empty assignment.feeYear}">
                        · ${assignment.feeYear}
                    </c:when>
                </c:choose>
            </dd>
            <dt class="col-sm-4">Danh mục</dt>
            <dd class="col-sm-8">${assignment.categoryName}</dd>
            <dt class="col-sm-4">Căn hộ</dt>
            <dd class="col-sm-8">${assignment.apartmentCode}
                <c:if test="${not empty assignment.building}"> (Tòa ${assignment.building})</c:if>
            </dd>
            <dt class="col-sm-4">Số tiền</dt>
            <dd class="col-sm-8 fw-bold">
                <fmt:formatNumber value="${assignment.amount}" type="number" maxFractionDigits="0"/> đ
            </dd>
            <dt class="col-sm-4">Trạng thái</dt>
            <dd class="col-sm-8">
                <c:choose>
                    <c:when test="${assignment.status == 'PAID'}"><span class="badge text-bg-success">PAID</span></c:when>
                    <c:otherwise><span class="badge text-bg-warning">UNPAID</span></c:otherwise>
                </c:choose>
            </dd>
            <dt class="col-sm-4">Gán lúc</dt>
            <dd class="col-sm-8">${assignment.assignedAt}</dd>
            <dt class="col-sm-4">Thanh toán lúc</dt>
            <dd class="col-sm-8"><c:choose><c:when test="${empty assignment.paidAt}">—</c:when><c:otherwise><t:rt value="${assignment.paidAt}" mode="full"/></c:otherwise></c:choose></dd>
        </dl>
    </div>
</div>
