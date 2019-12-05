import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;

class Assignment {

private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");

private static String readEntry(String prompt) {

	try {
		StringBuffer buffer = new StringBuffer();
		System.out.print(prompt);
		System.out.flush();
		int c = System.in.read();
		while (c != '\n' && c != -1) {
			buffer.append((char)c);
			c = System.in.read();
		}
		return buffer.toString().trim();
	} catch (IOException e) {
		return "";
	}

}

/**
 * Updates stock of each productID in the array by the quantity in the array.
 * Only updates if the productID exists and stock is more than quantity.
 * Will update if at least 1 product exists.
 *
 * @param  conn       An open database connection
 * @param  productIDs An array of productIDs associated with an order
 * @param  quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
 * @param  staffID    The id of the staff member who sold the order
 * @return            Returns a boolean array where each entry is whether a product exists or not
 */
public static boolean[] updatingStock(Connection conn, int[] productIDs, int[] quantities, int staffID) {

	PreparedStatement ps = null;
	ResultSet rs = null;

	boolean[] productExists = new boolean[productIDs.length];

	try {

		String selectStaff = "SELECT * FROM STAFF WHERE StaffID = ?";

		ps = conn.prepareStatement(selectStaff);

		ps.setInt(1, staffID);

		rs = ps.executeQuery();

		if (rs.next()) {

			for (int i = 0; i < productIDs.length; i++) {

				String selectInventory = "SELECT ProductStockAmount FROM INVENTORY WHERE ProductID = ?";

				ps = conn.prepareStatement(selectInventory);

				ps.setInt(1, productIDs[i]);

				rs = ps.executeQuery();

				if (rs.next()) {

					int stock = rs.getInt(1);

					if (stock < quantities[i]) {

						System.out.println("The Stock (" + stock + ") of ProductID " + productIDs[i] + " is less than the amount to be sold (" + quantities[i] + "). Product not added to order.");

					} else {

						String updateInventory = "UPDATE INVENTORY " +
						                         "SET ProductStockAmount = ProductStockAmount - ? " +
						                         "WHERE ProductID = ?";

						ps = conn.prepareStatement(updateInventory);

						ps.setInt(1, quantities[i]);
						ps.setInt(2, productIDs[i]);

						ps.executeUpdate();

						System.out.println("Product ID " + productIDs[i] + " stock is now at " + (stock - quantities[i]));

						productExists[i] = true;

					}

				} else {

					System.out.println("The ProductID (" + productIDs[i] + ") you've entered matches with no existing products. Product not added to order.");

				}
			}

		} else {

			System.out.println("The StaffID (" + staffID + ") you've entered matches with no existing staff members. Order could not be made.");

		}

	} catch (SQLException e) {
		System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		try {
			if (ps != null) ps.close();
			if (rs != null) rs.close();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		}
	}

	return productExists;

}

public static boolean purchases(Connection conn, int[] productIDs, int[] quantities, String orderDate, int staffID, int orderCompleted, String orderType) {

	PreparedStatement ps = null;

	boolean[] productExists = updatingStock(conn, productIDs, quantities, staffID);
	boolean order = false;

	try {

		// Turning off autocommit will allow the connection to rollback to this point if it encounters an SQLException
		conn.setAutoCommit(false);
		conn.commit();

		for (boolean b : productExists) {
			order = b ? b : false;
		}

		if (order) {

			String insertOrder = "INSERT INTO ORDERS " +
			                     "(OrderID, OrderType, OrderCompleted, OrderPlaced) VALUES (NULL, ?, ?, ?)";

			ps = conn.prepareStatement(insertOrder);

			ps.setString(1, orderType);
			ps.setInt(2, orderCompleted);
			ps.setDate(3, new Date(dateFormat.parse(orderDate).getTime()));

			ps.executeUpdate();

			String insertOrder_Products = "INSERT INTO ORDER_PRODUCTS " +
			                              "(OrderID, ProductID, ProductQuantity) VALUES (order_seq.currval, ?, ?)";

			for (int i = 0; i < productIDs.length; i++) {

				if (productExists[i]) {

					ps = conn.prepareStatement(insertOrder_Products);

					ps.setInt(1, productIDs[i]);
					ps.setInt(2, quantities[i]);

					ps.executeUpdate();

				}
			}

			String insertStaff_Orders = "INSERT INTO STAFF_ORDERS " +
			                            "(OrderID, StaffID) VALUES (order_seq.currval, ?)";

			ps = conn.prepareStatement(insertStaff_Orders);

			ps.setInt(1, staffID);

			ps.executeUpdate();

		}

	} catch (SQLException e) {
		System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		System.out.println("Rolling back data...");
		try {
			if (conn != null) conn.rollback();
		} catch (SQLException e2) {
			System.err.format("SQL State: %s\n%s", e2.getSQLState(), e2.getMessage());
		}
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		try {
			if (ps != null) ps.close();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		}
	}

	return order;

}

/**
 * @param conn			 An open database connection
 * @param productIDs An array of productIDs associated with an order
 * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
 * @param orderDate	 A string in the form of 'DD-Mon-YY' that represents the date the order was made
 * @param staffID		 The id of the staff member who sold the order
 */
public static void option1(Connection conn, int[] productIDs, int[] quantities, String orderDate, int staffID) {

	Set<Integer> s = new HashSet<Integer>();
	boolean dupes = false;

	for (int i : productIDs) {
		if (s.contains(i)) dupes = true;
		s.add(i);
	}

	if (!dupes) {
		if (productIDs.length == quantities.length) {
			if (dateValidator(orderDate)) {
				purchases(conn, productIDs, quantities, orderDate, staffID, 1, "InStore");
			} else System.out.println("Order Date is Invalid");
		} else System.out.println("ProductIDs array and Quantities array are not the same size");
	} else System.out.println("There are duplicate ProductIDs in the order");

}

/**
 * @param conn					 An open database connection
 * @param productIDs 		 An array of productIDs associated with an order
 * @param quantities		 An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
 * @param orderDate			 A string in the form of 'DD-Mon-YY' that represents the date the order was made
 * @param collectionDate A string in the form of 'DD-Mon-YY' that represents the date the order will be collected
 * @param fName					 The first name of the customer who will collect the order
 * @param LName					 The last name of the customer who will collect the order
 * @param staffID				 The id of the staff member who sold the order
 */
public static void option2(Connection conn, int[] productIDs, int[] quantities, String orderDate, String collectionDate, String fName, String LName, int staffID) {

	Set<Integer> s = new HashSet<Integer>();
	boolean dupes = false;

	for (int i : productIDs) {
		if (s.contains(i)) dupes = true;
		s.add(i);
	}

	boolean order = false;

	if (!dupes) {
		if (productIDs.length == quantities.length) {
			if (dateValidator(orderDate)) {
				if (dateValidator(collectionDate)) {
					if (fName.matches("[ A-Za-z]+")) {
						if (LName.matches("[ A-Za-z]+")) {
							order = purchases(conn, productIDs, quantities, orderDate, staffID, 0, "Collection");
						} else System.out.println("Last Name is Invalid");
					} else System.out.println("First Name is Invalid");
				} else System.out.println("Collection Date is Invalid");
			} else System.out.println("Order Date is Invalid");
		} else System.out.println("ProductIDs array and Quantities array are not the same size");
	} else System.out.println("There are duplicate ProductiDs in the order");

	PreparedStatement ps = null;

	if (order) {

		try {

			String insertCollections = "INSERT INTO COLLECTIONS " +
			                           "(OrderID, FName, LName, CollectionDate) VALUES (order_seq.currval, ?, ?, ?)";

			ps = conn.prepareStatement(insertCollections);

			ps.setString(1, fName);
			ps.setString(2, LName);
			ps.setDate(3, new Date(dateFormat.parse(collectionDate).getTime()));

			ps.executeUpdate();

		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) ps.close();
			} catch (SQLException e) {
				System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
			}
		}
	}
}

/**
 * @param conn				 An open database connection
 * @param productIDs	 An array of productIDs associated with an order
 * @param quantities	 An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
 * @param orderDate		 A string in the form of 'DD-Mon-YY' that represents the date the order was made
 * @param deliveryDate A string in the form of 'DD-Mon-YY' that represents the date the order will be delivered
 * @param fName				 The first name of the customer who will receive the order
 * @param LName				 The last name of the customer who will receive the order
 * @param house				 The house name or number of the delivery address
 * @param street			 The street name of the delivery address
 * @param city 				 The city name of the delivery address
 * @param staffID			 The id of the staff member who sold the order
 */
public static void option3(Connection conn, int[] productIDs, int[] quantities, String orderDate, String deliveryDate, String fName, String LName,
                           String house, String street, String city, int staffID) {

	Set<Integer> s = new HashSet<Integer>();
	boolean dupes = false;

	for (int i : productIDs) {
		if (s.contains(i)) dupes = true;
		s.add(i);
	}

	boolean order = false;

	if (!dupes) {
		if (productIDs.length == quantities.length) {
			if (dateValidator(orderDate)) {
				if (dateValidator(deliveryDate)) {
					if (fName.matches("[ A-Za-z]+")) {
						if (LName.matches("[ A-Za-z]+")) {
							if (house.matches("[a-zA-Z0-9]+")) {
								if (street.matches("[ A-Za-z]+")) {
									if (city.matches("[ A-Za-z]+")) {
										order = purchases(conn, productIDs, quantities, orderDate, staffID, 0, "Delivery");
									} else System.out.println("City Name is Invalid");
								} else System.out.println("Street Name is Invalid");
							} else System.out.println("House Name/Number is Invalid");
						} else System.out.println("Last Name is Invalid");
					} else System.out.println("First Name is Invalid");
				} else System.out.println("Delivery Date is Invalid");
			} else System.out.println("Order Date is Invalid");
		} else System.out.println("ProductIDs array and Quantities array are not the same size");
	} else System.out.println("There are duplicate ProductiDs in the order");

	PreparedStatement ps = null;

	if (order) {

		try {

			String insertDeliveries = "INSERT INTO DELIVERIES " +
			                          "(OrderID, FName, LName, House, Street, City, DeliveryDate) VALUES (order_seq.currval, ?, ?, ?, ?, ?, ?)";

			ps = conn.prepareStatement(insertDeliveries);

			ps.setString(1, fName);
			ps.setString(2, LName);
			ps.setString(3, house);
			ps.setString(4, street);
			ps.setString(5, city);
			ps.setDate(6, new Date(dateFormat.parse(deliveryDate).getTime()));

			ps.executeUpdate();

		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) ps.close();
			} catch (SQLException e) {
				System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
			}
		}
	}
}

/**
 * @param conn					 An open database connection
 * @param productIDs		 An array of productIDs associated with an order
 * @param quantities 		 An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
 * @param orderDate			 A string in the form of 'DD-Mon-YY' that represents the date the order was made
 * @param staffID 			 The id of the staff member who sold the order
 * @param orderCompleted An integer corresponding to whether an order is complete or not
 * @param orderType			 A string which is "InStore", "Collection" or "Delivery" tbat represents the type of order
 */
public static void option4(Connection conn) {

	Statement s = null;
	ResultSet rs = null;

	String selectBiggestSellers = "SELECT ProductID, ProductDesc, SUM(ProductQuantity * ProductPrice) TotalValueSold " +
	                              "FROM INVENTORY " +
	                              "NATURAL JOIN ORDER_PRODUCTS " +
	                              "GROUP BY ProductID, ProductDesc " +
	                              "ORDER BY TotalValueSold DESC";

	try {

		s = conn.createStatement();

		rs = s.executeQuery(selectBiggestSellers);

		if (rs.isBeforeFirst() ) {

			System.out.format("%-10s %-24s %-14s\n", "ProductID,", "ProductDesc,", "TotalValueSold");

			while (rs.next()) {
				System.out.format("%-10s %-24s £%13d\n", rs.getInt(1) + ",", rs.getString(2) + ",", rs.getInt(3));
			}

		} else System.out.println("No Products in Inventory");

	} catch (SQLException e) {
		System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		try {
			if (s != null) s.close();
			if (rs != null) rs.close();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		}
	}

}

/**
 * @param conn An open database connection
 * @param date The target date to test collection deliveries against
 */
public static void option5(Connection conn, String date) {

	ArrayList<Integer> productIDs = new ArrayList<>();
	ArrayList<Integer> quantities = new ArrayList<>();
	ArrayList<Integer> orderIDs = new ArrayList<>();

	PreparedStatement ps = null;
	ResultSet rs = null;

	String selectLateCollections = "SELECT ProductID, ProductQuantity, OrderID " +
	                               "FROM ORDERS " +
	                               "NATURAL JOIN COLLECTIONS " +
	                               "NATURAL JOIN ORDER_PRODUCTS " +
	                               "NATURAL JOIN INVENTORY " +
	                               "WHERE CollectionDate + 8 <= ? AND OrderCompleted = 0";

	if (dateValidator(date)) {

		try {

			conn.setAutoCommit(false);
			conn.commit();

			ps = conn.prepareStatement(selectLateCollections);

			ps.setDate(1, new Date(dateFormat.parse(date).getTime()));

			rs = ps.executeQuery();

			if (rs.isBeforeFirst()) {

				while (rs.next()) {

					productIDs.add(rs.getInt(1));

					quantities.add(rs.getInt(2));

					int orderID = rs.getInt(3);

					if (!orderIDs.contains(orderID))
						orderIDs.add(orderID);

				}

				String updateInventory = "UPDATE INVENTORY " +
				                         "SET ProductStockAmount = ProductStockAmount + ? " +
				                         "WHERE ProductID = ?";

				for (int i = 0; i < productIDs.size(); i++) {

					ps = conn.prepareStatement(updateInventory);

					ps.setInt(1, quantities.get(i));
					ps.setInt(2, productIDs.get(i));

					ps.executeUpdate();

				}

				String deleteOrders1 = "DELETE FROM COLLECTIONS WHERE OrderID = ?";
				String deleteOrders2 = "DELETE FROM STAFF_ORDERS WHERE OrderID = ?";
				String deleteOrders3 = "DELETE FROM ORDER_PRODUCTS WHERE OrderID = ?";
				String deleteOrders4 = "DELETE FROM ORDERS WHERE OrderID = ?";

				for (int i = 0; i < orderIDs.size(); i++) {

					ps = conn.prepareStatement(deleteOrders1);

					ps.setInt(1, orderIDs.get(i));

					ps.executeUpdate();

					ps = conn.prepareStatement(deleteOrders2);

					ps.setInt(1, orderIDs.get(i));

					ps.executeUpdate();

					ps = conn.prepareStatement(deleteOrders3);

					ps.setInt(1, orderIDs.get(i));

					ps.executeUpdate();

					ps = conn.prepareStatement(deleteOrders4);

					ps.setInt(1, orderIDs.get(i));

					ps.executeUpdate();

					System.out.format("Order %d has been cancelled\n", orderIDs.get(i));

				}

			} else System.out.println("No expired collections found");

		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
			System.out.println("Rolling back data...");
			try {
				if (conn != null) conn.rollback();
			} catch (SQLException e2) {
				System.err.format("SQL State: %s\n%s", e2.getSQLState(), e2.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) ps.close();
				if (rs != null) rs.close();
			} catch (SQLException e) {
				System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
			}

		}

	} else System.out.println("Date is invalid");

}

/**
 * @param conn An open database connection
 */
public static void option6(Connection conn) {

	Statement s = null;
	ResultSet rs = null;

	String selectBestStaff = "SELECT FName || ' ' || LName StaffName, SUM(ProductPrice * ProductQuantity) TotalValueSold " +
	                         "FROM INVENTORY " +
	                         "NATURAL JOIN ORDER_PRODUCTS " +
	                         "NATURAL JOIN STAFF_ORDERS "+
	                         "NATURAL JOIN STAFF " +
	                         "GROUP BY FName, LName " +
	                         "HAVING SUM(ProductPrice * ProductQuantity) >= 50000 " +
	                         "ORDER BY TotalValueSold DESC";

	try {

		s = conn.createStatement();

		rs = s.executeQuery(selectBestStaff);

		if (rs.isBeforeFirst() ) {

			System.out.format("%-24s %-14s\n", "EmployeeName,", "TotalValueSold");

			while (rs.next()) {
				System.out.format("%-24s £%13d\n", rs.getString(1) + ",", rs.getInt(2));
			}

		} else System.out.println("No Staff Member sold more than £50,000");

	} catch (SQLException e) {
		System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		try {
			if (s != null) s.close();
			if (rs != null) rs.close();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		}
	}

}

/**
 * @param conn An open database connection
 */
public static void option7(Connection conn) {

	ArrayList<String> productIDs = new ArrayList<>();
	int i;

	PreparedStatement ps = null;
	ResultSet rs = null;

	String selectHighValProducts = "SELECT ProductID " +
	                               "FROM INVENTORY " +
	                               "NATURAL JOIN ORDER_PRODUCTS " +
	                               "GROUP BY ProductID " +
	                               "HAVING SUM(ProductQuantity * ProductPrice) > 20000";

	try {

		ps = conn.prepareStatement(selectHighValProducts);

		rs = ps.executeQuery();

		while (rs.next()) {
			productIDs.add(Integer.toString(rs.getInt(1)));
		}

		if (!productIDs.isEmpty()) {

			String pivot_clause = "";

			for (i = 0; i < productIDs.size() - 1; i++) {
				pivot_clause += productIDs.get(i) + ", ";
			}

			pivot_clause += productIDs.get(i);

			String selectProductPivot = "SELECT t.* " +
			                            "FROM (SELECT * " +
			                            "      FROM (SELECT FName || ' ' || LName StaffName, ProductID, ProductQuantity " +
			                            "            FROM INVENTORY " +
			                            "            NATURAL JOIN ORDER_PRODUCTS " +
			                            "            NATURAL JOIN STAFF_ORDERS " +
			                            "            NATURAL JOIN STAFF) " +
			                            "      PIVOT (SUM(ProductQuantity) " +
			                            "             FOR ProductID IN (" + pivot_clause + "))) t " +
			                            "INNER JOIN " +
			                            "     (SELECT FName || ' ' || LName StaffName, sum(ProductPrice * ProductQuantity) TotalValueSold " +
			                            "      FROM INVENTORY " +
			                            "      NATURAL JOIN ORDER_PRODUCTS " +
			                            "      NATURAL JOIN STAFF_ORDERS " +
			                            "      NATURAL JOIN STAFF " +
			                            "      GROUP BY FName, LName, ProductID " +
			                            "      HAVING sum(ProductPrice * ProductQuantity) > 20000) t2 " +
			                            "ON t.StaffName = t2.StaffName " +
			                            "ORDER BY TotalValueSold DESC";

			ps = conn.prepareStatement(selectProductPivot);

			rs = ps.executeQuery();

			System.out.format("%-24s", "EmployeeName, ");

			for (i = 0; i < productIDs.size() - 1; i++) {

				System.out.format("%-12s", "Product " + productIDs.get(i) + ",");

			}

			System.out.format("%-12s\n", "Product " + productIDs.get(i));

			while (rs.next()) {

				System.out.format("%-24s", rs.getString(1) + ",");

				for (i = 0; i < productIDs.size() - 1; i++) {

					System.out.format("%-12s", rs.getInt(2 + i) + ",");

				}

				System.out.format("%-12s\n", rs.getInt(2 + i));

			}

		} else System.out.println("No Products with high enough revenue");

	} catch (SQLException e) {
		System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		try {
			if (ps != null) ps.close();
			if (rs != null) rs.close();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		}
	}
}

/**
 * @param conn An open database connection
 * @param year The target year we match employee and product sales against
 */
public static void option8(Connection conn, int year) {

	ArrayList<String> productIDs = new ArrayList<>();
	int i;

	PreparedStatement ps = null;
	ResultSet rs = null;

	String selectHighValProducts = "SELECT ProductID " +
	                               "FROM INVENTORY " +
	                               "NATURAL JOIN ORDER_PRODUCTS " +
	                               "GROUP BY ProductID " +
	                               "HAVING SUM(ProductQuantity * ProductPrice) > 20000 ";

	try {

		ps = conn.prepareStatement(selectHighValProducts);

		rs = ps.executeQuery();

		while (rs.next()) {
			productIDs.add(Integer.toString(rs.getInt(1)));
		}

		if (!productIDs.isEmpty()) {

			String where_clause = "";

			for (i = 0; i < productIDs.size() - 1; i++) {
				where_clause += productIDs.get(i) + ", ";
			}

			where_clause += productIDs.get(i);

			String selectEmployees = "SELECT * " +
			                         "FROM (SELECT FName || ' ' || LName StaffName " +
			                         "			FROM INVENTORY "+
			                         "      NATURAL JOIN ORDER_PRODUCTS " +
			                         "      NATURAL JOIN STAFF_ORDERS " +
			                         "      NATURAL JOIN STAFF " +
			                         "      NATURAL JOIN ORDERS " +
			                         "      WHERE OrderPlaced >= ? AND OrderPlaced < ? " +
			                         "      GROUP BY FName, LName " +
			                         "      HAVING SUM(ProductQuantity * ProductPrice) >= 30000) " +
			                         "INTERSECT (SELECT FName || ' ' || LName StaffName " +
			                         "      		 FROM INVENTORY "+
			                         "      		 NATURAL JOIN ORDER_PRODUCTS "+
			                         "      		 NATURAL JOIN STAFF_ORDERS "+
			                         "      		 NATURAL JOIN STAFF "+
			                         "      		 NATURAL JOIN ORDERS "+
			                         "      		 WHERE OrderPlaced >= ? AND OrderPlaced < ? "+
			                         "					 AND ProductID IN ("+ where_clause + ") " +
			                         "      		 GROUP BY FName, LName "+
			                         "      		 HAVING COUNT(*) = "+ Integer.toString(productIDs.size()) + ") ";

			ps = conn.prepareStatement(selectEmployees);

			ps.setDate(1, Date.valueOf(Integer.toString(year) + "-01-01"));
			ps.setDate(2, Date.valueOf(Integer.toString(year + 1) + "-01-01"));
			ps.setDate(3, Date.valueOf(Integer.toString(year) + "-01-01"));
			ps.setDate(4, Date.valueOf(Integer.toString(year + 1) + "-01-01"));

			rs = ps.executeQuery();

			while (rs.next()) {
				System.out.println(rs.getString(1));
			}

		} else System.out.println("No Products with high enough revenue");

	} catch (SQLException e) {
		System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		try {
			if (ps != null) ps.close();
			if (rs != null) rs.close();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		}
	}

}

public static Connection getConnection() {

	// User and password should be left blank. Do not alter!
	String user = "";
	String passwrd = "";
	Connection conn;

	try {
		Class.forName("oracle.jdbc.driver.OracleDriver");
	} catch (ClassNotFoundException x) {
		System.out.println("Driver could not be loaded");
	}

	try {
		conn = DriverManager.getConnection("jdbc:oracle:thin:@arryn-ora-prod-db-1.warwick.ac.uk:1521:cs2db",user,passwrd);
		return conn;
	} catch(SQLException e) {
		System.out.println("Error retrieving connection");
		return null;
	}

}

public static void main(String args[]) throws SQLException, IOException {
	Connection conn = getConnection();

	if (conn != null) {

		menu(conn);
		conn.close();
		System.out.println("Menu could not be loaded, try again with a secure connection");

	}

}

public static void menu(Connection conn) {

	System.out.println("\n----------------------------\n");

	System.out.println("MENU:\n");

	System.out.println("(1) In-Store Purchases");
	System.out.println("(2) Collection");
	System.out.println("(3) Delivery");
	System.out.println("(4) Biggest Sellers");
	System.out.println("(5) Reserved Stock");
	System.out.println("(6) Staff Life-Time Success");
	System.out.println("(7) Staff Contribution");
	System.out.println("(8) Employees of the Year");
	System.out.println("(0) Exit");

	System.out.println("\n----------------------------\n");

	String option = readEntry("Enter your option: ");

	ArrayList<Integer> productIDs = new ArrayList<>();
	ArrayList<Integer> quantities = new ArrayList<>();

	String productID, quantity, orderDate, staffID, fName, lName;;

	switch (option) {

	case "1":

		do {

			do {
				productID = readEntry("Enter the ProductID: ");
			} while (!productID.matches("\\d+"));

			do {
				quantity = readEntry("Enter the quantity: ");
			} while (!quantity.matches("\\d+"));

			if (!productIDs.contains(Integer.parseInt(productID))) {

				productIDs.add(Integer.parseInt(productID));
				quantities.add(Integer.parseInt(quantity));

			} else {

				System.out.println("Already added selected ProductID (" + productID + "). Cannot add it twice.");

				if (readEntry("Add this quantity (" + quantity + ") to the already existing quantity? (y if yes): ").equals("y")) {

					int dupeID = productIDs.indexOf(Integer.parseInt(productID));
					int oldQuant = quantities.get(dupeID);
					int newQuant = Integer.parseInt(quantity) + oldQuant;

					quantities.set(dupeID, newQuant);
					System.out.println("Product ID (" + productID + ") had quantity (" + oldQuant + ") Now has quantity (" + newQuant + ").");

				}

			}

		} while (readEntry("Order another product? (y if yes): ").equals("y"));

		do {
			orderDate = readEntry("Enter the date sold (dd-mon-yy): ");
		} while (!dateValidator(orderDate));

		do {
			staffID = readEntry("Enter your StaffID: ");
		} while (!staffID.matches("\\d+"));

		option1(conn, convertArrayList(productIDs), convertArrayList(quantities), orderDate, Integer.parseInt(staffID));

		break;

	case "2":

		String collectionDate;

		do {

			do {
				productID = readEntry("Enter the ProductID: ");
			} while (!productID.matches("\\d+"));

			do {
				quantity = readEntry("\nEnter the quantity: ");
			} while (!quantity.matches("\\d+"));

			if (!productIDs.contains(Integer.parseInt(productID))) {

				productIDs.add(Integer.parseInt(productID));
				quantities.add(Integer.parseInt(quantity));

			} else {

				System.out.println("Already added selected ProductID (" + productID + "). Cannot add it twice.");

				if (readEntry("Add this quantity (" + quantity + ") to the already existing quantity? (y if yes): ").equals("y")) {

					int dupeID = productIDs.indexOf(Integer.parseInt(productID));
					int oldQuant = quantities.get(dupeID);
					int newQuant = Integer.parseInt(quantity) + oldQuant;

					quantities.set(dupeID, newQuant);
					System.out.println("Product ID (" + productID + ") had quantity (" + oldQuant + ") Now has quantity (" + newQuant + ").");

				}

			}

		} while (readEntry("Order another product? (y if yes): ").equals("y"));

		do {
			orderDate = readEntry("Enter the date sold (dd-mon-yy): ");
		} while (!dateValidator(orderDate));

		do {
			collectionDate = readEntry("Enter the date to be collected (dd-mon-yy): ");
		} while (!dateValidator(collectionDate) || !afterDate(orderDate, collectionDate));

		do {
			fName = readEntry("Enter the first name of the recipient: ");
		} while (!fName.matches("[ A-Za-z]+"));

		do {
			lName = readEntry("Enter the last name of the recipient: ");
		} while (!lName.matches("[ A-Za-z]+"));

		do {
			staffID = readEntry("Enter your StaffID: ");
		} while (!staffID.matches("\\d+"));

		option2(conn, convertArrayList(productIDs), convertArrayList(quantities), orderDate, collectionDate, fName, lName, Integer.parseInt(staffID));

		break;

	case "3":

		String deliveryDate, house, street, city;

		do {

			do {
				productID = readEntry("Enter the ProductID: ");
			} while (!productID.matches("\\d+"));

			do {
				quantity = readEntry("Enter the quantity: ");
			} while (!quantity.matches("\\d+"));

			if (!productIDs.contains(Integer.parseInt(productID))) {

				productIDs.add(Integer.parseInt(productID));
				quantities.add(Integer.parseInt(quantity));

			} else {

				System.out.println("Already added selected ProductID (" + productID + "). Cannot add it twice.");

				if (readEntry("Add this quantity (" + quantity + ") to the already existing quantity? (y if yes): ").equals("y")) {

					int dupeID = productIDs.indexOf(Integer.parseInt(productID));
					int oldQuant = quantities.get(dupeID);
					int newQuant = Integer.parseInt(quantity) + oldQuant;

					quantities.set(dupeID, newQuant);
					System.out.println("Product ID (" + productID + ") had quantity (" + oldQuant + ") Now has quantity (" + newQuant + ").");

				}

			}

		} while (readEntry("Order another product? (y if yes): ").equals("y"));

		do {
			orderDate = readEntry("Enter the date sold (dd-mon-yy): ");
		} while (!dateValidator(orderDate));

		do {
			deliveryDate = readEntry("Enter the date to be delivered (dd-mon-yy): ");
		} while (!dateValidator(deliveryDate) || !afterDate(orderDate, deliveryDate));

		do {
			fName = readEntry("Enter the first name of the recipient: ");
		} while (!fName.matches("[ A-Za-z]+"));

		do {
			lName = readEntry("Enter the last name of the recipient: ");
		} while (!lName.matches("[ A-Za-z]+"));

		do {
			house = readEntry("Enter the house name/number: ");
		} while (!house.matches("[ a-zA-Z0-9]+"));

		do {
			street = readEntry("Enter the street: ");
		} while (!street.matches("[ A-Za-z]+"));

		do {
			city = readEntry("Enter the city: ");
		} while (!city.matches("[ A-Za-z]+"));

		do {
			staffID = readEntry("Enter your StaffID: ");
		} while (!staffID.matches("\\d+"));

		option3(conn, convertArrayList(productIDs), convertArrayList(quantities), orderDate, deliveryDate, fName, lName, house, street, city, Integer.parseInt(staffID));

		break;

	case "4":

		option4(conn);

		break;

	case "5":

		String chosenDate;

		do {
			chosenDate = readEntry("Enter the date sold (dd-mon-yy): ");
		} while (!dateValidator(chosenDate));

		option5(conn, chosenDate);

		break;

	case "6":

		option6(conn);

		break;

	case "7":

		option7(conn);

		break;

	case "8":

		String chosenYear;

		do {
			chosenYear = readEntry("Enter the year (yyyy): ");
		} while (!chosenYear.matches("\\d{4}"));

		option8(conn, Integer.parseInt(chosenYear));

		break;

	case "0":

		break;

	default:

		System.out.println("Enter a valid option.");

	}

	if (!option.equals("0")) {

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		menu(conn);

	}

}

/**
 * Validates a date using a ParseException according to the dateFormat
 * @param  date String representation of a date
 * @return      True if date is a valid date, false if it's not
 */
public static boolean dateValidator(String date) {
	try {
		dateFormat.parse(date);
	} catch (ParseException e) {
		return false;
	}
	return true;
}

public static boolean afterDate(String date1, String date2) {
	boolean ans = false;
	try {
		ans = (dateFormat.parse(date2).after(dateFormat.parse(date1))) || (dateFormat.parse(date2).equals(dateFormat.parse(date1)));
	} catch (ParseException e) {
		e.printStackTrace();
	}
	return ans;
}

public static int[] convertArrayList(ArrayList<Integer> integers) {
	int[] ints = new int[integers.size()];
	for (int i = 0; i < ints.length; i++) {
		ints[i] = integers.get(i).intValue();
	}
	return ints;
}

}
