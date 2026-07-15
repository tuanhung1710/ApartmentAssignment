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
                <c:when test="${isEdit}">Chỉnh sửa thông tin căn hộ (mã căn / tòa / tầng không đổi)</c:when>
                <c:otherwise>Nhập tòa nhà + tầng — hệ thống tự sinh mã căn theo format [tòa] - [tầng] [mã]</c:otherwise>
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
                <c:choose>
                    <c:when test="${isEdit}">
                        <%-- Định danh đồng bộ: [tòa] - [tầng] [mã] — không sửa --%>
                        <div class="col-md-8">
                            <label class="form-label">Định danh căn hộ</label>
                            <input type="text" class="form-control bg-light" readonly
                                   value="${form.building} - ${form.floorNumber} ${form.apartmentCode}">
                            <div class="form-text">
                                Format: [tên tòa] - [số tầng] [mã căn] · không thể thay đổi sau khi tạo
                            </div>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label" for="areaM2">
                                Diện tích (m²) <span class="text-danger">*</span>
                            </label>
                            <input type="number" class="form-control" id="areaM2" name="areaM2"
                                   value="${form.areaM2}"
                                   min="15" max="10000" step="0.01" required
                                   placeholder="VD: 75.50">
                        </div>
                    </c:when>
                    <c:otherwise>
                        <%-- Create: chỉ nhập tòa + tầng; mã căn tự sinh server-side --%>
                        <div class="col-md-4">
                            <label class="form-label" for="building">
                                Tòa nhà <span class="text-danger">*</span>
                            </label>
                            <input type="text" class="form-control" id="building" name="building"
                                   value="${form.building}"
                                   maxlength="50" required
                                   placeholder="VD: A hoặc Tòa A">
                            <div class="form-text">Dùng để sinh mã căn (vd: A → A-0401)</div>
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
                                   min="15" max="10000" step="0.01" required
                                   placeholder="VD: 75.50">
                        </div>

                        <div class="col-12">
                            <div class="alert alert-light border small mb-0">
                                <i class="bi bi-info-circle me-1"></i>
                                <strong>Mã căn tự động:</strong>
                                hệ thống sinh theo format
                                <code>[tên tòa] - [số tầng] [mã căn]</code>
                                (vd: tòa <strong>A</strong>, tầng <strong>4</strong> →
                                <code>A - 4 A-0401</code>). Không cho thêm nếu mã đã tồn tại.
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>

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
