<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="d-flex flex-wrap justify-content-between align-items-start gap-2 mb-3">
    <div>
        <h2 class="h4 mb-1">
            Căn hộ <span class="text-primary">${apartment.apartmentCode}</span>
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
            ${apartment.building} · Tầng ${apartment.floorNumber}
            ·
            <c:choose>
                <c:when test="${apartment.occupancyType == 'OWNED'}">OWNED – Sở hữu</c:when>
                <c:otherwise>RENTED – Thuê</c:otherwise>
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
                          onsubmit="return confirm('Vô hiệu hóa căn ${apartment.apartmentCode}?');">
                        <input type="hidden" name="action" value="deactivate">
                        <input type="hidden" name="id" value="${apartment.apartmentId}">
                        <button type="submit" class="btn btn-sm btn-outline-warning">
                            <i class="bi bi-pause-circle"></i> Vô hiệu
                        </button>
                    </form>
                </c:when>
                <c:otherwise>
                    <form method="post" action="${pageContext.request.contextPath}/apartment" class="d-inline"
                          onsubmit="return confirm('Kích hoạt lại căn ${apartment.apartmentCode}?');">
                        <input type="hidden" name="action" value="activate">
                        <input type="hidden" name="id" value="${apartment.apartmentId}">
                        <button type="submit" class="btn btn-sm btn-outline-success">
                            <i class="bi bi-play-circle"></i> Kích hoạt
                        </button>
                    </form>
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
                <div class="fw-semibold">${apartment.apartmentCode}</div>
            </div>
            <div class="col-md-3">
                <div class="text-muted small">Tòa nhà</div>
                <div>${apartment.building}</div>
            </div>
            <div class="col-md-3">
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
                        <c:otherwise>
                            <span class="badge text-bg-secondary">RENTED</span>
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

<%-- ===== Chủ sở hữu + Người thuê ===== --%>
<div class="row g-3 mb-3">
    <div class="col-md-6">
        <div class="card shadow-sm h-100">
            <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
                <span><i class="bi bi-person-badge me-1"></i> Chủ sở hữu</span>
                <c:if test="${canManage}">
                    <a class="btn btn-sm btn-outline-primary"
                       href="${pageContext.request.contextPath}/apartment?action=assign-owner&amp;id=${apartment.apartmentId}">
                        <c:choose>
                            <c:when test="${empty owners}">Gán owner</c:when>
                            <c:otherwise>Đổi owner</c:otherwise>
                        </c:choose>
                    </a>
                </c:if>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty owners}">
                        <div class="text-muted small py-3 text-center">
                            <i class="bi bi-person-x d-block mb-1 fs-4"></i>
                            Chưa gán chủ sở hữu
                            <c:if test="${canManage}">
                                <div class="mt-2">
                                    <a href="${pageContext.request.contextPath}/apartment?action=assign-owner&amp;id=${apartment.apartmentId}">
                                        Gán chủ sở hữu
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
    <div class="col-md-6">
        <div class="card shadow-sm h-100">
            <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
                <span><i class="bi bi-people me-1"></i> Người thuê</span>
                <c:if test="${canManage}">
                    <a class="btn btn-sm btn-outline-primary"
                       href="${pageContext.request.contextPath}/apartment?action=assign-tenant&amp;id=${apartment.apartmentId}">
                        Gán thuê
                    </a>
                </c:if>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty tenants}">
                        <div class="text-muted small py-3 text-center">
                            <i class="bi bi-person-x d-block mb-1 fs-4"></i>
                            Chưa gán người thuê
                            <c:if test="${canManage}">
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
</div>

<%-- ===== Thành viên hộ ===== --%>
<div class="card shadow-sm mb-3">
    <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
        <span><i class="bi bi-house-heart me-1"></i> Thành viên hộ</span>
        <div class="d-flex align-items-center gap-2">
            <span class="badge text-bg-light text-dark">${empty members ? 0 : members.size()}</span>
            <c:if test="${canManage}">
                <a class="btn btn-sm btn-outline-primary"
                   href="${pageContext.request.contextPath}/apartment?action=add-member&amp;id=${apartment.apartmentId}">
                    <i class="bi bi-person-plus"></i> Thêm TV
                </a>
            </c:if>
        </div>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-sm table-hover align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th>Họ tên</th>
                    <th>Quan hệ</th>
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
                                <c:if test="${canManage}">
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
                                                      onsubmit="return confirm('Gỡ thành viên ${m.fullName}? (Soft delete – giữ lịch sử, is_active=0)');">
                                                    <input type="hidden" name="action" value="remove-member">
                                                    <input type="hidden" name="apartmentId" value="${apartment.apartmentId}">
                                                    <input type="hidden" name="memberId" value="${m.memberId}">
                                                    <button type="submit" class="btn btn-sm btn-outline-danger">Gỡ</button>
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
    <div class="card-header bg-white fw-semibold">
        <i class="bi bi-clock-history me-1"></i> Lịch sử
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-sm align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th style="width: 180px;">Thời điểm</th>
                    <th>Hành động</th>
                    <th>Trạng thái</th>
                    <th>Người thực hiện</th>
                    <th>Ghi chú</th>
                </tr>
                </thead>
                <tbody>
                <%-- Dòng tối thiểu từ entity nếu chưa có history table data --%>
                <c:if test="${empty histories}">
                    <tr>
                        <td class="small text-muted">
                            <c:out value="${empty apartment.createdAt ? '—' : apartment.createdAt}"/>
                        </td>
                        <td><span class="badge text-bg-primary">CREATE</span></td>
                        <td class="small">→ ${apartment.status}</td>
                        <td class="small text-muted">Hệ thống / dữ liệu gốc</td>
                        <td class="small text-muted">Thông tin tạo căn (từ bản ghi căn hộ)</td>
                    </tr>
                    <c:if test="${not empty apartment.updatedAt}">
                        <tr>
                            <td class="small text-muted">${apartment.updatedAt}</td>
                            <td><span class="badge text-bg-secondary">UPDATE</span></td>
                            <td class="small">${apartment.status}</td>
                            <td class="small text-muted">—</td>
                            <td class="small text-muted">Cập nhật gần nhất (từ bản ghi căn hộ)</td>
                        </tr>
                    </c:if>
                </c:if>
                <c:forEach var="h" items="${histories}">
                    <tr>
                        <td class="small text-muted">
                            <c:out value="${empty h.createdAt ? '—' : h.createdAt}"/>
                        </td>
                        <td><span class="badge text-bg-dark">${h.action}</span></td>
                        <td class="small">
                            <c:out value="${empty h.oldStatus ? '—' : h.oldStatus}"/>
                            →
                            <c:out value="${empty h.newStatus ? '—' : h.newStatus}"/>
                        </td>
                        <td class="small">
                            <c:out value="${empty h.actorName ? '—' : h.actorName}"/>
                        </td>
                        <td class="small text-muted">
                            <c:out value="${empty h.note ? '—' : h.note}"/>
                        </td>
                    </tr>
                </c:forEach>
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
