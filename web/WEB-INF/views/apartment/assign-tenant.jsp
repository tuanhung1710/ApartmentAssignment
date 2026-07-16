<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h2 class="h4 mb-1">Gán người thuê</h2>
        <p class="text-muted small mb-0">
            Căn <strong><c:out value="${apartment.apartmentCode}"/></strong>
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

<div class="row g-3">
    <div class="col-md-5">
        <div class="card shadow-sm mb-3">
            <div class="card-header bg-white fw-semibold">Đại diện thuê hiện tại</div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty currentTenantRep}">
                        <div class="text-muted small text-center py-2">Chưa có đại diện thuê</div>
                    </c:when>
                    <c:otherwise>
                        <div class="fw-semibold"><c:out value="${currentTenantRep.fullName}"/></div>
                        <div class="small text-muted">@<c:out value="${currentTenantRep.username}"/></div>
                        <div class="small mt-1">
                            Từ ${empty currentTenantRep.startDate ? '—' : currentTenantRep.startDate}
                            → ${empty currentTenantRep.endDate ? 'đang mở' : currentTenantRep.endDate}
                        </div>
                        <span class="badge text-bg-success mt-2">CURRENT</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <div class="card shadow-sm">
            <div class="card-header bg-white fw-semibold">Người thuê hiện tại</div>
            <div class="card-body p-0">
                <c:choose>
                    <c:when test="${empty currentTenants}">
                        <div class="text-muted small text-center py-3">Chưa có TENANT</div>
                    </c:when>
                    <c:otherwise>
                        <ul class="list-group list-group-flush">
                            <c:forEach var="t" items="${currentTenants}">
                                <li class="list-group-item small">
                                    <strong><c:out value="${t.fullName}"/></strong>
                                    · @<c:out value="${t.username}"/>
                                    <div class="text-muted">
                                        ${empty t.startDate ? '—' : t.startDate}
                                        → ${empty t.endDate ? 'đang mở' : t.endDate}
                                    </div>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <div class="col-md-7">
        <div class="card shadow-sm">
            <div class="card-header bg-white fw-semibold">Form gán thuê</div>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/apartment" id="assignTenantForm">
                    <input type="hidden" name="action" value="assign-tenant">
                    <input type="hidden" name="apartmentId" value="${apartment.apartmentId}">

                    <div class="mb-3">
                        <label class="form-label d-block">Nguồn người gán <span class="text-danger">*</span></label>
                        <div class="btn-group" role="group">
                            <input type="radio" class="btn-check" name="personSource" id="srcExistingT"
                                   value="existing" ${empty personSource || personSource == 'existing' ? 'checked' : ''}
                                   onchange="toggleTenantSource()">
                            <label class="btn btn-outline-primary" for="srcExistingT">User có sẵn</label>
                            <input type="radio" class="btn-check" name="personSource" id="srcNewT"
                                   value="new" ${personSource == 'new' ? 'checked' : ''}
                                   onchange="toggleTenantSource()">
                            <label class="btn btn-outline-primary" for="srcNewT">Người mới</label>
                        </div>
                    </div>

                    <div id="blockExistingTenant" class="mb-3">
                        <label class="form-label" for="userId">Chọn user</label>
                        <select class="form-select" id="userId" name="userId">
                            <option value="">-- Chọn user --</option>
                            <c:forEach var="u" items="${candidateUsers}">
                                <option value="${u.userId}"
                                    ${selectedUserId != null && selectedUserId == u.userId ? 'selected' : ''}>
                                    <c:out value="${u.fullName}"/> (@<c:out value="${u.username}"/>) · ${u.role}
                                </option>
                            </c:forEach>
                        </select>
                        <c:if test="${not empty householdMembers}">
                            <div class="form-text">
                                Gợi ý từ thành viên hộ:
                                <c:forEach var="m" items="${householdMembers}" varStatus="st">
                                    <button type="button" class="btn btn-link btn-sm p-0 align-baseline"
                                            onclick="fillTenantFromMember('${m.fullName}', '${empty m.phone ? '' : m.phone}')">
                                        <c:out value="${m.fullName}"/>
                                    </button><c:if test="${!st.last}"> · </c:if>
                                </c:forEach>
                            </div>
                        </c:if>
                    </div>

                    <div id="blockNewTenant" class="mb-3 d-none">
                        <div class="row g-2">
                            <div class="col-md-6">
                                <label class="form-label" for="newFullName">Họ tên <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="newFullName" name="newFullName"
                                       value="${newFullName}" maxlength="100" placeholder="VD: Trần Thị B">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label" for="newPhone">SĐT</label>
                                <input type="text" class="form-control" id="newPhone" name="newPhone"
                                       value="${newPhone}" maxlength="20">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label" for="newEmail">Email</label>
                                <input type="email" class="form-control" id="newEmail" name="newEmail"
                                       value="${newEmail}" maxlength="100">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label" for="newUsername">Username (tuỳ chọn)</label>
                                <input type="text" class="form-control" id="newUsername" name="newUsername"
                                       value="${newUsername}" maxlength="50" placeholder="Tự sinh nếu trống">
                            </div>
                        </div>
                        <div class="form-text">
                            Tạo user <strong>RESIDENT</strong> (pass <code>123456</code>) rồi gán thuê.
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label" for="roleInApartment">
                            Vai trò thuê <span class="text-danger">*</span>
                        </label>
                        <select class="form-select" id="roleInApartment" name="roleInApartment" required>
                            <option value="TENANT_REP" ${selectedRole == 'TENANT_REP' || empty selectedRole ? 'selected' : ''}>
                                TENANT_REP – Đại diện thuê (tối đa 1 hiện tại)
                            </option>
                            <option value="TENANT" ${selectedRole == 'TENANT' ? 'selected' : ''}>
                                TENANT – Người thuê (cho phép nhiều)
                            </option>
                        </select>
                        <div class="form-text">
                            Đổi đại diện: hệ thống đóng đại diện cũ (giữ lịch sử) rồi gán mới.
                        </div>
                    </div>

                    <div class="row g-2 mb-3">
                        <div class="col-md-6">
                            <label class="form-label" for="startDate">Ngày bắt đầu <span class="text-danger">*</span></label>
                            <input type="date" class="form-control" id="startDate" name="startDate"
                                   value="${startDate}" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label" for="endDate">Ngày kết thúc</label>
                            <input type="date" class="form-control" id="endDate" name="endDate"
                                   value="${endDate}">
                            <div class="form-text">Để trống = đang thuê (CURRENT, end mở)</div>
                        </div>
                    </div>

                    <div class="alert alert-light border small">
                        <strong>Trạng thái thuê sau gán:</strong>
                        <span class="badge text-bg-success">CURRENT</span>
                        nếu end trống hoặc ≥ hôm nay.
                        Bản ghi cũ khi đổi rep →
                        <span class="badge text-bg-secondary">ENDED</span>
                        (<code>is_current=0</code>.
                    </div>

                    <div class="d-flex gap-2">
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-check-lg me-1"></i> Lưu gán thuê
                        </button>
                        <a class="btn btn-outline-secondary"
                           href="${pageContext.request.contextPath}/apartment?action=detail&amp;id=${apartment.apartmentId}">
                            Hủy
                        </a>
                    </div>
                </form>
                <script>
                    function toggleTenantSource() {
                        var isNew = document.getElementById('srcNewT').checked;
                        document.getElementById('blockExistingTenant').classList.toggle('d-none', isNew);
                        document.getElementById('blockNewTenant').classList.toggle('d-none', !isNew);
                        var sel = document.getElementById('userId');
                        if (sel) {
                            if (isNew) { sel.removeAttribute('required'); }
                            else { sel.setAttribute('required', 'required'); }
                        }
                    }
                    function fillTenantFromMember(name, phone) {
                        document.getElementById('srcNewT').checked = true;
                        toggleTenantSource();
                        document.getElementById('newFullName').value = name || '';
                        document.getElementById('newPhone').value = phone || '';
                    }
                    toggleTenantSource();
                </script>
            </div>
        </div>
    </div>
</div>
