package server;

import java.sql.*;

public class JdbcConnect {
	public static final String tableName = "toilet";
	
	private Connection con = null;
	private Statement stmt = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;

	public JdbcConnect(String dbName, String rootPassword) throws Exception {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			// System.out.println("Driver Install Complete");
		} catch (ClassNotFoundException cnfe) {
			System.err.println("Error : " + cnfe);
		}

		Connection con = null;

		String url = "jdbc:mysql://localhost:3306/db?useUnicode=true&characterEncoding=utf8";
		String id = "root";
		String pw = "1234";

		try {
			con = DriverManager.getConnection(url, id, pw);
			stmt = con.createStatement();
			System.out.println("DB connect complete");
		} catch (SQLException e) {
			System.err.println("Error : " + e);
		}
	}

	public boolean closeDB() {
		try {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (stmt != null)
				stmt.close();
			if (con != null)
				con.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	void excuteInsert(String tableName) throws SQLException {
		String sql = new String();

		for (int i = 0; i < Data.dataList.size(); i++) {
			sql = ("INSERT INTO " + tableName + " VALUES('" + Data.dataList.get(i).getBuildingName() + "','"
					+ Data.dataList.get(i).getRoomName() + "','" + Data.dataList.get(i).getGender() + "','"
					+ Data.dataList.get(i).getUsingNumber() + "','" + Data.dataList.get(i).getUsingTime() + "');");
			if (stmt != null)
				stmt.executeUpdate(sql);
		}
	}

	void createStatement() throws SQLException {
		// 4窜拌 : Statement 按眉 积己
		stmt = con.createStatement();
	}

	void prepareStatement(String sql) throws SQLException {
		// 4窜拌 : prepareStatement 按眉 积己
		pstmt = con.prepareStatement(sql);
	}

	void setString(int parameterIndex, String x) throws SQLException {
		if (pstmt != null)
			pstmt.setString(parameterIndex, x);
	}

	void excuteUpdate(String sql) throws SQLException {
		if (stmt != null)
			stmt.executeUpdate(sql);
	}

	void excuteUpdate() throws SQLException {
		pstmt.executeUpdate();
	}

	public ResultSet excuteQuery() throws SQLException {
		rs = pstmt.executeQuery();
		return rs;
	}

	public ResultSet excuteQuery(String sql) throws SQLException {
		if (stmt != null) {
			rs = stmt.executeQuery(sql);
			return rs;
		} else
			return null;
	}

	public ResultSet getResultSet() {
		return rs;
	}

	public boolean isUnique(String primaryKey, String columnName, String tableName) throws SQLException {
		String sql = "SELECT * FROM " + tableName + " WHERE " + columnName + "= '" + primaryKey + "'";
		return isUnique(sql);
	}

	public boolean isUnique(int primaryKey, String columnName, String tableName) throws SQLException {
		String sql = "SELECT * FROM " + tableName + " WHERE " + columnName + "= " + primaryKey;
		return isUnique(sql);
	}

	private boolean isUnique(String sql) throws SQLException {
		ResultSet rsTemp = stmt.executeQuery(sql);

		if (rsTemp.next())
			return false;

		return true;
	}

	public boolean showSelect() throws SQLException {
		rs = stmt.executeQuery("SELECT num FROM toilet order by name DESC;");
		if (rs == null) {
			return false;
		}

		ResultSetMetaData rsmd = rs.getMetaData();

		int numberColumn = rsmd.getColumnCount();
		String[] columName = new String[numberColumn];

		for (int i = 0; i < numberColumn; i++) {
			System.out.print(columName[i] = rsmd.getColumnName(i + 1));
			System.out.print(" | ");
		}
		System.out.println();

		while (rs.next()) {
			for (int i = 0; i < numberColumn; i++) {
				System.out.print(rs.getString(columName[i]));
				System.out.print(" | ");
			}
			System.out.println();
		}
		return true;
	}
}
