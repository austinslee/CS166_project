/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


//I import more
import java.time.LocalDate;
import java.lang.Object;
import java.time.format.DateTimeFormatter;
import java.text.*;
import java.util.Date;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		while(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Doctor");
				System.out.println("2. Add Patient");
				System.out.println("3. Add Appointment");
				System.out.println("4. Make an Appointment");
				System.out.println("5. List appointments of a given doctor");
				System.out.println("6. List all available appointments of a given department");
				System.out.println("7. List total number of different types of appointments per doctor in descending order");
				System.out.println("8. Find total number of patients per doctor with a given status");
				System.out.println("9. < EXIT");
				
				switch (readChoice()){
					case 1: AddDoctor(esql); break;
					case 2: AddPatient(esql); break;
					case 3: AddAppointment(esql); break;
					case 4: MakeAppointment(esql); break;
					case 5: ListAppointmentsOfDoctor(esql); break;
					case 6: ListAvailableAppointmentsOfDepartment(esql); break;
					case 7: ListStatusNumberOfAppointmentsPerDoctor(esql); break;
					case 8: FindPatientsCountWithStatus(esql); break;
					case 9: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddDoctor(DBproject esql) {//1
		try {

			int rowCount = esql.executeQuery("SELECT * FROM Doctor");
			
         		String query = "INSERT INTO Doctor VALUES (" + rowCount + ",\'";
			
         		System.out.print("\tEnter name (MAX: 128 CHAR): ");
         		String input = in.readLine();
         		query += input;
	 		query += "\',\'";

			if (input.length() > 128) {
				throw new SQLException("Name exceeds CHAR limit");
			}
			
			System.out.print("\tEnter specialty (MAX: 24 CHAR): ");
         		input = in.readLine();
         		query += input;
	 		query += "\',";

			if (input.length() > 24) {
                                throw new SQLException("Specialty exceeds CHAR limit");
                        }
			
			System.out.print("\tEnter did: ");
         		input = in.readLine();
         		query += input;
	 		query += ");";

			List<List<String>> result = esql.executeQueryAndReturnResult("SELECT * FROM Department WHERE dept_ID = \'" + input + "\';");
			if (result.isEmpty()) {
				throw new SQLException("Invalid department");
			}		

         		esql.executeUpdate(query);
      		}
		catch(Exception e) {
         		System.err.println ("ERROR: " + e.getMessage());
       		}
	}

	public static void AddPatient(DBproject esql) {//2
		try {
			int rowCount = esql.executeQuery("SELECT * FROM Patient");
			
         		String query = "INSERT INTO Patient VALUES (" + rowCount + ",\'";
			
         		System.out.print("\tEnter name (MAX: 128 CHAR): ");
         		String input = in.readLine();
         		query += input;
	 		query += "\',\'";
		
			if (input.length() > 128) {
                                throw new SQLException("Name exceeds CHAR limit");
                        }
	
			System.out.print("\tEnter gender (M/F): ");
         		input = in.readLine();
			input = input.toUpperCase();
         		query += input;
	 		query += "\',";

			if (!input.equals("M") && !input.equals("F")) {
				throw new SQLException("Invalid gender");
			}
			
			System.out.print("\tEnter age: ");
         		input = in.readLine();
         		query += input;
	 		query += ",\'";

			int i = Integer.parseInt(input);
			
			System.out.print("\tEnter address (MAX: 256 CHAR): ");
         		input = in.readLine();
         		query += input;
	 		query += "\',";
			
			if (input.length() > 256) {
                                throw new SQLException("Address exceeds CHAR limit");
                        }

			System.out.print("\tEnter number of appointments: ");
         		input = in.readLine();
         		query += input;
	 		query += ");";

			i = Integer.parseInt(input);

			esql.executeUpdate(query);
      		}
		catch(NumberFormatException e) {
			System.out.println("ERROR: Input must be INTEGER");
		}
		catch(Exception e) {
         		System.err.println ("ERROR: " + e.getMessage());
       		}
	}

	public static void AddAppointment(DBproject esql) {//3
		try {
			DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd/yyyy");	
			
			int rowCount = esql.executeQuery("SELECT * FROM Appointment");
			
         		String query = "INSERT INTO Appointment VALUES (" + rowCount + ",\'";
			
         		System.out.print("\tEnter date: ");
         		String input = in.readLine();
			LocalDate localadd = LocalDate.parse(input, format);
         		query += input;
	 		query += "\',\'";
			
			System.out.print("\tEnter timeslot: ");
         		input = in.readLine();
			Date date1 = new SimpleDateFormat("HH:mm-HH:mm").parse(input);
         		query += input;
	 		query += "\',\'";
			
			System.out.print("\tEnter status: ");
         		input = in.readLine();
			input = input.toUpperCase();
         		query += input;
	 		query += "\');";

			if (!input.equals("WL") && !input.equals("AV") && !input.equals("AC") && !input.equals("PA")) {
                                throw new SQLException("Invalid status");
                        }

         		esql.executeUpdate(query);
      		}
		catch(Exception e) {
         		System.err.println ("ERROR: " + e.getMessage());
       		}
	}


	public static void MakeAppointment(DBproject esql) {//4
		try {
			
         		System.out.print("\tEnter patient name: ");
         		String pname = in.readLine();

			if (pname.length() > 128) {
                                throw new SQLException("Name exceeds CHAR limit");
                        }

			System.out.print("\tEnter patient gender: ");
                        String pgender = in.readLine();
			pgender = pgender.toUpperCase();

			if (!pgender.equals("M") && !pgender.equals("F")) {
                                throw new SQLException("Invalid gender");
                        }

			System.out.print("\tEnter patient age: ");
                        String page = in.readLine();
			
			int i = Integer.parseInt(page);
	
			System.out.print("\tEnter patient address: ");
                        String paddress = in.readLine();

			if (paddress.length() > 256) {
                                throw new SQLException("Address exceeds CHAR limit");
                        }

			List<List<String>> result = esql.executeQueryAndReturnResult("SELECT patient_ID FROM Patient WHERE name=\'" + pname + "\' AND gtype=\'" + pgender + "\' AND age=" + page + " AND address=\'" + paddress + "\';");
			
			int p_id = esql.executeQuery("SELECT * FROM Patient");
			String pid = "" + p_id + "";

			if (result.isEmpty()) {
				String new_patient = "INSERT INTO Patient VALUES (" + p_id + ",\'" + pname + "\',\'" + pgender + "\'," + page + ",\'" + paddress + "\',0);";
                       		esql.executeUpdate(new_patient);
                        }
			else {
				pid = result.get(0).get(0);
			}
			
			System.out.print("\tEnter doctor: ");
         		String doct_id = in.readLine();
			result = esql.executeQueryAndReturnResult("SELECT * FROM Doctor WHERE doctor_ID = \'" + doct_id + "\';");
                        if (result.isEmpty()) {
                                throw new SQLException("Invalid doctor");
                        }
	
			System.out.print("\tEnter appointment: ");
         		String aid = in.readLine();
			result = esql.executeQueryAndReturnResult("SELECT * FROM Appointment WHERE appnt_ID = \'" + aid + "\';");
                        if (result.isEmpty()) {
                                throw new SQLException("Invalid appointment");
                        }

			result = esql.executeQueryAndReturnResult("SELECT did FROM Doctor WHERE doctor_ID = \'" + doct_id + "\';");
                        String did = (result.get(0).get(0));
                        result = esql.executeQueryAndReturnResult("SELECT hid FROM Department WHERE dept_ID = \'" + did + "\';");
                        String hid = (result.get(0).get(0));
	
			result = esql.executeQueryAndReturnResult("SELECT status FROM Appointment WHERE appnt_ID = \'" + aid + "\';");
			String str = result.get(0).get(0);
			if (str.equals("AV")) {
				String query = "UPDATE Appointment SET status = \'AC\' WHERE appnt_ID=" + aid + ";";
				System.out.println("Appointment status: " + str + " -> AC");
			}
			else if (str.equals("AC")) {
				String query = "UPDATE Appointment SET status = \'WL\' WHERE appnt_ID=" + aid + ";";
                                System.out.println("Appointment status: " + str + " -> WL");
			}
			else {
				System.out.println("Appointment status: " + str);
			}

			if (!str.equals("PA")) {
				
				String query = "INSERT INTO searches VALUES (" + hid + "," + pid + "," + aid + ");";
                        	// query += "INSERT INTO schedules VALUES (" + aid + "," + sid + ");";
                        	query += "INSERT INTO has_appointment VALUES (" + aid + "," + doct_id + ");";

         			esql.executeUpdate(query);

				query = "SELECT number_of_appts FROM Patient WHERE patient_ID=" + pid + ";";
				result = esql.executeQueryAndReturnResult(query);
				str = result.get(0).get(0);
				int num_appt = Integer.parseInt(str);
				num_appt++;
				query = "UPDATE Patient SET number_of_appts=" + num_appt + " WHERE patient_ID=" + pid + ";";
				
				esql.executeUpdate(query);
			}
      		}
		catch(NumberFormatException e) {
                        System.out.println("ERROR: Input must be INTEGER");
                }
		catch(Exception e) {
         		System.err.println ("ERROR: " + e.getMessage());
       		}
	}

	public static void ListAppointmentsOfDoctor(DBproject esql) {//5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor
//		try{

		int dID;
		String startDate;
		String endDate;
		Date sDate;
		Date eDate;
		DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		SimpleDateFormat sdformat = new SimpleDateFormat("MM/dd/yyyy");
		while(true) {
			try {	
				System.out.println("Enter doctor ID:");
				dID = Integer.parseInt(in.readLine());
				if (dID < 0) {
					throw new RuntimeException("ERROR: Doctor ID can't be less than 0");
				}
				break;
			} catch (Exception e) {
				System.out.println("ERROR: Invalid input for doctor ID. " + e);
				continue;
			}
		}
		while(true) {
			try {
				System.out.println("Enter starting date(MM/DD/YYYY):");
				startDate = in.readLine();
				sDate = sdformat.parse(startDate);
				LocalDate localadd = LocalDate.parse(startDate, format);
				break;
			} catch (Exception e) {
				System.out.println("ERROR: Invalid input for starting date. " + e);
				continue;
			}
		}
		while(true) {
			try {
				System.out.println("Enter ending date(MM/DD/YYYY):");
				endDate = in.readLine();
				eDate = sdformat.parse(endDate);
				LocalDate localaad = LocalDate.parse(endDate, format);
				if(eDate.compareTo(sDate) < 0) {
					throw new RuntimeException("ERROR: Ending date can't be less than starting date");
				}
				
				break;
			} catch (Exception e) {
				System.out.println("ERROR: Invalid input for ending date. " + e);
				continue;
			}
		}
			try {
				String query = "SELECT D.name, D.doctor_ID, A.appnt_ID, A.adate, A.time_slot, A.status FROM Appointment A, Doctor D, has_appointment HA WHERE HA.appt_id = A.appnt_ID AND (A.status = 'AC' OR A.status = 'AV') AND HA.doctor_id = D.doctor_ID AND D.doctor_ID = ";
	                        query += dID + " AND A.adate >= \'";
				query += startDate + "\' AND A.adate <= \'";
				query += endDate + "\';";

				
				
				esql.executeQueryAndPrintResult(query);
			} catch (Exception e) {
				System.out.println("ERROR: Query failed to execute. " + e);
			}
			
/*		} catch (Exception e) {
			System.err.println(e.getMessage());
		}*/
	}

	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
		// For a department name and a specific date, find the list of available appointments of the department
		

		String deptName;
		String Date;	
		DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd/yyyy");	


                        String query = "SELECT A.appnt_ID, A.adate, A.time_slot, A.status FROM Appointment A, Doctor D, has_appointment HA, Department DE  WHERE HA.appt_id = A.appnt_ID AND HA.doctor_id = D.doctor_ID AND D.did = DE.dept_ID AND A.status = 'AV' AND DE.name = '";
		while(true) {
                        try {
                                System.out.println("Enter department name:");
				deptName = in.readLine();
                                break;
                        } catch (Exception e) {
                                System.out.println("ERROR: Invalid input for department Name. " + e);
                                continue;
                        }
                }
                while(true) {
                        try {
                                System.out.println("Enter date(MM/DD/YYYY):");
                                Date = in.readLine();
                                LocalDate localadd = LocalDate.parse(Date, format);
                                break;
                        } catch (Exception e) {
                                System.out.println("ERROR: Invalid input for date. " + e);
                                continue;
                        }
                }

		try {
			query += deptName + "' AND A.adate = '";
                        query += Date + "\';";
                        esql.executeQueryAndPrintResult(query);
		} catch (Exception e) {
			System.out.println("ERROR: Query failed to execute. " + e);
		}


	}

	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
		// Count number of different types of appointments per doctors and list them in descending order
		try { 
			String query = "SELECT D.doctor_ID, D.name, A.status, COUNT(*) FROM Appointment A, Doctor D, has_appointment HA WHERE HA.appt_id = A.appnt_ID AND HA.doctor_id = D.doctor_ID GROUP BY D.doctor_ID, D.name, A.status ORDER BY COUNT(*) DESC;";

			esql.executeQueryAndPrintResult(query);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}


	}

	
	public static void FindPatientsCountWithStatus(DBproject esql) {//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.
		String status;
		String PA = "PA";
		String AC = "AC";
		String AV = "AV";
		String WL = "WL";
		while(true) {			
			try {
                	        System.out.println("Enter status(PA, AC, AV, WL):");
                        	status = in.readLine();
				if(status.equals(PA) || status.equals(AC) || status.equals(AV) || status.equals(WL)) {
					break;
				}
				throw new RuntimeException("Please type PA, AC, AV, or WL");				
			} catch (Exception e) {
				System.out.println("ERROR: Invalid input for status. " + e);
			}
		}
			try {
				String query = "SELECT DISTINCT D.doctor_ID, D.name, COUNT(P.patient_ID) FROM Appointment A, Doctor D, has_appointment HA, Patient P, searches S WHERE HA.appt_id = A.appnt_ID AND HA.doctor_id = D.doctor_ID AND S.pid = P.patient_ID AND S.aid = A.appnt_ID AND A.status = '";			
                        	query += status + "' GROUP BY D.doctor_ID, D.name;";
	                        esql.executeQueryAndPrintResult(query);
			} catch (Exception e) {
				System.out.println("ERROR: Query failed to execute. " + e);
			}
	}
}
