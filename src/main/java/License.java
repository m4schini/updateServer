import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * For now this has some quick and dirty methods. Planned is a license key verification connected
 * to a database.
 */
public class License {
  private static DBConnection dbconnection = null;
  
  public License() {
    dbconnection = new DBConnection();
  }
  
  boolean verify(String key) {
    try {
      ResultSet resultSet = dbconnection.execute("SELECT * FROM licenseKeys WHERE licenseKey=?", key);
      if (resultSet.next() && new Date().before(resultSet.getDate("validUntil"))) {
        return true;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
    return false;
  }
  
  boolean logActivity(String key) {
    try {
      dbconnection.update("INSERT INTO logActivity VALUES(?,?)",
              key,
              new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                      .format(Calendar.getInstance().getTime())
      );
      return true;
      
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }
  
  void close() {
    dbconnection.close();
  }
}


