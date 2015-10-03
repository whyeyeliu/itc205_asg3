package library.daos;

import static org.junit.Assert.*;
import library.interfaces.entities.IBook;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BookDAOTest {
	
	BookHelper helper = new BookHelper();
	BookDAO bookData = new BookDAO(helper);

	@Before
	public void setUp() {
		
		bookData.addBook("Mark", "Good book", "abcdef");
		bookData.addBook("Tom", "Nice book", "293kds");
		bookData.addBook("Jane", "Great book", "1230dw");
		bookData.addBook("Emily", "Super book", "o20dgx");
		bookData.addBook("Mark", "Good book", "92ld8x");
		bookData.addBook("Jamie", "Cook book", "2id9xs");
		bookData.addBook("Sarah", "Sexy book", "0di204");
		
	}
	
	@After
	public void tearDown() {
		
		bookData.clearList();
		helper.resetCount();
		
	}
	
	// Test that nextID in BookHelper class is incrementing properly
	@Test
	public void testNextID() {
		
		assertEquals(bookData.getSize() + 1, helper.getNextID());
		
	}
	
	// Test for IllegalArgumentException if helper is null
	@Test (expected = IllegalArgumentException.class)
	public void testNullHelper() {
		
		new BookDAO(null);
		
	}
	
	// Test that getBooksByID method works
	@Test
	public void testGetBooksByID() {
		
		IBook b = bookData.getBookByID(6);		
		assertEquals(b.getAuthor(), "Jamie");
		
	}
	
	// Test that book listing works
	@Test
	public void testListing() {
		
		assertEquals(bookData.listBooks().size(), 7);
		
	}
	
	// Test that findBooksByAuthor works
	@Test
	public void testBooksByAuthor() {
		
		assertEquals(bookData.findBooksByAuthor("Mark").size(), 2);
		assertEquals(bookData.findBooksByAuthor("Tom").size(), 1);
		assertEquals(bookData.findBooksByAuthor("Jane").size(), 1);
		assertEquals(bookData.findBooksByAuthor("Emily").size(), 1);
		assertEquals(bookData.findBooksByAuthor("Jamie").size(), 1);
		assertEquals(bookData.findBooksByAuthor("Sarah").size(), 1);
		
	}
	
	// Test that findBooksByTitle works
	@Test
	public void testBooksByTitle() {
		
		assertEquals(bookData.findBooksByTitle("Good book").size(), 2);
		assertEquals(bookData.findBooksByTitle("Nice book").size(), 1);
		assertEquals(bookData.findBooksByTitle("Great book").size(), 1);
		assertEquals(bookData.findBooksByTitle("Super book").size(), 1);
		assertEquals(bookData.findBooksByTitle("Cook book").size(), 1);
		assertEquals(bookData.findBooksByTitle("Sexy book").size(), 1);
		
	}
	
	// Test that findBooksByAuthorTitle works
	@Test
	public void testBooksByAuthorTitle() {
		
		assertEquals(bookData.findBooksByAuthorTitle("Mark", "Good book").size(), 2);
		assertEquals(bookData.findBooksByAuthorTitle("Emily", "Super book").size(), 1);
		assertEquals(bookData.findBooksByAuthorTitle("Jamie", "Sexy book").size(), 0);
		
	}

}
