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
}
