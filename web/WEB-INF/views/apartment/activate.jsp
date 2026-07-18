<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h2 class="h4 mb-1">Kích hoạt căn hộ</h2>
        <p class="text-muted small mb-0">
            Căn <strong class="text-primary"><c:out value="${apartment.apartmentCode}"/></strong>
            · Tòa <c:out value="${apartment.building}"/>
            · Tầng ${apartment.floorNumber}
        </p>
    </div>
    <a class="btn btn-outline-secondary btn-sm"
       href="${pageContext.request.contextPath}/apartment?action=detail&amp;id=${apartment.apartmentId}">
        <i class="bi bi-arrow-left me-1"></i> Về chi tiết
    </a>
</div>

<c:if test="${not empty errors}">
    <div class="alert alert-danger">
        <ul class="mb-0 ps-3">
            <c:forEach var="err" items="${errors}">
                <li>${err}</li>
            </c:forEach>
        </ul>
    </div>
</c:if>

<div class="card shadow-sm">
    <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/apartment" novalidate>
            <input type="hidden" name="action" value="activate">
            <input type="hidden" name="id" value="${apartment.apartmentId}">

            <div class="row g-3">
                <div class="col-md-3">
                    <label class="form-label">Mã căn</label>
                    <input type="text" class="form-control bg-light" readonly value="${apartment.apartmentCode}">
                </div>
                <div class="col-md-3">
                    <label class="form-label">Tòa</label>
                    <input type="text" class="form-control bg-light" readonly value="${apartment.building}">
                </div>
                <div class="col-md-2">
                    <label class="form-label">Tầng</label>
                    <input type="text" class="form-control bg-light" readonly value="${apartment.floorNumber}">
                </div>
                <div class="col-md-4">
                    <label class="form-label" for="occupancyType">
                        Loại hình sau khi active <span class="text-danger">*</span>
                    </label>
                    <select class="form-select" id="occupancyType" name="occupancyType" required>
                        <option value="VACANT" ${occupancyType == 'VACANT' ? 'selected' : ''}>
                            VACANT – Sẵn sàng, chưa có người ở
                        </option>
                        <option value="OWNED" ${occupancyType == 'OWNED' ? 'selected' : ''}>
                            OWNED – Sở hữu
                        </option>
                        <option value="RENTED" ${occupancyType == 'RENTED' ? 'selected' : ''}>
                            RENTED – Thuê
                        </option>
                    </select>
                    <div class="form-text">Bắt buộc chọn khi kích hoạt (không dùng N/A)</div>
                </div>

                <div class="col-12">
                    <div class="alert alert-light border small mb-0">
                        <i class="bi bi-info-circle me-1"></i>
                        Sau khi kích hoạt: <code>ACTIVE</code> + loại hình đã chọn.
                        <strong>Chủ sở hữu / người thuê để trống</strong> — gán sau trên màn chi tiết.
                        Vẫn <strong>thêm thành viên hộ</strong> bình thường (kể cả căn <code>RENTED</code>).
                        Có thể sửa loại hình / diện tích ở form <strong>Sửa</strong>.
                    </div>
                </div>
            </div>

            <div class="d-flex gap-2 mt-4">
                <button type="submit" class="btn btn-success">
                    <i class="bi bi-play-circle me-1"></i> Kích hoạt
                </button>
                <a class="btn btn-outline-secondary"
                   href="${pageContext.request.contextPath}/apartment?action=detail&amp;id=${apartment.apartmentId}">
                    Hủy
                </a>
            </div>
        </form>
    </div>
</div>
