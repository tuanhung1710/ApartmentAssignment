<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="d-flex flex-wrap justify-content-between align-items-start gap-2 mb-3">
    <div>
        <h2 class="h4 mb-1">
            Căn hộ <span class="text-primary"><c:out value="${apartment.apartmentCode}"/></span>
            <c:choose>
                <c:when test="${apartment.status == 'ACTIVE'}">
                    <span class="badge text-bg-success align-middle">ACTIVE</span>
                </c:when>
                <c:otherwise>
                    <span class="badge text-bg-warning align-middle">INACTIVE</span>
                </c:otherwise>
            </c:choose>
        </h2>
        <p class="text-muted small mb-0">
            Tòa <c:out value="${apartment.building}"/>
            · Tầng ${apartment.floorNumber}
            ·
            <c:choose>
                <c:when test="${apartment.occupancyType == 'OWNED'}">OWNED – Sở hữu</c:when>
                <c:when test="${apartment.occupancyType == 'RENTED'}">RENTED – Thuê</c:when>
                <c:when test="${apartment.occupancyType == 'VACANT'}">VACANT – Trống</c:when>
                <c:otherwise>N/A – Chưa vận hành</c:otherwise>
            </c:choose>
        </p>
    </div>
    <div class="d-flex flex-wrap gap-1">
        <a class="btn btn-sm btn-outline-secondary"
           href="${pageContext.request.contextPath}/apartment?action=list">
            <i class="bi bi-arrow-left"></i> Về danh sách
        </a>
        <c:if test="${canManage}">
            <a class="btn btn-sm btn-outline-primary"
               href="${pageContext.request.contextPath}/apartment?action=edit&amp;id=${apartment.apartmentId}">
                <i class="bi bi-pencil"></i> Sửa
            </a>
            <c:choose>
                <c:when test="${apartment.status == 'ACTIVE'}">
                    <form method="post" action="${pageContext.request.contextPath}/apartment" class="d-inline"
                          onsubmit="return confirm('Vô hiệu hóa căn ${apartment.apartmentCode}? (→ INACTIVE · N/A)');">
                        <input type="hidden" name="action" value="deactivate">
                        <input type="hidden" name="id" value="${apartment.apartmentId}">
                        <button type="submit" class="btn btn-sm btn-outline-warning">
                            <i class="bi bi-pause-circle"></i> Vô hiệu
                        </button>
                    </form>
                </c:when>
                <c:otherwise>
                    <a class="btn btn-sm btn-outline-success"
                       href="${pageContext.request.contextPath}/apartment?action=activate&amp;id=${apartment.apartmentId}">
                        <i class="bi bi-play-circle"></i> Kích hoạt
                    </a>
                    <form method="post" action="${pageContext.request.contextPath}/apartment" class="d-inline"
                          onsubmit="return confirm('XÓA VĨNH VIỄN căn ${apartment.apartmentCode}?');">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${apartment.apartmentId}">
                        <button type="submit" class="btn btn-sm btn-outline-danger">
                            <i class="bi bi-trash"></i> Xóa
                        </button>
                    </form>
                </c:otherwise>
            </c:choose>
        </c:if>
    </div>
</div>

<%-- ===== Thông tin cơ bản ===== --%>
<div class="card shadow-sm mb-3">
    <div class="card-header bg-white fw-semibold">
        <i class="bi bi-info-circle me-1"></i> Thông tin cơ bản
    </div>
    <div class="card-body">
        <div class="row g-3">
            <div class="col-md-3">
                <div class="text-muted small">Mã căn</div>
                <div class="fw-semibold"><c:out value="${apartment.apartmentCode}"/></div>
            </div>
            <div class="col-md-3">
                <div class="text-muted small">Tòa nhà</div>
                <div><c:out value="${apartment.building}"/></div>
            </div>
            <div class="col-md-2">
                <div class="text-muted small">Tầng</div>
                <div>${apartment.floorNumber}</div>
            </div>
            <div class="col-md-3">
                <div class="text-muted small">Diện tích</div>
                <div>
                    <fmt:formatNumber value="${apartment.areaM2}" minFractionDigits="0" maxFractionDigits="2"/> m²
                </div>
            </div>
            <div class="col-md-3">
                <div class="text-muted small">Loại hình</div>
                <div>
                    <c:choose>
                        <c:when test="${apartment.occupancyType == 'OWNED'}">
                            <span class="badge text-bg-info">OWNED</span>
                        </c:when>
                        <c:when test="${apartment.occupancyType == 'RENTED'}">
                            <span class="badge text-bg-primary">RENTED</span>
                        </c:when>
                        <c:when test="${apartment.occupancyType == 'VACANT'}">
                            <span class="badge text-bg-light border">VACANT</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge text-bg-secondary">N/A</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="col-md-3">
                <div class="text-muted small">Trạng thái</div>
                <div>
                    <c:choose>
                        <c:when test="${apartment.status == 'ACTIVE'}">
                            <span class="badge text-bg-success">ACTIVE</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge text-bg-warning">INACTIVE</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="col-md-3">
                <div class="text-muted small">Ngày tạo</div>
                <div class="small">
                    <c:choose>
                        <c:when test="${empty apartment.createdAt}">—</c:when>
                        <c:otherwise>${apartment.createdAt}</c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="col-md-3">
                <div class="text-muted small">Cập nhật lần cuối</div>
                <div class="small">
                    <c:choose>
                        <c:when test="${empty apartment.updatedAt}">—</c:when>
                        <c:otherwise>${apartment.updatedAt}</c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="col-12">
                <div class="text-muted small">Ghi chú</div>
                <div>
                    <c:choose>
                        <c:when test="${empty apartment.notes}"><span class="text-muted">—</span></c:when>
                        <c:otherwise><c:out value="${apartment.notes}"/></c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</div>

<%-- ===== Chủ sở hữu + Người thuê =====
     VACANT: phải Sửa loại hình → OWNED/RENTED trước mới gán
     OWNED: gán owner → TV
     RENTED: gán người thuê (+ chủ nhà tùy chọn)
--%>
<c:set var="isOwnedOnly" value="${apartment.occupancyType == 'OWNED'}" />
<c:set var="isVacant" value="${apartment.occupancyType == 'VACANT' || empty apartment.occupancyType || apartment.occupancyType == 'N/A'}" />
<c:set var="isRented" value="${apartment.occupancyType == 'RENTED'}" />
<div class="row g-3 mb-3">
    <div class="${isOwnedOnly || isVacant ? 'col-md-12' : 'col-md-6'}">
        <div class="card shadow-sm h-100">
            <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
                <span>
                    <i class="bi bi-person-badge me-1"></i>
                    <c:choose>
                        <c:when test="${isRented}">Chủ nhà (landlord)</c:when>
                        <c:otherwise>Chủ sở hữu</c:otherwise>
                    </c:choose>
                </span>
                <c:if test="${canManage && !isVacant && (isOwnedOnly || isRented)}">
                    <div class="d-flex flex-wrap gap-1">
                        <a class="btn btn-sm btn-outline-primary"
                           href="${pageContext.request.contextPath}/apartment?action=assign-owner&amp;id=${apartment.apartmentId}">
                            <c:choose>
                                <c:when test="${isRented && empty owners}">Gán chủ nhà</c:when>
                                <c:when test="${isRented}">Đổi chủ nhà</c:when>
                                <c:when test="${empty owners}">Gán owner</c:when>
                                <c:otherwise>Đổi owner</c:otherwise>
                            </c:choose>
                        </a>
                        <c:if test="${not empty owners}">
                            <form method="post" action="${pageContext.request.contextPath}/apartment" class="d-inline"
                                  onsubmit="return confirm('${isRented ? 'Gỡ chủ nhà khỏi căn này? (không ảnh hưởng thành viên hộ)' : 'Gỡ chủ sở hữu khỏi căn này? Thành viên hộ gỡ riêng nếu cần.'}');">
                                <input type="hidden" name="action" value="remove-owner">
                                <input type="hidden" name="apartmentId" value="${apartment.apartmentId}">
                                <button type="submit" class="btn btn-sm btn-outline-danger">
                                    ${isRented ? 'Gỡ chủ nhà' : 'Gỡ owner'}
                                </button>
                            </form>
                        </c:if>
                    </div>
                </c:if>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${isVacant}">
                        <div class="text-muted small py-3 text-center">
                            <i class="bi bi-info-circle d-block mb-1 fs-4"></i>
                            Căn <strong>VACANT</strong> — chưa gán cư dân.
                            <c:if test="${canManage}">
                                <div class="mt-2">
                                    Hãy
                                    <a href="${pageContext.request.contextPath}/apartment?action=edit&amp;id=${apartment.apartmentId}">
                                        Sửa loại hình
                                    </a>
                                    → <strong>OWNED</strong> (mua) hoặc <strong>RENTED</strong> (thuê), rồi gán thông tin.
                                </div>
                            </c:if>
                        </div>
                    </c:when>
                    <c:when test="${empty owners}">
                        <div class="text-muted small py-3 text-center">
                            <i class="bi bi-person-x d-block mb-1 fs-4"></i>
                            <c:choose>
                                <c:when test="${isRented}">Chưa gán chủ nhà</c:when>
                                <c:otherwise>Chưa gán chủ sở hữu</c:otherwise>
                            </c:choose>
                            <c:if test="${canManage && (isOwnedOnly || isRented)}">
                                <div class="mt-2">
                                    <a href="${pageContext.request.contextPath}/apartment?action=assign-owner&amp;id=${apartment.apartmentId}">
                                        ${isRented ? 'Gán chủ nhà' : 'Gán chủ sở hữu'}
                                    </a>
                                </div>
                            </c:if>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="o" items="${owners}">
                            <div class="border rounded p-2 mb-2">
                                <div class="fw-semibold">
                                    <c:out value="${empty o.fullName ? '—' : o.fullName}"/>
                                </div>
                                <div class="small text-muted">
                                    @<c:out value="${empty o.username ? '—' : o.username}"/>
                                    · role: ${o.roleInApartment}
                                </div>
                                <div class="small">
                                    Từ:
                                    <c:choose>
                                        <c:when test="${empty o.startDate}">—</c:when>
                                        <c:otherwise>${o.startDate}</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
    <%-- OWNED / VACANT: không hiện cột người thuê — chỉ RENTED --%>
    <c:if test="${isRented}">
    <div class="col-md-6">
        <div class="card shadow-sm h-100">
            <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
                <span><i class="bi bi-people me-1"></i> Người thuê</span>
                <c:if test="${canManage && apartment.status == 'ACTIVE'}">
                    <div class="d-flex flex-wrap gap-1">
                        <a class="btn btn-sm btn-outline-primary"
                           href="${pageContext.request.contextPath}/apartment?action=assign-tenant&amp;id=${apartment.apartmentId}">
                            <c:choose>
                                <c:when test="${empty tenants}">Gán thuê</c:when>
                                <c:otherwise>Gán thêm / đổi</c:otherwise>
                            </c:choose>
                        </a>
                        <c:if test="${not empty tenants}">
                            <form method="post" action="${pageContext.request.contextPath}/apartment" class="d-inline"
                                  onsubmit="return confirm('Gỡ toàn bộ người thuê / đại diện thuê khỏi căn này? Thành viên hộ không bị xóa.');">
                                <input type="hidden" name="action" value="remove-tenant">
                                <input type="hidden" name="apartmentId" value="${apartment.apartmentId}">
                                <button type="submit" class="btn btn-sm btn-outline-danger">Gỡ thuê</button>
                            </form>
                        </c:if>
                    </div>
                </c:if>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty tenants}">
                        <div class="text-muted small py-3 text-center">
                            <i class="bi bi-person-x d-block mb-1 fs-4"></i>
                            Chưa gán người thuê
                            <c:if test="${canManage && apartment.status == 'ACTIVE'}">
                                <div class="mt-2">
                                    <a href="${pageContext.request.contextPath}/apartment?action=assign-tenant&amp;id=${apartment.apartmentId}">
                                        Gán người thuê
                                    </a>
                                </div>
                            </c:if>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="t" items="${tenants}">
                            <div class="border rounded p-2 mb-2">
                                <div class="fw-semibold">
                                    <c:out value="${empty t.fullName ? '—' : t.fullName}"/>
                                    <c:choose>
                                        <c:when test="${t.roleInApartment == 'TENANT_REP'}">
                                            <span class="badge text-bg-info">Đại diện</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-secondary">Thuê</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="small text-muted">
                                    @<c:out value="${empty t.username ? '—' : t.username}"/>
                                    · ${t.roleInApartment}
                                </div>
                                <div class="small">
                                    Từ:
                                    <c:choose>
                                        <c:when test="${empty t.startDate}">—</c:when>
                                        <c:otherwise>${t.startDate}</c:otherwise>
                                    </c:choose>
                                    →
                                    <c:choose>
                                        <c:when test="${empty t.endDate}">đang mở</c:when>
                                        <c:otherwise>${t.endDate}</c:otherwise>
                                    </c:choose>
                                    · <span class="badge text-bg-success">CURRENT</span>
                                </div>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
    </c:if>
</div>

<%-- ===== Thành viên hộ =====
     Thứ tự: gán owner (OWNED) hoặc owner+thuê (RENTED) trước → mới thêm TV khác.
     Owner / người thuê auto có trong list TV khi gán.
--%>
<div class="card shadow-sm mb-3">
    <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
        <span><i class="bi bi-house-heart me-1"></i> Thành viên hộ</span>
        <div class="d-flex align-items-center gap-2">
            <span class="badge text-bg-light text-dark">${empty members ? 0 : members.size()}</span>
            <c:if test="${canManage && canAddMember}">
                <a class="btn btn-sm btn-outline-primary"
                   href="${pageContext.request.contextPath}/apartment?action=add-member&amp;id=${apartment.apartmentId}">
                    <i class="bi bi-person-plus"></i> Thêm TV
                </a>
            </c:if>
        </div>
    </div>
    <c:if test="${canManage && not empty addMemberBlockReason}">
        <div class="alert alert-warning border-0 border-bottom rounded-0 small mb-0">
            <i class="bi bi-info-circle me-1"></i>
            <c:out value="${addMemberBlockReason}"/>
            <span class="ms-1">
                <c:choose>
                    <c:when test="${isVacant}">
                        <a href="${pageContext.request.contextPath}/apartment?action=edit&amp;id=${apartment.apartmentId}">Sửa loại hình</a>
                    </c:when>
                    <c:when test="${isRented}">
                        <a href="${pageContext.request.contextPath}/apartment?action=assign-tenant&amp;id=${apartment.apartmentId}">Gán thuê</a>
                        ·
                        <a href="${pageContext.request.contextPath}/apartment?action=assign-owner&amp;id=${apartment.apartmentId}">Gán chủ nhà</a>
                    </c:when>
                    <c:when test="${isOwnedOnly}">
                        <a href="${pageContext.request.contextPath}/apartment?action=assign-owner&amp;id=${apartment.apartmentId}">Gán chủ sở hữu</a>
                    </c:when>
                </c:choose>
            </span>
        </div>
    </c:if>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-sm table-hover align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th>Họ tên</th>
                    <th>Vai trò</th>
                    <th>SĐT</th>
                    <th>CCCD/CMND</th>
                    <th>Ngày sinh</th>
                    <th>TT</th>
                    <c:if test="${canManage}"><th style="min-width:140px;">Thao tác</th></c:if>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty members}">
                        <tr>
                            <td colspan="${canManage ? 7 : 6}" class="text-center text-muted py-4">
                                <i class="bi bi-inbox me-1"></i> Chưa có thành viên hộ
                                <c:if test="${canManage && canAddMember}">
                                    ·
                                    <a href="${pageContext.request.contextPath}/apartment?action=add-member&amp;id=${apartment.apartmentId}">
                                        Thêm thành viên
                                    </a>
                                </c:if>
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="m" items="${members}">
                            <tr class="${m.isActive ? '' : 'table-secondary'}">
                                <td class="fw-semibold"><c:out value="${m.fullName}"/></td>
                                <td><c:out value="${empty m.relationship ? '—' : m.relationship}"/></td>
                                <td><c:out value="${empty m.phone ? '—' : m.phone}"/></td>
                                <td><c:out value="${empty m.idNumber ? '—' : m.idNumber}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty m.dateOfBirth}">—</c:when>
                                        <c:otherwise>${m.dateOfBirth}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${m.isActive}">
                                            <span class="badge text-bg-success">Active</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-secondary">Off</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <c:if test="${canManage}">
                                    <td>
                                        <div class="d-flex flex-wrap gap-1">
                                            <a class="btn btn-sm btn-outline-primary"
                                               href="${pageContext.request.contextPath}/apartment?action=edit-member&amp;apartmentId=${apartment.apartmentId}&amp;memberId=${m.memberId}">
                                                Sửa
                                            </a>
                                            <c:if test="${m.isActive}">
                                                <form method="post"
                                                      action="${pageContext.request.contextPath}/apartment"
                                                      class="d-inline"
                                                      onsubmit="return confirm('Xóa thành viên ${m.fullName}?\nNếu người này là chủ sở hữu thì sẽ gỡ luôn vai trò owner.\nThao tác không hoàn tác.');">
                                                    <input type="hidden" name="action" value="remove-member">
                                                    <input type="hidden" name="apartmentId" value="${apartment.apartmentId}">
                                                    <input type="hidden" name="memberId" value="${m.memberId}">
                                                    <button type="submit" class="btn btn-sm btn-outline-danger">Xóa</button>
                                                </form>
                                            </c:if>
                                        </div>
                                    </td>
                                </c:if>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%-- ===== Lịch sử ===== --%>
<div class="card shadow-sm mb-3">
    <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
        <span><i class="bi bi-clock-history me-1"></i> Lịch sử cập nhật</span>
        <span class="badge text-bg-light text-dark">${empty histories ? 0 : histories.size()} sự kiện</span>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-sm table-hover align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th style="width: 170px;">Thời điểm</th>
                    <th style="width: 160px;">Hành động</th>
                    <th style="width: 120px;">Trạng thái / vai trò</th>
                    <th style="width: 140px;">Người thực hiện</th>
                    <th>Ghi chú</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty histories}">
                        <tr>
                            <td class="small text-muted">
                                <c:out value="${empty apartment.createdAt ? '—' : apartment.createdAt}"/>
                            </td>
                            <td><span class="badge text-bg-primary">CREATE</span></td>
                            <td class="small">→ ${apartment.status}</td>
                            <td class="small text-muted">Hệ thống</td>
                            <td class="small text-muted">Tạo căn (từ bản ghi apartment — chưa có history DB)</td>
                        </tr>
                        <tr>
                            <td colspan="5" class="small text-muted px-3 pb-3">
                                Chưa có sự kiện gán owner/thuê/thành viên.
                                Sau khi gán owner / gán thuê / thêm TV, các dòng
                                <code>ASSIGN_OWNER</code>, <code>ASSIGN_TENANT</code>, <code>ADD_MEMBER</code>…
                                sẽ hiện tại đây.
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="h" items="${histories}">
                            <tr>
                                <td class="small text-muted">
                                    <c:out value="${empty h.createdAt ? '—' : h.createdAt}"/>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${h.action == 'ASSIGN_OWNER' || h.action == 'CHANGE_OWNER'}">
                                            <span class="badge text-bg-primary">${h.action}</span>
                                        </c:when>
                                        <c:when test="${h.action == 'REMOVE_OWNER'}">
                                            <span class="badge text-bg-danger">${h.action}</span>
                                        </c:when>
                                        <c:when test="${h.action == 'ASSIGN_TENANT' || h.action == 'ASSIGN_TENANT_REP' || h.action == 'CHANGE_TENANT_REP'}">
                                            <span class="badge text-bg-info text-dark">${h.action}</span>
                                        </c:when>
                                        <c:when test="${h.action == 'ADD_MEMBER' || h.action == 'UPDATE_MEMBER'}">
                                            <span class="badge text-bg-success">${h.action}</span>
                                        </c:when>
                                        <c:when test="${h.action == 'REMOVE_MEMBER'}">
                                            <span class="badge text-bg-warning text-dark">${h.action}</span>
                                        </c:when>
                                        <c:when test="${h.action == 'DEACTIVATE' || h.action == 'ACTIVATE' || h.action == 'DELETE'}">
                                            <span class="badge text-bg-secondary">${h.action}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-dark">${h.action}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="small">
                                    <c:choose>
                                        <c:when test="${empty h.oldStatus && empty h.newStatus}">—</c:when>
                                        <c:when test="${empty h.oldStatus}">→ <c:out value="${h.newStatus}"/></c:when>
                                        <c:when test="${empty h.newStatus}"><c:out value="${h.oldStatus}"/> →</c:when>
                                        <c:otherwise>
                                            <c:out value="${h.oldStatus}"/> → <c:out value="${h.newStatus}"/>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="small">
                                    <c:out value="${empty h.actorName ? '—' : h.actorName}"/>
                                </td>
                                <td class="small text-muted">
                                    <c:out value="${empty h.note ? '—' : h.note}"/>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<c:if test="${canManage}">
    <div class="alert alert-light border small mb-0">
        <i class="bi bi-lightbulb me-1"></i>
        <strong>Gán owner</strong> (UC-06) · <strong>Gán thuê</strong> (UC-07) ·
        <strong>Thành viên</strong> thêm/sửa/gỡ soft-delete (UC-08/09) đã có.
    </div>
</c:if>
