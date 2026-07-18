<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
  UC-APT-06 · Gán owner / chủ nhà
  - OWNED: chủ ở → có thể chọn TV hộ + user hệ thống + tạo mới (sync TV Chủ hộ)
  - RENTED: chủ nhà (landlord) → CHỈ user hệ thống / tạo mới — KHÔNG chọn thành viên hộ, KHÔNG vào TV
  UX: search autocomplete + sub-form tạo mới (giống assign-tenant)
--%>

<c:set var="isRented" value="${apartment.occupancyType == 'RENTED'}" />
<c:set var="allowHousehold" value="${!isRented && not empty householdMembers}" />
<c:set var="roleLabel" value="${isRented ? 'Chủ nhà' : 'Chủ sở hữu'}" />

<style>
    .assign-owner-page .card { border: 0; box-shadow: 0 .25rem .75rem rgba(15, 23, 42, .06); }
    .assign-owner-page .section-title {
        font-size: .72rem; font-weight: 700; letter-spacing: .04em;
        text-transform: uppercase; color: #64748b; margin-bottom: .75rem;
    }
    .assign-owner-page .search-wrap { position: relative; }
    .assign-owner-page .search-results {
        position: absolute; z-index: 20; left: 0; right: 0; top: calc(100% + 4px);
        max-height: 280px; overflow-y: auto; background: #fff;
        border: 1px solid #e2e8f0; border-radius: .5rem;
        box-shadow: 0 .5rem 1.25rem rgba(15, 23, 42, .12);
        display: none;
    }
    .assign-owner-page .search-results.show { display: block; }
    .assign-owner-page .search-item {
        width: 100%; text-align: left; border: 0; background: transparent;
        padding: .65rem .9rem; display: flex; align-items: center; justify-content: space-between;
        gap: .75rem; border-bottom: 1px solid #f1f5f9;
    }
    .assign-owner-page .search-item:last-child { border-bottom: 0; }
    .assign-owner-page .search-item:hover { background: #eef2ff; }
    .assign-owner-page .search-item .meta { color: #94a3b8; font-size: .8rem; }
    .assign-owner-page .selected-chip {
        display: flex; align-items: center; justify-content: space-between;
        gap: .75rem; padding: .75rem .9rem; border-radius: .5rem;
        background: #eef2ff; border: 1px solid #c7d2fe;
    }
    .assign-owner-page .subtext { font-size: .8rem; color: #94a3b8; }
    .assign-owner-page .btn-link-create {
        color: #4f46e5; text-decoration: none; font-weight: 600; font-size: .9rem;
    }
    .assign-owner-page .btn-link-create:hover { color: #3730a3; text-decoration: underline; }
    .assign-owner-page .new-person-box {
        border: 1px dashed #c7d2fe; border-radius: .75rem; background: #f8fafc; padding: 1rem;
    }
</style>

<div class="assign-owner-page">
    <div class="d-flex justify-content-between align-items-start gap-2 mb-3">
        <div>
            <h2 class="h4 mb-1 text-dark">
                <c:choose>
                    <c:when test="${isRented}">Gán chủ nhà</c:when>
                    <c:when test="${empty currentOwner}">Gán chủ sở hữu</c:when>
                    <c:otherwise>Đổi chủ sở hữu</c:otherwise>
                </c:choose>
            </h2>
            <p class="text-muted small mb-0">
                Căn <strong class="text-primary"><c:out value="${apartment.apartmentCode}"/></strong>
                · Tòa <c:out value="${apartment.building}"/>
                · Tầng ${apartment.floorNumber}
                ·
                <c:choose>
                    <c:when test="${isRented}"><span class="badge text-bg-primary">RENTED</span> · chủ nhà (landlord)</c:when>
                    <c:otherwise><span class="badge text-bg-info">OWNED</span> · chủ ở</c:otherwise>
                </c:choose>
            </p>
        </div>
        <a class="btn btn-outline-secondary btn-sm"
           href="${pageContext.request.contextPath}/apartment?action=detail&amp;id=${apartment.apartmentId}">
            <i class="bi bi-arrow-left me-1"></i> Về chi tiết
        </a>
    </div>

    <c:if test="${not empty errors}">
        <div class="alert alert-danger shadow-sm">
            <div class="fw-semibold mb-1"><i class="bi bi-exclamation-triangle me-1"></i> Không thể gán</div>
            <ul class="mb-0 ps-3">
                <c:forEach var="err" items="${errors}">
                    <li>${err}</li>
                </c:forEach>
            </ul>
        </div>
    </c:if>

    <div class="row g-3">
        <div class="col-lg-4">
            <div class="card mb-3">
                <div class="card-body">
                    <div class="section-title">${roleLabel} hiện tại</div>
                    <c:choose>
                        <c:when test="${empty currentOwner}">
                            <div class="text-muted small text-center py-3">
                                <i class="bi bi-person-x fs-4 d-block mb-1"></i>
                                Chưa gán · gán lần đầu
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="fw-semibold"><c:out value="${currentOwner.fullName}"/></div>
                            <div class="subtext">@<c:out value="${currentOwner.username}"/></div>
                            <div class="small mt-2 text-muted">
                                Từ:
                                <c:choose>
                                    <c:when test="${empty currentOwner.startDate}">—</c:when>
                                    <c:otherwise>${currentOwner.startDate}</c:otherwise>
                                </c:choose>
                            </div>
                            <div class="alert alert-warning small mt-3 mb-0">
                                Khi gán người mới, bản ghi hiện tại sẽ được
                                <strong>đóng</strong> (giữ lịch sử).
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <c:if test="${isRented}">
                <div class="card">
                    <div class="card-body small text-muted">
                        <i class="bi bi-info-circle text-primary me-1"></i>
                        <strong>Chủ nhà</strong> trên căn RENTED là người cho thuê (landlord),
                        <strong>không</strong> nằm trong thành viên hộ.
                        Thành viên hộ chỉ gồm người thuê / đại diện thuê.
                    </div>
                </div>
            </c:if>
        </div>

        <div class="col-lg-8">
            <div class="card">
                <div class="card-body p-4">
                    <form method="post" action="${pageContext.request.contextPath}/apartment"
                          id="assignOwnerForm" novalidate>
                        <input type="hidden" name="action" value="assign-owner">
                        <input type="hidden" name="apartmentId" value="${apartment.apartmentId}">
                        <input type="hidden" name="personSource" id="personSource"
                               value="${empty personSource ? 'existing' : personSource}">
                        <input type="hidden" name="userId" id="userId"
                               value="${selectedUserId != null ? selectedUserId : ''}">
                        <input type="hidden" name="memberId" id="memberId"
                               value="${selectedMemberId != null ? selectedMemberId : ''}">

                        <%-- PHẦN 1: Chọn người --%>
                        <div class="mb-4">
                            <div class="section-title">
                                <i class="bi bi-1-circle text-primary me-1"></i>
                                <c:choose>
                                    <c:when test="${isRented}">Xác định chủ nhà</c:when>
                                    <c:otherwise>Xác định chủ sở hữu</c:otherwise>
                                </c:choose>
                            </div>

                            <div id="blockSearchOwner" class="${personSource == 'new' ? 'd-none' : ''}">
                                <label class="form-label" for="ownerSearch">
                                    Tìm ${isRented ? 'chủ nhà' : 'chủ sở hữu'} <span class="text-danger">*</span>
                                </label>
                                <div class="search-wrap">
                                    <div class="input-group">
                                        <span class="input-group-text bg-white text-secondary">
                                            <i class="bi bi-search"></i>
                                        </span>
                                        <input type="text" class="form-control" id="ownerSearch"
                                               placeholder="Gõ tên hoặc SĐT..."
                                               autocomplete="off"
                                               value="${empty searchLabel ? '' : searchLabel}">
                                    </div>
                                    <div class="search-results" id="ownerSearchResults" role="listbox"></div>
                                </div>
                                <div class="subtext mt-1 mb-2">
                                    <c:choose>
                                        <c:when test="${isRented}">
                                            Chỉ tìm <strong>user hệ thống</strong> (chủ nhà không lấy từ thành viên hộ).
                                        </c:when>
                                        <c:when test="${allowHousehold}">
                                            Gõ để lọc thành viên hộ và user hệ thống.
                                        </c:when>
                                        <c:otherwise>
                                            Gõ để lọc user hệ thống.
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div id="selectedOwnerChip" class="selected-chip mb-2 ${empty searchLabel ? 'd-none' : ''}">
                                    <div>
                                        <div class="fw-semibold" id="selectedOwnerName">
                                            <c:out value="${searchLabel}"/>
                                        </div>
                                        <div class="subtext" id="selectedOwnerTag"></div>
                                    </div>
                                    <button type="button" class="btn btn-sm btn-outline-secondary" id="btnClearOwner">
                                        Đổi
                                    </button>
                                </div>

                                <button type="button" class="btn btn-link btn-link-create p-0" id="btnShowNewOwner">
                                    <i class="bi bi-plus-lg me-1"></i> Tạo người mới
                                </button>
                            </div>

                            <div id="blockNewOwner" class="new-person-box ${personSource == 'new' ? '' : 'd-none'}">
                                <div class="d-flex justify-content-between align-items-center mb-3">
                                    <div class="fw-semibold text-primary">
                                        <i class="bi bi-person-plus me-1"></i> Tạo người mới
                                    </div>
                                    <button type="button" class="btn btn-sm btn-outline-secondary" id="btnBackOwnerSearch">
                                        ← Quay lại tìm kiếm
                                    </button>
                                </div>
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label class="form-label" for="newFullName">
                                            Họ và tên <span class="text-danger">*</span>
                                        </label>
                                        <input type="text" class="form-control" id="newFullName" name="newFullName"
                                               value="${newFullName}" maxlength="100" placeholder="VD: Nguyễn Văn A">
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
                                        <label class="form-label" for="newEmail">
                                            Email <span class="subtext">(tuỳ chọn)</span>
                                        </label>
                                        <input type="email" class="form-control" id="newEmail" name="newEmail"
                                               value="${newEmail}" maxlength="100">
                                    </div>
                                </div>
                                <div class="subtext mt-2 mb-0">
                                    Tạo user <strong>RESIDENT</strong> (pass <code>123456</code>) rồi gán
                                    <c:choose>
                                        <c:when test="${isRented}">làm chủ nhà (không vào thành viên hộ).</c:when>
                                        <c:otherwise>làm chủ sở hữu + thêm TV (Chủ hộ) nếu chưa có.</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>

                        <hr class="text-secondary-subtle my-4">

                        <%-- PHẦN 2: Thời điểm --%>
                        <div class="mb-4">
                            <div class="section-title">
                                <i class="bi bi-2-circle text-primary me-1"></i> Thời điểm gán
                            </div>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label" for="startDate">
                                        Ngày bắt đầu <span class="text-danger">*</span>
                                    </label>
                                    <input type="date" class="form-control" id="startDate" name="startDate"
                                           value="${startDate}" required>
                                    <div class="subtext mt-1">1 căn tối đa 1 ${isRented ? 'chủ nhà' : 'owner'} hiện tại.</div>
                                </div>
                            </div>
                        </div>

                        <div class="d-flex flex-wrap gap-2 pt-2">
                            <button type="submit" class="btn btn-primary px-4">
                                <i class="bi bi-check-lg me-1"></i>
                                <c:choose>
                                    <c:when test="${isRented && empty currentOwner}">Gán chủ nhà</c:when>
                                    <c:when test="${isRented}">Đổi chủ nhà</c:when>
                                    <c:when test="${empty currentOwner}">Gán chủ sở hữu</c:when>
                                    <c:otherwise>Đổi chủ sở hữu</c:otherwise>
                                </c:choose>
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

<div id="ownerCandidatesData" class="d-none" aria-hidden="true">
    <c:if test="${allowHousehold}">
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
    </c:if>
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
    var candidates = [];
    var nodes = document.querySelectorAll('#ownerCandidatesData .cand');
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
    var searchInput = document.getElementById('ownerSearch');
    var resultsBox = document.getElementById('ownerSearchResults');
    var blockSearch = document.getElementById('blockSearchOwner');
    var blockNew = document.getElementById('blockNewOwner');
    var chip = document.getElementById('selectedOwnerChip');
    var chipName = document.getElementById('selectedOwnerName');
    var chipTag = document.getElementById('selectedOwnerTag');
    var form = document.getElementById('assignOwnerForm');

    function norm(s) { return (s || '').toString().toLowerCase().trim(); }

    function filterCandidates(q) {
        q = norm(q);
        if (!q) return candidates.slice(0, 12);
        return candidates.filter(function (c) {
            return norm(c.name).indexOf(q) >= 0
                || norm(c.phone).indexOf(q) >= 0
                || norm(c.username).indexOf(q) >= 0;
        }).slice(0, 20);
    }

    function escapeHtml(s) {
        return String(s || '')
            .replace(/&/g, '&amp;').replace(/</g, '&lt;')
            .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
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

    function findByKey(key) {
        for (var i = 0; i < candidates.length; i++) {
            if (candidates[i].key === key) return candidates[i];
        }
        return null;
    }

    function selectByKey(key) {
        var c = findByKey(key);
        if (!c) return;
        // RENTED: không bao giờ set household (đã lọc candidates)
        personSourceEl.value = c.source;
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
        searchInput.focus();
    }

    function showSearchMode(keep) {
        if (!keep) {
            personSourceEl.value = 'existing';
            userIdEl.value = '';
            memberIdEl.value = '';
        }
        blockSearch.classList.remove('d-none');
        blockNew.classList.add('d-none');
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

    searchInput.addEventListener('focus', function () {
        renderResults(filterCandidates(searchInput.value));
    });
    searchInput.addEventListener('input', function () {
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

    document.getElementById('btnShowNewOwner').addEventListener('click', showNewMode);
    document.getElementById('btnBackOwnerSearch').addEventListener('click', function () {
        showSearchMode(false);
    });
    document.getElementById('btnClearOwner').addEventListener('click', clearSelection);

    (function restore() {
        var src = personSourceEl.value || 'existing';
        if (src === 'new') { showNewMode(); return; }
        // RENTED: bỏ household nếu lỡ gửi
        if (src === 'household' && ${isRented ? 'true' : 'false'}) {
            personSourceEl.value = 'existing';
            memberIdEl.value = '';
        }
        var uid = userIdEl.value;
        var mid = memberIdEl.value;
        if (mid && ${allowHousehold ? 'true' : 'false'}) selectByKey('member:' + mid);
        else if (uid) selectByKey('user:' + uid);
        else showSearchMode(false);
    })();

    form.addEventListener('submit', function (e) {
        var src = personSourceEl.value;
        var msgs = [];
        if (src === 'new') {
            var name = (document.getElementById('newFullName').value || '').trim();
            var phone = (document.getElementById('newPhone').value || '').trim();
            if (!name || name.length < 2) msgs.push('Vui lòng nhập họ và tên.');
            if (!phone) msgs.push('Vui lòng nhập số điện thoại.');
            else if (!/^0?\d{9,10}$/.test(phone)) msgs.push('Số điện thoại không hợp lệ.');
        } else if (!userIdEl.value && !memberIdEl.value) {
            msgs.push('Vui lòng chọn người từ kết quả tìm kiếm hoặc tạo người mới.');
        }
        if (!document.getElementById('startDate').value) {
            msgs.push('Vui lòng chọn ngày bắt đầu.');
        }
        if (msgs.length) {
            e.preventDefault();
            alert(msgs.join('\n'));
        }
    });
})();
</script>
