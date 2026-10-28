package com.smartmanager.nativeapp;
import java.io.IOException;
import java.util.List;
import java.util.Map;
public final class HotspotService {
    private final RouterService router;
    public HotspotService(RouterService router) { this.router = router; }
    public List<Map<String,String>> users() throws IOException { return router.execute("/ip/hotspot/user/print", "=.proplist=.id,name,profile,disabled"); }
    public List<Map<String,String>> profiles() throws IOException { return router.execute("/ip/hotspot/user/profile/print", "=.proplist=name"); }
    public List<Map<String,String>> active() throws IOException { return router.execute("/ip/hotspot/active/print", "=.proplist=.id,user,address,uptime"); }
    public void addUser(String name,String password,String profile) throws IOException { router.execute("/ip/hotspot/user/add","=name="+name,"=password="+password,"=profile="+profile); }
    public void setDisabled(String id,boolean disabled) throws IOException { router.execute("/ip/hotspot/user/set","=.id="+id,"=disabled="+disabled); }
    public void updateUser(String id,String profile) throws IOException { router.execute("/ip/hotspot/user/set","=.id="+id,"=profile="+profile); }
    public void removeUser(String id) throws IOException { router.execute("/ip/hotspot/user/remove","=.id="+id); }
    public void disconnect(String id) throws IOException { router.execute("/ip/hotspot/active/remove","=.id="+id); }
}
