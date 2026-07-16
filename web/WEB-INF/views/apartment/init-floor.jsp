<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h2 class="h4 mb-1">Khởi tạo tầng (6 căn)</h2>
        <p class="text-muted small mb-0">
            Tạo các unit còn thiếu 01–06 trên một tầng · mặc định
            <span class="badge text-bg-warning">INACTIVE</span>
            <span class="badge text-bg-secondary">N/A</span>
        </p>
    </div>
    <a class="btn btn-outline-secondary btn-sm"
       href="${pageContext.request.contextPath}/apartment?action=list">
        <i class="bi bi-arrow-left me-1"></i> Về danh sách
    </a>
</div>

<c:if test="${not empty errors}">
    <div class="alert alert-danger">
        <div class="fw-semibold mb-1"><i class="bi bi-exclamation-triangle me-1"></i> Không thể khởi tạo:</div>
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
            <input type="hidden" name="action" value="init-floor">

            <div class="row g-3">
                <div class="col-md-4">
                    <label class="form-label" for="building">
                        Tòa nhà <span class="text-danger">*</span>
                    </label>
                    <input type="text" class="form-control" id="building" name="building"
                           value="${form.building}"
                           maxlength="50" required
                           placeholder="VD: A hoặc Tòa A">
                    <div class="form-text">Token mã căn (A → A-0301 … A-0306)</div>
                </div>

                <div class="col-md-4">
                    <label class="form-label" for="floorNumber">
                        Tầng <span class="text-danger">*</span>
                    </label>
                    <input type="number" class="form-control" id="floorNumber" name="floorNumber"
                           value="${form.floorNumber}"
                           min="0" max="200" step="1" required
                           placeholder="0 = trệt">
                </div>

                <div class="col-md-4">
                    <label class="form-label" for="areaM2">
                        Diện tích mặc định (m²) <span class="text-danger">*</span>
                    </label>
                    <input type="number" class="form-control" id="areaM2" name="areaM2"
                           value="${form.areaM2}"
                           min="15" max="10000" step="0.01" required
                           placeholder="VD: 50">
                    <div class="form-text">Áp dụng cho mọi unit được tạo</div>
                </div>

                <div class="col-12">
                    <label class="form-label" for="notes">Ghi chú (tuỳ chọn)</label>
                    <textarea class="form-control" id="notes" name="notes" rows="2"
                              maxlength="500"
                              placeholder="Ghi chú chung cho các căn mới">${form.notes}</textarea>
                </div>

                <div class="col-12">
                    <div class="alert alert-light border small mb-0">
                        <i class="bi bi-info-circle me-1"></i>
                        Hệ thống chỉ tạo các unit <strong>chưa có</strong> (01–06).
                        Unit đã tồn tại được bỏ qua. Sau khi tạo, dùng
                        <strong>Kích hoạt</strong> để chọn OWNED / RENTED / VACANT.
                    </div>
                </div>
            </div>

            <div class="d-flex gap-2 mt-4">
                <button type="submit" class="btn btn-primary">
                    <i class="bi bi-building-add me-1"></i> Khởi tạo tầng
                </button>
                <a class="btn btn-outline-secondary"
                   href="${pageContext.request.contextPath}/apartment?action=list">
                    Hủy
                </a>
            </div>
        </form>
    </div>
</div>
