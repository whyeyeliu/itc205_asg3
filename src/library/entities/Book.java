package library.entities;

import library.interfaces.entities.EBookState;
import library.interfaces.entities.IBook;
import library.interfaces.entities.ILoan;

public class Book implements IBook{
	
	private String author;
	private String title;
	private String callNumber;
	private int bookID;
	private ILoan loan;
	private EBookState state = EBookState.AVAILABLE;

	// Constructor
	public Book(String author, String title, String callNumber, int bookID) {
		
		// Determine if parameters are valid
		boolean isValid = true;
		if (author == null || title == null || callNumber == null || bookID <= 0)
			isValid = false;
		
		// Throw exception if not valid
		if (!isValid)
			throw new IllegalArgumentException("Invalid entry.");
		else {
			this.author = author;
			this.title = title;
			this.callNumber = callNumber;
			this.bookID = bookID;
		}
		
	}
	
	@Override
	public void borrow(ILoan loan) {
		
		if (this.state != EBookState.AVAILABLE)
			throw new RuntimeException("Book is not currently available.");
		else {
			this.loan = loan;
			this.state = EBookState.ON_LOAN;
		}
		
	}

	@Override
	public ILoan getLoan() {
		
		return this.loan;
	}

	@Override
	public void returnBook(boolean damaged) {
		
		if (this.state != EBookState.ON_LOAN)
			throw new RuntimeException("Book is not currently on loan.");
		else {
			if (damaged)
				this.state = EBookState.DAMAGED;
			else
				this.state = EBookState.AVAILABLE;
			this.loan = null;
			
		}
		
	}

	@Override
	public void lose() {
		
		if (this.state != EBookState.ON_LOAN)
			throw new RuntimeException("Book is not currently on loan.");
		else
			this.state = EBookState.LOST;
		
	}

	@Override
	public void repair() {
		
		if (this.state != EBookState.DAMAGED)
			throw new RuntimeException("Book is not currently damaged.");
		else
			this.state = EBookState.AVAILABLE;
		
	}

	@Override
	public void dispose() {
		
		if (this.state == EBookState.ON_LOAN || this.state == EBookState.DISPOSED)
			throw new RuntimeException("Book is not currently in a disposable state.");
		
	}

	@Override
	public EBookState getState() {
		
		return this.state;
	}

	@Override
	public String getAuthor() {
		
		return this.author;
	}

	@Override
	public String getTitle() {
		
		return this.title;
		
	}

	@Override
	public String getCallNumber() {
		
		return this.callNumber;
	}

	@Override
	public int getID() {
		
		return this.bookID;
	}
	
	public void setState(EBookState state) {
		
		this.state = state;
		
	}
	

}
