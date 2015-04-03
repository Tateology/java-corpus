
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestTemplate extends TestCase {
   
  // Variables, inner classes used by tests
   
  public TestTemplate(String name) { 
    super(name);
  }
   
  public static Test suite() { 
    return new TestSuite(TestTemplate.class);
  }
   
  protected void setUp() {
    /* Code to set up a fresh scaffold for each test */ 
  }
   
  protected void tearDown() { 
    /* Code to destroy the scaffold after each test */
  }
   
  public void testSomething() {
    /* do something and assert success */
  }
   
  // other test methods

}
