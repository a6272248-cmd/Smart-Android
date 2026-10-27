package com.smartmanager.nativeapp;
import java.io.IOException;
import java.util.List;
import java.util.Map;
public final class HotspotService {
    private final RouterService router;
    public HotspotService(RouterService router) { this.router = router; }
    public List<Map<String,String>> users() throws IOException { return router.execute("/ip/hotspot/user/print", "=.proplist=.id,name,profile,disabled"); }
    public List<Map<String,String>> profiles() throws IOException { return router.execute("/ip/hotspot/user/profile/print", "=.proplist=name"); }
    public List<Map<String,String>> active() throws IOException { return router.execute("/ip/hotspot/active/print", "=.proplist=user,address,uptime"); }
}
