package library.daos;

import java.util.ArrayList;
import java.util.List;

import library.interfaces.daos.IBookDAO;
import library.interfaces.daos.IBookHelper;
import library.interfaces.entities.IBook;

public class BookDAO implements IBookDAO {
	
	private List<IBook> books = new ArrayList<IBook>();	
	
	// Constructor
	public BookDAO(IBookHelper helper) {
		
		if (helper == null)
			throw new IllegalArgumentException("Helper is null");
		
	}

	@Override
	public IBook addBook(String author, String title, String callNumber) {
		
		// Create new helper object
		BookHelper helper = new BookHelper();
		int id = helper.getNextID();
		
		// Create a new book and add it to list
		IBook newBook = helper.makeBook(author, title, callNumber, id);
		books.add(newBook);
		
		return newBook;
		
	}

	@Override
	public IBook getBookByID(int id) {
		
		// Look for id in each entry in list
		for (IBook book : books)
			if (book.getID() == id)
				return book;
		
		return null;
		
	}

	@Override
	public List<IBook> listBooks() {
		
		return books;
		
	}

	@Override
	public List<IBook> findBooksByAuthor(String author) {
	
		// Create new list for books by particular author
		List<IBook> byAuthor = new ArrayList<IBook>();
		
		// Add each book that matches author to the new list
		for (IBook book : books)
			if (book.getAuthor().equals(author))
				byAuthor.add(book);
		
		return byAuthor;
		
	}

	@Override
	public List<IBook> findBooksByTitle(String title) {

		// Create new list for books of particular title
		List<IBook> byTitle = new ArrayList<IBook>();
		
		// Add each book that matches title to the new list
		for (IBook book : books)
			if (book.getTitle().equals(title))
				byTitle.add(book);
		
		return byTitle;
		
	}

	@Override
	public List<IBook> findBooksByAuthorTitle(String author, String title) {

		// Create new list for books by particular author and title
		List<IBook> authorTitle = new ArrayList<IBook>();
		
		// Add each book that matches author and title to the new list
		for (IBook book : books)
			if (book.getAuthor().equals(author) && book.getTitle().equals(title))
				authorTitle.add(book);
		
		return authorTitle;		
		
	}
	
	public int getSize() {
		
		return books.size();
		
	}
	
	public void clearList() {
		
		books.clear();
		
	}

}
