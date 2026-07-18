<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="isEdit" value="${formMode == 'edit'}" />
<c:set var="isActive" value="${form.status == 'ACTIVE'}" />

<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h2 class="h4 mb-1">
            <c:choose>
                <c:when test="${isEdit}">Cập nhật căn hộ</c:when>
                <c:otherwise>Thêm căn hộ (lẻ)</c:otherwise>
            </c:choose>
        </h2>
        <p class="text-muted small mb-0">
            <c:choose>
                <c:when test="${isEdit}">
                    Mã / tòa / tầng không đổi ·
                    <c:choose>
                        <c:when test="${isActive}">ACTIVE: sửa được loại hình OWNED/RENTED/VACANT</c:when>
                        <c:otherwise>INACTIVE: loại hình khóa N/A — dùng Kích hoạt để đổi</c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    Mặc định <span class="badge text-bg-warning">INACTIVE</span>
                    <span class="badge text-bg-secondary">N/A</span>
                    · tối đa 6 căn / tầng · hoặc dùng
                    <a href="${pageContext.request.contextPath}/apartment?action=init-floor">Khởi tạo tầng</a>
                </c:otherwise>
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
                        <div class="col-md-3">
                            <label class="form-label">Mã căn</label>
                            <input type="text" class="form-control bg-light" readonly
                                   value="${form.apartmentCode}">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">Tòa nhà</label>
                            <input type="text" class="form-control bg-light" readonly
                                   value="${form.building}">
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Tầng</label>
                            <input type="text" class="form-control bg-light" readonly
                                   value="${form.floorNumber}">
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
                        <div class="col-md-4">
                            <label class="form-label">Trạng thái</label>
                            <input type="text" class="form-control bg-light" readonly value="${form.status}">
                            <div class="form-text">Đổi status bằng Kích hoạt / Vô hiệu</div>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label" for="occupancyType">
                                Loại hình <span class="text-danger">*</span>
                            </label>
                            <c:choose>
                                <c:when test="${form.status == 'ACTIVE'}">
                                    <select class="form-select" id="occupancyType" name="occupancyType" required>
                                        <option value="VACANT" ${form.occupancyType == 'VACANT' ? 'selected' : ''}>
                                            VACANT – Sẵn sàng, chưa có người ở
                                        </option>
                                        <option value="OWNED" ${form.occupancyType == 'OWNED' ? 'selected' : ''}>
                                            OWNED – Sở hữu
                                        </option>
                                        <option value="RENTED" ${form.occupancyType == 'RENTED' ? 'selected' : ''}>
                                            RENTED – Thuê
                                        </option>
                                    </select>
                                    <div class="form-text">
                                        VACANT = trống (chưa gán). Muốn gán: chọn <strong>OWNED</strong> rồi gán owner,
                                        hoặc <strong>RENTED</strong> rồi gán người thuê.
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <input type="text" class="form-control bg-light" readonly value="N/A">
                                    <input type="hidden" name="occupancyType" value="N/A">
                                    <div class="form-text">INACTIVE luôn N/A — dùng Kích hoạt để chọn loại hình.</div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="col-md-4">
                            <label class="form-label" for="building">
                                Tòa nhà <span class="text-danger">*</span>
                            </label>
                            <input type="text" class="form-control" id="building" name="building"
                                   value="${form.building}"
                                   maxlength="50" required
                                   placeholder="VD: A hoặc Tòa A">
                            <div class="form-text">Sinh mã (vd: A → A-0203) · tối đa 6 unit/tầng</div>
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

                        <div class="col-md-4">
                            <label class="form-label">Trạng thái</label>
                            <input type="text" class="form-control bg-light" readonly value="INACTIVE">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Loại hình</label>
                            <input type="text" class="form-control bg-light" readonly value="N/A">
                        </div>

                        <div class="col-12">
                            <div class="alert alert-light border small mb-0">
                                <i class="bi bi-info-circle me-1"></i>
                                Căn mới luôn <strong>INACTIVE · N/A</strong>.
                                Muốn đưa vào hoạt động: bấm <strong>Kích hoạt</strong> và chọn OWNED / RENTED / VACANT.
                                Tầng đầy 6 căn → dùng <a href="${pageContext.request.contextPath}/apartment?action=init-floor">Khởi tạo tầng</a> cho tầng khác.
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>

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
