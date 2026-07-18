package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.FeeAssignment;
import apartmentmanagement.util.AppConstants;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class FeeAssignmentDAO extends DBContext {

    private static final String ASSIGNMENT_SELECT =
            "SELECT fa.*, a.apartment_code, a.building, a.floor_number, "
            + "f.title AS fee_title, c.name AS category_name, "
            + "f.fee_month, f.fee_year, f.fee_type "
            + "FROM fee_assignments fa "
            + "JOIN apartments a ON a.apartment_id = fa.apartment_id "
            + "JOIN fees f ON f.fee_id = fa.fee_id "
            + "JOIN fee_categories c ON c.category_id = f.category_id ";

    public FeeAssignment getFromResultSet(ResultSet rs) throws SQLException {
        FeeAssignment.FeeAssignmentBuilder b = FeeAssignment.builder()
                .assignmentId(rs.getInt("assignment_id"))
                .feeId(rs.getInt("fee_id"))
                .apartmentId(rs.getInt("apartment_id"))
                .amount(rs.getBigDecimal("amount"))
                .status(rs.getString("status"))
                .assignedAt(rs.getTimestamp("assigned_at"))
                .paidAt(rs.getTimestamp("paid_at"));
        try {
            String code = rs.getString("apartment_code");
            if (code != null) {
                b.apartmentCode(code);
            }
        } catch (SQLException ignored) {
        }
        try {
            b.building(rs.getString("building"));
            if (rs.getObject("floor_number") != null) {
                b.floorNumber(rs.getInt("floor_number"));
            }
        } catch (SQLException ignored) {
        }
        try {
            b.feeTitle(rs.getString("fee_title"));
            b.categoryName(rs.getString("category_name"));
            if (rs.getObject("fee_month") != null) {
                b.feeMonth(rs.getInt("fee_month"));
            }
            if (rs.getObject("fee_year") != null) {
                b.feeYear(rs.getInt("fee_year"));
            }
            String feeType = rs.getString("fee_type");
            b.feeType(feeType != null && !feeType.isEmpty()
                    ? feeType : AppConstants.FEE_TYPE_MONTHLY);
        } catch (SQLException ignored) {
            b.feeType(AppConstants.FEE_TYPE_MONTHLY);
        }
        return b.build();
    }

    public int[] createAssignments(int feeId, List<Integer> apartmentIds, BigDecimal amount) {
        int created = 0;
        int skipped = 0;
        if (apartmentIds == null || apartmentIds.isEmpty()) {
            return new int[]{0, 0};
        }
        BigDecimal amt = amount != null ? amount : BigDecimal.ZERO;
        String existsSql = "SELECT 1 FROM fee_assignments WHERE fee_id = ? AND apartment_id = ?";
        String insertSql = "INSERT INTO fee_assignments (fee_id, apartment_id, amount, status) VALUES (?, ?, ?, ?)";

        try {
            connection = getConnection();
            for (Integer aptId : apartmentIds) {
                if (aptId == null) {
                    skipped++;
                    continue;
                }
                boolean exists = false;
                try (PreparedStatement check = connection.prepareStatement(existsSql)) {
                    check.setInt(1, feeId);
                    check.setInt(2, aptId);
                    try (ResultSet rs = check.executeQuery()) {
                        exists = rs.next();
                    }
                }
                if (exists) {
                    skipped++;
                    continue;
                }
                try (PreparedStatement ins = connection.prepareStatement(insertSql)) {
                    ins.setInt(1, feeId);
                    ins.setInt(2, aptId);
                    ins.setBigDecimal(3, amt);
                    ins.setString(4, AppConstants.ASSIGNMENT_UNPAID);
                    if (ins.executeUpdate() > 0) {
                        created++;
                    } else {
                        skipped++;
                    }
                } catch (SQLException ex) {
                    System.out.println("FeeAssignmentDAO.createAssignments skip " + aptId + ": " + ex.getMessage());
                    skipped++;
                }
            }

        } catch (SQLException e) {
            System.out.println("FeeAssignmentDAO.createAssignments error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return new int[]{created, skipped};
    }

    public List<FeeAssignment> findByFee(int feeId, int page, int pageSize) {
        return findByFee(feeId, null, null, null, page, pageSize);
    }

    public List<FeeAssignment> findByFee(int feeId, String keyword, int page, int pageSize) {
        return findByFee(feeId, keyword, null, null, page, pageSize);
    }

    public List<FeeAssignment> findByFee(int feeId, String keyword, String building,
                                          Integer floorNumber, int page, int pageSize) {
        List<FeeAssignment> list = new ArrayList<>();
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = AppConstants.DEFAULT_PAGE_SIZE;
        }
        int offset = (page - 1) * pageSize;
        List<String> tokens = tokenizeKeyword(keyword);
        StringBuilder sql = new StringBuilder(ASSIGNMENT_SELECT)
                .append("WHERE fa.fee_id = ? ");
        appendBuildingFloorFilter(sql, building, floorNumber);
        appendApartmentSearch(sql, tokens);
        sql.append("ORDER BY a.apartment_code OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql.toString());
            int idx = 1;
            statement.setInt(idx++, feeId);
            idx = bindBuildingFloorFilter(statement, idx, building, floorNumber);
            idx = bindApartmentSearch(statement, idx, tokens);
            statement.setInt(idx++, offset);
            statement.setInt(idx, pageSize);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(getFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("FeeAssignmentDAO.findByFee error: " + e.getMessage()
                    + " [SQLState=" + e.getSQLState() + "]");
            e.printStackTrace();
        } finally {
            closeResources();
        }
        return list;
    }

    public int countByFee(int feeId) {
        return countByFee(feeId, null, null, null);
    }

    public int countByFee(int feeId, String keyword) {
        return countByFee(feeId, keyword, null, null);
    }

    public int countByFee(int feeId, String keyword, String building, Integer floorNumber) {
        List<String> tokens = tokenizeKeyword(keyword);
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM fee_assignments fa "
                + "JOIN apartments a ON a.apartment_id = fa.apartment_id "
                + "WHERE fa.fee_id = ? ");
        appendBuildingFloorFilter(sql, building, floorNumber);
        appendApartmentSearch(sql, tokens);
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql.toString());
            int idx = 1;
            statement.setInt(idx++, feeId);
            idx = bindBuildingFloorFilter(statement, idx, building, floorNumber);
            bindApartmentSearch(statement, idx, tokens);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("FeeAssignmentDAO.countByFee error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    public List<String> findDistinctBuildingsByFee(int feeId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT a.building FROM fee_assignments fa "
                + "JOIN apartments a ON a.apartment_id = fa.apartment_id "
                + "WHERE fa.fee_id = ? AND a.building IS NOT NULL AND LTRIM(RTRIM(a.building)) <> N'' "
                + "ORDER BY a.building";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, feeId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getString("building"));
            }
        } catch (SQLException e) {
            System.out.println("FeeAssignmentDAO.findDistinctBuildingsByFee error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public List<Integer> findFloorsByFeeAndBuilding(int feeId, String building) {
        List<Integer> list = new ArrayList<>();
        if (building == null || building.trim().isEmpty()) {
            return list;
        }
        String sql = "SELECT DISTINCT a.floor_number FROM fee_assignments fa "
                + "JOIN apartments a ON a.apartment_id = fa.apartment_id "
                + "WHERE fa.fee_id = ? AND a.building = ? AND a.floor_number IS NOT NULL "
                + "ORDER BY a.floor_number";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, feeId);
            statement.setString(2, building.trim());
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getInt("floor_number"));
            }
        } catch (SQLException e) {
            System.out.println("FeeAssignmentDAO.findFloorsByFeeAndBuilding error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    private void appendBuildingFloorFilter(StringBuilder sql, String building, Integer floorNumber) {
        if (building != null && !building.trim().isEmpty()) {
            sql.append("AND a.building = ? ");
        }
        if (floorNumber != null) {
            sql.append("AND a.floor_number = ? ");
        }
    }

    private int bindBuildingFloorFilter(PreparedStatement ps, int startIdx,
                                         String building, Integer floorNumber) throws SQLException {
        int idx = startIdx;
        if (building != null && !building.trim().isEmpty()) {
            ps.setString(idx++, building.trim());
        }
        if (floorNumber != null) {
            ps.setInt(idx++, floorNumber);
        }
        return idx;
    }

    private void appendApartmentSearch(StringBuilder sql, List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        for (String token : tokens) {
            if (isNumericToken(token)) {
                sql.append("AND (a.floor_number = ? ")
                        .append("OR a.apartment_code LIKE ? ")
                        .append("OR a.building LIKE ? ")
                        .append("OR CAST(a.floor_number AS NVARCHAR(20)) LIKE ?) ");
            } else {
                sql.append("AND (a.apartment_code LIKE ? ")
                        .append("OR a.building LIKE ? ")
                        .append("OR CAST(a.floor_number AS NVARCHAR(20)) LIKE ? ")
                        .append("OR (a.building + N'/' + CAST(a.floor_number AS NVARCHAR(20))) LIKE ? ")
                        .append("OR (a.building + N' / ' + CAST(a.floor_number AS NVARCHAR(20))) LIKE ? ")
                        .append("OR (a.building + N'-' + CAST(a.floor_number AS NVARCHAR(20))) LIKE ?) ");
            }
        }
    }

    private int bindApartmentSearch(PreparedStatement ps, int startIdx, List<String> tokens)
            throws SQLException {
        int idx = startIdx;
        if (tokens == null || tokens.isEmpty()) {
            return idx;
        }
        for (String token : tokens) {
            String like = "%" + token + "%";
            if (isNumericToken(token)) {
                ps.setInt(idx++, Integer.parseInt(token));
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            } else {
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
        }
        return idx;
    }

    private List<String> tokenizeKeyword(String keyword) {
        List<String> tokens = new ArrayList<>();
        if (keyword == null) {
            return tokens;
        }
        String raw = keyword.trim();
        if (raw.isEmpty()) {
            return tokens;
        }

        String cleaned = raw.replace("%", "").replace("_", "");
        cleaned = cleaned.replaceAll("(?iu)\\b(tầng|tang|floor|toà|tòa|toa)\\b", " ");
        cleaned = cleaned.replaceAll("[/\\\\|,\\-–—]+", " ");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        if (cleaned.isEmpty()) {
            return tokens;
        }
        for (String part : cleaned.split(" ")) {
            if (part == null) {
                continue;
            }
            String t = part.trim();
            if (!t.isEmpty() && !tokens.contains(t)) {
                tokens.add(t);
            }
        }
        return tokens;
    }

    private boolean isNumericToken(String token) {
        if (token == null || token.isEmpty() || token.length() > 9) {
            return false;
        }
        for (int i = 0; i < token.length(); i++) {
            if (!Character.isDigit(token.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public FeeAssignment findById(int assignmentId) {
        String sql = ASSIGNMENT_SELECT + "WHERE fa.assignment_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, assignmentId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("FeeAssignmentDAO.findById error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    public List<FeeAssignment> findByResidentUser(int userId, int page, int pageSize) {
        return findByResidentUser(userId, null, null, null, null, null, page, pageSize);
    }

    public List<FeeAssignment> findByResidentUser(int userId, String feeType, int page, int pageSize) {
        return findByResidentUser(userId, null, feeType, null, null, null, page, pageSize);
    }

    public List<FeeAssignment> findByResidentUser(int userId, Integer categoryId, String feeType,
                                                  Integer feeMonth, Integer feeYear, String assignmentStatus,
                                                  int page, int pageSize) {
        List<FeeAssignment> list = new ArrayList<>();
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = AppConstants.DEFAULT_PAGE_SIZE;
        }
        int offset = (page - 1) * pageSize;
        StringBuilder sql = new StringBuilder(ASSIGNMENT_SELECT
                + "JOIN apartment_residents ar ON ar.apartment_id = fa.apartment_id "
                + "WHERE ar.user_id = ? AND ar.is_current = 1 AND f.status = ? ");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        params.add(AppConstants.FEE_STATUS_PUBLISHED);
        appendResidentFilters(sql, params, categoryId, feeType, feeMonth, feeYear, assignmentStatus);
        sql.append("ORDER BY fa.assigned_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql.toString());
            int idx = bindParams(statement, params);
            statement.setInt(idx++, offset);
            statement.setInt(idx, pageSize);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(getFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("FeeAssignmentDAO.findByResidentUser error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public int countByResidentUser(int userId) {
        return countByResidentUser(userId, null, null, null, null, null);
    }

    public int countByResidentUser(int userId, String feeType) {
        return countByResidentUser(userId, null, feeType, null, null, null);
    }

    public int countByResidentUser(int userId, Integer categoryId, String feeType,
                                   Integer feeMonth, Integer feeYear, String assignmentStatus) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM fee_assignments fa "
                + "JOIN fees f ON f.fee_id = fa.fee_id "
                + "JOIN apartment_residents ar ON ar.apartment_id = fa.apartment_id "
                + "WHERE ar.user_id = ? AND ar.is_current = 1 AND f.status = ? ");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        params.add(AppConstants.FEE_STATUS_PUBLISHED);
        appendResidentFilters(sql, params, categoryId, feeType, feeMonth, feeYear, assignmentStatus);
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql.toString());
            bindParams(statement, params);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("FeeAssignmentDAO.countByResidentUser error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    public FeeDAO.FeeSummary getResidentSummary(int userId) {
        String sql = "SELECT "
                + "ISNULL(COUNT(fa.assignment_id), 0) AS fee_batch_count, "
                + "ISNULL(SUM(CASE WHEN ISNULL(f.fee_type, N'MONTHLY') = N'MONTHLY' THEN 1 ELSE 0 END), 0) AS monthly_count, "
                + "ISNULL(SUM(CASE WHEN f.fee_type = N'ONE_TIME' THEN 1 ELSE 0 END), 0) AS one_time_count, "
                + "ISNULL(SUM(fa.amount), 0) AS total_receivable, "
                + "ISNULL(SUM(CASE WHEN ISNULL(f.fee_type, N'MONTHLY') = N'MONTHLY' THEN fa.amount ELSE 0 END), 0) AS monthly_receivable, "
                + "ISNULL(SUM(CASE WHEN f.fee_type = N'ONE_TIME' THEN fa.amount ELSE 0 END), 0) AS one_time_receivable, "
                + "ISNULL(SUM(CASE WHEN fa.status = N'PAID' THEN fa.amount ELSE 0 END), 0) AS total_paid, "
                + "ISNULL(SUM(CASE WHEN fa.status = N'UNPAID' THEN fa.amount ELSE 0 END), 0) AS total_unpaid, "
                + "ISNULL(SUM(CASE WHEN fa.status = N'PAID' THEN 1 ELSE 0 END), 0) AS paid_count, "
                + "ISNULL(SUM(CASE WHEN fa.status = N'UNPAID' THEN 1 ELSE 0 END), 0) AS unpaid_count "
                + "FROM fee_assignments fa "
                + "JOIN fees f ON f.fee_id = fa.fee_id "
                + "JOIN apartment_residents ar ON ar.apartment_id = fa.apartment_id "
                + "WHERE ar.user_id = ? AND ar.is_current = 1 AND f.status = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setString(2, AppConstants.FEE_STATUS_PUBLISHED);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new FeeDAO.FeeSummary(
                        resultSet.getInt("fee_batch_count"),
                        0,
                        resultSet.getBigDecimal("total_receivable"),
                        resultSet.getBigDecimal("total_paid"),
                        resultSet.getBigDecimal("total_unpaid"),
                        resultSet.getInt("paid_count"),
                        resultSet.getInt("unpaid_count"),
                        resultSet.getInt("monthly_count"),
                        resultSet.getInt("one_time_count"),
                        resultSet.getBigDecimal("monthly_receivable"),
                        resultSet.getBigDecimal("one_time_receivable"));
            }
        } catch (SQLException e) {
            System.out.println("FeeAssignmentDAO.getResidentSummary error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return FeeDAO.FeeSummary.empty();
    }

    private void appendResidentFilters(StringBuilder sql, List<Object> params,
                                       Integer categoryId, String feeType,
                                       Integer feeMonth, Integer feeYear, String assignmentStatus) {
        if (categoryId != null) {
            sql.append("AND f.category_id = ? ");
            params.add(categoryId);
        }
        if (feeType != null && !feeType.isEmpty()) {
            if (AppConstants.FEE_TYPE_MONTHLY.equals(feeType)) {
                sql.append("AND (f.fee_type = ? OR f.fee_type IS NULL) ");
            } else {
                sql.append("AND f.fee_type = ? ");
            }
            params.add(feeType);
        }
        if (feeMonth != null) {
            sql.append("AND f.fee_month = ? ");
            params.add(feeMonth);
        }
        if (feeYear != null) {
            sql.append("AND f.fee_year = ? ");
            params.add(feeYear);
        }
        if (assignmentStatus != null && !assignmentStatus.isEmpty()) {
            sql.append("AND fa.status = ? ");
            params.add(assignmentStatus);
        }
    }

    private int bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        int idx = 1;
        for (Object p : params) {
            if (p instanceof Integer) {
                ps.setInt(idx++, (Integer) p);
            } else {
                ps.setString(idx++, (String) p);
            }
        }
        return idx;
    }

    public boolean isCurrentResidentOfApartment(int userId, int apartmentId) {
        String sql = "SELECT 1 FROM apartment_residents "
                + "WHERE user_id = ? AND apartment_id = ? AND is_current = 1";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setInt(2, apartmentId);
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("FeeAssignmentDAO.isCurrentResidentOfApartment error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    public boolean markPaid(int assignmentId, Integer recordedBy, String note) {
        try {
            connection = getConnection();
            try (PreparedStatement upd = connection.prepareStatement(
                    "UPDATE fee_assignments SET status = ?, paid_at = SYSUTCDATETIME() "
                    + "WHERE assignment_id = ? AND status = ?")) {
                upd.setString(1, AppConstants.ASSIGNMENT_PAID);
                upd.setInt(2, assignmentId);
                upd.setString(3, AppConstants.ASSIGNMENT_UNPAID);
                if (upd.executeUpdate() <= 0) {
                    return false;
                }
            }
            BigDecimal amount = null;
            try (PreparedStatement q = connection.prepareStatement(
                    "SELECT amount FROM fee_assignments WHERE assignment_id = ?")) {
                q.setInt(1, assignmentId);
                try (ResultSet rs = q.executeQuery()) {
                    if (rs.next()) {
                        amount = rs.getBigDecimal("amount");
                    }
                }
            }
            if (amount != null) {
                try (PreparedStatement pay = connection.prepareStatement(
                        "INSERT INTO payments (assignment_id, amount, note, recorded_by) VALUES (?, ?, ?, ?)")) {
                    pay.setInt(1, assignmentId);
                    pay.setBigDecimal(2, amount);
                    pay.setString(3, note);
                    if (recordedBy != null) {
                        pay.setInt(4, recordedBy);
                    } else {
                        pay.setNull(4, Types.INTEGER);
                    }
                    pay.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            System.out.println("FeeAssignmentDAO.markPaid error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    public boolean markUnpaid(int assignmentId) {
        try {
            connection = getConnection();
            try (PreparedStatement del = connection.prepareStatement(
                    "DELETE FROM payments WHERE assignment_id = ?")) {
                del.setInt(1, assignmentId);
                del.executeUpdate();
            }
            try (PreparedStatement upd = connection.prepareStatement(
                    "UPDATE fee_assignments SET status = ?, paid_at = NULL "
                    + "WHERE assignment_id = ? AND status = ?")) {
                upd.setString(1, AppConstants.ASSIGNMENT_UNPAID);
                upd.setInt(2, assignmentId);
                upd.setString(3, AppConstants.ASSIGNMENT_PAID);
                return upd.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("FeeAssignmentDAO.markUnpaid error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    public int countUnpaid() {
        String sql = "SELECT COUNT(*) FROM fee_assignments WHERE status = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, AppConstants.ASSIGNMENT_UNPAID);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("FeeAssignmentDAO.countUnpaid error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }
}
