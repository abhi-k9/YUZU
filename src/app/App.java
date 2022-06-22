package app;


import java.util.HashSet;
import java.util.TreeSet;
import java.util.Scanner;
import java.util.Vector;

import java.sql.*;


public class App {

	// Get logger for this class
	private static java.util.logging.Logger logger =  java.util.logging.Logger.getLogger(App.class.getSimpleName());
	
	private static Scanner in = new Scanner(System.in);
	
	/**
     * Connects to the database if it exists, creates it if it does not, and returns the connection object.
     * 
     * @param databaseFileName the database file name
     * @return a connection object to the designated database
     */
    public static Connection initializeDB(String databaseFileName) {
    	/**
    	 * The "Connection String" or "Connection URL".
    	 * 
    	 * "jdbc:sqlite:" is the "subprotocol".
    	 * (If this were a SQL Server database it would be "jdbc:sqlserver:".)
    	 */
        String url = "jdbc:sqlite:" + databaseFileName;
        Connection conn = null; // If you create this variable inside the Try block it will be out of scope
        try {
            conn = DriverManager.getConnection(url);
            if (conn != null) {
            	// Provides some positive assurance the connection and/or creation was successful.
                System.out.println("The connection to the database was successful.");
            } else {
            	// Provides some feedback in case the connection failed but did not throw an exception.
            	System.out.println("Null Connection");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("There was a problem connecting to the database.");
        }
        return conn;
    }
    
    // Get names of all tables in the database.
    public static HashSet<String> getTableNames(Connection conn) {
    	HashSet<String> tableNames = new HashSet<String>();;
    	
    	try {
	    	DatabaseMetaData meta = conn.getMetaData();
	    	ResultSet tRes =  meta.getTables(null, null, "%", null);
	    	
	    	while (tRes.next()) {
	    		tableNames.add(tRes.getString("TABLE_NAME"));
	    	}
    	}
    	catch (Exception e) {
    		System.out.println(e.getMessage());
    	}
    	
    	return tableNames;
    }
    
    // Get names of all columns in a table.
    public static TreeSet<String> getColumnNames(Connection conn, String tableName) throws Exception { // TODO: Specialize exception thrown
    	
    	// Make sure table name provided exists in the database.
    	if (!getTableNames(conn).contains(tableName)) {
    		System.out.println("X Given table name does not exist in the database. X"); // TODO: Better error handling.
    		System.exit(1);
    	}
    	
    	// TODO: Figure out a better way without potentially retrieving the whole table.
    	ResultSet rst = null;
    	try {
	    	String prepQuery = "SELECT *\n" +
					   		   "FROM ?\n" +
					   		   "WHERE FALSE;";
			
	    	PreparedStatement pStmt=  conn.prepareStatement(prepQuery);
	    	
	    	pStmt.setString(1, tableName); // add in the table name
	    	
	    	rst = pStmt.executeQuery();

    	}
    	catch (Exception e) {
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
    	}
    	catch (Exception e) {
    		System.out.println(e.getMessage());
    	}
    	
    	return colNames;
    }
    
    // Get type of data in a column for a query result set.
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
    	}
    	catch (Exception e) {
    		System.out.println(e.getMessage());
    	}
    	
    	return colTypes;
    }
    
    // All possible main menu options.
	enum MainMenuOption {
		ADD,
		UPDATE,
		DELETE,
		SEARCH,
		REPORT,
		EXIT
	};
	
	// Prompt user with a main menu selection.
	public static MainMenuOption promptMainMenu() {
				
		// Prompt user with a main menu
		boolean isValid = false;
		int choice = 0;
		while(!isValid) {
			// Show main menu
			System.out.println("-- Main Menu --");
			System.out.println(
					"Select an option: (int)\n" +
					"	1) Add Record(s)\n" +
					"	2) Update Record(s)\n" +	
					"	3) Delete Record(s)\n" +
					"	4) Search Record(s)\n" +
					"   	*) Exit\n"
			);
			
			// Read integer input.
			try {
				choice = in.nextInt();
				isValid = true;
			}
			catch (java.util.InputMismatchException ie) {
				// restart if not a valid integer.
				System.out.println("X Please enter an integer! X");
				in.next(); // Read any pending input
			}
			
		}
		
		switch (choice) {
		
		// Add record
		case 1:
			return MainMenuOption.ADD;
			//break;
		
		// Update record
		case 2:
			return MainMenuOption.UPDATE;
			//break;
		
		case 3:
			return MainMenuOption.DELETE;
			//break;
		
		case 4:
			return MainMenuOption.SEARCH;
			//break;
			
		default:
			return MainMenuOption.EXIT;
			//break;
		}
		
	}

	// Prompt user to get the name of table to modify and check for its validity
	public static String promptTableSelection(Connection conn) {
		
		System.out.println("Which table do you want to address?: (string)");
		
		try {
			String tName = in.next();

	    	// Make sure table name provided exists in the database.
	    	if (!getTableNames(conn).contains(tName)) {
	    		System.out.println("X Given table name does not exist in the database. X"); // TODO: Better error handling.
	    		System.exit(1);
	    	}

	    	return tName;
		}
		catch (Exception e){
			System.out.println("X Error! X");
			throw e; // TODO: Better exception handling.
		}
		
	}
	
	// Collect input from user for the records to add.
	public static TreeSet<String> promptAddRecords() {
		Scanner in = new Scanner(System.in);
		
		System.out.print("How many records to add data?: (int)\n");
		int numRecords = in.nextInt();
		
		System.out.println("** Enter data for each record followed by enter key. **\n");
		
		TreeSet<String> recordStrings = new TreeSet<String>(); // TODO: should change to DB
		for (int r = 0; r < numRecords; ++r ) {
			String data = in.nextLine();
			recordStrings.add(data);
		}
		
		in.close();
		
		// Print data for debug if needed
		logger.fine(recordStrings.toString());
		
		
		return recordStrings;
	}
	
	// Add given record strings to the database.
	public static void addRecords(Connection conn, String tableName, TreeSet<String> recsToAdd) {
// 		System.out.println("Do you want to add data? (Y/N): ");
// 		String option = in.nextLine(); //so need another 
// 		System.out.println("option: " + option);
// 		while (!option.equals("N")) {
// 			System.out.print("Enter the data: ");
// 			String data = in.nextLine();
// 			s.add(data);
// 			System.out.println("Do you want to add data? (Y/N): ");
// 			option = in.nextLine();
// 		}
// 		System.out.println(s);
	}
	
	public static void updateRecords() {
		Scanner in = new Scanner(System.in);
		
//		sArray.addAll(s); //transfer set data to arrayList
//		System.out.println(sArray);
//		System.out.print("Do you want to update data? (Y/N): ");
//		option = in.nextLine();
//		System.out.println("Select which element to update?: ");
//		int index = in.nextInt();
//		System.out.println("what data to update?: ");
//		String element = in.nextLine();
//		while (!sArray.isEmpty() && !option.equals("N")) {
//			System.out.println("Enter element to update?: ");
// 			String element = in.nextLine();
// 			int index = sArray.indexOf(element);
// 			System.out.println("Enter to-be update element: ");
// 			String updateE = in.nextLine();
// 			sArray.set(index, updateE);
// 			System.out.println("Do you want to update data? (Y/N): ");
// 			option = in.nextLine();
//		}
//		if (sArray.isEmpty()) {
// 			System.out.println("Set is empty, update option is unavailable");
// 		} else {
// 			System.out.println(sArray);
// 		}
//		
//		System.out.print("How many records to add data?: (int)\n");
//		int numRecords = in.nextInt();
//		
//		System.out.println("** Enter data for each record followed by enter key. **\n");
//		
//		Set<String> recordStrings = new HashSet<String>(); // TODO: should change to DB
//		for (int r = 0; r < numRecords; ++r ) {
//			String data = in.nextLine();
//			recordStrings.add(data);
//		}
//		
		in.close();
//		
//		// Print data for debug if needed
//		logger.fine(recordStrings.toString());;
	}
	
	
	public static void deleteRecords() { 
	//		System.out.println("Do you want to add data? (Y/N): ");
	//		String option = in.nextLine();
	//		while (!s.isEmpty() && !option.equals("N")) {
	//			System.out.print("Enter the data to be removed: ");
	//			String data = in.nextLine();
	//			s.remove(data);
	//			System.out.print("Do you want to add data? (Y/N): ");
	//			option = in.nextLine();
	//		}
	//		if (s.isEmpty()) {
// 				System.out.println("Set is empty, delete option is unavailable");
// 			} else {
// 				System.out.println(s);
// 			}
			
		}
	
	
	public static void searchRecords() { 
//		System.out.print("Search the data? (Y/N): ");
//		option = in.nextLine();
//		while (!option.equals("N")) {
//			System.out.print("Enter the data to search: ");
//			String data = in.nextLine();
//			 System.out.println("Does the Set contains the data? "
//                     + s.contains(data));
//			System.out.print("Search the data? (Y/N): ");
//			option = in.nextLine();
//		}
	
	}
	
	
	public static void main(String[] args) {

		Connection conn = initializeDB("data/YUZU.db");
		
		MainMenuOption selectedOption = promptMainMenu();
		
		switch (selectedOption) {
		case ADD:
			String tName = promptTableSelection(conn);
			TreeSet<String> recordsToAdd = promptAddRecords();
			addRecords(conn, tName, recordsToAdd);
			
			break;
		case UPDATE:
			break;
		case DELETE:
			break;
		case SEARCH:
			break;
		case REPORT:
			break;
		case EXIT:
			break;
		}
		
	}

}
