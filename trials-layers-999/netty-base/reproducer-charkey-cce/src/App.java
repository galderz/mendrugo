import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Minimal application to reproduce ClassCastException in
 * CrossLayerConstantRegistryFeature.isConstantRegistered().
 *
 * References XML serialization to make CharInfo (and its CharKey inner class)
 * reachable during the base layer build.
 */
public class App {
    public static void main(String[] args) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        System.out.println("OK: " + factory.getClass().getName());
    }
}
