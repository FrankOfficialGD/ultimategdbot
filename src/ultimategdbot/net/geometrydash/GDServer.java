package ultimategdbot.net.geometrydash;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Connection with Geometry Dash official servers is managed here
 * Contains utility levels to interact with the game
 * 
 * @author Alex1304
 *
 */
public class GDServer {

	private static final String gdservURLPrefix = "http://www.boomlings.com/database/";
	
	/**
	 * Submits a POST request to the specified page on the GD server and returns the result as a String
	 * 
	 * @param webpage relative URL to the page
	 * @param urlParams Content of the POST request
	 * @return server response as String
	 */
	public static String sendRequest(String webpage, String urlParams) throws IOException {
			HttpURLConnection con;
			con = (HttpURLConnection) new URL(gdservURLPrefix + webpage).openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);

			// Sending the request to the server
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParams);
			wr.flush();
			wr.close();

			// Fetching response
			String result = "";
			BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result += line + "\n";
			}

			return result;
	}
	
	/**
	 * Fetches the latest awarded levels
	 * @return server response as String
	 * @throws IOException 
	 */
	public static String fetchNewAwardedLevels() throws IOException {
		return sendRequest("getGJLevels21.php",
				"gameVersion=21&binaryVersion=33&gdw=0&type=11&str=&diff=-&len=-&page=0&total=0"
				+ "&uncompleted=0&onlyCompleted=0&featured=0&original=0&twoPlayer=0&coins=0&epic=0"
				+ "&secret=Wmfd2893gb7");
	}
	
	/**
	 * Fetches the latest uploaded levels
	 * @return server response as String
	 * @throws IOException 
	 */
	public static String fetchMostRecentLevels() throws IOException {
		return sendRequest("getGJLevels21.php",
				"gameVersion=21&binaryVersion=33&gdw=0&type=4&str=&diff=-&len=-&page=0&total=9999"
				+ "&uncompleted=0&onlyCompleted=0&featured=0&original=0&twoPlayer=0&coins=0&epic=0"
				+ "&secret=Wmfd2893gb7");
	}
	
	/**
	 * Fetches the first level corresponding to the given name
	 * @return server response as String
	 * @throws IOException 
	 */
	public static String fetchLevelByNameOrID(String levelNameOrID) throws IOException {
		return sendRequest("getGJLevels21.php",
				"gameVersion=21&binaryVersion=33&gdw=0&type=0&str=" + URLEncoder.encode(levelNameOrID, "UTF-8") + "&diff=-&len=-&page=0&total=0"
				+ "&uncompleted=0&onlyCompleted=0&featured=0&original=0&twoPlayer=0&coins=0&epic=0"
				+ "&secret=Wmfd2893gb7");
	}
}
