package com.smartmanager.nativeapp;
import java.io.IOException;
import java.util.List;
import java.util.Map;
public final class UserManagerService {
    private final RouterService router;
    public UserManagerService(RouterService router) { this.router = router; }
    public String commandRoot(String version) { return version != null && version.startsWith("6.") ? "/tool/user-manager" : "/user-manager"; }
    public List<Map<String,String>> users(String version) throws IOException { return router.execute(commandRoot(version)+"/user/print", "=.proplist=name,username,disabled"); }
    public List<Map<String,String>> profiles(String version) throws IOException { return router.execute(commandRoot(version)+"/profile/print"); }
    public List<Map<String,String>> sessions(String version) throws IOException { return router.execute(commandRoot(version)+"/session/print", "=.proplist=user,uptime,started,ended"); }
}
