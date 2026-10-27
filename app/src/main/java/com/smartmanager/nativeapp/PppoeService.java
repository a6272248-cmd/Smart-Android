package com.smartmanager.nativeapp;
import java.io.IOException;
import java.util.List;
import java.util.Map;
public final class PppoeService {
    private final RouterService router;
    public PppoeService(RouterService router) { this.router = router; }
    public List<Map<String,String>> secrets() throws IOException { return router.execute("/ppp/secret/print", "=.proplist=.id,name,service,disabled"); }
    public List<Map<String,String>> profiles() throws IOException { return router.execute("/ppp/profile/print", "=.proplist=name,local-address,remote-address,rate-limit"); }
    public List<Map<String,String>> active() throws IOException { return router.execute("/ppp/active/print", "=.proplist=name,address,uptime,service"); }
    public void addSecret(String name,String password,String profile) throws IOException { router.execute("/ppp/secret/add","=name="+name,"=password="+password,"=profile="+profile); }
    public void setDisabled(String id,boolean disabled) throws IOException { router.execute("/ppp/secret/set","=.id="+id,"=disabled="+disabled); }
    public void updateSecret(String id,String profile) throws IOException { router.execute("/ppp/secret/set","=.id="+id,"=profile="+profile); }
    public void removeSecret(String id) throws IOException { router.execute("/ppp/secret/remove","=.id="+id); }
    public void disconnect(String id) throws IOException { router.execute("/ppp/active/remove","=.id="+id); }
}
