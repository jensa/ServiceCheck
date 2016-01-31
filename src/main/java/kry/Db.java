package kry;

import java.nio.file.Files;
import java.io.File;
import java.nio.file.Paths;
import java.io.IOException;
import io.vertx.core.json.JsonObject;

public class Db {
    public static String dbFile = "dbfile.json";

    public static String readDbContents() throws IOException{
      File f = new File(dbFile);
      if(f.isDirectory())
        return "";
      if(!f.exists()){
        f.createNewFile();
      }
      String contents = new String(Files.readAllBytes(Paths.get(dbFile)));
      if(contents.isEmpty()){
        Files.write(Paths.get(dbFile), "{\"services\":[]}".getBytes());
      }
      return new String(Files.readAllBytes(Paths.get(dbFile)));
    }

    public static void writeToDb(JsonObject json) throws IOException{
      Files.write(Paths.get(dbFile), json.toString().getBytes());
    }
}
