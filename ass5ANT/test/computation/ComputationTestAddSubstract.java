package computation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import computation.Computation;


public class ComputationTestAddSubstract {
	
	Computation computation;

	@Before
	public void setUp() {
	    computation = new Computation();		
	}
    
    @Test
	public void testAdd() {
	    int addResult = computation.add(2, 5);
	    assertEquals(7, addResult);
	}

    @Test
	public void testSubstract() {
	    int substractResult = computation.subtract(2, 5);
	    assertEquals(-3, substractResult);
	}

}
