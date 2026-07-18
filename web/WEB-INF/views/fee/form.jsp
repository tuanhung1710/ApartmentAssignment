<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="fee-form-page mx-auto">
    <div class="fee-form-top d-flex justify-content-between align-items-start gap-3 mb-3">
        <div>
            <h1 class="fee-form-title mb-1">Tạo khoản phí</h1>
            <p class="fee-form-subtitle mb-0">
                Tạo nháp (DRAFT) · chọn phạm vi · sau đó gán căn (ASSIGNED) và công bố (PUBLISHED) trên màn chi tiết.
            </p>
        </div>
        <a class="btn fee-btn-back flex-shrink-0"
           href="${pageContext.request.contextPath}/fee?action=list">
            <i class="bi bi-arrow-left me-1"></i> Quay lại
        </a>
    </div>

    <div class="fee-form-card">
        <div class="fee-form-card-head">
            <div class="fee-form-icon-box">
                <i class="bi bi-receipt"></i>
            </div>
            <div>
                <div class="fee-form-card-title">Thông tin khoản phí</div>
                <div class="fee-form-card-desc">Lưu DRAFT trước — gán căn và công bố ở bước sau</div>
            </div>
        </div>

        <div class="fee-form-card-body">
            <form method="post" action="${pageContext.request.contextPath}/fee" id="feeCreateForm" novalidate>
                <input type="hidden" name="action" value="create"/>

                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="fee-label" for="categoryId">
                            Danh mục phí <span class="req">*</span>
                        </label>
                        <select class="form-select fee-input" id="categoryId" name="categoryId" required>
                            <option value="">-- Chọn danh mục --</option>
                            <c:forEach var="cat" items="${categories}">
                                <option value="${cat.categoryId}"
                                    ${formCategoryId == cat.categoryId ? 'selected' : ''}>${cat.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="fee-label" for="title">
                            Tên khoản phí <span class="req">*</span>
                        </label>
                        <input type="text" class="form-control fee-input" id="title" name="title"
                               required maxlength="200"
                               placeholder="VD: Phí quản lý tháng 8/2026"
                               value="${formTitle}"/>
                    </div>
                </div>

                <div class="mb-1 mt-3">
                    <label class="fee-label mb-2">
                        Loại phí <span class="req">*</span>
                    </label>
                    <div class="row g-2">
                        <div class="col-md-6">
                            <input class="btn-check fee-type-radio" type="radio" name="feeType" id="feeTypeMonthly"
                                   value="MONTHLY" ${formFeeType == 'MONTHLY' || empty formFeeType ? 'checked' : ''}/>
                            <label class="scope-tile" for="feeTypeMonthly">
                                <i class="bi bi-calendar-month"></i>
                                <span>Phí hàng tháng / thường niên</span>
                            </label>
                        </div>
                        <div class="col-md-6">
                            <input class="btn-check fee-type-radio" type="radio" name="feeType" id="feeTypeOneTime"
                                   value="ONE_TIME" ${formFeeType == 'ONE_TIME' ? 'checked' : ''}/>
                            <label class="scope-tile" for="feeTypeOneTime">
                                <i class="bi bi-lightning-charge"></i>
                                <span>Phí phát sinh trong tháng</span>
                            </label>
                        </div>
                    </div>
                    <div class="form-text text-muted mt-2" id="feeTypeHint">
                        Phí theo kỳ hàng tháng — chọn tháng/năm áp dụng.
                    </div>
                </div>

                <div class="row g-3 mt-1" id="feePeriodFields">
                    <div class="col-md-4" id="amountField">
                        <label class="fee-label" for="amount">
                            Số tiền (đ) <span class="req">*</span>
                        </label>
                        <div class="input-group fee-amount-group">
                            <input type="number" step="1000" min="10000" class="form-control fee-input"
                                   id="amount" name="amount" required
                                   placeholder="10000"
                                   value="${empty formAmount ? '' : formAmount}"/>
                            <span class="input-group-text fee-amount-suffix">đ</span>
                        </div>
                    </div>
                    <div class="col-md-4" id="monthField">
                        <label class="fee-label" for="feeMonth" id="feeMonthLabel">
                            Tháng áp dụng <span class="req">*</span>
                        </label>
                        <select class="form-select fee-input" id="feeMonth" name="feeMonth">
                            <option value="">-- Chọn tháng --</option>
                            <c:forEach begin="1" end="12" var="m">
                                <option value="${m}" ${formMonth == m ? 'selected' : ''}>Tháng ${m}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-4" id="yearField">
                        <label class="fee-label" for="feeYear" id="feeYearLabel">
                            Năm áp dụng <span class="req">*</span>
                        </label>
                        <input type="number" class="form-control fee-input" id="feeYear" name="feeYear"
                               min="2000" max="2100" required
                               value="${formYear}" placeholder="YYYY"/>
                    </div>
                </div>

                <div class="fee-divider"></div>

                <div class="mb-1">
                    <label class="fee-label mb-2">
                        Phạm vi áp dụng <span class="req">*</span>
                    </label>
                    <div class="row g-2 scope-grid">
                        <div class="col-6 col-md-3">
                            <input class="btn-check scope-radio" type="radio" name="scopeType" id="scopeAll"
                                   value="ALL" ${formScopeType == 'ALL' || empty formScopeType ? 'checked' : ''}/>
                            <label class="scope-tile" for="scopeAll">
                                <i class="bi bi-globe2"></i>
                                <span>Toàn khu</span>
                            </label>
                        </div>
                        <div class="col-6 col-md-3">
                            <input class="btn-check scope-radio" type="radio" name="scopeType" id="scopeBuilding"
                                   value="BUILDING" ${formScopeType == 'BUILDING' ? 'checked' : ''}/>
                            <label class="scope-tile" for="scopeBuilding">
                                <i class="bi bi-building"></i>
                                <span>Theo tòa</span>
                            </label>
                        </div>
                        <div class="col-6 col-md-3">
                            <input class="btn-check scope-radio" type="radio" name="scopeType" id="scopeFloor"
                                   value="FLOOR" ${formScopeType == 'FLOOR' ? 'checked' : ''}/>
                            <label class="scope-tile" for="scopeFloor">
                                <i class="bi bi-layers"></i>
                                <span>Theo tầng</span>
                            </label>
                        </div>
                        <div class="col-6 col-md-3">
                            <input class="btn-check scope-radio" type="radio" name="scopeType" id="scopeApt"
                                   value="APARTMENT" ${formScopeType == 'APARTMENT' ? 'checked' : ''}/>
                            <label class="scope-tile" for="scopeApt">
                                <i class="bi bi-door-open"></i>
                                <span>Theo căn hộ</span>
                            </label>
                        </div>
                    </div>
                    <div class="form-text text-muted mt-2" id="scopeHint">
                        Hệ thống gán cho <strong>mọi căn</strong> trong phạm vi đã chọn (ACTIVE và INACTIVE).
                    </div>
                </div>

                <div class="row g-3 mt-2 align-items-start" id="scopeFieldsRow">
                    <div class="col-12 col-md-4" id="buildingField">
                        <label class="fee-label" for="buildingSelect">
                            Tòa nhà <span class="req scope-req-building d-none">*</span>
                            <span class="text-muted fw-normal small scope-opt-building d-none">(tuỳ chọn)</span>
                        </label>
                        <select class="form-select fee-input" name="building" id="buildingSelect">
                            <option value="">-- Chọn tòa --</option>
                            <c:forEach var="b" items="${buildings}">
                                <option value="${b}" ${formBuilding == b ? 'selected' : ''}>${b}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-12 col-md-4" id="floorField">
                        <label class="fee-label" for="floorSelect">
                            Tầng <span class="req scope-req-floor d-none">*</span>
                            <span class="text-muted fw-normal small scope-opt-floor d-none">(tuỳ chọn)</span>
                        </label>
                        <select class="form-select fee-input" name="floorNumber" id="floorSelect">
                            <option value="">-- Chọn tầng --</option>
                        </select>
                    </div>
                    <div class="col-12 col-md-4" id="apartmentField">
                        <label class="fee-label" for="apartmentSearch">
                            Mã căn hộ <span class="req scope-req-apt d-none">*</span>
                        </label>
                        <div class="input-group fee-search-group">
                            <span class="input-group-text"><i class="bi bi-search"></i></span>
                            <input type="text" class="form-control fee-input" id="apartmentSearch"
                                   placeholder="VD: A101"
                                   autocomplete="off"/>
                        </div>
                        <select class="form-select fee-input mt-2" name="apartmentId" id="apartmentSelect">
                            <option value="">-- Chọn căn --</option>
                            <c:forEach var="a" items="${apartments}">
                                <option value="${a.apartmentId}"
                                        data-building="${a.building}"
                                        data-floor="${a.floorNumber}"
                                        data-status="${a.status}"
                                        data-code="${a.apartmentCode}"
                                    ${formApartmentId == a.apartmentId ? 'selected' : ''}>
                                    ${a.apartmentCode}
                                    <c:if test="${not empty a.building}"> (Tòa ${a.building}<c:if test="${not empty a.floorNumber}"> · Tầng ${a.floorNumber}</c:if>)</c:if>
                                    <c:if test="${a.status == 'INACTIVE'}"> · INACTIVE</c:if>
                                </option>
                            </c:forEach>
                        </select>
                        <div class="form-text text-muted" id="apartmentSearchHint">
                            Gõ mã căn để tự điền tòa/tầng.
                        </div>
                    </div>
                </div>

                <div class="mt-3">
                    <label class="fee-label" for="note">Ghi chú</label>
                    <textarea class="form-control fee-input" id="note" name="note" rows="3"
                              maxlength="500"
                              placeholder="Ghi chú thêm (không bắt buộc)">${formNote}</textarea>
                </div>

                <div class="fee-form-actions">
                    <button type="submit" class="btn fee-btn-submit" id="btnSubmitFee">
                        <i class="bi bi-check2-circle me-1"></i> Lưu nháp (DRAFT)
                    </button>
                    <a class="btn fee-btn-cancel"
                       href="${pageContext.request.contextPath}/fee?action=list">
                        Hủy
                    </a>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    (function () {
        var radios = document.querySelectorAll('.scope-radio');
        var typeRadios = document.querySelectorAll('.fee-type-radio');
        var buildingField = document.getElementById('buildingField');
        var floorField = document.getElementById('floorField');
        var apartmentField = document.getElementById('apartmentField');
        var buildingSelect = document.getElementById('buildingSelect');
        var floorSelect = document.getElementById('floorSelect');
        var apartmentSelect = document.getElementById('apartmentSelect');
        var apartmentSearch = document.getElementById('apartmentSearch');
        var apartmentSearchHint = document.getElementById('apartmentSearchHint');
        var form = document.getElementById('feeCreateForm');
        var feeMonthLabel = document.getElementById('feeMonthLabel');
        var feeYearLabel = document.getElementById('feeYearLabel');
        var feeTypeHint = document.getElementById('feeTypeHint');
        var monthField = document.getElementById('monthField');
        var yearField = document.getElementById('yearField');
        var amountField = document.getElementById('amountField');
        var feeMonth = document.getElementById('feeMonth');
        var feeYear = document.getElementById('feeYear');

        var floorsByBuilding = {};
        if (apartmentSelect) {
            for (var i = 0; i < apartmentSelect.options.length; i++) {
                var opt = apartmentSelect.options[i];
                var b = opt.getAttribute('data-building');
                var f = opt.getAttribute('data-floor');
                if (!b || !f) continue;
                if (!floorsByBuilding[b]) floorsByBuilding[b] = {};
                floorsByBuilding[b][f] = true;
            }
        }

        var preselectedFloor = '${formFloor}';

        function currentScope() {
            for (var i = 0; i < radios.length; i++) {
                if (radios[i].checked) return radios[i].value;
            }
            return 'ALL';
        }

        function currentFeeType() {
            for (var i = 0; i < typeRadios.length; i++) {
                if (typeRadios[i].checked) return typeRadios[i].value;
            }
            return 'MONTHLY';
        }

        function updateFeeTypeLabels() {
            var t = currentFeeType();
            var isOneTime = (t === 'ONE_TIME');

            if (monthField) {
                monthField.style.display = isOneTime ? '' : 'none';
            }
            if (feeMonth) {
                feeMonth.required = isOneTime;
            }
            if (amountField) {
                amountField.className = isOneTime ? 'col-md-4' : 'col-md-6';
            }
            if (yearField) {
                yearField.className = isOneTime ? 'col-md-4' : 'col-md-6';
            }

            if (isOneTime) {
                if (feeMonthLabel) feeMonthLabel.innerHTML = 'Tháng áp dụng <span class="req">*</span>';
                if (feeYearLabel) feeYearLabel.innerHTML = 'Năm áp dụng <span class="req">*</span>';
                if (feeTypeHint) feeTypeHint.textContent = 'Phí phát sinh trong tháng — chọn số tiền, tháng và năm.';
            } else {
                if (feeYearLabel) feeYearLabel.innerHTML = 'Năm áp dụng <span class="req">*</span>';
                if (feeTypeHint) feeTypeHint.textContent = 'Phí hàng tháng / thường niên — chọn số tiền và năm áp dụng.';
            }
        }

        function fillFloors(building, selectedFloor) {
            if (!floorSelect) return;
            floorSelect.innerHTML = '<option value="">-- Chọn tầng --</option>';
            if (!building || !floorsByBuilding[building]) return;
            var floors = Object.keys(floorsByBuilding[building]).map(function (x) {
                return parseInt(x, 10);
            }).filter(function (n) {
                return !isNaN(n);
            }).sort(function (a, b) {
                return a - b;
            });
            for (var i = 0; i < floors.length; i++) {
                var o = document.createElement('option');
                o.value = String(floors[i]);
                o.textContent = 'Tầng ' + floors[i];
                if (selectedFloor !== '' && String(floors[i]) === String(selectedFloor)) {
                    o.selected = true;
                }
                floorSelect.appendChild(o);
            }
        }

        var syncingFromApartment = false;

        function setBuildingAndFloor(building, floor) {
            if (!buildingSelect || !floorSelect) return;
            syncingFromApartment = true;
            try {
                if (building) {
                    if (buildingSelect.value !== building) {
                        buildingSelect.value = building;
                    }
                    fillFloors(building, floor != null && floor !== '' ? String(floor) : '');
                }
                if (floor != null && floor !== '') {
                    floorSelect.value = String(floor);
                }
            } finally {
                syncingFromApartment = false;
            }
        }

        function applyApartmentToBuildingFloor(opt, fillSearch) {
            if (!opt || !opt.value) return;
            var b = opt.getAttribute('data-building') || '';
            var f = opt.getAttribute('data-floor') || '';
            var code = opt.getAttribute('data-code') || '';
            setBuildingAndFloor(b, f);
            if (fillSearch && apartmentSearch && code) {
                apartmentSearch.value = code;
            }
        }

        function filterApartments(opts) {
            opts = opts || {};
            if (!apartmentSelect) return;
            var qRaw = apartmentSearch ? (apartmentSearch.value || '').trim() : '';
            var q = qRaw.toLowerCase();
            var scope = currentScope();
            var filterBuilding = '';
            var filterFloor = '';

            var searchingByCode = !!q;
            if (scope === 'APARTMENT' && !searchingByCode) {
                filterBuilding = buildingSelect ? (buildingSelect.value || '') : '';
                filterFloor = floorSelect ? (floorSelect.value || '') : '';
            }

            var visible = 0;
            var visibleOpts = [];
            var exactCodeMatch = null;
            for (var i = 0; i < apartmentSelect.options.length; i++) {
                var opt = apartmentSelect.options[i];
                if (!opt.value) {
                    opt.hidden = false;
                    opt.disabled = false;
                    continue;
                }
                var code = (opt.getAttribute('data-code') || '').toLowerCase();
                var building = (opt.getAttribute('data-building') || '');
                var floor = (opt.getAttribute('data-floor') || '');
                var matchBuilding = !filterBuilding || building === filterBuilding;
                var matchFloor = !filterFloor || String(floor) === String(filterFloor);

                var matchQuery = !q || code.indexOf(q) !== -1;
                var show = matchBuilding && matchFloor && matchQuery;
                opt.hidden = !show;
                opt.disabled = !show;
                if (show) {
                    visible++;
                    visibleOpts.push(opt);
                    if (code === q) {
                        exactCodeMatch = opt;
                    }
                }
            }
            if (apartmentSearchHint) {
                if (q) {
                    apartmentSearchHint.textContent = 'Tìm thấy ' + visible + ' căn theo mã "' + qRaw + '".';
                } else if (filterBuilding || filterFloor) {
                    var parts = [];
                    if (filterBuilding) parts.push('tòa ' + filterBuilding);
                    if (filterFloor) parts.push('tầng ' + filterFloor);
                    apartmentSearchHint.textContent = 'Tìm thấy ' + visible + ' căn · ' + parts.join(' · ') + '.';
                } else {
                    apartmentSearchHint.textContent = 'Gõ mã căn để tự điền tòa/tầng.';
                }
            }
            if (apartmentSelect.selectedIndex >= 0) {
                var sel = apartmentSelect.options[apartmentSelect.selectedIndex];
                if (sel && sel.hidden) {
                    apartmentSelect.value = '';
                }
            }

            if (opts.autoFillFromSearch && searchingByCode) {
                var pick = exactCodeMatch;
                if (!pick && visibleOpts.length === 1) {
                    pick = visibleOpts[0];
                }
                if (pick) {
                    apartmentSelect.value = pick.value;
                    applyApartmentToBuildingFloor(pick, false);
                    if (apartmentSearchHint) {
                        var pb = pick.getAttribute('data-building') || '';
                        var pf = pick.getAttribute('data-floor') || '';
                        var pc = pick.getAttribute('data-code') || '';
                        apartmentSearchHint.textContent = 'Đã chọn ' + pc
                            + (pb ? ' · Tòa ' + pb : '')
                            + (pf ? ' · Tầng ' + pf : '')
                            + ' (đã điền tòa/tầng).';
                    }
                }
            }
        }

        function updateScopeHint(scope) {
            var hint = document.getElementById('scopeHint');
            if (!hint) return;
            if (scope === 'APARTMENT') {
                hint.innerHTML = 'Gõ <strong>mã căn hộ</strong> để tự điền tòa/tầng, hoặc chọn tòa/tầng rồi chọn căn trong danh sách.';
            } else if (scope === 'FLOOR') {
                hint.innerHTML = 'Gán cho <strong>mọi căn</strong> thuộc tòa + tầng đã chọn (ACTIVE và INACTIVE).';
            } else if (scope === 'BUILDING') {
                hint.innerHTML = 'Gán cho <strong>mọi căn</strong> thuộc tòa đã chọn (ACTIVE và INACTIVE).';
            } else {
                hint.innerHTML = 'Gán cho <strong>mọi căn</strong> trong toàn khu (ACTIVE và INACTIVE).';
            }
        }

        function toggle() {
            var s = currentScope();

            var showBuilding = (s === 'BUILDING' || s === 'FLOOR' || s === 'APARTMENT');
            var showFloor = (s === 'FLOOR' || s === 'APARTMENT');
            var showApt = (s === 'APARTMENT');
            var needBuilding = (s === 'BUILDING' || s === 'FLOOR');
            var needFloor = (s === 'FLOOR');
            var needApt = (s === 'APARTMENT');

            buildingField.style.display = showBuilding ? '' : 'none';
            floorField.style.display = showFloor ? '' : 'none';
            apartmentField.style.display = showApt ? '' : 'none';

            buildingSelect.required = needBuilding;
            floorSelect.required = needFloor;
            apartmentSelect.required = needApt;

            document.querySelectorAll('.scope-req-building').forEach(function (el) {
                el.classList.toggle('d-none', !needBuilding);
            });
            document.querySelectorAll('.scope-req-floor').forEach(function (el) {
                el.classList.toggle('d-none', !needFloor);
            });
            document.querySelectorAll('.scope-req-apt').forEach(function (el) {
                el.classList.toggle('d-none', !needApt);
            });

            document.querySelectorAll('.scope-opt-building').forEach(function (el) {
                el.classList.toggle('d-none', s !== 'APARTMENT');
            });
            document.querySelectorAll('.scope-opt-floor').forEach(function (el) {
                el.classList.toggle('d-none', s !== 'APARTMENT');
            });

            if (showFloor && buildingSelect.value) {
                fillFloors(buildingSelect.value, preselectedFloor || floorSelect.value);
            }
            if (!showBuilding) {
                buildingSelect.value = '';
            }
            if (!showFloor) {
                floorSelect.value = '';
            }
            if (!showApt) {
                apartmentSelect.value = '';
                if (apartmentSearch) {
                    apartmentSearch.value = '';
                }
            }
            if (showApt) {
                filterApartments();
            }
            updateScopeHint(s);
        }

        for (var i = 0; i < radios.length; i++) {
            radios[i].addEventListener('change', function () {
                preselectedFloor = '';
                toggle();
            });
        }

        for (var t = 0; t < typeRadios.length; t++) {
            typeRadios[t].addEventListener('change', updateFeeTypeLabels);
        }

        if (buildingSelect) {
            buildingSelect.addEventListener('change', function () {
                if (syncingFromApartment) return;
                preselectedFloor = '';
                var s = currentScope();
                if (s === 'FLOOR' || s === 'APARTMENT') {
                    fillFloors(buildingSelect.value, '');
                }
                if (s === 'APARTMENT') {

                    if (apartmentSelect) apartmentSelect.value = '';
                    filterApartments();
                }
            });
        }

        if (floorSelect) {
            floorSelect.addEventListener('change', function () {
                if (syncingFromApartment) return;
                if (currentScope() === 'APARTMENT') {
                    if (apartmentSelect) apartmentSelect.value = '';
                    filterApartments();
                }
            });
        }

        if (apartmentSearch) {
            apartmentSearch.addEventListener('input', function () {
                filterApartments({ autoFillFromSearch: true });
            });
        }

        if (apartmentSelect) {
            apartmentSelect.addEventListener('change', function () {
                var opt = apartmentSelect.options[apartmentSelect.selectedIndex];
                if (opt && opt.value) {
                    applyApartmentToBuildingFloor(opt, true);
                    filterApartments();
                }
            });
        }

        if (buildingSelect && buildingSelect.value) {
            fillFloors(buildingSelect.value, preselectedFloor);
        }
        toggle();
        updateFeeTypeLabels();

        if (form) {
            form.addEventListener('submit', function (e) {
                var cat = document.getElementById('categoryId');
                var title = document.getElementById('title');
                var amount = document.getElementById('amount');
                var s = currentScope();
                var feeType = currentFeeType();

                if (!cat.value) {
                    e.preventDefault();
                    cat.focus();
                    alert('Vui lòng chọn danh mục phí.');
                    return;
                }
                if (!title.value || !title.value.trim()) {
                    e.preventDefault();
                    title.focus();
                    alert('Tên khoản phí không được trống.');
                    return;
                }
                if (amount.value === '' || isNaN(Number(amount.value)) || Number(amount.value) < 10000) {
                    e.preventDefault();
                    amount.focus();
                    alert('Số tiền không hợp lệ (≥ 10.000).');
                    return;
                }
                if (feeType === 'ONE_TIME' && feeMonth && !feeMonth.value) {
                    e.preventDefault();
                    feeMonth.focus();
                    alert('Vui lòng chọn tháng áp dụng.');
                    return;
                }
                if (!feeYear.value || isNaN(Number(feeYear.value))) {
                    e.preventDefault();
                    feeYear.focus();
                    alert('Vui lòng nhập năm hợp lệ.');
                    return;
                }
                if ((s === 'BUILDING' || s === 'FLOOR') && !buildingSelect.value) {
                    e.preventDefault();
                    buildingSelect.focus();
                    alert('Vui lòng chọn tòa nhà.');
                    return;
                }
                if (s === 'FLOOR' && !floorSelect.value) {
                    e.preventDefault();
                    floorSelect.focus();
                    alert('Vui lòng chọn tầng.');
                    return;
                }
                if (s === 'APARTMENT' && !apartmentSelect.value) {
                    e.preventDefault();
                    apartmentSelect.focus();
                    alert('Vui lòng chọn căn hộ.');
                    return;
                }
            });
        }
    })();
</script>
