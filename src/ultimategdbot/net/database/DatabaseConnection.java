package ultimategdbot.net.database;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * This class is used to get an instance of the connection to the database using the Singleton Pattern
 * @author Alex1304
 *
 */
public class DatabaseConnection {
	
	private static Connection conn = createInstance();
	
	public static Connection getInstance() {
		return conn;
	}
	
	private static Connection createInstance() {
		try {
			conn = DriverManager.getConnection("jdbc:mysql://mysql-alex1304.alwaysdata.net/alex1304_ultimategdbot",
					System.getenv().get("DB_USERNAME"), System.getenv().get("DB_PASSWORD"));
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
