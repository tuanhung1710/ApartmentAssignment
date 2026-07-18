package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.Fee;
import apartmentmanagement.model.FeeScope;
import apartmentmanagement.util.AppConstants;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class FeeDAO extends DBContext {

    private static final String FEE_STATS_SELECT =
            "SELECT f.*, c.name AS category_name, "
            + "s.scope_type, s.building AS scope_building, s.floor_number AS scope_floor, "
            + "s.apartment_id AS scope_apartment_id, a.apartment_code AS scope_apartment_code, "
            + "ISNULL(st.assignment_count, 0) AS assignment_count, "
            + "ISNULL(st.paid_count, 0) AS paid_count, "
            + "ISNULL(st.unpaid_count, 0) AS unpaid_count, "
            + "ISNULL(st.total_receivable, 0) AS total_receivable, "
            + "ISNULL(st.total_paid, 0) AS total_paid, "
            + "ISNULL(st.total_unpaid, 0) AS total_unpaid "
            + "FROM fees f "
            + "JOIN fee_categories c ON c.category_id = f.category_id "
            + "LEFT JOIN fee_scopes s ON s.fee_id = f.fee_id "
            + "LEFT JOIN apartments a ON a.apartment_id = s.apartment_id "
            + "LEFT JOIN ("
            + "  SELECT fee_id, "
            + "    COUNT(*) AS assignment_count, "
            + "    SUM(CASE WHEN status = N'PAID' THEN 1 ELSE 0 END) AS paid_count, "
            + "    SUM(CASE WHEN status = N'UNPAID' THEN 1 ELSE 0 END) AS unpaid_count, "
            + "    SUM(amount) AS total_receivable, "
            + "    SUM(CASE WHEN status = N'PAID' THEN amount ELSE 0 END) AS total_paid, "
            + "    SUM(CASE WHEN status = N'UNPAID' THEN amount ELSE 0 END) AS total_unpaid "
            + "  FROM fee_assignments GROUP BY fee_id"
            + ") st ON st.fee_id = f.fee_id ";

    public Fee getFromResultSet(ResultSet rs) throws SQLException {
        Fee.FeeBuilder b = Fee.builder()
                .feeId(rs.getInt("fee_id"))
                .categoryId(rs.getInt("category_id"))
                .title(rs.getString("title"))
                .amount(rs.getBigDecimal("amount"))
                .feeMonth(rs.getObject("fee_month") != null ? rs.getInt("fee_month") : null)
                .feeYear(rs.getObject("fee_year") != null ? rs.getInt("fee_year") : null)
                .status(rs.getString("status"))
                .note(rs.getString("note"))
                .createdBy(rs.getObject("created_by") != null ? rs.getInt("created_by") : null)
                .createdAt(rs.getTimestamp("created_at"))
                .updatedAt(rs.getTimestamp("updated_at"));
        try {
            String ft = rs.getString("fee_type");
            b.feeType(ft != null && !ft.isEmpty() ? ft : AppConstants.FEE_TYPE_MONTHLY);
        } catch (SQLException ignored) {
            b.feeType(AppConstants.FEE_TYPE_MONTHLY);
        }
        try {
            String cn = rs.getString("category_name");
            if (cn != null) {
                b.categoryName(cn);
            }
        } catch (SQLException ignored) {
        }
        try {
            b.assignmentCount(rs.getObject("assignment_count") != null ? rs.getInt("assignment_count") : null);
        } catch (SQLException ignored) {
        }
        try {
            b.paidCount(rs.getObject("paid_count") != null ? rs.getInt("paid_count") : null);
            b.unpaidCount(rs.getObject("unpaid_count") != null ? rs.getInt("unpaid_count") : null);
            b.totalReceivable(rs.getBigDecimal("total_receivable"));
            b.totalPaid(rs.getBigDecimal("total_paid"));
            b.totalUnpaid(rs.getBigDecimal("total_unpaid"));
        } catch (SQLException ignored) {
        }
        try {
            b.scopeType(rs.getString("scope_type"));
            b.scopeBuilding(rs.getString("scope_building"));
            if (rs.getObject("scope_floor") != null) {
                b.scopeFloor(rs.getInt("scope_floor"));
            }
            if (rs.getObject("scope_apartment_id") != null) {
                b.scopeApartmentId(rs.getInt("scope_apartment_id"));
            }
            b.scopeApartmentCode(rs.getString("scope_apartment_code"));
        } catch (SQLException ignored) {
        }
        return b.build();
    }

    public FeeScope getScopeFromResultSet(ResultSet rs) throws SQLException {
        FeeScope.FeeScopeBuilder b = FeeScope.builder()
                .scopeId(rs.getInt("scope_id"))
                .feeId(rs.getInt("fee_id"))
                .scopeType(rs.getString("scope_type"))
                .building(rs.getString("building"))
                .floorNumber(rs.getObject("floor_number") != null ? rs.getInt("floor_number") : null)
                .apartmentId(rs.getObject("apartment_id") != null ? rs.getInt("apartment_id") : null)
                .createdAt(rs.getTimestamp("created_at"));
        try {
            String code = rs.getString("apartment_code");
            if (code != null) {
                b.apartmentCode(code);
            }
        } catch (SQLException ignored) {
        }
        return b.build();
    }

    public Fee findById(int feeId) {
        String sql = FEE_STATS_SELECT + "WHERE f.fee_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, feeId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("FeeDAO.findById error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    public List<Fee> findFees(Integer categoryId, String status, int page, int pageSize) {
        return findFees(categoryId, status, null, null, null, page, pageSize);
    }

    public List<Fee> findFees(Integer categoryId, String status, Integer feeMonth, Integer feeYear,
                              int page, int pageSize) {
        return findFees(categoryId, status, feeMonth, feeYear, null, page, pageSize);
    }

    public List<Fee> findFees(Integer categoryId, String status, Integer feeMonth, Integer feeYear,
                              String feeType, int page, int pageSize) {
        List<Fee> list = new ArrayList<>();
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = AppConstants.DEFAULT_PAGE_SIZE;
        }
        int offset = (page - 1) * pageSize;

        StringBuilder sql = new StringBuilder(FEE_STATS_SELECT + "WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFeeFilters(sql, params, categoryId, status, feeMonth, feeYear, feeType);
        sql.append(" ORDER BY f.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

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
            System.out.println("FeeDAO.findFees error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public int countFees(Integer categoryId, String status) {
        return countFees(categoryId, status, null, null, null);
    }

    public int countFees(Integer categoryId, String status, Integer feeMonth, Integer feeYear) {
        return countFees(categoryId, status, feeMonth, feeYear, null);
    }

    public int countFees(Integer categoryId, String status, Integer feeMonth, Integer feeYear, String feeType) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM fees f WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFeeFilters(sql, params, categoryId, status, feeMonth, feeYear, feeType);
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql.toString());
            bindParams(statement, params);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("FeeDAO.countFees error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    public FeeSummary getOverallSummary() {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM fees) AS fee_batch_count, "
                + "(SELECT COUNT(*) FROM fees WHERE fee_type = N'MONTHLY' OR fee_type IS NULL) AS monthly_count, "
                + "(SELECT COUNT(*) FROM fees WHERE fee_type = N'ONE_TIME') AS one_time_count, "
                + "ISNULL(COUNT(fa.assignment_id), 0) AS total_apartments, "
                + "ISNULL(SUM(fa.amount), 0) AS total_receivable, "
                + "ISNULL(SUM(CASE WHEN ISNULL(f.fee_type, N'MONTHLY') = N'MONTHLY' THEN fa.amount ELSE 0 END), 0) AS monthly_receivable, "
                + "ISNULL(SUM(CASE WHEN f.fee_type = N'ONE_TIME' THEN fa.amount ELSE 0 END), 0) AS one_time_receivable, "
                + "ISNULL(SUM(CASE WHEN fa.status = N'PAID' THEN fa.amount ELSE 0 END), 0) AS total_paid, "
                + "ISNULL(SUM(CASE WHEN fa.status = N'UNPAID' THEN fa.amount ELSE 0 END), 0) AS total_unpaid, "
                + "ISNULL(SUM(CASE WHEN fa.status = N'PAID' THEN 1 ELSE 0 END), 0) AS paid_count, "
                + "ISNULL(SUM(CASE WHEN fa.status = N'UNPAID' THEN 1 ELSE 0 END), 0) AS unpaid_count "
                + "FROM fee_assignments fa "
                + "LEFT JOIN fees f ON f.fee_id = fa.fee_id";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new FeeSummary(
                        resultSet.getInt("fee_batch_count"),
                        resultSet.getInt("total_apartments"),
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
            System.out.println("FeeDAO.getOverallSummary error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return FeeSummary.empty();
    }

    public boolean deleteFee(int feeId) {
        String checkSql = "SELECT COUNT(*) FROM fee_assignments WHERE fee_id = ? AND status = ?";
        String deleteSql = "DELETE FROM fees WHERE fee_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(checkSql);
            statement.setInt(1, feeId);
            statement.setString(2, AppConstants.ASSIGNMENT_PAID);
            resultSet = statement.executeQuery();
            if (resultSet.next() && resultSet.getInt(1) > 0) {
                return false;
            }
            closeQuietly(resultSet);
            closeQuietly(statement);
            statement = connection.prepareStatement(deleteSql);
            statement.setInt(1, feeId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("FeeDAO.deleteFee error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    public int insertFee(Fee fee) {
        String sql = "INSERT INTO fees (category_id, title, amount, fee_month, fee_year, fee_type, status, note, created_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, fee.getCategoryId());
            statement.setString(2, fee.getTitle());
            statement.setBigDecimal(3, fee.getAmount() != null ? fee.getAmount() : BigDecimal.ZERO);
            if (fee.getFeeMonth() != null) {
                statement.setInt(4, fee.getFeeMonth());
            } else {
                statement.setNull(4, Types.TINYINT);
            }
            if (fee.getFeeYear() != null) {
                statement.setInt(5, fee.getFeeYear());
            } else {
                statement.setNull(5, Types.SMALLINT);
            }
            String feeType = fee.getFeeType();
            if (feeType == null || feeType.isEmpty()) {
                feeType = AppConstants.FEE_TYPE_MONTHLY;
            }
            statement.setString(6, feeType);
            statement.setString(7, fee.getStatus() != null ? fee.getStatus() : AppConstants.FEE_STATUS_DRAFT);
            statement.setString(8, fee.getNote());
            if (fee.getCreatedBy() != null) {
                statement.setInt(9, fee.getCreatedBy());
            } else {
                statement.setNull(9, Types.INTEGER);
            }
            if (statement.executeUpdate() > 0) {
                resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("FeeDAO.insertFee error: " + e.getMessage()
                    + " [SQLState=" + e.getSQLState() + "]");
            e.printStackTrace();
        } finally {
            closeResources();
        }
        return -1;
    }

    public int insertScope(FeeScope scope) {
        String sql = "INSERT INTO fee_scopes (fee_id, scope_type, building, floor_number, apartment_id) "
                + "VALUES (?, ?, ?, ?, ?)";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, scope.getFeeId());
            statement.setString(2, scope.getScopeType());
            if (scope.getBuilding() != null && !scope.getBuilding().isEmpty()) {
                statement.setString(3, scope.getBuilding());
            } else {
                statement.setNull(3, Types.NVARCHAR);
            }
            if (scope.getFloorNumber() != null) {
                statement.setInt(4, scope.getFloorNumber());
            } else {
                statement.setNull(4, Types.INTEGER);
            }
            if (scope.getApartmentId() != null) {
                statement.setInt(5, scope.getApartmentId());
            } else {
                statement.setNull(5, Types.INTEGER);
            }
            if (statement.executeUpdate() > 0) {
                resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("FeeDAO.insertScope error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return -1;
    }

    public boolean updateStatus(int feeId, String expectedStatus, String newStatus) {
        String sql = "UPDATE fees SET status = ?, updated_at = SYSUTCDATETIME() "
                + "WHERE fee_id = ? AND status = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, newStatus);
            statement.setInt(2, feeId);
            statement.setString(3, expectedStatus);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("FeeDAO.updateStatus error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    public boolean setStatus(int feeId, String newStatus) {
        String sql = "UPDATE fees SET status = ?, updated_at = SYSUTCDATETIME() WHERE fee_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, newStatus);
            statement.setInt(2, feeId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("FeeDAO.setStatus error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    public List<Integer> resolveApartmentIdsByFee(int feeId) {
        FeeScope scope = findScopeByFeeId(feeId);
        if (scope == null) {
            return new ArrayList<>();
        }
        return resolveApartmentIds(
                scope.getScopeType(),
                scope.getBuilding(),
                scope.getFloorNumber(),
                scope.getApartmentId());
    }

    public FeeScope findScopeByFeeId(int feeId) {
        String sql = "SELECT s.*, a.apartment_code FROM fee_scopes s "
                + "LEFT JOIN apartments a ON a.apartment_id = s.apartment_id "
                + "WHERE s.fee_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, feeId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getScopeFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("FeeDAO.findScopeByFeeId error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    public List<Integer> resolveApartmentIds(String scopeType, String building, Integer floor, Integer apartmentId) {
        List<Integer> ids = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT apartment_id FROM apartments WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (AppConstants.FEE_SCOPE_BUILDING.equalsIgnoreCase(scopeType)) {
            sql.append(" AND building = ?");
            params.add(building);
        } else if (AppConstants.FEE_SCOPE_FLOOR.equalsIgnoreCase(scopeType)) {
            sql.append(" AND building = ? AND floor_number = ?");
            params.add(building);
            params.add(floor);
        } else if (AppConstants.FEE_SCOPE_APARTMENT.equalsIgnoreCase(scopeType)) {
            sql.append(" AND apartment_id = ?");
            params.add(apartmentId);
        }
        sql.append(" ORDER BY apartment_code");

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql.toString());
            bindParams(statement, params);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                ids.add(resultSet.getInt("apartment_id"));
            }
        } catch (SQLException e) {
            System.out.println("FeeDAO.resolveApartmentIds error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return ids;
    }

    public List<String> findDistinctBuildings() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT building FROM apartments "
                + "WHERE building IS NOT NULL AND LTRIM(RTRIM(building)) <> N'' "
                + "ORDER BY building";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getString("building"));
            }
        } catch (SQLException e) {
            System.out.println("FeeDAO.findDistinctBuildings error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public List<Integer> findFloorsByBuilding(String building) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT DISTINCT floor_number FROM apartments "
                + "WHERE building = ? AND floor_number IS NOT NULL "
                + "ORDER BY floor_number";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, building);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getInt("floor_number"));
            }
        } catch (SQLException e) {
            System.out.println("FeeDAO.findFloorsByBuilding error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public List<ApartmentOpt> findApartmentsForScope() {
        List<ApartmentOpt> list = new ArrayList<>();
        String sql = "SELECT apartment_id, apartment_code, building, floor_number, status FROM apartments "
                + "ORDER BY CASE WHEN status = N'ACTIVE' THEN 0 ELSE 1 END, apartment_code";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(new ApartmentOpt(
                        resultSet.getInt("apartment_id"),
                        resultSet.getString("apartment_code"),
                        resultSet.getString("building"),
                        resultSet.getObject("floor_number") != null ? resultSet.getInt("floor_number") : null,
                        resultSet.getString("status")));
            }
        } catch (SQLException e) {
            System.out.println("FeeDAO.findApartmentsForScope error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public List<ApartmentOpt> findActiveApartments() {
        return findApartmentsForScope();
    }

    private void appendFeeFilters(StringBuilder sql, List<Object> params,
                                  Integer categoryId, String status,
                                  Integer feeMonth, Integer feeYear, String feeType) {
        if (categoryId != null) {
            sql.append(" AND f.category_id = ?");
            params.add(categoryId);
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND f.status = ?");
            params.add(status);
        }
        if (feeMonth != null) {
            sql.append(" AND f.fee_month = ?");
            params.add(feeMonth);
        }
        if (feeYear != null) {
            sql.append(" AND f.fee_year = ?");
            params.add(feeYear);
        }
        if (feeType != null && !feeType.isEmpty()) {
            if (AppConstants.FEE_TYPE_MONTHLY.equals(feeType)) {
                sql.append(" AND (f.fee_type = ? OR f.fee_type IS NULL)");
            } else {
                sql.append(" AND f.fee_type = ?");
            }
            params.add(feeType);
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

    private void closeQuietly(AutoCloseable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (Exception ignored) {
        }
    }

    public static class ApartmentOpt {
        private final Integer apartmentId;
        private final String apartmentCode;
        private final String building;
        private final Integer floorNumber;
        private final String status;

        public ApartmentOpt(Integer apartmentId, String apartmentCode, String building, Integer floorNumber) {
            this(apartmentId, apartmentCode, building, floorNumber, "ACTIVE");
        }

        public ApartmentOpt(Integer apartmentId, String apartmentCode, String building,
                            Integer floorNumber, String status) {
            this.apartmentId = apartmentId;
            this.apartmentCode = apartmentCode;
            this.building = building;
            this.floorNumber = floorNumber;
            this.status = status;
        }

        public Integer getApartmentId() {
            return apartmentId;
        }

        public String getApartmentCode() {
            return apartmentCode;
        }

        public String getBuilding() {
            return building;
        }

        public Integer getFloorNumber() {
            return floorNumber;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class FeeSummary {
        private final int feeBatchCount;
        private final int totalApartments;
        private final BigDecimal totalReceivable;
        private final BigDecimal totalPaid;
        private final BigDecimal totalUnpaid;
        private final int paidCount;
        private final int unpaidCount;
        private final int monthlyCount;
        private final int oneTimeCount;
        private final BigDecimal monthlyReceivable;
        private final BigDecimal oneTimeReceivable;

        public FeeSummary(int feeBatchCount, int totalApartments,
                          BigDecimal totalReceivable, BigDecimal totalPaid, BigDecimal totalUnpaid,
                          int paidCount, int unpaidCount) {
            this(feeBatchCount, totalApartments, totalReceivable, totalPaid, totalUnpaid,
                    paidCount, unpaidCount, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        public FeeSummary(int feeBatchCount, int totalApartments,
                          BigDecimal totalReceivable, BigDecimal totalPaid, BigDecimal totalUnpaid,
                          int paidCount, int unpaidCount,
                          int monthlyCount, int oneTimeCount,
                          BigDecimal monthlyReceivable, BigDecimal oneTimeReceivable) {
            this.feeBatchCount = feeBatchCount;
            this.totalApartments = totalApartments;
            this.totalReceivable = totalReceivable != null ? totalReceivable : BigDecimal.ZERO;
            this.totalPaid = totalPaid != null ? totalPaid : BigDecimal.ZERO;
            this.totalUnpaid = totalUnpaid != null ? totalUnpaid : BigDecimal.ZERO;
            this.paidCount = paidCount;
            this.unpaidCount = unpaidCount;
            this.monthlyCount = monthlyCount;
            this.oneTimeCount = oneTimeCount;
            this.monthlyReceivable = monthlyReceivable != null ? monthlyReceivable : BigDecimal.ZERO;
            this.oneTimeReceivable = oneTimeReceivable != null ? oneTimeReceivable : BigDecimal.ZERO;
        }

        public static FeeSummary empty() {
            return new FeeSummary(0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0,
                    0, 0, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        public int getFeeBatchCount() {
            return feeBatchCount;
        }

        public int getTotalApartments() {
            return totalApartments;
        }

        public BigDecimal getTotalReceivable() {
            return totalReceivable;
        }

        public BigDecimal getTotalPaid() {
            return totalPaid;
        }

        public BigDecimal getTotalUnpaid() {
            return totalUnpaid;
        }

        public int getPaidCount() {
            return paidCount;
        }

        public int getUnpaidCount() {
            return unpaidCount;
        }

        public int getMonthlyCount() {
            return monthlyCount;
        }

        public int getOneTimeCount() {
            return oneTimeCount;
        }

        public BigDecimal getMonthlyReceivable() {
            return monthlyReceivable;
        }

        public BigDecimal getOneTimeReceivable() {
            return oneTimeReceivable;
        }
    }
}
