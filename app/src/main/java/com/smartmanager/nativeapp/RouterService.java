package com.smartmanager.nativeapp;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/** Lifecycle and command boundary for RouterOS operations. */
public final class RouterService implements Closeable {
    private final RouterApi api;

    public RouterService(String host, int port, boolean tls) throws IOException {
        api = new RouterApi(host, port, tls);
    }

    public void login(String username, String password) throws Exception { api.login(username, password); }

    public List<Map<String,String>> execute(String command, String... arguments) throws IOException {
        return api.execute(command, arguments);
    }

    @Override public void close() throws IOException { api.close(); }
}
