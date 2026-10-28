package com.smartmanager.nativeapp;
import java.util.*;
/** Read-only reconciliation; it never calls a write command. */
public final class BatchReconciler { private BatchReconciler(){}
 public static int reconcile(BatchLedger ledger,String router,List<Map<String,String>> users){int found=0;for(BatchLedger.Card c:ledger.cards())if(c.router.equals(router)){Map<String,String> hit=null;for(Map<String,String> u:users)if(c.name.equals(u.get("name"))){hit=u;break;}if(hit==null)c.state=BatchLedger.State.FAILED;else if(c.profile.equals(hit.getOrDefault("profile",""))) {c.state=BatchLedger.State.CONFIRMED;found++;}else c.state=BatchLedger.State.UNKNOWN;}return found;}
}
