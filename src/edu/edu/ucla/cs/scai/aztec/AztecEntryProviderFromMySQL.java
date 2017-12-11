package edu.ucla.cs.scai.aztec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class AztecEntryProviderFromMySQL implements AztecEntryProvider {

    String url, username, password;
    Connection conn = null;

    static {
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
        }
    }

    public AztecEntryProviderFromMySQL(String url, String port, String username, String password) throws SQLException {
        String connectionString = "jdbc:mysql://" + url + ":" + port + "?" + "user=" + username + "&password=" + password;
        conn = DriverManager.getConnection(connectionString);
    }

    @Override
    public ArrayList<AztecEntry> load() throws Exception {
        Statement stmt = null;
        ResultSet rs = null;
        HashMap<String, AztecEntry> res = new HashMap<>();
        try {
            stmt = conn.createStatement();

            //load basic data from the main table
            rs = stmt.executeQuery("select * from AZ_Curation.TOOL_INFO;");
            while (rs.next()) {
                AztecEntry e = new AztecEntry();
                String id = rs.getString("AZID");
                res.put(id, e);
                e.setId(id);
                e.setSource(rs.getString("SOURCE"));
                //SOURCE ID not used
                e.setName(rs.getString("NAME"));
                e.setLogo(rs.getString("LOGO_LINK"));
                e.setDescription(rs.getString("DESCRIPTION"));
                e.setSourceCodeURL(rs.getString("SOURCE_LINK"));
                e.setDateCreated(rs.getDate("SUBMIT_DATE"));
                e.setDateUpdated(new Date()); //the update field is missing in the DB - we assume all the
            }

            //load additional data from related tables
            //load tags
            rs = stmt.executeQuery("select AZ_Curation.TOOL_TAG.AZID AS AZID, AZ_Curation.TAG.NAME AS NAME from AZ_Curation.TOOL_TAG AS TOOL_TAG, AZ_Curation.TAG AS TAG WHERE TOOL_TAG.TAG_ID=TAG.TAG_ID;");
            while (rs.next()) {
                String id = rs.getString("AZID");
                AztecEntry e = res.get(id);
                String tagName = rs.getString("NAME");
                if (tagName != null && tagName.trim().length() > 0) {
                    if (e.getTags() == null) {
                        e.setTags(new ArrayList<String>());
                    }
                    System.out.println(e.getId() + " -> " + tagName);
                    e.getTags().add(tagName.trim());
                }
            }

        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore

                stmt = null;
            }
        }
        /*
         StringBuilder json = new StringBuilder();
         try (BufferedReader in = new BufferedReader(new FileReader(fileName))) {
         String l;
         while ((l = in.readLine()) != null) {
         json.append(l).append(" ");
         }
         }
         Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
         EntryWrapper1 w = gson.fromJson(json.toString(), EntryWrapper1.class);
         System.out.println("Loaded " + w.getdocs().size() + " entries");
         return w.getdocs();
         */
        return null;
    }

    public static void main(String[] args) throws Exception {
        String url = System.getProperty("mysql_url.path", "dev.aztec.io");
        String port = System.getProperty("mysql_url.path", "33060");
        String username = System.getProperty("mysql_username.path", "developer");
        String password = System.getProperty("mysql_password.path", "ucla2015");
        ArrayList<AztecEntry> entries = new AztecEntryProviderFromMySQL(url, port, username, password).load();
        System.out.println(entries.size());
    }

}
