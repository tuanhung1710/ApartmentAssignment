<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
  UC-APT-07 · Form gán người thuê (UX admin dashboard)
  Stack: JSP + JSTL + Bootstrap 5 (coding-standards)
  Phần 1: Search Select (TV hộ / User hệ thống) + sub-form tạo người mới
  Phần 2: Vai trò thuê · Ngày bắt đầu · Ngày kết thúc
--%>

<style>
    .assign-tenant-page .card { border: 0; box-shadow: 0 .25rem .75rem rgba(15, 23, 42, .06); }
    .assign-tenant-page .section-title {
        font-size: .72rem; font-weight: 700; letter-spacing: .04em;
        text-transform: uppercase; color: #64748b; margin-bottom: .75rem;
    }
    .assign-tenant-page .search-wrap { position: relative; }
    .assign-tenant-page .search-results {
        position: absolute; z-index: 20; left: 0; right: 0; top: calc(100% + 4px);
        max-height: 280px; overflow-y: auto; background: #fff;
        border: 1px solid #e2e8f0; border-radius: .5rem;
        box-shadow: 0 .5rem 1.25rem rgba(15, 23, 42, .12);
        display: none;
    }
    .assign-tenant-page .search-results.show { display: block; }
    .assign-tenant-page .search-item {
        width: 100%; text-align: left; border: 0; background: transparent;
        padding: .65rem .9rem; display: flex; align-items: center; justify-content: space-between;
        gap: .75rem; border-bottom: 1px solid #f1f5f9;
    }
    .assign-tenant-page .search-item:last-child { border-bottom: 0; }
    .assign-tenant-page .search-item:hover,
    .assign-tenant-page .search-item.active { background: #eef2ff; }
    .assign-tenant-page .search-item .meta { color: #94a3b8; font-size: .8rem; }
    .assign-tenant-page .selected-chip {
        display: flex; align-items: center; justify-content: space-between;
        gap: .75rem; padding: .75rem .9rem; border-radius: .5rem;
        background: #eef2ff; border: 1px solid #c7d2fe;
    }
    .assign-tenant-page .subtext { font-size: .8rem; color: #94a3b8; }
    .assign-tenant-page .btn-link-create {
        color: #4f46e5; text-decoration: none; font-weight: 600; font-size: .9rem;
    }
    .assign-tenant-page .btn-link-create:hover { color: #3730a3; text-decoration: underline; }
    .assign-tenant-page .new-person-box {
        border: 1px dashed #c7d2fe; border-radius: .75rem; background: #f8fafc;
        padding: 1rem;
    }
    .assign-tenant-page .status-side .label { font-size: .75rem; color: #94a3b8; }
</style>

<div class="assign-tenant-page">
    <div class="d-flex justify-content-between align-items-start gap-2 mb-3">
        <div>
            <h2 class="h4 mb-1 text-dark">Gán người thuê</h2>
            <p class="text-muted small mb-0">
                Căn <strong class="text-primary"><c:out value="${apartment.apartmentCode}"/></strong>
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
        <div class="alert alert-danger shadow-sm">
            <div class="fw-semibold mb-1"><i class="bi bi-exclamation-triangle me-1"></i> Không thể gán thuê</div>
            <ul class="mb-0 ps-3">
                <c:forEach var="err" items="${errors}">
                    <li>${err}</li>
                </c:forEach>
            </ul>
        </div>
    </c:if>

    <div class="row g-3">
        <%-- ===== Sidebar trạng thái hiện tại ===== --%>
        <div class="col-lg-4 status-side">
            <div class="card mb-3">
                <div class="card-body">
                    <div class="section-title">Đại diện thuê hiện tại</div>
                    <c:choose>
                        <c:when test="${empty currentTenantRep}">
                            <div class="text-muted small text-center py-2">
                                <i class="bi bi-person-x d-block fs-4 mb-1"></i>
                                Chưa có đại diện thuê
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="fw-semibold"><c:out value="${currentTenantRep.fullName}"/></div>
                            <div class="meta small text-muted">@<c:out value="${currentTenantRep.username}"/></div>
                            <div class="small mt-1 text-muted">
                                Từ ${empty currentTenantRep.startDate ? '—' : currentTenantRep.startDate}
                                → ${empty currentTenantRep.endDate ? 'đang mở' : currentTenantRep.endDate}
                            </div>
                            <span class="badge text-bg-success mt-2">CURRENT</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="card">
                <div class="card-body">
                    <div class="section-title">Người thuê hiện tại</div>
                    <c:choose>
                        <c:when test="${empty currentTenants}">
                            <div class="text-muted small text-center py-2">Chưa có TENANT</div>
                        </c:when>
                        <c:otherwise>
                            <ul class="list-unstyled mb-0">
                                <c:forEach var="t" items="${currentTenants}">
                                    <li class="py-2 border-bottom">
                                        <div class="fw-semibold small"><c:out value="${t.fullName}"/></div>
                                        <div class="subtext">@<c:out value="${t.username}"/>
                                            · ${empty t.startDate ? '—' : t.startDate}
                                            → ${empty t.endDate ? 'mở' : t.endDate}
                                        </div>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <%-- ===== Form chính ===== --%>
        <div class="col-lg-8">
            <div class="card">
                <div class="card-body p-4">
                    <form method="post" action="${pageContext.request.contextPath}/apartment"
                          id="assignTenantForm" novalidate>
                        <input type="hidden" name="action" value="assign-tenant">
                        <input type="hidden" name="apartmentId" value="${apartment.apartmentId}">
                        <input type="hidden" name="personSource" id="personSource"
                               value="${empty personSource ? 'existing' : personSource}">
                        <input type="hidden" name="userId" id="userId"
                               value="${selectedUserId != null ? selectedUserId : ''}">
                        <input type="hidden" name="memberId" id="memberId"
                               value="${selectedMemberId != null ? selectedMemberId : ''}">

                        <%-- ========== PHẦN 1: Xác định đối tượng thuê ========== --%>
                        <div class="mb-4">
                            <div class="section-title">
                                <i class="bi bi-1-circle text-primary me-1"></i> Xác định đối tượng thuê
                            </div>

                            <%-- Search Select (Autocomplete) --%>
                            <div id="blockSearchTenant" class="${personSource == 'new' ? 'd-none' : ''}">
                                <label class="form-label" for="tenantSearch">
                                    Tìm người thuê <span class="text-danger">*</span>
                                </label>
                                <div class="search-wrap">
                                    <div class="input-group">
                                        <span class="input-group-text bg-white text-secondary">
                                            <i class="bi bi-search"></i>
                                        </span>
                                        <input type="text" class="form-control" id="tenantSearch"
                                               placeholder="Gõ tên hoặc SĐT..."
                                               autocomplete="off"
                                               value="${empty searchLabel ? '' : searchLabel}">
                                    </div>
                                    <div class="search-results" id="tenantSearchResults" role="listbox"></div>
                                </div>
                                <div class="subtext mt-1 mb-2">
                                    Gõ để lọc thành viên hộ và user hệ thống. Kết quả có tag phân loại.
                                </div>

                                <div id="selectedTenantChip" class="selected-chip mb-2 ${empty searchLabel ? 'd-none' : ''}">
                                    <div>
                                        <div class="fw-semibold" id="selectedTenantName">
                                            <c:out value="${searchLabel}"/>
                                        </div>
                                        <div class="subtext" id="selectedTenantTag"></div>
                                    </div>
                                    <button type="button" class="btn btn-sm btn-outline-secondary" id="btnClearSelection">
                                        Đổi
                                    </button>
                                </div>

                                <button type="button" class="btn btn-link btn-link-create p-0" id="btnShowNewPerson">
                                    <i class="bi bi-plus-lg me-1"></i> Tạo người mới
                                </button>
                            </div>

                            <%-- Sub-form tạo người mới --%>
                            <div id="blockNewTenant" class="new-person-box ${personSource == 'new' ? '' : 'd-none'}">
                                <div class="d-flex justify-content-between align-items-center mb-3">
                                    <div class="fw-semibold text-primary">
                                        <i class="bi bi-person-plus me-1"></i> Tạo người mới
                                    </div>
                                    <button type="button" class="btn btn-sm btn-outline-secondary" id="btnBackToSearch">
                                        ← Quay lại tìm kiếm
                                    </button>
                                </div>
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label class="form-label" for="newFullName">
                                            Họ và tên <span class="text-danger">*</span>
                                        </label>
                                        <input type="text" class="form-control" id="newFullName" name="newFullName"
                                               value="${newFullName}" maxlength="100"
                                               placeholder="VD: Nguyễn Văn A">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label" for="newPhone">
                                            Số điện thoại <span class="text-danger">*</span>
                                        </label>
                                        <input type="text" class="form-control" id="newPhone" name="newPhone"
                                               value="${newPhone}" maxlength="20"
                                               placeholder="0xxxxxxxxx" inputmode="numeric">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label" for="newIdNumber">
                                            Căn cước công dân
                                            <span class="subtext">(tuỳ chọn)</span>
                                        </label>
                                        <input type="text" class="form-control" id="newIdNumber" name="newIdNumber"
                                               value="${newIdNumber}" maxlength="12"
                                               placeholder="9–12 chữ số" inputmode="numeric">
                                    </div>
                                </div>
                                <div class="subtext mt-2 mb-0">
                                    Hệ thống tạo user <strong>RESIDENT</strong> (pass <code>123456</code>),
                                    gán thuê và thêm vào thành viên hộ nếu chưa có.
                                </div>
                            </div>
                        </div>

                        <hr class="text-secondary-subtle my-4">

                        <%-- ========== PHẦN 2: Thông tin thuê nhà ========== --%>
                        <div class="mb-4">
                            <div class="section-title">
                                <i class="bi bi-2-circle text-primary me-1"></i> Thông tin thuê nhà
                            </div>

                            <div class="mb-3">
                                <label class="form-label" for="roleInApartment">
                                    Vai trò thuê <span class="text-danger">*</span>
                                </label>
                                <select class="form-select" id="roleInApartment" name="roleInApartment" required>
                                    <option value="TENANT_REP"
                                        ${selectedRole == 'TENANT_REP' || empty selectedRole ? 'selected' : ''}>
                                        Đại diện thuê (TENANT_REP)
                                    </option>
                                    <option value="TENANT" ${selectedRole == 'TENANT' ? 'selected' : ''}>
                                        Thành viên thuê (TENANT)
                                    </option>
                                </select>
                                <div class="subtext mt-1">
                                    Nếu chọn <strong>Đại diện thuê</strong>, hệ thống sẽ tự động kết thúc nhiệm kỳ của đại diện cũ.
                                </div>
                            </div>

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label" for="startDate">
                                        Ngày bắt đầu <span class="text-danger">*</span>
                                    </label>
                                    <input type="date" class="form-control" id="startDate" name="startDate"
                                           value="${startDate}" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label" for="endDate">
                                        Ngày kết thúc
                                        <span class="subtext">(tuỳ chọn)</span>
                                    </label>
                                    <input type="date" class="form-control" id="endDate" name="endDate"
                                           value="${endDate}">
                                    <div class="subtext mt-1">Để trống nếu hợp đồng vô thời hạn.</div>
                                </div>
                            </div>
                        </div>

                        <div class="d-flex flex-wrap gap-2 pt-2">
                            <button type="submit" class="btn btn-primary px-4" id="btnSubmitAssign">
                                <i class="bi bi-check-lg me-1"></i> Lưu gán thuê
                            </button>
                            <a class="btn btn-outline-secondary"
                               href="${pageContext.request.contextPath}/apartment?action=detail&amp;id=${apartment.apartmentId}">
                                Hủy
                            </a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<%-- Candidate data: data-* attributes (tránh JSON/EL escape phức tạp) --%>
<div id="tenantCandidatesData" class="d-none" aria-hidden="true">
    <c:forEach var="m" items="${householdMembers}">
        <c:if test="${m.isActive}">
            <div class="cand"
                 data-key="member:${m.memberId}"
                 data-source="household"
                 data-member-id="${m.memberId}"
                 data-user-id=""
                 data-name="<c:out value='${m.fullName}'/>"
                 data-phone="<c:out value='${m.phone}'/>"
                 data-username=""
                 data-tag="Thành viên hộ"
                 data-tag-class="text-bg-info"></div>
        </c:if>
    </c:forEach>
    <c:forEach var="u" items="${candidateUsers}">
        <div class="cand"
             data-key="user:${u.userId}"
             data-source="existing"
             data-member-id=""
             data-user-id="${u.userId}"
             data-name="<c:out value='${u.fullName}'/>"
             data-phone="<c:out value='${u.phone}'/>"
             data-username="<c:out value='${u.username}'/>"
             data-tag="User hệ thống"
             data-tag-class="text-bg-secondary"></div>
    </c:forEach>
</div>

<script>
(function () {
    // ===== Parse candidates từ data-* (an toàn hơn JSON trong JSP) =====
    var candidates = [];
    var nodes = document.querySelectorAll('#tenantCandidatesData .cand');
    for (var i = 0; i < nodes.length; i++) {
        var el = nodes[i];
        var mid = el.getAttribute('data-member-id');
        var uid = el.getAttribute('data-user-id');
        candidates.push({
            key: el.getAttribute('data-key'),
            source: el.getAttribute('data-source'),
            memberId: mid ? parseInt(mid, 10) : null,
            userId: uid ? parseInt(uid, 10) : null,
            name: el.getAttribute('data-name') || '',
            phone: el.getAttribute('data-phone') || '',
            username: el.getAttribute('data-username') || '',
            tag: el.getAttribute('data-tag') || '',
            tagClass: el.getAttribute('data-tag-class') || 'text-bg-secondary'
        });
    }

    var personSourceEl = document.getElementById('personSource');
    var userIdEl = document.getElementById('userId');
    var memberIdEl = document.getElementById('memberId');
    var searchInput = document.getElementById('tenantSearch');
    var resultsBox = document.getElementById('tenantSearchResults');
    var blockSearch = document.getElementById('blockSearchTenant');
    var blockNew = document.getElementById('blockNewTenant');
    var chip = document.getElementById('selectedTenantChip');
    var chipName = document.getElementById('selectedTenantName');
    var chipTag = document.getElementById('selectedTenantTag');
    var form = document.getElementById('assignTenantForm');

    function norm(s) {
        return (s || '').toString().toLowerCase().trim();
    }

    /** Lọc theo tên / SĐT (client-side) */
    function filterCandidates(q) {
        q = norm(q);
        if (!q) {
            return candidates.slice(0, 12);
        }
        return candidates.filter(function (c) {
            return norm(c.name).indexOf(q) >= 0
                || norm(c.phone).indexOf(q) >= 0
                || norm(c.username).indexOf(q) >= 0;
        }).slice(0, 20);
    }

    function renderResults(list) {
        if (!list.length) {
            resultsBox.innerHTML = '<div class="p-3 subtext">Không tìm thấy. Thử tạo người mới.</div>';
            resultsBox.classList.add('show');
            return;
        }
        var html = '';
        for (var i = 0; i < list.length; i++) {
            var c = list[i];
            var meta = c.phone ? c.phone : (c.username ? '@' + c.username : '');
            html += '<button type="button" class="search-item" data-key="' + c.key + '">'
                + '<span><span class="fw-semibold">' + escapeHtml(c.name) + '</span>'
                + (meta ? '<div class="meta">' + escapeHtml(meta) + '</div>' : '')
                + '</span>'
                + '<span class="badge ' + c.tagClass + '">' + escapeHtml(c.tag) + '</span>'
                + '</button>';
        }
        resultsBox.innerHTML = html;
        resultsBox.classList.add('show');

        var items = resultsBox.querySelectorAll('.search-item');
        for (var j = 0; j < items.length; j++) {
            items[j].addEventListener('click', function () {
                selectByKey(this.getAttribute('data-key'));
            });
        }
    }

    function escapeHtml(s) {
        return String(s || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function findByKey(key) {
        for (var i = 0; i < candidates.length; i++) {
            if (candidates[i].key === key) return candidates[i];
        }
        return null;
    }

    function selectByKey(key) {
        var c = findByKey(key);
        if (!c) return;
        personSourceEl.value = c.source; // household | existing
        userIdEl.value = c.userId != null ? c.userId : '';
        memberIdEl.value = c.memberId != null ? c.memberId : '';
        searchInput.value = c.name + (c.phone ? ' · ' + c.phone : '');
        chipName.textContent = c.name;
        chipTag.innerHTML = '<span class="badge ' + c.tagClass + '">' + escapeHtml(c.tag) + '</span>'
            + (c.phone ? ' <span class="ms-1">' + escapeHtml(c.phone) + '</span>' : '');
        chip.classList.remove('d-none');
        resultsBox.classList.remove('show');
        showSearchMode(true);
    }

    function clearSelection() {
        personSourceEl.value = 'existing';
        userIdEl.value = '';
        memberIdEl.value = '';
        searchInput.value = '';
        chip.classList.add('d-none');
        chipName.textContent = '';
        chipTag.textContent = '';
        searchInput.focus();
    }

    function showSearchMode(keepSelection) {
        personSourceEl.value = keepSelection ? personSourceEl.value : 'existing';
        if (!keepSelection) {
            userIdEl.value = '';
            memberIdEl.value = '';
        }
        blockSearch.classList.remove('d-none');
        blockNew.classList.add('d-none');
        // clear required on new fields
        document.getElementById('newFullName').removeAttribute('required');
        document.getElementById('newPhone').removeAttribute('required');
    }

    function showNewMode() {
        personSourceEl.value = 'new';
        userIdEl.value = '';
        memberIdEl.value = '';
        chip.classList.add('d-none');
        searchInput.value = '';
        resultsBox.classList.remove('show');
        blockSearch.classList.add('d-none');
        blockNew.classList.remove('d-none');
        document.getElementById('newFullName').setAttribute('required', 'required');
        document.getElementById('newPhone').setAttribute('required', 'required');
        document.getElementById('newFullName').focus();
    }

    // ===== Events =====
    searchInput.addEventListener('focus', function () {
        renderResults(filterCandidates(searchInput.value));
    });
    searchInput.addEventListener('input', function () {
        // đang gõ lại → bỏ selection cũ
        userIdEl.value = '';
        memberIdEl.value = '';
        personSourceEl.value = 'existing';
        chip.classList.add('d-none');
        renderResults(filterCandidates(searchInput.value));
    });
    document.addEventListener('click', function (e) {
        if (!resultsBox.contains(e.target) && e.target !== searchInput) {
            resultsBox.classList.remove('show');
        }
    });

    document.getElementById('btnShowNewPerson').addEventListener('click', showNewMode);
    document.getElementById('btnBackToSearch').addEventListener('click', function () {
        showSearchMode(false);
    });
    document.getElementById('btnClearSelection').addEventListener('click', clearSelection);

    // Prefill selection sau validate fail
    (function restoreSelection() {
        var src = personSourceEl.value || 'existing';
        if (src === 'new') {
            showNewMode();
            return;
        }
        var uid = userIdEl.value;
        var mid = memberIdEl.value;
        if (mid) {
            selectByKey('member:' + mid);
        } else if (uid) {
            selectByKey('user:' + uid);
        } else {
            showSearchMode(false);
        }
    })();

    // ===== Client validation =====
    form.addEventListener('submit', function (e) {
        var src = personSourceEl.value;
        var start = document.getElementById('startDate').value;
        var end = document.getElementById('endDate').value;
        var msgs = [];

        if (src === 'new') {
            var name = (document.getElementById('newFullName').value || '').trim();
            var phone = (document.getElementById('newPhone').value || '').trim();
            var idn = (document.getElementById('newIdNumber').value || '').trim();
            if (!name || name.length < 2) msgs.push('Vui lòng nhập họ và tên (tối thiểu 2 ký tự).');
            if (!phone) msgs.push('Vui lòng nhập số điện thoại.');
            else if (!/^0?\d{9,10}$/.test(phone)) msgs.push('Số điện thoại không hợp lệ.');
            if (idn && !/^\d{9,12}$/.test(idn)) msgs.push('CCCD phải gồm 9–12 chữ số.');
        } else {
            if (!userIdEl.value && !memberIdEl.value) {
                msgs.push('Vui lòng chọn người thuê từ kết quả tìm kiếm hoặc tạo người mới.');
            }
        }
        if (!start) msgs.push('Vui lòng chọn ngày bắt đầu.');
        if (start && end && end < start) {
            msgs.push('Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu.');
        }

        if (msgs.length) {
            e.preventDefault();
            alert(msgs.join('\n'));
        }
    });
})();
</script>
