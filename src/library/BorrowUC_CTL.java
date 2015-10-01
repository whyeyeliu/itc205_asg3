package library;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import library.interfaces.EBorrowState;
import library.interfaces.IBorrowUI;
import library.interfaces.IBorrowUIListener;
import library.interfaces.daos.IBookDAO;
import library.interfaces.daos.ILoanDAO;
import library.interfaces.daos.IMemberDAO;
import library.interfaces.entities.EBookState;
import library.interfaces.entities.IBook;
import library.interfaces.entities.ILoan;
import library.interfaces.entities.IMember;
import library.interfaces.hardware.ICardReader;
import library.interfaces.hardware.ICardReaderListener;
import library.interfaces.hardware.IDisplay;
import library.interfaces.hardware.IPrinter;
import library.interfaces.hardware.IScanner;
import library.interfaces.hardware.IScannerListener;

public class BorrowUC_CTL implements ICardReaderListener, 
									 IScannerListener, 
									 IBorrowUIListener {
	
	private ICardReader reader;
	private IScanner scanner; 
	private IPrinter printer; 
	private IDisplay display;
	//private String state;
	private int scanCount = 0;
	private IBorrowUI ui;
	private EBorrowState state; 
	private IBookDAO bookDAO;
	private IMemberDAO memberDAO;
	private ILoanDAO loanDAO;
	
	private List<IBook> bookList;
	private List<ILoan> loanList;
	private IMember borrower;
	
	private JPanel previous;


	public BorrowUC_CTL(ICardReader reader, IScanner scanner, 
			IPrinter printer, IDisplay display,
			IBookDAO bookDAO, ILoanDAO loanDAO, IMemberDAO memberDAO ) {

		this.bookDAO = bookDAO;
		this.memberDAO= memberDAO;
		this.loanDAO = loanDAO;
		
		this.ui = new BorrowUC_UI(this);
		this.reader = reader;
		reader.addListener(this);
		this.scanner = scanner;
		scanner.addListener(this);
		this.printer = printer;
		this.display = display;
		
		state = EBorrowState.CREATED;
	}
	
	public void initialise() {
		previous = display.getDisplay();
		display.setDisplay((JPanel) ui, "Borrow UI");		
		setState(EBorrowState.INITIALIZED);
	}
	
	public void close() {
		display.setDisplay(previous, "Main Menu");
	}

	@Override
	public void cardSwiped(int memberID) {
		System.out.println("cardSwiped: got " + memberID);
		if (!state.equals(EBorrowState.INITIALIZED)) {
			throw new RuntimeException(
					String.format("BorrowUC_CTL : cardSwiped : illegal operation in state: %s", state));
		}
		borrower = memberDAO.getMemberByID(memberID);
		if (borrower == null) {
			ui.displayErrorMessage(String.format("Member ID %d not found", memberID));
			return;
		}
		boolean overdue = borrower.hasOverDueLoans();
		boolean atLoanLimit = borrower.hasReachedLoanLimit();
		boolean hasFines = borrower.hasFinesPayable();
		boolean overFineLimit = borrower.hasReachedFineLimit();
		boolean borrowing_restricted = (overdue || atLoanLimit || overFineLimit);
		
		if (borrowing_restricted) {
			setState(EBorrowState.BORROWING_RESTRICTED);
		}
		else {
			setState(EBorrowState.SCANNING_BOOKS);
		}

		//display member details
		int mID = borrower.getID();
		String mName = borrower.getFirstName() + " " + borrower.getLastName();
		String mContact = borrower.getContactPhone();
		ui.displayMemberDetails(mID, mName, mContact);	
		
		if (hasFines) {
			float amountOwing = borrower.getFineAmount();
			ui.displayOutstandingFineMessage(amountOwing);
		}
		
		if (overdue) {
			ui.displayOverDueMessage();
		}
		
		if (atLoanLimit) {
			ui.displayAtLoanLimitMessage();
		}
		
		if (overFineLimit) {
			System.out.println("State: " + state);
			float amountOwing = borrower.getFineAmount();
			ui.displayOverFineLimitMessage(amountOwing);
		}
		
		//display existing loans
		String loanString = buildLoanListDisplay(borrower.getLoans());
		ui.displayExistingLoan(loanString);
	}
	
	
	
	@Override
	public void bookScanned(int barcode) {
		System.out.println("bookScanned: got " + barcode);
		if (state != EBorrowState.SCANNING_BOOKS) {
			throw new RuntimeException(
					String.format("BorrowUC_CTL : bookScanned : illegal operation in state: %s", state));			
		}
		ui.displayErrorMessage("");
		
		IBook book = bookDAO.getBookByID(barcode);
		if (book == null) {
			ui.displayErrorMessage(String.format("Book %d not found", barcode));
			return;
		}

		if (book.getState() != EBookState.AVAILABLE) {
			ui.displayErrorMessage(String.format("Book %d is not available: %s", book.getID(), book.getState()));
			return;
		}
		
		if (bookList.contains(book)) {
			ui.displayErrorMessage(String.format("Book %d already scanned: ", book.getID()));
			return;
		}

		scanCount++;
		bookList.add(book);
		ILoan loan = loanDAO.createLoan(borrower, book);
		loanList.add(loan);
		
		//display current book
		ui.displayScannedBookDetails(book.toString());
		
		//display pending loans
		ui.displayPendingLoan(buildLoanListDisplay(loanList));
		
		if (scanCount >= IMember.LOAN_LIMIT) {
			setState(EBorrowState.CONFIRMING_LOANS);
		}	
	}

	
	private void setState(EBorrowState state) {
		System.out.println("Setting state: " + state);
		
		this.state = state;
		ui.setState(state);

		switch (state) {
		
		case INITIALIZED:
			reader.setEnabled(true);
			scanner.setEnabled(false);
			break;
			
		case SCANNING_BOOKS:
			reader.setEnabled(false);
			scanner.setEnabled(true);
			this.bookList = new ArrayList<IBook>();
			this.loanList = new ArrayList<ILoan>();
			scanCount = borrower.getLoans().size();
			
			//clear currentBook display
			ui.displayScannedBookDetails("");			
			//clear pending loan display
			ui.displayPendingLoan("");			
			break;
			
		case CONFIRMING_LOANS:
			reader.setEnabled(false);
			scanner.setEnabled(false);
			//display pending loans
			ui.displayConfirmingLoan(buildLoanListDisplay(loanList));
			break;
			
		case COMPLETED:
			reader.setEnabled(false);
			scanner.setEnabled(false);
			for (ILoan loan : loanList) {
				loanDAO.commitLoan(loan);
			}
			printer.print(buildLoanListDisplay(loanList));
			close();
			break;
			
		case CANCELLED:
			reader.setEnabled(false);
			scanner.setEnabled(false);
			close();
			break;
			
		case BORROWING_RESTRICTED:
			reader.setEnabled(false);
			scanner.setEnabled(false);
			ui.displayErrorMessage(String.format("Member %d cannot borrow at this time.", borrower.getID()));
			break;
			
		default:
			throw new RuntimeException("Unknown state");
		}
	}

	@Override
	public void cancelled() {
		setState(EBorrowState.CANCELLED);
	}
	
	@Override
	public void scansCompleted() {
		setState(EBorrowState.CONFIRMING_LOANS);		
	}

	@Override
	public void loansConfirmed() {
		setState(EBorrowState.COMPLETED);				
	}

	@Override
	public void loansRejected() {
		System.out.println("Loans Rejected");
		setState(EBorrowState.SCANNING_BOOKS);		
	}

	private String buildLoanListDisplay(List<ILoan> loans) {
		StringBuilder bld = new StringBuilder();
		for (ILoan ln : loans) {
			if (bld.length() > 0) bld.append("\n\n");
			bld.append(ln.toString());
		}
		return bld.toString();		
	}

}
