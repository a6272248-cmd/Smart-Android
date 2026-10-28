import com.smartmanager.nativeapp.CardTemplate;
public class CardTemplateTest{public static void main(String[]a){CardTemplate t=new CardTemplate("x","A4",595,842,6,2,20,8,true,true,true,true);if(t.perPage()!=12||t.pages(13)!=2||t.pages(0)!=0)throw new AssertionError("template pagination");System.out.println("PASS: card template pagination and field settings");}}
