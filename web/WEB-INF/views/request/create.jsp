<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h2 class="h4 mb-1">Gửi yêu cầu</h2>
        <p class="text-muted small mb-0">
            Căn hộ: <strong>${myApartment.apartmentCode}</strong>
            · Vai trò: ${myApartment.roleInApartment}
        </p>
    </div>
    <a class="btn btn-outline-secondary btn-sm" href="${pageContext.request.contextPath}/request?action=my">
        ← Danh sách
    </a>
</div>

<div class="card shadow-sm">
    <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/request" id="createRequestForm">
            <input type="hidden" name="action" value="create"/>

            <div class="mb-3">
                <label class="form-label">Loại yêu cầu <span class="text-danger">*</span></label>
                <select name="requestType" id="requestType" class="form-select" required>
                    <option value="">-- Chọn loại --</option>
                    <option value="REPAIR" ${formType == 'REPAIR' ? 'selected' : ''}>Sửa chữa</option>
                    <option value="PARKING" ${formType == 'PARKING' ? 'selected' : ''}>Đăng ký trông xe</option>
                    <option value="MOVE_IN" ${formType == 'MOVE_IN' ? 'selected' : ''}>Chuyển đồ vào</option>
                    <option value="MOVE_OUT" ${formType == 'MOVE_OUT' ? 'selected' : ''}>Chuyển đồ ra</option>
                    <option value="OTHER" ${formType == 'OTHER' ? 'selected' : ''}>Khác</option>
                </select>
            </div>

            <div class="mb-3">
                <label class="form-label">Tiêu đề <span class="text-danger">*</span></label>
                <input type="text" name="title" class="form-control" maxlength="200" required
                       placeholder="Tóm tắt ngắn gọn yêu cầu"/>
            </div>

            <div class="mb-3">
                <label class="form-label">Mô tả chi tiết</label>
                <textarea name="description" class="form-control" rows="3"
                          placeholder="Mô tả thêm (tuỳ chọn)"></textarea>
            </div>

            <!-- REPAIR fields -->
            <div id="fields-REPAIR" class="type-fields border rounded p-3 mb-3 bg-light" style="display:none;">
                <h3 class="h6 text-primary mb-3"><i class="bi bi-tools me-1"></i> Thông tin sửa chữa</h3>
                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label">Vị trí hỏng <span class="text-danger">*</span></label>
                        <input type="text" name="locationDetail" class="form-control"
                               placeholder="VD: Bếp, WC, Phòng khách..."/>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Mức độ ưu tiên</label>
                        <select name="urgency" class="form-select">
                            <option value="LOW">Thấp (LOW)</option>
                            <option value="MEDIUM" selected>Trung bình (MEDIUM)</option>
                            <option value="HIGH">Cao (HIGH)</option>
                        </select>
                    </div>
                </div>
            </div>

            <!-- PARKING fields -->
            <div id="fields-PARKING" class="type-fields border rounded p-3 mb-3 bg-light" style="display:none;">
                <h3 class="h6 text-primary mb-3"><i class="bi bi-p-circle me-1"></i> Thông tin xe</h3>
                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label">Loại xe <span class="text-danger">*</span></label>
                        <select name="vehicleType" class="form-select">
                            <option value="">-- Chọn --</option>
                            <option value="Xe máy">Xe máy</option>
                            <option value="Ô tô">Ô tô</option>
                            <option value="Xe đạp điện">Xe đạp điện</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Biển số <span class="text-danger">*</span></label>
                        <input type="text" name="plateNumber" class="form-control"
                               placeholder="VD: 59X1-12345" maxlength="20"/>
                    </div>
                </div>
            </div>

            <!-- MOVE_IN / MOVE_OUT fields -->
            <div id="fields-MOVE" class="type-fields border rounded p-3 mb-3 bg-light" style="display:none;">
                <h3 class="h6 text-primary mb-3"><i class="bi bi-box-seam me-1"></i> Đăng ký chuyển đồ</h3>
                <div class="alert alert-info small py-2">
                    <i class="bi bi-info-circle me-1"></i>
                    Khung giờ cho phép: <strong>${moveTimeStart} – ${moveTimeEnd}</strong>
                    · Ngày: T2–T7 (theo quy định BQL).
                </div>
                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label">Thời gian dự kiến <span class="text-danger">*</span></label>
                        <input type="datetime-local" name="scheduledAt" class="form-control"/>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Ghi chú chuyển đồ</label>
                        <input type="text" name="moveNote" class="form-control"
                               placeholder="VD: Sofa + tủ lạnh, xe tải nhỏ"/>
                    </div>
                </div>
            </div>

            <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary">
                    <i class="bi bi-send me-1"></i> Gửi yêu cầu
                </button>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/request?action=my">Hủy</a>
            </div>
        </form>
    </div>
</div>

<script>
    (function () {
        var typeSelect = document.getElementById('requestType');

        function showFields() {
            var type = typeSelect.value;
            var repair = document.getElementById('fields-REPAIR');
            var parking = document.getElementById('fields-PARKING');
            var move = document.getElementById('fields-MOVE');

            repair.style.display = 'none';
            parking.style.display = 'none';
            move.style.display = 'none';

            if (type === 'REPAIR') {
                repair.style.display = 'block';
            } else if (type === 'PARKING') {
                parking.style.display = 'block';
            } else if (type === 'MOVE_IN' || type === 'MOVE_OUT') {
                move.style.display = 'block';
            }
        }

        typeSelect.addEventListener('change', showFields);
        showFields();
    })();
</script>
