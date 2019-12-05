------------------------------------------------------------------------------

# CS258 Coursework
Name: David Ferreira  
ID Number: 1705344

## Java File

### Menu
The menu where the user selects which options they want

The user can input any value from **1 to 8** or **q** to exit the menu.  
After an option is selected and completed the menu appears again (after a delay).

These are the constraints used to ensure the entered data is of the right type:

  - **fName**, **LName**, **street**, **city** can only contain letters
  - **house** can only contain letters and integers
  - **orderDate**, **deliveryDate**, **collectionDate** can only be of format **DD-MON-YY(YY)**
  - **productIDs**, **quantities**, **staffID** have to be integers
  - In **Option 8** the year is an integer that has to be 4 digits long
  - In **Purchases** if you try and order from the same **Product ID** twice, you can either cancel that addition or add the quantity you specified onto the pre-existing quantity of the duplicate **Product ID**
  
In all java code assume any `Statements` or `PreparedStatements` are handled inside of a try-catch block like this one:

Auto-commit is only turned off in the **Purchases** section.

```java
try{

	...
	
} catch (SQLException e) {
	System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
	
	System.out.println("Rolling back data..."); // This section is only added if auto-commit was set to off
	try {
		if (conn != null) conn.rollback();
	} catch (SQLException e2) {
		System.err.format("SQL State: %s\n%s", e2.getSQLState(), e2.getMessage());
	}
	
} catch (Exception e) {
	e.printStackTrace();
} finally {
	try {
		if (s != null) s.close();   // Close a Statement if it exists
		if (ps != null) ps.close(); // Close a PreparedStatement if it exists
		if (rs != null) rs.close(); // Close a ResultSet if it exists
		
		conn.setAutoCommit(true);   // Only added if auto-commit was set to off
		
	} catch (SQLException e) {
		System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
	}
}
```

The code for `menu()` is as follows:

```java
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
	case "q":
		break;
	default:
		System.out.println("Enter a valid option.");
	}
	if (!option.equals("q")) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		menu(conn);
	}
}
```

The code for the `main()` method is as follows:

```java
public static void main(String args[]) throws SQLException, IOException {
	Connection conn = getConnection();
	if (conn != null) {
		menu(conn);
		conn.close();
	}
}
```

The code for all auxilliary methods are as follows:

```java
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
```


### Purchases
**Option 1**, **Option 2** and **Option 3** are all forms of purchases.

Each one of these requires the program to:

  - Store the date the order was made, whether the order is completed or not and the type of order on in **ORDERS**
  - For each product specified:
    - Store the **Product ID** and quantity of that product sold in **ORDER_PRODUCTS**
    - Reduce the amount of stock of the product sold by the amount sold in **INVENTORY**
  - Store the **Staff ID** of the employee who added the order in **STAFF_ORDERS**
  - Print out a list of updated product quantities with **Product ID**s

More specifically **Option 1** is an in-store purchase, **Option 2** is a collection purchase and **Option 3** is a delivery.

In **Option 1**:

  - **OrderType** is set to 'InStore'
  - The order is considered 'Complete' i.e. **OrderCompleted** is set to 1

In **Option 2**:

  - **OrderType** is set to 'Collection'
  - The order is considered 'Incomplete' i.e. **OrderCompleted** is set to 0
  - The date that the item will be collected on needs to be stored in **COLLECTIONS**
  - The first and last name of the person collecting the order needs to be stored in **COLLECTIONS**

In **Option 3**:

  - **OrderType** is set to 'Delivery'
  - The order is considered 'Incomplete' i.e. **OrderCompleted** is set to 0
  - The date that the item will be delivered on needs to be stored in **DELIVERIES**
  - The first and last name of the person receiving the order needs to be stored in **DELIVERIES**
  - The house name/number, street and city that the delivery is going to needs to be stored in **DELIVERIES**

I have created two methods to simplify this process, `updatingStock()` and `purchases()`.

`updatingStock()` returns an array of booleans, where if an element is true that means that the product exists.

Firstly it checks if the **Staff ID** exists in the database already, using an SQL query:

```sql
SELECT * FROM STAFF WHERE STAFFID = ?; -- ? is replaced with the input StaffID
```
If the ID doesn't exist, then it prints an error message and no stock is updated.

If the **Staff ID** does exist, it then checks if each **Product ID** in the input array exists by looping through the array and on each element running:

```sql
SELECT ProductStockAmount FROM INVENTORY WHERE ProductID = ?; -- ? is replaced with the input ProductID
```

If it does exist, it then checks the available stock of that **Product ID** and makes sure it is greater than the specified quantity to be sold. `if (stock < quantities[i])` where stock is the value we get from the previous SQL query.

If the stock is less, then it prints an error message and no stock is updated.

If the stock is greater, then the stock is updated by subtracting the quantity and the corresponding element in the boolean array is changed to true.

```sql
UPDATE INVENTORY
SET ProductStockAmount = ProductStockAmount - ? -- ? is replaced with quantities[i]
WHERE ProductID = ?; -- ? is replaced with productIDs[i]
```

The code for `updatingStock()` is as follows:

```java
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
```

`purchases()` returns a boolean corresponding to whether or not an order can be made

In this method I have turned auto-commit off, which allows me to specify when to commit to the database.

By doing this and committing before inserting anything into the table, and then specifying to rollback if any **SQLExceptions** are encounterd, I am able to prevent some of the values from being inserted into the tables which would mess a lot of things up.

After having updated the stock, we then update the database with all the new values, however we only do this if there is at least one product that exists in the order.

```sql
INSERT INTO ORDERS (OrderID, OrderType, OrderCompleted, OrderPlaced)
VALUES (NULL, ?, ?, ?);          -- ?s replaced with input values

INSERT INTO ORDER_PRODUCTS (OrderID, ProductID, ProductQuantity)
VALUES (order_seq.currval, ?, ?) -- ?s replaced with input values

INSERT INTO STAFF_ORDERS (OrderID, StaffID)
VALUES (order_seq.currval, ?)    -- ?s replaced with input values
```

I have decided to do it this way instead of cancelling the whole order, since that is how most companies currently do it, instead of having to enter all the products again minus the one without stock or the one that doesn't exist, the ones that do are ordered.

If we have no products that we are ordering then this method simply returns false, for ease in **Option 2** and **Option 3** methods, so as to not have to search the boolean array once again for true elements.

Since **Option 1** has no extra details that need to be saved, it just needs to run the purchases method with 'InStore' for **orderType** and 1 for **orderCompleted**.

**Option 2** has to add to the COLLECTIONS table:

```sql
INSERT INTO COLLECTIONS (OrderID, FName, LName, CollectionDate)
VALUES (order_seq.currval, ?, ?, ?); -- ?s replaced with input values
```
Option 3 has to add to the DELIVERIES table:

```sql
INSERT INTO DELIVERIES (OrderID, FName, LName, House, Street, City, DeliveryDate)
VALUES (order_seq.currval, ?, ?, ?, ?, ?, ?); -- ?s replaced with input values
```

The code for `purchases()` is as follows:

```java
public static boolean purchases(Connection conn, int[] productIDs, int[] quantities, String orderDate, int staffID, int orderCompleted, String orderType) {
	PreparedStatement ps = null;
	boolean[] productExists = updatingStock(conn, productIDs, quantities, staffID);
	boolean order = false;
	try {
		// Turning off autocommit allows the database to rollback to here if it encounters an SQLException
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
	return order
}
```

At the start of each method multiple checks are run on the input parameters:

  - **fName**, **LName**, **street**, **city** can only contain letters
  - **house** can only contain letters and integers
  - **orderDate**, **deliveryDate**, **collectionDate** can only be of format **DD-MON-YY(YY)**
  - Array of **productIDs**, Array of **quantities**, **staffID** have to be integers
  - Array of **ProductIDs** is the same size as array of **quantities**
  - No duplicates in the **productIDs** array

The code for `option1()` is as follows:

```java
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
```

The code for `option2()` is as follows:

```java
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
```

The code for `option3()` is as follows:

```java
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
```

### Biggest Sellers
This is **Option 4**.

This method gets each Product in **INVENTORY** and lists them in descending order according to how much revenue they have generated.

Specifcally it lists **ProductID**, **ProductDesc** and total revenute one product per line and separated by commas, where total revenue is the sum of **ProductQuantity** \* **ProductPrice** when **Product ID** is the same.

```sql
SELECT ProductID, ProductDesc, sum(ProductQuantity * ProductPrice) TotalValueSold
FROM INVENTORY
NATURAL JOIN ORDER_PRODUCTS 
GROUP BY ProductID, ProductDesc 
ORDER BY TotalValueSold DESC;
```
Nothing needs to be input here we just get output and display it using System.out.format in the specified manner.

The code for `option4()` is as follows:

```java
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
```

### Reserved Stock
This is **Option 5**.

This method removes all orders from **ORDERS**, **ORDER_PRODUCTS**, **STAFF_ORDERS** and **COLLECTIONS** from the table that are collections, uncompleted and have a collection date _**8 days or older**_ than the provided date. It also re-adds the quantity that was sold back onto the product's stock.

It then prints out the orders which were deleted.

So firstly we find the **Product ID**, **ProductQuantity** and **OrderID** from the orders that fall under our specifications.

```sql
SELECT ProductID, ProductQuantity, OrderID
FROM ORDERS
NATURAL JOIN COLLECTIONS
NATURAL JOIN ORDER_PRODUCTS
NATURAL JOIN INVENTORY -- Linking ORDERS with INVENTORY
WHERE CollectionDate + 8 <= ? AND OrderCompleted = 0;
```

Our only input is the date which we have specified as input from the menu.

`CollectionDate + 8 <= x` is the same as `CollectionDate <= x - 8` which is 8 days or older.

We then take our output from this query and put it into lists, making sure we have no duplicate OrderIDs (in other words we only have 1 one of each OrderID).

We then loop through the length of ProductIDs (which is the same as the length of quantities) and for each pair of entries we update the stock in **INVENTORY**.

```sql
UPDATE INVENTORY
SET ProductStockAmount = ProductStockAmount + ? -- ? is replaced with quantities[i]
WHERE ProductID = ?;                            -- ? is replaced with productIDs[i]
```

We then make 4 different statements for deleting from the database, making sure to order it correctly as **ORDERS** must go last (as its the 'Parent Table').

Then we loop through each distinct **Order ID** and delete the orders from every table.

```sql
DELETE FROM COLLECTIONS WHERE OrderID = ?;
DELETE FROM STAFF_ORDERS WHERE OrderID = ?;
DELETE FROM ORDER_PRODUCTS WHERE OrderID = ?;
DELETE FROM ORDERS WHERE OrderID = ?;
```

Where ? is replacd with each different **Order ID**.

We then output which **orders** have been deleted

We also check if no expired orders exist and print a message for that occurrence.

The code for `option5()` is as follows:

```java
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
```

### Staff Life-Time Success
This is **Option 6**.

This method gets each Staff Member in **STAFF** who has sold at least £50,000 worth of prodcuts from **INVENTORY** in the store's lifetime.

Specifcally it lists *StaffName* and *TotalValueSold* one staff member per line and separated by commas, where *StaffName* is **FName** combined with **LName** and *TotalValueSold* is the sum of **ProductQuantity** \* **ProductPrice** when the staff member is the same.

So firstly we find the *StaffName* and *ProductQuantity* from the orders that fall under our specifications.

```sql
SELECT FName || ' ' || LName StaffName, SUM(ProductPrice * ProductQuantity) TotalValueSold
FROM INVENTORY
NATURAL JOIN ORDER_PRODUCTS
NATURAL JOIN STAFF_ORDERS
NATURAL JOIN STAFF    -- Linking STAFF with INVENTORY
GROUP BY FName, LName -- When the staff member is the same
HAVING SUM(ProductPrice * ProductQuantity) >= 50000
ORDER BY TotalValueSold DESC;
```

Nothing needs to be input here we just get output and display it using System.out.format in the specified manner.

The code for `option6()` is as follows:

```java
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
```

### Staff Contributions
This is **Option 7**.

This method, for any Product from **INVENTORY** that has sold over £20,000 in the store's lifetime, get any Staff Member from **STAFF** who has sold at least one of any of these Products as well as how many of each of them they have sold.

It prints them out in a table where the 1st column is *StaffName* and the following columns are the high-selling products (over £20,000).

We also sort the *StaffNames* in descending order by the total monetary value of the high-selling products only sold by each employee.

So firstly we find the **Product ID**s from **INVENTORY** that have sold over £20,000

```sql
SELECT ProductID 
FROM INVENTORY 
NATURAL JOIN ORDER_PRODUCTS
GROUP BY ProductID
HAVING SUM(ProductQuantity * ProductPrice) > 20000; -- Similar to WHERE but for aggregated data
```

We then create a string in the form of (a, b, ..., z) with the **Product IDs** inside:

```java
ArrayList<String> productIDs = new ArrayList<>();   // Type String in order to create a String later easily
int i;                                              // Initialise i to use it outside the for loop

PreparedStatement ps = null;
ResultSet rs = null;

ps = conn.prepareStatement(selectHighValProducts);
rs = ps.executeQuery();

while (rs.next()) {
	productIDs.add(Integer.toString(rs.getInt(1))); // We get the ProductIDs from the ResultSet
}

if (!productIDs.isEmpty()) {
	String pivot_clause = "";
	for (i = 0; i < productIDs.size() - 1; i++) {
		pivot_clause += productIDs.get(i) + ", ";   // Adds ProductIDs to string separated by commas
	}
	pivot_clause += productIDs.get(i);              // Adds the final ProductID without a comma
}
```

Next we run the SQL Statement to create the pivot table:

```sql
SELECT t.*
FROM (SELECT *
      FROM (SELECT FName || ' ' || LName StaffName, ProductID, ProductQuantity 
            FROM INVENTORY
            NATURAL JOIN ORDER_PRODUCTS
            NATURAL JOIN STAFF_ORDERS
            NATURAL JOIN STAFF)
      PIVOT (SUM(ProductQuantity) -- Pivot clause puts section below as the columns for the table
             FOR ProductID IN ('pivot_clause'))) t
INNER JOIN -- We have the joining of the pivot table with a table of High-Selling ProductIDs
     (SELECT FName || ' ' || LName StaffName, sum(ProductPrice * ProductQuantity) TotalValueSold 
      FROM INVENTORY 
      NATURAL JOIN ORDER_PRODUCTS 
      NATURAL JOIN STAFF_ORDERS 
      NATURAL JOIN STAFF 
      GROUP BY FName, LName, ProductID
      HAVING sum(ProductPrice * ProductQuantity) > 20000) t2 
ON t.StaffName = t2.StaffName
ORDER BY TotalValueSold DESC";
```

The 'pivot_clause' in the code section is not actually part of the SQL code, it is a String we insert into the String we use for the PreparedStatement inside java.

So we get the pivot table returned. Due to the amount of columns being unknown our display of the table is a bit different than usual but it works.

```java
System.out.format("%-24s", "EmployeeName, "); // First column title is static
for (i = 0; i < productIDs.size() - 1; i++) {
	System.out.format("%-12s", "Product " + productIDs.get(i) + ","); // Every other column title printed here except last
}
System.out.format("%-12s\n", "Product " + productIDs.get(i)); // Final column title printed without a comma
while (rs.next()) {
	System.out.format("%-24s", rs.getString(1) + ",");
	for (i = 0; i < productIDs.size() - 1; i++) {
		System.out.format("%-12s", rs.getInt(2 + i) + ",");
	}
	System.out.format("%-12s\n", rs.getInt(2 + i)); // Final entry in each row with no comma
}
```

The code for `option7()` is as follows:

```java
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
```

### Employees of the Year
This is **Option 8**.

This method, for Staff from **STAFF** that have sold at least £30,000 worth of products (from **INVENTORY**) in a specified year, and sold at least one of each of the items that have sold over £20,000 worth of product (from **INVENTORY**) for the specified year.

It prints prints out the names, one employee per line.

This is quite similar to combining elements of **Option 6** and **Option 7**.
**Option 6** gets all products with revenue of at least £50,000 sold by staff.
**Option 7** gets staff that have sold **one of any** of the products that have sold over £20,000.

The differences being that this time it is £30,000 (not very different) and it is **one of EACH**.

This means an employee must sell at least one of all the high-selling products.

So firstly we find the **Product ID**s from **INVENTORY** that have sold over £20,000 (same as **Option 7**)

```sql
SELECT ProductID 
FROM INVENTORY 
NATURAL JOIN ORDER_PRODUCTS
GROUP BY ProductID
HAVING SUM(ProductQuantity * ProductPrice) > 20000; -- Similar to WHERE but for aggregated data
```

We then create a string in the form of (a, b, ..., z) with the **Product IDs** inside, so far this is the same as **Option 7** so there is no need to repear it.

Then we run the intersection of two tables of the specifications we need:

```sql
SELECT * 
FROM (SELECT FName || ' ' || LName StaffName 
	  FROM INVENTORY 
	  NATURAL JOIN ORDER_PRODUCTS 
	  NATURAL JOIN STAFF_ORDERS 	   
	  NATURAL JOIN STAFF 
	  NATURAL JOIN ORDERS 
	  WHERE OrderPlaced >= ? AND OrderPlaced < ? 
	  GROUP BY FName, LName 
	  HAVING SUM(ProductQuantity * ProductPrice) >= 30000) 
INTERSECT (SELECT FName || ' ' || LName StaffName
		   FROM INVENTORY 
		   NATURAL JOIN ORDER_PRODUCTS 
		   NATURAL JOIN STAFF_ORDERS 
		   NATURAL JOIN STAFF 
		   NATURAL JOIN ORDERS 
		   WHERE OrderPlaced >= ? AND OrderPlaced < ? 
		   AND ProductID IN ('where_clause')
		   GROUP BY FName, LName 
		   HAVING COUNT(*) = 'productIDs.size().toString()';
```

The 'where_clause' in the code section is not actually part of the SQL code, it is a String we insert into the String we use for the PreparedStatement inside java.

As is 'productIDs.size().toString()', it represents the total amount of **Product IDs** in the String 'where_clause'. This ensures **one of EACH** product.

So we get the names returned and we print them out to the user.

The code for `option8()` is as follows:

```sql
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
			                         "      		 HAVING COUNT(*) = "+ productIDs.size().toString() + ") ";
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
```

## SQL Schema
The schema.sql file contains all the sql I have used in this project.

Firstly, table creation:

Each table has basic constraints like every column that isn't a primary or foreign key cannot be null. I have also constrained any column with a name containing ID to be a primary or foreign key if required.

I will call:

  - **ORDERS**, **INVENTORY**, **STAFF** 'Primary Tables'
  - **COLLECTIONS**, **DELIVERIES** 'Secondary Tables'
  - **ORDER_PRODUCTS**, **STAFF_ORDERS** 'Linking Tables'

The main distinctions being 'Primary Tables' only contain Primary keys, 'Secondary tables' only contain foreign keys and 'Linking tables' link two Primary Tables.

I have not made many changes to the data types of columns, except for Street in **DELIVERIES** I have changed to **VARCHAR(60)** since street names can be quite long.

For constraints in the initialisation of the table I have included the required constraints of:

  - **OrderType** can only be one of 'InStore', 'Collection', 'Delivery'.
  - **OrderCompleted** can only be one of 0, 1.
  
```sql
DROP TABLE INVENTORY CASCADE CONSTRAINTS;
CREATE TABLE INVENTORY (
  ProductID INTEGER PRIMARY KEY,
  ProductDesc VARCHAR(30) NOT NULL,
  ProductPrice NUMERIC(8, 2) NOT NULL,
  ProductStockAmount INTEGER NOT NULL
);

DROP TABLE ORDERS CASCADE CONSTRAINTS;
CREATE TABLE ORDERS (
  OrderID INTEGER PRIMARY KEY,
  OrderType VARCHAR(10) NOT NULL,
  OrderCompleted INTEGER NOT NULL,
  OrderPlaced DATE NOT NULL,
  CONSTRAINT CheckOrderType CHECK (OrderType IN ('InStore', 'Collection', 'Delivery')),
  CONSTRAINT CheckOrderCompleted CHECK (OrderCompleted IN (0, 1))
);

DROP TABLE ORDER_PRODUCTS;
CREATE TABLE ORDER_PRODUCTS (
  OrderID INTEGER NOT NULL,
  ProductID INTEGER NOT NULL,
  ProductQuantity INTEGER NOT NULL,
  PRIMARY KEY (OrderID, ProductID),
  FOREIGN KEY (OrderID) REFERENCES ORDERS(OrderID),
  FOREIGN KEY (ProductID) REFERENCES INVENTORY(ProductID)
);

DROP TABLE DELIVERIES;
CREATE TABLE DELIVERIES (
  OrderID INTEGER PRIMARY KEY,
  FName VARCHAR(30) NOT NULL,
  LName VARCHAR(30) NOT NULL,
  House VARCHAR(30) NOT NULL,
  Street VARCHAR(60) NOT NULL,
  City VARCHAR(30) NOT NULL,
  DeliveryDate DATE NOT NULL,
  FOREIGN KEY (OrderID) REFERENCES ORDERS(OrderID)
);

DROP TABLE COLLECTIONS;
CREATE TABLE COLLECTIONS (
  OrderID INTEGER PRIMARY KEY,
  FName VARCHAR(30) NOT NULL,
  LName VARCHAR(30) NOT NULL,
  CollectionDate DATE NOT NULL,
  FOREIGN KEY (OrderID) REFERENCES ORDERS(OrderID)
);

DROP TABLE STAFF CASCADE CONSTRAINTS;
CREATE TABLE STAFF (
  StaffID INTEGER PRIMARY KEY,
  FName VARCHAR(30) NOT NULL,
  LName VARCHAR(30) NOT NULL
);

DROP TABLE STAFF_ORDERS;
CREATE TABLE STAFF_ORDERS (
  StaffID INTEGER,
  OrderID INTEGER,
  PRIMARY KEY (StaffID, OrderID),
  FOREIGN KEY (StaffID) REFERENCES STAFF(StaffID),
  FOREIGN KEY (OrderID) REFERENCES ORDERS(OrderID)
);
```

I have also created 3 sequences, 3 functions and 3 triggers to take care of auto-incrementing for the 3 Primary tables.

Each of them corresponds to one table. They are the exact same method apart from the table changing.

What these triggers do is auto-incrememnt the Primary Key of their respective table if **NULL** is entered as the ID of a row. They start at 1 and can go on until the limit of the **INTEGER** data type.

There is another purpose, however.

If a row is inserted into the table with a value higher than the current value of the sequence, then when the sequence reaches this value an error will occur.

In order to avoid this error I have made it so the program checks if the current value of the sequence already exists as a row in the table.

If it doesn't then it inserts it like normal, however if it does then it will add **100** to the value and keep going until it can be added.

Before, I had it so it would add **1** to the value, however, there could be a problem with this approach, as adding **1** could make the program very slow if there exists a large amount of sequential values in the table. Since the value of the ID doesn't matter, I decided that adding **100** would be a better decision.

Having an integer constraint on the Primary Key value also means that any floating point value that is attempted to be added will be rounded into an integer.

```sql
-- Example with ORDERS (Same as INVENTORY and STAFF ones)

-- Sequence that starts at 1 and increments by 1
DROP SEQUENCE order_seq;
CREATE SEQUENCE order_seq start with 1;

/**
 * Function that finds if an OrderID exists in the ORDERS table
 * If it does exist it returns it, if it doesn't it returns NULL
 */
CREATE OR REPLACE FUNCTION find_OrderID (y INTEGER)
RETURN INTEGER
IS
  x INTEGER;
BEGIN
  SELECT OrderID
  INTO x
  FROM ORDERS
  WHERE OrderID = y;
  RETURN x;
EXCEPTION WHEN NO_DATA_FOUND THEN
  RETURN NULL;
END;
/

/**
 * Trigger activates before INSERT ON ORDERS and for each row
 * If the OrderID isn't null the Order is added as normal, however
 * if it is null, the program checks if that ID already exists inside
 * of ORDERS, if it does it adds 100 to the value and tries again until
 * a spot is found.
 */
CREATE OR REPLACE TRIGGER order_autoinc
  BEFORE
  INSERT
  ON ORDERS
  FOR EACH ROW
DECLARE
  y INTEGER := order_seq.nextval;
  x INTEGER := find_OrderID(y);
BEGIN
  IF :new.OrderID IS NULL THEN
    WHILE (x IS NOT NULL)
    LOOP
      y := y + 100;
      x := find_OrderID(y);
    END LOOP;
    :new.OrderID := y;
  ELSIF :new.OrderID <= 0 THEN
    DBMS_OUTPUT.PUT_LINE('ORDERID MUST BE GREATER THAN 0');
  END IF;
END;
/
```

## Design Choces
Things I would change:

Having FName and LName in 3 different tables could make it harder to differentiate between them.

Changing them to something like D_FName, C_FName and S/Staff_FName would be better.