/* Renoverb is a program aimed at theater engineers that predicts the effect
   of sound absorbing materials on the reverberation of a room.
   Copyright 2008: Anthony Heathcoat, Nicholas Roth, Jim Simon, Yusuke Hasegawa

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.LinkedList;

public class CalcSQL {

	private Connection con = null;
	private String url = "jdbc:sqlite:materials.db";
	private String allMaterials = 
		"SELECT " +
			"MID" +
			", name" +
			", manufacturer" +
			", absorp_32Hz" +
			", absorp_65Hz" +
			", absorp_125Hz" +
			", absorp_250Hz" +
			", absorp_500Hz" +
			", absorp_1000Hz" +
			", absorp_2000Hz" +
			", absorp_4000Hz" +
			", absorp_8000Hz" +
		" FROM " +
			"material";

	private String insert = "INSERT INTO material (name,manufacturer,absorp_32Hz,absorp_65Hz,absorp_125Hz,absorp_250Hz,absorp_500Hz,absorp_1000Hz,absorp_2000Hz,absorp_4000Hz,absorp_8000Hz) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
	private String getManufacts = "SELECT DISTINCT manufacturer FROM material";
	private String searchName = null;
	private String searchManufacturer = null;
	private HashMap<Integer, Double[]> minMaxes = new HashMap<Integer, Double[]>();
	
	private String create_table = "CREATE TABLE IF NOT EXISTS material"
		+" (MID INTEGER constraint pk_MID primary key AUTOINCREMENT,"
        +"name TEXT NOT NULL,"
        +"manufacturer TEXT default None,"
        +"absorp_32Hz REAL(1,3) default NULL,"
        +"absorp_65Hz REAL(1,3) default NULL,"
        +"absorp_125Hz REAL(1,3) default NULL,"
        +"absorp_250Hz REAL(1,3) default NULL,"
        +"absorp_500Hz REAL(1,3) default NULL,"
        +"absorp_1000Hz REAL(1,3) default NULL,"
        +"absorp_2000Hz REAL(1,3) default NULL,"
        +"absorp_4000Hz REAL(1,3) default NULL,"
        +"absorp_8000Hz REAL(1,3) default NULL"
        +");";

	public CalcSQL() throws SQLException{
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			System.out.println("Failed to find database drivers.");
		}
		
		con = DriverManager.getConnection(url);
		try {
			Statement stmt = con.createStatement();
			stmt.execute(create_table);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
	}
	
	public CalcSQL(String databasePathName) throws SQLException{
		url = "jdbc:sqlite:"+databasePathName;
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			System.out.println("Failed to find database drivers.");
		}
		
		con = DriverManager.getConnection(url);
		try {
			Statement stmt = con.createStatement();
			stmt.execute(create_table);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
	}
	

	public void addMaterial(Material m) {
		openConnection();
		try {
			PreparedStatement stmt = con.prepareStatement(insert);
			stmt.setString(1, m.name);
			stmt.setString(2, m.manufacturer);
			stmt.setDouble(3, m._32Hz);
			stmt.setDouble(4, m._65Hz);
			stmt.setDouble(5, m._125Hz);
			stmt.setDouble(6, m._250Hz);
			stmt.setDouble(7, m._500Hz);
			stmt.setDouble(8, m._1kHz);
			stmt.setDouble(9, m._2kHz);
			stmt.setDouble(10, m._4kHz);
			stmt.setDouble(11, m._8kHz);
			
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		closeConnection();
	}
	
	public void importFromDatabase(String databasePath) throws SQLException {
		CalcSQL fromDB = new CalcSQL(databasePath);
		Vector<Material> mats = fromDB.getAllMaterials();
		for (Iterator<Material> iterator = mats.iterator(); iterator.hasNext();) {
			Material material = iterator.next();
			addMaterial(material);
		}
	}
	
	public void importFromFile(String filepath) throws NumberFormatException, IOException{
		LinkedList<Material> mats = Parser.parse(filepath);
		for (Iterator<Material> iterator = mats.iterator(); iterator.hasNext();) {
			Material material = iterator.next();
			addMaterial(material);
		}
	}
	
	public void importFromFile(File file) throws NumberFormatException, IOException{
		LinkedList<Material> mats = Parser.parse(file);
		for (Iterator<Material> iterator = mats.iterator(); iterator.hasNext();) {
			Material material = iterator.next();
			addMaterial(material);
		}
	}
	
	public void exportToDatabase(String databasePath) throws SQLException {
		CalcSQL toDB = new CalcSQL(databasePath);
		Vector<Material> mats = getAllMaterials();
		for (Iterator<Material> iterator = mats.iterator(); iterator.hasNext();) {
			Material material = iterator.next();
			toDB.addMaterial(material);
		}
	}

	public LinkedList<Material> customSearch() {
		LinkedList<Material> materials = new LinkedList<Material>();

		openConnection();

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(createCustomSearchSQL());

			while (rs.next()) {
				//get columns

				String returnedName = rs.getString("name");
				String returnedManufacturer = rs.getString("manufacturer");
				double _32Hz  = rs.getDouble("absorp_32Hz" );
				double _65Hz  = rs.getDouble("absorp_65Hz" );
				double _125Hz = rs.getDouble("absorp_125Hz");
				double _250Hz = rs.getDouble("absorp_250Hz");
				double _500Hz = rs.getDouble("absorp_500Hz");
				double _1kHz  = rs.getDouble("absorp_1000Hz" );
				double _2kHz  = rs.getDouble("absorp_2000Hz" );
				double _4kHz  = rs.getDouble("absorp_4000Hz" );
				double _8kHz  = rs.getDouble("absorp_8000Hz" );
				int db_key 	  = rs.getInt("MID"); 
				
				if( (searchName==null || searchName.length()==0 || returnedName.contains(searchName)) && (searchManufacturer==null || searchManufacturer.length()==0 || returnedManufacturer.contains(searchManufacturer)) )
					materials.add(new Material(returnedName,returnedManufacturer,_32Hz,_65Hz,_125Hz,_250Hz,_500Hz,_1kHz,_2kHz,_4kHz,_8kHz,db_key));	
			}
			
			stmt.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		closeConnection();
		resetCustomSearch();
		return materials;
	}

	public Vector<Material> getAllMaterials() {
		Vector<Material> materials = new Vector<Material>();

		openConnection();

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(allMaterials);

			while (rs.next()) {
				materials.add(new Material(
						rs.getString("name"),
						rs.getString("manufacturer"),
						rs.getDouble("absorp_32Hz"),
						rs.getDouble("absorp_65Hz"),
						rs.getDouble("absorp_125Hz"),
						rs.getDouble("absorp_250Hz"),
						rs.getDouble("absorp_500Hz"),
						rs.getDouble("absorp_1000Hz"),
						rs.getDouble("absorp_2000Hz"),
						rs.getDouble("absorp_4000Hz"),
						rs.getDouble("absorp_8000Hz"),
						rs.getInt("MID")));
			}
			stmt.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		closeConnection();

		return materials;
	}

	public Vector<String> getManufacturers() {

		Vector<String> manufacts = new Vector<String>();

		openConnection();

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(getManufacts);

			while (rs.next()) {
				manufacts.add(rs.getString("manufacturer"));
			}
			
			stmt.close();
			rs.close();
		} catch (SQLException e){
			e.printStackTrace();
		}

		closeConnection();

		return manufacts;
	}
	
	public void setRange(int freq, double min, double max) {
		if (!minMaxes.containsKey(freq)) {
			Double[] mm = {min,max};
			minMaxes.put(freq, mm);
		}
	}

	public void setSearchManufacturer(String manufacturer) {
		searchManufacturer = manufacturer;
	}

	public void setSearchName(String name) {
		searchName = name;
	}

	private void closeConnection() {		
		try {
			if (con != null) {
				con.close();
				con = null;
			}
		} catch(SQLException e) {
			System.out.println("Error when closing the connection to the database.");
		}

	}

	private String createCustomSearchSQL() {
		
		if (minMaxes.size() > 0) {
			String custom = allMaterials + " WHERE";
			Set<Integer> keys = minMaxes.keySet();
			String and = "";
			for (Iterator<Integer> iterator = keys.iterator(); iterator.hasNext();) {
				Integer integer = iterator.next();
				Double[] mm = minMaxes.get(integer);
				switch (integer) {
					case 32:
						custom += (and + " absorp_32Hz BETWEEN '" + mm[0] + "' AND '" + mm[1] + "'");
						and = " AND ";
						break;
					case 65:
						custom += (and + " absorp_65Hz BETWEEN '" + mm[0] + "' AND '" + mm[1] + "'");
						and = " AND ";
						break;
					case 125:
						custom += (and + " absorp_125Hz BETWEEN '" + mm[0] + "' AND '" + mm[1] + "'");
						and = " AND ";
						break;
					case 250:
						custom += (and + " absorp_250Hz BETWEEN '" + mm[0] + "' AND '" + mm[1] + "'");
						and = " AND ";
						break;
					case 500:
						custom += (and + " absorp_500Hz BETWEEN '" + mm[0] + "' AND '" + mm[1] + "'");
						and = " AND ";
						break;
					case 1000:
						custom += (and + " absorp_1000Hz BETWEEN '" + mm[0] + "' AND '" + mm[1] + "'");
						and = " AND ";
						break;
					case 2000:
						custom += (and + " absorp_2000Hz BETWEEN '" + mm[0] + "' AND '" + mm[1] + "'");
						and = " AND ";
						break;
					case 4000:
						custom += (and + " absorp_4000Hz BETWEEN '" + mm[0] + "' AND '" + mm[1] + "'");
						and = " AND ";
						break;
					case 8000:
						custom += (and + " absorp_8000Hz BETWEEN '" + mm[0] + "' AND '" + mm[1] + "'");
						and = " AND ";
						break;
					default:break;
				}
			}
			return custom;
		}
		else {
			return allMaterials;
		}
	}
	
	public void removeMaterial(int db_key) {
		openConnection();
		
		try {
			String remove = "DELETE FROM material WHERE MID=?";
			PreparedStatement stmt = con.prepareStatement(remove);
			stmt.setInt(1, db_key);
			stmt.executeUpdate();
			stmt.close();
			
		} catch (SQLException e) {
			
		}
		
		closeConnection();
	}

	private void openConnection() {
		closeConnection();
		try {
			con = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println("Failed to connect to the database.");
		}
	}

	private void resetCustomSearch() {
		searchName = null;
		searchManufacturer = null;
		minMaxes.clear();
	}
}
