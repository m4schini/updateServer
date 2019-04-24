import net.freeutils.httpserver.HTTPServer;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
  
  static List keys = new ArrayList();
  DBConnection_mariadb dbconnection = new DBConnection_mariadb();
  
  public static void main(String[] args) {
    if (License.loadKeys()) {
      Log.status("[1/3] Loaded license Keys " + keys.toString());
    } else {
      Log.critical("Unable to load license Keys");
      System.exit(-1);
    }
    
    Log.status("[2/3] Starting server");
    HTTPServer server = new HTTPServer(1337);
    HTTPServer.VirtualHost host = server.getVirtualHost(null);
    
    host.addContext("/update", new getUpdater());
    
    try {
      server.start();
      
    } catch (IOException e) {
      Log.error("Server start failed");
      System.exit(-1);
      //e.printStackTrace();
    }
    
    Scanner scanner = new Scanner(System.in);
  
    Log.success("[3/3] Server start successfull");
    while (true) {
      String consoleInput = scanner.nextLine();
      if (consoleInput.equals("exit")) {
        Log.status("Until next time");
        System.exit(0);
      }
    }
  }
  
  private static class getUpdater implements HTTPServer.ContextHandler {
    @Override
    public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
      Log.warning("new update request");
      Map<String, String> params = request.getParams();
      
      if (License.verify(params.get("key"))) {
        Log.status("used licensekey: " + params.get("key"));
  
        File file = new File("updateFiles/" + params.get("file"));
        if (file.exists()) {
          try {
            response.getHeaders().add("Content-Disposition", "filename=" + params.get("file"));
            sendFile(200, "application/zip", file, response);
            Log.success("File " + params.get("file") + " served");
          } catch (IOException e) {
            Log.error("Something went wrong with the file");
            //e.printStackTrace();
          }
        } else {
          Log.error("File does not exist");
          response.send(404, "File wasn't found");
        }
      } else {
        Log.error("request used invalid key");
        response.send(401, "You're not allowed to access this file");
      }
      return 0;
    }
    
    /**
     * Modified net.freeutils.httpserver.HTTPServer.Response#send(int, java.lang.String)
     * makes it possible to serve Files instead of an HTML Page
     *
     * @param status http status code
     * @param contentType MIME Content type
     * @param file File you want to serve
     * @param response HTTPServer.Response, needed to answer to request
     * @throws IOException HTTPServer.Response, FileInputStream
     */
    void sendFile(int status, String contentType, File file, HTTPServer.Response response) throws IOException {
      
      response.sendHeaders(status, file.length(), -1,
              "W/\"" + Integer.toHexString(file.hashCode()) + "\"",
              contentType, null);
      OutputStream out = response.getBody();
      
      if (out != null)
        out.write(IOUtils.toByteArray(new FileInputStream(file)));
    }
  }
}
