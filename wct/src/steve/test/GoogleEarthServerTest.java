//package steve.test;
//
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.net.InetSocketAddress;
//import java.net.URL;
//import java.util.concurrent.Executors;
//
//import javax.imageio.ImageIO;
//
//import com.sun.net.httpserver.Headers;
//import com.sun.net.httpserver.HttpExchange;
//import com.sun.net.httpserver.HttpHandler;
//import com.sun.net.httpserver.HttpServer;
//
//public class GoogleEarthServerTest {
//  public static void main(String[] args) throws IOException {
//    InetSocketAddress addr = new InetSocketAddress(8080);
//    HttpServer server = HttpServer.create(addr, 0);
//
//    server.createContext("/", new MyHandler());
//    server.setExecutor(Executors.newCachedThreadPool());
//    server.start();
//    System.out.println("Server is listening on port 8080" );
//  }
//}
//
//class MyHandler implements HttpHandler {
//  public void handle(HttpExchange exchange) throws IOException {
//    String requestMethod = exchange.getRequestMethod();
//    if (requestMethod.equalsIgnoreCase("GET")) {
//      Headers responseHeaders = exchange.getResponseHeaders();
////      responseHeaders.set("Content-Type", "text/plain");
//      responseHeaders.set("Content-Type", "image/png");
//      exchange.sendResponseHeaders(200, 0);
//
//      OutputStream responseBody = exchange.getResponseBody();
//      Headers requestHeaders = exchange.getRequestHeaders();
//      ByteArrayOutputStream baos = new ByteArrayOutputStream();
//      ImageIO.write(
//              ImageIO.read(new URL("http://l.yimg.com/a/i/us/sp/fn/default/full/add.gif")),
//              "png", baos);
//      responseBody.write(baos.toByteArray());
////      Set<String> keySet = requestHeaders.keySet();
////      Iterator<String> iter = keySet.iterator();
////      while (iter.hasNext()) {
////        String key = iter.next();
////        List values = requestHeaders.get(key);
////        String s = key + " = " + values.toString() + "\n";
////        responseBody.write(s.getBytes());
////      }
//      responseBody.close();
//    }
//  }
//}
