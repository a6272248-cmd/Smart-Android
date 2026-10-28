package com.smartmanager.nativeapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pure, deterministic batch state model. Persistence is supplied by BatchStore. */
public final class BatchLedger {
    public enum State { PENDING, CONFIRMED, FAILED, UNKNOWN }
    public static final class Card {
        public final String router, name, password, profile; public State state;
        public Card(String router,String name,String password,String profile,State state){this.router=clean(router);this.name=clean(name);this.password=clean(password);this.profile=clean(profile);this.state=state;}
        private static String clean(String s){return s==null?"":s.replace("|","_").replace("\n","_").replace("\r","_");}
        String encode(){return router+"|"+name+"|"+password+"|"+profile+"|"+state.name();}
        static Card decode(String line){String[] p=line.split("\\|",-1);if(p.length!=5)throw new IllegalArgumentException("Invalid batch record");return new Card(p[0],p[1],p[2],p[3],State.valueOf(p[4]));}
    }
    private final List<Card> cards=new ArrayList<>();
    public void addPending(String router,String name,String password,String profile){if(find(router,name)!=null)throw new IllegalArgumentException("Duplicate card");cards.add(new Card(router,name,password,profile,State.PENDING));}
    public Card find(String router,String name){for(Card c:cards)if(c.router.equals(router)&&c.name.equals(name))return c;return null;}
    public void mark(String router,String name,State state){Card c=find(router,name);if(c==null)throw new IllegalArgumentException("Unknown card");c.state=state;}
    public List<Card> cards(){return Collections.unmodifiableList(cards);}
    public String encode(){StringBuilder b=new StringBuilder();for(Card c:b.append(c.encode()).append('\n');return b.toString();}
    public static BatchLedger decode(String value){BatchLedger l=new BatchLedger();if(value==null||value.isEmpty())return l;for(String line:value.split("\\n")){if(!line.isEmpty())l.cards.add(Card.decode(line));}return l;}
}
