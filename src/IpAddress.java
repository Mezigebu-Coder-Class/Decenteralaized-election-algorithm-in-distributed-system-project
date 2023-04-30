import java.net.InetAddress;
import java.net.UnknownHostException;
public class IpAddress {
  public static void main(String args[]){
    try {
      InetAddress inetAddress = InetAddress.getLocalHost();
      System.out.println("IP Address: "+inetAddress.getHostAddress());
      System.out.println("Hostname: "+inetAddress.getHostName());
    }catch(UnknownHostException unknownHostException){
      unknownHostException.printStackTrace();
    }
  }
}