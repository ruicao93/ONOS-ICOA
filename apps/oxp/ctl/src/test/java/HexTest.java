import org.junit.Test;

/**
 * Created by cr on 16-8-20.
 */
public class HexTest {
    @Test
    public void main() {
        System.out.println(String.format("%06x", 10000000));
        System.out.println(Long.valueOf("000000000000000f",16));
    }
}
