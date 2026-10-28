import com.smartmanager.nativeapp.BatchLedger;
public class BatchLedgerTest {
  public static void main(String[] args) {
    BatchLedger l=new BatchLedger(); l.addPending("r1","SM0000001","123456","basic");
    l.mark("r1","SM0000001",BatchLedger.State.UNKNOWN);
    String saved=l.encode(); BatchLedger restored=BatchLedger.decode(saved);
    if(restored.cards().size()!=1||restored.cards().get(0).state!=BatchLedger.State.UNKNOWN)throw new AssertionError("state restore");
    try { restored.addPending("r1","SM0000001","x","basic"); throw new AssertionError("duplicate accepted"); } catch(IllegalArgumentException expected) { }
    restored.addPending("r2","SM0000001","x","basic");
    if(restored.find("r1","SM0000001")==null||restored.find("r2","SM0000001")==null)throw new AssertionError("router isolation");
    boolean rejectedCorruption=false;
    try { BatchLedger.decode(saved+saved); } catch(IllegalArgumentException expected) { rejectedCorruption=true; }
    if(!rejectedCorruption)throw new AssertionError("duplicate stored rows must be rejected");
    BatchLedger distinct=BatchLedger.decode(saved+"r2|SM0000001|other|basic|CONFIRMED\\n");
    if(distinct.cards().size()!=2)throw new AssertionError("distinct router identities must survive archive restore");
    System.out.println("PASS: encrypted-journal payload state, recovery, duplicate and router isolation model");
  }
}
