package com.smartmanager.nativeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Durable, encrypted batch journal. Each line is id|name|password|profile|state. */
public final class BatchStore {
    private static final String ALIAS="smart_manager_batch_key"; private final SharedPreferences prefs;
    public BatchStore(Context c){prefs=c.getSharedPreferences("voucher_batches",Context.MODE_PRIVATE);}
    public synchronized void save(String value)throws Exception{byte[] iv=new byte[12];new java.security.SecureRandom().nextBytes(iv);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key(),new GCMParameterSpec(128,iv));String v=Base64.encodeToString(iv,2)+"."+Base64.encodeToString(c.doFinal(value.getBytes(StandardCharsets.UTF_8)),2);if(!prefs.edit().putString("current",v).commit())throw new java.io.IOException("batch journal write failed");}
    public synchronized String load()throws Exception{String v=prefs.getString("current",null);if(v==null)return "";String[] p=v.split("\\.",2);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(p[0],2)));return new String(c.doFinal(Base64.decode(p[1],2)),StandardCharsets.UTF_8);}
    private SecretKey key()throws Exception{KeyStore s=KeyStore.getInstance("AndroidKeyStore");s.load(null);if(s.containsAlias(ALIAS))return ((KeyStore.SecretKeyEntry)s.getEntry(ALIAS,null)).getSecretKey();KeyGenerator g=KeyGenerator.getInstance("AES","AndroidKeyStore");g.init(new android.security.keystore.KeyGenParameterSpec.Builder(ALIAS,3).setBlockModes("GCM").setEncryptionPaddings("NoPadding").setRandomizedEncryptionRequired(true).build());return g.generateKey();}
}
