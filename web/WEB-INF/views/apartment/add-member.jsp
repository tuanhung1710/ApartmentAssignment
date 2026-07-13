<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h2 class="h4 mb-1">Thêm thành viên sinh sống</h2>
        <p class="text-muted small mb-0">
            Căn <strong>${apartment.apartmentCode}</strong>
            · ${apartment.building} · Tầng ${apartment.floorNumber}
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

<div class="card shadow-sm" style="max-width: 720px;">
    <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/apartment" novalidate>
            <input type="hidden" name="action" value="add-member">
            <input type="hidden" name="apartmentId" value="${apartment.apartmentId}">

            <div class="mb-3">
                <label class="form-label" for="fullName">
                    Họ và tên <span class="text-danger">*</span>
                </label>
                <input type="text" class="form-control" id="fullName" name="fullName"
                       value="${form.fullName}" maxlength="100" required
                       placeholder="VD: Nguyễn Văn A">
            </div>

            <div class="mb-3">
                <label class="form-label" for="relationship">
                    Quan hệ (Relationship) <span class="text-danger">*</span>
                </label>
                <select class="form-select" id="relationship" name="relationship" required>
                    <option value="">-- Chọn quan hệ --</option>
                    <c:forEach var="rel" items="${relationshipOptions}">
                        <option value="${rel}" ${form.relationship == rel ? 'selected' : ''}>${rel}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="row g-2 mb-3">
                <div class="col-md-6">
                    <label class="form-label" for="idNumber">CCCD / CMND</label>
                    <input type="text" class="form-control" id="idNumber" name="idNumber"
                           value="${form.idNumber}" maxlength="12"
                           placeholder="9–12 chữ số" inputmode="numeric">
                    <div class="form-text">Không bắt buộc · nếu nhập: 9–12 số</div>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="phone">Số điện thoại</label>
                    <input type="text" class="form-control" id="phone" name="phone"
                           value="${form.phone}" maxlength="11"
                           placeholder="VD: 0901234567" inputmode="tel">
                    <div class="form-text">Không bắt buộc · 9–11 số</div>
                </div>
            </div>

            <div class="mb-3">
                <label class="form-label" for="dateOfBirth">Ngày sinh (Date of Birth)</label>
                <input type="date" class="form-control" id="dateOfBirth" name="dateOfBirth"
                       value="${dobValue}">
                <div class="form-text">Không bắt buộc · không được ở tương lai</div>
            </div>

            <div class="alert alert-light border small">
                Thành viên mới mặc định <span class="badge text-bg-success">Active</span>
                (đang sinh sống trong căn).
            </div>

            <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary">
                    <i class="bi bi-check-lg me-1"></i> Lưu thành viên
                </button>
                <a class="btn btn-outline-secondary"
                   href="${pageContext.request.contextPath}/apartment?action=detail&amp;id=${apartment.apartmentId}">
                    Hủy
                </a>
            </div>
        </form>
    </div>
</div>
