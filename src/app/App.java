package app;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Scanner;
import java.util.Vector;

import java.sql.*;

public class App {

	// Get logger for this class
	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(App.class.getSimpleName());

	private static Scanner in = new Scanner(System.in);

	/**
	 * Connects to the database if it exists, creates it if it does not, and returns
	 * the connection object.
	 * 
	 * @param databaseFileName the database file name
	 * @return a connection object to the designated database
	 */
	public static Connection initializeDB(String databaseFileName) {
		/**
		 * The "Connection String" or "Connection URL".
		 * 
		 * "jdbc:sqlite:" is the "subprotocol". (If this were a SQL Server database it
		 * would be "jdbc:sqlserver:".)
		 */
		String url = "jdbc:sqlite:" + databaseFileName;
		Connection conn = null; // If you create this variable inside the Try block it will be out of scope
		try {
			conn = DriverManager.getConnection(url);
			if (conn != null) {
				// Provides some positive assurance the connection and/or creation was
				// successful.
				System.out.println("The connection to the database was successful.");
			} else {
				// Provides some feedback in case the connection failed but did not throw an
				// exception.
				System.out.println("Null Connection");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("There was a problem connecting to the database.");
		}
		return conn;
	}

	/**
	 * Get names of all tables in the database.
	 * 
	 * @param conn Connection to database
	 * @return a connection object to the designated database
	 */
	public static HashSet<String> getTableNames(Connection conn) {
		HashSet<String> tableNames = new HashSet<String>();
		;

		try {
			DatabaseMetaData meta = conn.getMetaData();
			ResultSet tRes = meta.getTables(null, null, "%", null);

			while (tRes.next()) {
				tableNames.add(tRes.getString("TABLE_NAME"));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return tableNames;
	}

	/**
	 * Get names of all columns in a table.
	 * 
	 * @param conn Connection to database
	 * @param tableName Name of the table
	 * @return a TreeSet of the attributes
	 */
	public static TreeSet<String> getColumnNames(Connection conn, String tableName) throws Exception { // TODO:
																										// Specialize
																										// exception
																										// thrown

		// Make sure table name provided exists in the database.
		if (!getTableNames(conn).contains(tableName)) {
			System.out.println("X Given table name does not exist in the database. X"); // TODO: Better error handling.
			System.exit(1);
		}

		// TODO: Figure out a better way without potentially retrieving the whole table.
		ResultSet rst = null;
		try {
			String prepQuery = "SELECT *\n" + "FROM ?\n" + "WHERE FALSE;";

			PreparedStatement pStmt = conn.prepareStatement(prepQuery);

			pStmt.setString(1, tableName); // add in the table name

			rst = pStmt.executeQuery();

		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		}

		TreeSet<String> colNames = getColumnNames(rst);
		return colNames;

	}

	// Get names of all columns in a query result set.
	public static TreeSet<String> getColumnNames(ResultSet rst) {
		TreeSet<String> colNames = new TreeSet<String>();

		// TODO: Figure out a better way without potentially retrieving the whole table.
		try {

			ResultSetMetaData rstMeta = rst.getMetaData();

			int numCols = rstMeta.getColumnCount();

			for (int c = 1; c <= numCols; ++c) {
				colNames.add(rstMeta.getColumnName(c));
				rstMeta.getColumnType(c);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return colNames;
	}

	/**
	 * Get type of data in a column for a query result set.
	 * 
	 * @param rst ResultSet used to acquire column types
	 * @return a vector of the column types
	 */
	public static Vector<Integer> getColumnTypes(ResultSet rst) {
		Vector<Integer> colTypes = new Vector<Integer>();

		// TODO: Figure out a better way without potentially retrieving the whole table.
		try {

			ResultSetMetaData rstMeta = rst.getMetaData();

			int numCols = rstMeta.getColumnCount();

			for (int c = 1; c <= numCols; ++c) {
				int t = rstMeta.getColumnType(c); // TODO: Why does it returns an "int" when doc says "java.sql.Types".
				colTypes.add(t);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return colTypes;
	}

	public static void printResultSet(ResultSet rSet) {

		TreeSet<String> colNames = getColumnNames(rSet);

		// Print column names.
		int numCols = colNames.size();

		Iterator<String> colNamesIter = colNames.iterator();

		for (int i = 1; i <= numCols; i++) {
			String value = colNamesIter.next();
			System.out.print(value);
			if (i < numCols)
				System.out.print(",  ");
		}

		System.out.print("\n");

		try {
			while (rSet.next()) {
				for (int i = 1; i <= numCols; i++) {

					String columnValue = rSet.getString(i);
					System.out.print(columnValue);

					if (i < numCols)
						System.out.print(",  ");
				}

				System.out.print("\n");
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1); // TODO: Better error handling
		}
	}

	// All possible main menu options.
	enum MainMenuOption {
		ADD, UPDATE, DELETE, SEARCH, REPORT, EXIT
	};

	// Prompt user with a main menu selection.
	public static MainMenuOption promptMainMenu() {

		// Prompt user with a main menu
		boolean isValid = false;
		int choice = 0;
		while (!isValid) {
			// Show main menu
			System.out.println("-- Main Menu --");
			System.out.println("Select an option: (int)\n" + "	1) Add Record(s)\n" + "	2) Update Record(s)\n"
					+ "	3) Delete Record(s)\n" + "	4) Search Record(s)\n" + "	5) Report(s)\n" + "   	*) Exit\n");

			// Read integer input.
			try {
				choice = in.nextInt();
				isValid = true;
			} catch (java.util.InputMismatchException ie) {
				// restart if not a valid integer.
				System.out.println("X Please enter an integer! X");
				in.next(); // Read any pending input
			}

		}

		switch (choice) {

		// Add record
		case 1:
			return MainMenuOption.ADD;
		// break;

		// Update record
		case 2:
			return MainMenuOption.UPDATE;
		// break;

		case 3:
			return MainMenuOption.DELETE;
		// break;

		case 4:
			return MainMenuOption.SEARCH;
		// break;
			
		case 5:
			return MainMenuOption.REPORT;
		// break;
			
		default:
			return MainMenuOption.EXIT;
		// break;
		}

	}

	// Prompt user to get the name of table to modify and check for its validity
	public static String promptTableSelection(Connection conn) {

		System.out.println("Which table do you want to address?: (string)");

		try {
			String tName = in.next();
			in.nextLine(); // Consume rest of the line

			// Make sure table name provided exists in the database.
			HashSet<String> tableNames = getTableNames(conn);
			if (!tableNames.contains(tName)) {

				System.out.println("X Given table name does not exist in the database. X"); // TODO: Better error
																							// handling.
				System.out.println("Please select from: ");
				tableNames.forEach(element -> {
					System.out.println(element);
				});

				// TODO: Handle this part better.
				System.exit(1);
			}

			return tName;
		} catch (Exception e) {
			System.out.println("X Error! X");
			throw e; // TODO: Better exception handling.
		}

	}

	// Collect input from user for the records to add.
	public static Vector<String> promptAddRecords() {

		System.out.print("How many records would you like to add?: (int)\n");
		int numRecords = in.nextInt(); //User input
		in.nextLine(); // Consume remaining input

		System.out.print("** Enter data for each record followed by enter key. **\n");

		Vector<String> recordStrings = new Vector<String>();

		for (int i = 0; i < numRecords; ++i) { //input vector with record data
			String data = in.nextLine();
			recordStrings.add(data);
		}

		// Print data for debug if needed
		logger.fine(recordStrings.toString());

		return recordStrings; 
	}

	
	public static int addRecords(Connection conn, String tableName, Vector<String> records) {

		StringBuilder sqlRecsStr = new StringBuilder();
		int numRecs = records.size();

		for (int i = 0; i < numRecs; ++i) {
			sqlRecsStr.append("( ");
			sqlRecsStr.append(records.get(i));
			sqlRecsStr.append(" ),\n");
		}

		int strSize = sqlRecsStr.length();
		sqlRecsStr.delete(strSize - 2, strSize); // Remove comma and newline at end

		System.out.println(sqlRecsStr);

		int numRecordsAffected = 0;
		try {
			String prepQuery = "INSERT INTO " + tableName + "\n" + "VALUES " + sqlRecsStr + " ;"; // Query statement formatted

			PreparedStatement pStmt = conn.prepareStatement(prepQuery);

			numRecordsAffected = pStmt.executeUpdate();

			System.out.println("Number of records added: " + numRecordsAffected);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1); // TODO: Better error handling
		}

		return numRecordsAffected;

	}

	public static Vector<String> promptUpdateRecords() {

		System.out.print("** Enter the attribute name and new value pairs for update. (attrName=newVal, ...) **\n");

		Vector<String> recordStrings = new Vector<String>();

		String updatePairs = in.nextLine();
		recordStrings.add(updatePairs);

		System.out.print("** Enter the selection condition. **\n");

		String selCondition = in.nextLine();
		recordStrings.add(selCondition);

		// Print data for debug if needed
		logger.fine(recordStrings.toString());

		return recordStrings;
	}

	public static int updateRecords(Connection conn, String tableName, Vector<String> userInput) {

		String updateStr = userInput.get(0);
		String selCondition = userInput.get(1);
		int numRecordsAffected = 0;

		try {
			String prepQuery = "UPDATE " + tableName + "\n" + "SET " + updateStr + "\n" + "WHERE " + selCondition
					+ " ;";

			PreparedStatement pStmt = conn.prepareStatement(prepQuery);

			numRecordsAffected = pStmt.executeUpdate();

			System.out.println("Number of records updated: " + numRecordsAffected);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1); // TODO: Better error handling
		}

		return numRecordsAffected;
	}

	public static String promptDeleteRecords() {

		System.out.print("Please enter the condition for selecting records to delete.\n");
		String delCondition = in.nextLine();

		return delCondition;
	}

	public static int deleteRecords(Connection conn, String tableName, String delCondition) {

		int numRecordsAffected = 0;
		try {
			String prepQuery = "DELETE FROM " + tableName + "\n" + "WHERE " + delCondition + " ;";

			PreparedStatement pStmt = conn.prepareStatement(prepQuery);

			numRecordsAffected = pStmt.executeUpdate();

			System.out.println("Number of records deleted: " + numRecordsAffected);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1); // TODO: Better error handling
		}

		return numRecordsAffected;

	}

	public static String promptSearchRecords() {

		System.out.print("Please enter the condition for selecting records.\n");
		String searchCondition = in.nextLine();

		return searchCondition;
	}

	public static void searchRecords(Connection conn, String tableName, String searchCondition) {
		try {
			String prepQuery = "SELECT *\n" + "FROM " + tableName + "\n" + "WHERE " + searchCondition + " ;";

			PreparedStatement pStmt = conn.prepareStatement(prepQuery);

			ResultSet rst = pStmt.executeQuery();

			System.out.println("** RESULT: ** ");

			printResultSet(rst);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1); // TODO: Better error handling
		}
	}
	
	public static void UsefulReports(Connection conn) {
    	System.out.println("-- End-User Menu --");
		System.out.println(
				"Select Following Report: \n" +
				"	1) Patron Sale\n" +
				"	2) Popular Service\n" +	
				"	3) Popular User\n" +
				"	4) Popular Date For Service\n" +
				"	5) User and Sales\n" +
				"	6) Popular Region"
		);
		int choice = in.nextInt();
		
		switch (choice) {
			case 1:
				//Find total num of diff items purchased/provided by a patron	
				System.out.println("Enter Customer ID: ");
				int CustomerId = in.nextInt();
				try {
					String query = "SELECT COUNT(DISTINCT S.Scooter_ID) \r\n"
							+ "FROM SCOOTER S, PERSON P, RIDE R\r\n"
							+ "WHERE S.Scooter_ID = R.Scooter_ID AND P.ID = R.Customer_ID AND Customer_ID = ?";
					PreparedStatement stmt = conn.prepareStatement(query);
					stmt.setInt(1, CustomerId);
					ResultSet rs = stmt.executeQuery();
					printResultSet(rs);
				} catch (SQLException e) {
		            System.out.println(e.getMessage());
		        }
				break;
			case 2:
				//Find most pop.item/service in DB(one who had most purchases)
				try {
					String query = "SELECT Scooter_ID FROM RIDE GROUP BY Scooter_ID "
							+ "ORDER BY SUM(Fare) desc, COUNT(Scooter_ID) desc limit 1;";
					PreparedStatement stmt = conn.prepareStatement(query);
					ResultSet rs = stmt.executeQuery();
					printResultSet(rs);
				} catch (SQLException e) {
		            System.out.println(e.getMessage());
				}
				break;
			case 3:
				//Pop.user:Find user with most purchase/service in DB
				try {
					String query = "SELECT P.ID, SUM(R.Fare), P.Username FROM PERSON P, RIDE R\r\n"
							+ "WHERE P.ID = R.Customer_ID\r\n"
							+ "GROUP BY P.Username \r\n"
							+ "ORDER BY SUM(R.Fare) desc\r\n"
							+ "limit 1;";
					PreparedStatement stmt = conn.prepareStatement(query);
					ResultSet rs = stmt.executeQuery();
					printResultSet(rs);
				} catch (SQLException e) {
		            System.out.println(e.getMessage());
				}
				break;
			case 4: 
				//Pop.time:Find most pop.date for services in DB & num of instances/count
				try {
					String query = "SELECT Date\r\n"
							+ "FROM RIDE\r\n"
							+ "GROUP BY Date \r\n"
							+ "ORDER BY COUNT(Date) desc\r\n"
							+ "limit 1;";
					PreparedStatement stmt = conn.prepareStatement(query);
					ResultSet rs = stmt.executeQuery();
					printResultSet(rs);
				} catch (SQLException e) {
		            System.out.println(e.getMessage());
				}
				break;
			case 5: 
				//User&sale:Find users w/h highest amount of purchases, how many times, avg dollar, total dollar
				try {
					String query1 = "SELECT COUNT(R.Fare), P.Username\r\n"
							+ "FROM PERSON P, RIDE R\r\n"
							+ "WHERE P.ID = R.Customer_ID\r\n"
							+ "GROUP BY P.Username\r\n"
							+ "ORDER BY MAX(R.Fare) desc\r\n"
							+ "limit 1;";
					PreparedStatement stmt1 = conn.prepareStatement(query1);
					ResultSet rs1 = stmt1.executeQuery();
					printResultSet(rs1);
					String query2 = "SELECT AVG(Fare), SUM(Fare) FROM RIDE";
					PreparedStatement stmt2 = conn.prepareStatement(query2);
					ResultSet rs2 = stmt2.executeQuery();
					printResultSet(rs2);
				} catch (SQLException e) {
	            System.out.println(e.getMessage());
	            }
				break;
			case 6:
				//Popular region: Find most pop.region most scooters are used. 
				try {
					String query = "SELECT Region_ID\r\n"
							+ "FROM SCOOTER\r\n"
							+ "GROUP BY Region_ID\r\n"
							+ "ORDER BY COUNT(Region_ID) desc\r\n"
							+ "limit 1;";
					PreparedStatement stmt = conn.prepareStatement(query);
					ResultSet rs = stmt.executeQuery();
					printResultSet(rs);
				} catch (SQLException e) {
		            System.out.println(e.getMessage());
				}
				break;
		}
    }


	public static void main(String[] args) {
		
		
		
			try {
				Connection conn = initializeDB("data/YUZU.db");
			
				String choice = "Y";
				Scanner in = new Scanner(System.in);
				while (choice.equals("Y")) {
					MainMenuOption selectedOption = promptMainMenu();
					switch (selectedOption) {
						case ADD: 
							String tName = promptTableSelection(conn);
							Vector<String> records = promptAddRecords();
							addRecords(conn, tName, records);
		
							// Test Inputs:
							// 100099,'Andrew','J','Kelly',22,'Male','abcde','fghij','513-687-5746','kelly15@yahoo.com'
							// 100100,'Mason','K','Dempsy',19,'Male','qwerty','asdfgh','513-250-2692','dempsy@gmail.com'
							// 100101,'Jacob','B','Stuart',20,'Male','cuandxl','aiawkgh','614-398-2247','stuuy@outlook.com'
							break;
						
						case UPDATE: 
							tName = promptTableSelection(conn);
							Vector<String> userInput = promptUpdateRecords();
							updateRecords(conn, tName, userInput);
							break;
							// Test Inputs:
							// Age = 25
							// ID = 100040
						
						case DELETE: 
							String tableName = promptTableSelection(conn);
							String delCondition = promptDeleteRecords();
							deleteRecords(conn, tableName, delCondition);
							break;
						
						case SEARCH: 
							tableName = promptTableSelection(conn);
							String selCondition = promptSearchRecords();
							searchRecords(conn, tableName, selCondition);
							break;
						
						case REPORT:
							UsefulReports(conn);
							break;
								
						case EXIT:
							System.out.println("Thank you! See you later.");
							System.exit(0);
							break;
					}
					System.out.println("Choose Option again or quit: (Y/Q) ");
					choice = in.nextLine();
				
				}} catch (Exception e) {
					System.out.println("ERROR");
				}
		}
	
	}
