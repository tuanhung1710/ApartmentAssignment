<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="isEdit" value="${formMode == 'edit'}" />

<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h2 class="h4 mb-1">
            <c:choose>
                <c:when test="${isEdit}">Cập nhật căn hộ</c:when>
                <c:otherwise>Thêm căn hộ</c:otherwise>
            </c:choose>
        </h2>
        <p class="text-muted small mb-0">
            <c:choose>
                <c:when test="${isEdit}">Chỉnh sửa thông tin căn hộ (mã căn không đổi)</c:when>
                <c:otherwise>Nhập thông tin căn hộ mới vào hệ thống</c:otherwise>
            </c:choose>
        </p>
    </div>
    <a class="btn btn-outline-secondary btn-sm"
       href="${pageContext.request.contextPath}/apartment?action=list">
        <i class="bi bi-arrow-left me-1"></i> Về danh sách
    </a>
</div>

<c:if test="${not empty errors}">
    <div class="alert alert-danger">
        <div class="fw-semibold mb-1"><i class="bi bi-exclamation-triangle me-1"></i> Không thể lưu căn hộ:</div>
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
            <input type="hidden" name="action" value="${isEdit ? 'update' : 'create'}">
            <c:if test="${isEdit}">
                <input type="hidden" name="apartmentId" value="${form.apartmentId}">
            </c:if>

            <div class="row g-3">
                <div class="col-md-4">
                    <label class="form-label" for="apartmentCode">
                        Mã căn hộ
                        <c:if test="${!isEdit}"><span class="text-danger">*</span></c:if>
                    </label>
                    <input type="text" class="form-control" id="apartmentCode" name="apartmentCode"
                           value="${form.apartmentCode}"
                           maxlength="20"
                           placeholder="VD: A-1201"
                           pattern="[A-Za-z0-9][A-Za-z0-9_-]{0,19}"
                           <c:if test="${isEdit}">readonly</c:if>
                           <c:if test="${!isEdit}">required</c:if>>
                    <div class="form-text">
                        <c:choose>
                            <c:when test="${isEdit}">Mã căn không thể thay đổi sau khi tạo</c:when>
                            <c:otherwise>Chữ, số, - hoặc _ · tối đa 20 ký tự · không trùng</c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="col-md-4">
                    <label class="form-label" for="building">
                        Tòa nhà <span class="text-danger">*</span>
                    </label>
                    <input type="text" class="form-control" id="building" name="building"
                           value="${form.building}"
                           maxlength="50" required
                           placeholder="VD: Tòa A">
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
                        Diện tích (m²) <span class="text-danger">*</span>
                    </label>
                    <input type="number" class="form-control" id="areaM2" name="areaM2"
                           value="${form.areaM2}"
                           min="0.01" max="10000" step="0.01" required
                           placeholder="VD: 75.50">
                </div>

                <div class="col-md-4">
                    <label class="form-label" for="occupancyType">
                        Loại hình <span class="text-danger">*</span>
                    </label>
                    <select class="form-select" id="occupancyType" name="occupancyType" required>
                        <option value="OWNED" ${form.occupancyType == 'OWNED' ? 'selected' : ''}>OWNED – Sở hữu</option>
                        <option value="RENTED" ${form.occupancyType == 'RENTED' ? 'selected' : ''}>RENTED – Thuê</option>
                    </select>
                </div>

                <div class="col-md-4">
                    <label class="form-label" for="status">Trạng thái</label>
                    <select class="form-select" id="status" name="status">
                        <option value="ACTIVE" ${empty form.status || form.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE – Đang hoạt động</option>
                        <option value="INACTIVE" ${form.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE – Ngừng</option>
                    </select>
                </div>

                <div class="col-12">
                    <label class="form-label" for="notes">Ghi chú</label>
                    <textarea class="form-control" id="notes" name="notes" rows="3"
                              maxlength="500"
                              placeholder="Ghi chú thêm (không bắt buộc)">${form.notes}</textarea>
                    <div class="form-text">Tối đa 500 ký tự</div>
                </div>
            </div>

            <div class="d-flex gap-2 mt-4">
                <button type="submit" class="btn btn-primary">
                    <i class="bi bi-check-lg me-1"></i> Lưu
                </button>
                <a class="btn btn-outline-secondary"
                   href="${pageContext.request.contextPath}/apartment?action=list">
                    Hủy
                </a>
            </div>
        </form>
    </div>
</div>
