import org.junit.Test;
import static org.junit.Assert.*;
public class DemoTest {
	@Test
	public void shouldReturnTrue(){
		FaceDetectionMain demo = new FaceDetectionMain();
			assertTrue(demo.getBool());
		}
}
