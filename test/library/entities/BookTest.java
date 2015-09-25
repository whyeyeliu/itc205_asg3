package library.entities;

import static org.junit.Assert.*;

import org.junit.Test;

public class BookTest {

	@Test (expected = IllegalArgumentException.class)
	public void parameterTest() {
		Book b = new Book("John Doe", null , "100", 1);
	}

}
