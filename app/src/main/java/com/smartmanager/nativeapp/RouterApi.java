package com.smartmanager.nativeapp;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import javax.net.ssl.*;

/** RouterOS Native API client. Single request at a time per connection; no implicit unsafe TLS fallback. */
public final class RouterApi implements Closeable {
    private static final int MAX_WORD = 4 * 1024 * 1024;
    private static final int MAX_REPLY_WORDS = 100000;
    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;

    public RouterApi(String host, int port, boolean tls) throws IOException {
        if (host == null || host.trim().isEmpty() || port < 1 || port > 65535) throw new IllegalArgumentException("Invalid address");
        // No insecure trust managers: TLS certificates must pass standard Android validation.
        if (tls) {
            SSLSocket ssl = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
            ssl.connect(new InetSocketAddress(host,port), 6000);
            ssl.setSoTimeout(12000);
            SSLParameters params = ssl.getSSLParameters();
            params.setEndpointIdentificationAlgorithm("HTTPS");
            ssl.setSSLParameters(params);
            ssl.startHandshake();
            socket = ssl;
        } else {
            Socket tcp = new Socket();
            tcp.connect(new InetSocketAddress(host,port), 6000);
            tcp.setSoTimeout(12000);
            socket = tcp;
        }
        input = socket.getInputStream(); output = socket.getOutputStream();
    }

    public void login(String username, String password) throws Exception {
        List<Map<String,String>> replies = execute("/login", "=name="+username, "=password="+password);
        // RouterOS pre-6.43 challenge login returns =ret= challenge instead of a successful !done.
        String challenge = replies.isEmpty() ? null : replies.get(replies.size()-1).get("ret");
        if (challenge != null && !challenge.isEmpty()) {
            byte[] bytes = hexToBytes(challenge);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update((byte)0); md.update(password.getBytes(StandardCharsets.UTF_8)); md.update(bytes);
            StringBuilder code = new StringBuilder("00");
            for (byte b : md.digest()) code.append(String.format(Locale.ROOT,"%02x", b & 0xff));
            execute("/login", "=name="+username, "=response="+code);
        }
    }

    private static byte[] hexToBytes(String str) {
        if ((str.length() & 1) != 0 || str.length() > 128) throw new IllegalArgumentException("Invalid challenge");
        byte[] data = new byte[str.length()/2];
        for (int i=0;i<data.length;i++) {
            int hi=Character.digit(str.charAt(2*i),16), lo=Character.digit(str.charAt(2*i+1),16);
            if (hi<0 || lo<0) throw new IllegalArgumentException("Invalid challenge");
            data[i]=(byte)((hi<<4)|lo);
        }
        return data;
    }

    public synchronized List<Map<String,String>> execute(String command, String... arguments) throws IOException {
        if (command == null || !command.startsWith("/") || (command.contains("\n") || command.contains("\r"))) throw new IllegalArgumentException("Invalid command");
        // Validate the complete sentence before any bytes are transmitted.
        for (String arg: arguments) {
            if (arg == null || !(arg.startsWith("=") || arg.startsWith("?")) || arg.contains("\n") || arg.contains("\r")) throw new IllegalArgumentException("Invalid argument");
        }
        writeWord(command);
        for (String arg: arguments) writeWord(arg);
        writeWord(""); output.flush();
        List<Map<String,String>> result = new ArrayList<>();
        int words = 0;
        while (true) {
            String type = readWord(); words++;
            if (words > MAX_REPLY_WORDS || !type.startsWith("!")) throw new IOException("Unexpected or oversized reply");
            Map<String,String> map=new LinkedHashMap<>();
            while (true) {
                String word=readWord(); words++;
                if (words > MAX_REPLY_WORDS) throw new IOException("Reply too large");
                if (word.isEmpty()) break;
                if (word.startsWith("=")) {
                    int pos=word.indexOf('=',1);
                    if (pos>1) map.put(word.substring(1,pos),word.substring(pos+1));
                }
            }
            if ("!trap".equals(type) || "!fatal".equals(type)) throw new IOException("RouterOS: " + map.getOrDefault("message", type));
            if ("!re".equals(type)) result.add(map);
            if ("!done".equals(type)) { if (!map.isEmpty()) result.add(map); return result; }
        }
    }

    private void writeWord(String word) throws IOException { byte[] bytes=word.getBytes(StandardCharsets.UTF_8); writeLength(output,bytes.length);output.write(bytes); }
    private String readWord() throws IOException { int len=readLength(input); if (len<0 || len>MAX_WORD) throw new IOException("Invalid word length"); byte[] bytes=new byte[len]; new DataInputStream(input).readFully(bytes);return new String(bytes,StandardCharsets.UTF_8); }

    public static void writeLength(OutputStream out, int n) throws IOException {
        if(n<0 || n>MAX_WORD)throw new IOException("Invalid length");
        if(n<0x80)out.write(n);
        else if(n<0x4000){out.write((n>>8)|0x80);out.write(n);}
        else if(n<0x200000){out.write((n>>16)|0xC0);out.write(n>>8);out.write(n);}
        else {out.write((n>>24)|0xE0);out.write(n>>16);out.write(n>>8);out.write(n);out.write(n);}
    }
    public static int readLength(InputStream in) throws IOException {
        int x=in.read();if(x<0)throw new EOFException();
        if((x&0x80)==0)return x;
        if((x&0xC0)==0x80)return ((x&0x3F)<<8)|readByte(in);
        if((x&0xE0)==0xC0)return ((x&0x1F)<<16)|(readByte(in)<<8)|readByte(in);
        if((x&0xF0)==0xE0)return ((x&0x0F)<<24)|(readByte(in)<<16)|(readByte(in)<<8)|readByte(in);
        if(x==0xF0)return (readByte(in)<<24)|(readByte(in)<<16)|(readByte(in)<<8)|readByte(in);
        throw new IOException("Invalid length prefix");
    }
    private static int readByte(InputStream in)throws IOException{int b=in.read();if(b<0)throw new EOFException();return b;}
    @Override public void close() throws IOException {socket.close();}
}
