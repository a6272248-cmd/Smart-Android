package com.smartmanager.nativeapp;
import java.nio.charset.StandardCharsets;import java.security.MessageDigest;
/** Stable non-secret identity used to isolate journals belonging to different routers. */
public final class RouterIdentity { private RouterIdentity(){} public static String of(String host,int port,String user,boolean tls){try{byte[] d=MessageDigest.getInstance("SHA-256").digest((host.trim().toLowerCase()+"|"+port+"|"+user+"|"+tls).getBytes(StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();for(byte x:d)b.append(String.format(java.util.Locale.ROOT,"%02x",x&255));return b.toString();}catch(Exception e){throw new IllegalStateException(e);}} }
