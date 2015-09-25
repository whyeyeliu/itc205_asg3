package library.daos;

import library.entities.Book;
import library.interfaces.daos.IBookHelper;
import library.interfaces.entities.IBook;

public class BookHelper implements IBookHelper {
	
	private static int nextID = 1;

	@Override
	public IBook makeBook(String author, String title, String callNumber, int id) {

		IBook newBook = new Book(author, title, callNumber, id);
		nextID++;
		
		return newBook;
		
	}
	
	public int getNextID() {
		
		return BookHelper.nextID;
		
	}
	
	public void resetCount() {
		
		BookHelper.nextID = 1;
		
	}

}
