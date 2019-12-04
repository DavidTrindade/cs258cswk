import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

class test {

  private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

  public static void main(String[] args) throws ParseException {
    String date1 = "31/07/99";
    String date2 = "31/08/99";


    System.out.println(dateFormat.parse(date1).before(dateFormat.parse(date2)));
  }

}
