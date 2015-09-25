package library.entities;

import static org.junit.Assert.*;
import library.interfaces.entities.EBookState;

import org.junit.Test;

public class BookTest {

	@Test (expected = IllegalArgumentException.class)
	public void parameterTest() {
		new Book("John Doe", null, "100", 1);
	}
	
	@Test (expected = RuntimeException.class)
	public void borrowUnavailableBook() {
		Loan l = new Loan();
		Book b = new Book("John Doe", "Good Book", "100", 1);
		b.setState(EBookState.DAMAGED);
		b.borrow(l);
		
	}
	
	@Test
	public void borrowBook() {
		Loan l = new Loan();
		Book b = new Book("John Doe", "Good Book", "100", 1);
		b.borrow(l);
		assertEquals(l, b.getLoan());
	}

}
