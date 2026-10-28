package com.smartmanager.nativeapp;
import java.io.IOException;
import java.util.List;
import java.util.Map;
public final class UserManagerService {
    private final RouterService router;
    public UserManagerService(RouterService router) { this.router = router; }
    public String commandRoot(String version) { return version != null && version.startsWith("6.") ? "/tool/user-manager" : "/user-manager"; }
    public List<Map<String,String>> users(String version) throws IOException { return router.execute(commandRoot(version)+"/user/print", "=.proplist=.id,name,username,profile,disabled,uptime-used,limit-uptime,comment"); }
    public List<Map<String,String>> profiles(String version) throws IOException { return router.execute(commandRoot(version)+"/profile/print"); }
    public List<Map<String,String>> sessions(String version) throws IOException { return router.execute(commandRoot(version)+"/session/print", "=.proplist=user,uptime,started,ended"); }
    public void addUser(String version,String name,String password,String profile)throws IOException{router.execute(commandRoot(version)+"/user/add","=name="+name,"=password="+password,"=profile="+profile);}
    public void setDisabled(String version,String id,boolean disabled)throws IOException{router.execute(commandRoot(version)+"/user/set","=.id="+id,"=disabled="+disabled);}
    public void removeUser(String version,String id)throws IOException{router.execute(commandRoot(version)+"/user/remove","=.id="+id);}
}
