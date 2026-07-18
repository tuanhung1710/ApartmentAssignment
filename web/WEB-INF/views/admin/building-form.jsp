<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="isEdit" value="${formMode == 'edit'}"/>

<div class="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-2 mb-3">
    <div>
        <h2 class="h4 mb-1">
            <i class="bi bi-building-add me-1 text-primary"></i>
            <c:choose>
                <c:when test="${isEdit}">Sửa tòa nhà</c:when>
                <c:otherwise>Thêm tòa nhà</c:otherwise>
            </c:choose>
        </h2>
        <p class="text-muted small mb-0">
            <c:choose>
                <c:when test="${isEdit}">Cập nhật thông tin tòa <strong>${building.buildingCode}</strong></c:when>
                <c:otherwise>Tạo master tòa cho TV2 gán căn hộ theo <code>building_id</code></c:otherwise>
            </c:choose>
        </p>
    </div>
    <a class="btn btn-outline-secondary"
       href="${pageContext.request.contextPath}/building?action=list">
        <i class="bi bi-arrow-left me-1"></i> Quay lại
    </a>
</div>

<c:if test="${not empty formError}">
    <div class="alert alert-danger">
        <i class="bi bi-exclamation-triangle me-1"></i> ${formError}
    </div>
</c:if>

<div class="row g-3">
    <div class="col-12 col-lg-8">
        <div class="card shadow-sm">
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/building" class="row g-3" novalidate>
                    <input type="hidden" name="action" value="save"/>
                    <c:if test="${isEdit}">
                        <input type="hidden" name="buildingId" value="${building.buildingId}"/>
                    </c:if>

                    <div class="col-12 col-md-4">
                        <label class="form-label" for="buildingCode">
                            Mã tòa <span class="text-danger">*</span>
                        </label>
                        <input type="text" class="form-control text-uppercase" id="buildingCode" name="buildingCode"
                               maxlength="20" required
                               value="${building.buildingCode}"
                               placeholder="VD: A, B, C1"
                               <c:if test="${isEdit}">aria-describedby="codeHelp"</c:if>>
                        <div class="form-text" id="codeHelp">Chữ/số/gạch ngang · unique</div>
                    </div>

                    <div class="col-12 col-md-8">
                        <label class="form-label" for="buildingName">
                            Tên tòa <span class="text-danger">*</span>
                        </label>
                        <input type="text" class="form-control" id="buildingName" name="buildingName"
                               maxlength="100" required
                               value="${building.buildingName}"
                               placeholder="VD: Tòa A – Sky View">
                    </div>

                    <div class="col-12">
                        <label class="form-label" for="address">Địa chỉ</label>
                        <input type="text" class="form-control" id="address" name="address"
                               maxlength="300"
                               value="${building.address}"
                               placeholder="Số nhà, đường, quận…">
                    </div>

                    <div class="col-12 col-md-4">
                        <label class="form-label" for="totalFloors">Số tầng</label>
                        <input type="number" class="form-control" id="totalFloors" name="totalFloors"
                               min="1" max="200" step="1"
                               value="${building.totalFloors}"
                               placeholder="VD: 20">
                    </div>

                    <div class="col-12 col-md-4">
                        <label class="form-label" for="status">Trạng thái</label>
                        <select class="form-select" id="status" name="status">
                            <option value="ACTIVE" ${building.status == 'ACTIVE' || empty building.status ? 'selected' : ''}>
                                ACTIVE – đang dùng
                            </option>
                            <option value="INACTIVE" ${building.status == 'INACTIVE' ? 'selected' : ''}>
                                INACTIVE – ngưng
                            </option>
                        </select>
                    </div>

                    <div class="col-12">
                        <label class="form-label" for="description">Mô tả</label>
                        <textarea class="form-control" id="description" name="description" rows="3"
                                  maxlength="1000"
                                  placeholder="Ghi chú tiện ích, block…">${building.description}</textarea>
                    </div>

                    <div class="col-12 d-flex flex-column flex-sm-row gap-2 pt-2">
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-check2 me-1"></i>
                            <c:choose>
                                <c:when test="${isEdit}">Lưu thay đổi</c:when>
                                <c:otherwise>Tạo tòa nhà</c:otherwise>
                            </c:choose>
                        </button>
                        <a class="btn btn-outline-secondary"
                           href="${pageContext.request.contextPath}/building?action=list">Hủy</a>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div class="col-12 col-lg-4">
        <div class="card border-0 bg-light h-100">
            <div class="card-body">
                <h3 class="h6 text-uppercase text-muted">Gợi ý</h3>
                <ul class="small mb-0 ps-3">
                    <li class="mb-2">Mã tòa dùng làm prefix căn hộ (<code>A-0801</code>).</li>
                    <li class="mb-2">TV2 gán căn qua <code>apartments.building_id</code>.</li>
                    <li class="mb-2">TV3/TV5 filter phí &amp; cư dân theo tòa.</li>
                    <li>Không xóa cứng tòa còn căn — dùng <strong>INACTIVE</strong>.</li>
                </ul>
            </div>
        </div>
    </div>
</div>
