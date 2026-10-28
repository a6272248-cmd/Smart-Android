import com.smartmanager.nativeapp.PdfLayout;
public class PdfLayoutTest { public static void main(String[] a){if(PdfLayout.pages(0,12)!=0||PdfLayout.pages(25,12)!=3||PdfLayout.firstIndex(2,12)!=24)throw new AssertionError("pagination");System.out.println("PASS: PDF pagination and ordering");} }
