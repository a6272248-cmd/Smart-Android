import com.smartmanager.nativeapp.RouterApi;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class ApiMockServerTest {
 static String read(InputStream in)throws IOException {int len=RouterApi.readLength(in);byte[] b=new byte[len];new DataInputStream(in).readFully(b);return new String(b,StandardCharsets.UTF_8);}
 static void write(OutputStream out,String s)throws IOException{byte[] b=s.getBytes(StandardCharsets.UTF_8);RouterApi.writeLength(out,b.length);out.write(b);}
 static void sentence(OutputStream out,String... words)throws IOException{for(String s:words)write(out,s);write(out,"");out.flush();}
 public static void main(String[] args)throws Exception{
  ServerSocket listener=new ServerSocket(0,1,InetAddress.getLoopbackAddress());
  Thread fake=new Thread(()->{try(Socket client=listener.accept()){
   InputStream in=client.getInputStream();OutputStream out=client.getOutputStream();
   if(!read(in).equals("/login"))throw new AssertionError("login command");
   if(!read(in).equals("=name=admin"))throw new AssertionError("name");
   if(!read(in).equals("=password=demo"))throw new AssertionError("password");
   if(!read(in).equals(""))throw new AssertionError("sentence end");
   sentence(out,"!done");
   if(!read(in).equals("/system/resource/print"))throw new AssertionError("resource command");
   if(!read(in).equals("=.proplist=version"))throw new AssertionError("property argument");
   if(!read(in).equals(""))throw new AssertionError("sentence end");
   sentence(out,"!re","=version=7.24.2"); sentence(out,"!done");
   if(!read(in).equals("/ip/hotspot/user/print"))throw new AssertionError("filtered query command");
   if(!read(in).equals("?name=SM0000001"))throw new AssertionError("filter word");
   if(!read(in).equals("=.proplist=name"))throw new AssertionError("projection word");
   if(!read(in).equals(""))throw new AssertionError("sentence end");
   sentence(out,"!done");
   if(!read(in).equals("/ip/hotspot/user/add"))throw new AssertionError("add command");
   read(in);read(in);read(in);
   sentence(out,"!trap","=message=not enough permissions");sentence(out,"!done");
  }catch(Exception e){throw new RuntimeException(e);}});
  fake.start();
  try(RouterApi api=new RouterApi("127.0.0.1",listener.getLocalPort(),false)){
   api.login("admin","demo");
   List<Map<String,String>> r=api.execute("/system/resource/print","=.proplist=version");
   if(r.size()!=1||!"7.24.2".equals(r.get(0).get("version")))throw new AssertionError(r.toString());
   if(!api.execute("/ip/hotspot/user/print","?name=SM0000001","=.proplist=name").isEmpty())throw new AssertionError("expected no duplicates");
   try{api.execute("/ip/hotspot/user/add","=name=x","=password=y");throw new AssertionError("trap not thrown");}
   catch(IOException e){if(!e.getMessage().contains("not enough permissions"))throw e;}
  }
  fake.join(2000);listener.close();if(fake.isAlive())throw new AssertionError("server hung");
  System.out.println("PASS: socket login, RouterOS resource query, trap error propagation");
 }
}
