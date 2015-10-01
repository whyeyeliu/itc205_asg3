package library.entities;

import static org.junit.Assert.*;
import library.interfaces.entities.EBookState;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class BookTest {
	
	// Instance variables
	private Loan l;
	private Book b;

	// Create new Book and Loan object for each test
	@Before
	public void setUp() {
		
		this.l = mock(Loan.class);
		this.b = new Book("John Doe", "Good Book", "100", 1);
		
	}
	
	// Test for invalid parameter entry to Book constructor
	@Test (expected = IllegalArgumentException.class)
	public void parameterTest() {
		
		new Book("John Doe", null, "100", 1);
		
	}
	
	// Try to borrow an unavailable book
	@Test (expected = RuntimeException.class)
	public void borrowUnavailableBook() {
		
		b.setState(EBookState.DAMAGED);
		b.borrow(l);
		
	}
	
	// Test normal borrow
	@Test
	public void borrowBook() {
		
		b.borrow(l);
		assertEquals(l, b.getLoan());
		
	}
	
	// Test that returning a damaged book works correctly
	@Test
	public void returnDamaged() {
		
		b.borrow(l);
		b.returnBook(true);
		assertTrue(b.getLoan() == null);
		assertTrue(b.getState() == EBookState.DAMAGED);
		
	}
	
	// Try to return a book that is not on loan
	@Test (expected = RuntimeException.class)
	public void wrongStateReturn() {
		
		b.returnBook(true);
		
	}
	
	// Try to lose a book that is not on loan
	@Test (expected = RuntimeException.class)
	public void wrongStateLoseBook() {
		
		b.lose();
		
	}
	
	// Lose a book
	@Test
	public void loseBook(){
		
		b.borrow(l);
		b.lose();
		assertTrue(b.getState() == EBookState.LOST);
		
	}
	
	// Try to repair a book that is not damaged
	@Test (expected = RuntimeException.class)
	public void wrongStateRepair() {
		
		b.repair();
		
	}
	
	// Repair a book
	@Test
	public void repairBook() {
		
		b.borrow(l);
		b.returnBook(true);
		b.repair();
		assertTrue(b.getState() == EBookState.AVAILABLE);
		
	}
	
	// Try to dispose a book that is on loan
	@Test (expected = RuntimeException.class)
	public void wrongStateDispose() {
		
		b.borrow(l);
		b.dispose();
		
	}
	
	// Dispose a book
	@Test
	public void disposeBook() {
		
		b.dispose();
		assertTrue(b.getState() == EBookState.DISPOSED);
		
	}
	

}
