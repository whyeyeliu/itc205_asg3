package library;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Calendar;
import java.util.Date;

import library.daos.BookDAO;
import library.daos.LoanMapDAO;
import library.daos.MemberMapDAO;
import library.entities.Book;
import library.entities.Loan;
import library.entities.Member;
import library.interfaces.EBorrowState;
import library.interfaces.IBorrowUI;
import library.interfaces.daos.IBookDAO;
import library.interfaces.daos.ILoanDAO;
import library.interfaces.daos.IMemberDAO;
import library.interfaces.entities.ELoanState;
import library.interfaces.entities.IBook;
import library.interfaces.entities.ILoan;
import library.interfaces.entities.IMember;
import library.interfaces.hardware.ICardReader;
import library.interfaces.hardware.IDisplay;
import library.interfaces.hardware.IPrinter;
import library.interfaces.hardware.IScanner;
import library.panels.borrow.ABorrowPanel;
import library.panels.borrow.ScanningPanel;
import library.panels.borrow.SwipeCardPanel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class OperationsTest {
	
	private IBorrowUI ui;
	private ICardReader reader;
	private IScanner scanner;
	private IPrinter printer; 
	private IDisplay display;
	private IBookDAO bookDAO;
	private IMemberDAO memberDAO;
	private ILoanDAO loanDAO;
	
	private BorrowUC_CTL ctl;
	
	@Before
	public void setUp() throws Exception {
		
		this.ui = mock(ABorrowPanel.class);
		this.reader = mock(ICardReader.class);
		this.scanner = mock(IScanner.class);
		this.printer = mock(IPrinter.class);
		this.display = mock(IDisplay.class);
		
		this.bookDAO = new BookDAO(new library.daos.BookHelper());
		this.memberDAO = new MemberMapDAO(new library.daos.MemberHelper());
		this.loanDAO = new LoanMapDAO(new library.daos.LoanHelper());
		
		this.ctl = new BorrowUC_CTL(reader, scanner, printer, display,
				bookDAO, loanDAO, memberDAO, ui);
		
	}
	
	@After
	public void tearDown() throws Exception {
		
		this.ui = null;
		this.reader = null;
		this.scanner = null;
		this.printer = null;
		this.display = null;
		this.bookDAO = null;
		this.memberDAO = null;
		this.loanDAO = null;
		this.ctl = null;
		
	}

	// Test that the initialise() operation works as intended
	@Test
	public void testInitialiseMethod() {
		
		System.out.println("-------- Start test --------");
		System.out.println("Test that the initialise() operation works as intended:");
		
		assertEquals(ctl.getState(), EBorrowState.CREATED);		
		ctl.initialise();
		
		verify(display).getDisplay();
		verify(ui).setState(EBorrowState.INITIALIZED);
		verify(reader).setEnabled(true);
		verify(scanner).setEnabled(false);
		
		assertEquals(ctl.getState(), EBorrowState.INITIALIZED);
		
		System.out.println("-------- End of test --------\n");
		
	}
	
	// Test swipeCard operation when not restricted with no fine
	@Test
	public void testSwipeCardNotRestrictedNoFine() {
		
		System.out.println("-------- Start test --------");
		System.out.println("Test swipeCard operation when not restricted with no fine:");
		
		memberDAO.addMember("First", "Last", "1234", "abcd");
		
		ctl.initialise();
		assertEquals(ctl.getState(), EBorrowState.INITIALIZED);
		ctl.cardSwiped(1);
		
		verify(ui).setState(EBorrowState.SCANNING_BOOKS);
		verify(reader).setEnabled(false);
		verify(scanner).setEnabled(true);
		verify(ui).displayMemberDetails(anyInt(), anyString(), anyString());
		verify(ui).displayExistingLoan(anyString());
		
		assertEquals(ctl.getState(), EBorrowState.SCANNING_BOOKS);
		
		System.out.println("-------- End of test --------\n");
		
	}
	
	// Test swipeCard operation when not restricted but with outstanding fine
	@Test
	public void testSwipeCardNotRestrictedWithFine() {
		
		System.out.println("-------- Start test --------");
		System.out.println("Test swipeCard operation when not restricted but with outstanding fine:");
		
		// Create member with a fine
		IMember m = memberDAO.addMember("First", "Last", "1234", "abcd");
		m.addFine(1.0f);
		
		ctl.initialise();
		assertEquals(ctl.getState(), EBorrowState.INITIALIZED);
		ctl.cardSwiped(1);
		
		verify(ui).setState(EBorrowState.SCANNING_BOOKS);
		verify(reader).setEnabled(false);
		verify(scanner).setEnabled(true);
		verify(ui).displayMemberDetails(anyInt(), anyString(), anyString());
		verify(ui).displayExistingLoan(anyString());
		verify(ui).displayOutstandingFineMessage(eq(1.0f));
		
		assertEquals(ctl.getState(), EBorrowState.SCANNING_BOOKS);
		
		System.out.println("-------- End of test --------\n");
		
	}
	
	// Test swipeCard operation with overdue loans
	@Test
	public void testSwipeCardOverdueLoans() {
		
		System.out.println("-------- Start test --------");
		System.out.println("Test swipeCard operation with overdue loans:");
		
        // Create an overdue loan record
		IBook b = bookDAO.addBook("author1", "title1", "callNo1");
		IMember m = memberDAO.addMember("fName0", "lName0", "0001", "email0");
		
		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		
		ILoan l = loanDAO.createLoan(m, b);
		loanDAO.commitLoan(l);
		
		cal.setTime(now);
		cal.add(Calendar.DATE, ILoan.LOAN_PERIOD + 1);
		Date checkDate = cal.getTime();		
		loanDAO.updateOverDueStatus(checkDate);
		
		ctl.initialise();
		assertEquals(ctl.getState(), EBorrowState.INITIALIZED);
		ctl.cardSwiped(1);
		
		verify(ui).setState(EBorrowState.BORROWING_RESTRICTED);
		verify(reader).setEnabled(false);
		verify(scanner, atLeast(1)).setEnabled(false);
		verify(scanner, never()).setEnabled(true);
		verify(ui).displayMemberDetails(anyInt(), anyString(), anyString());
		verify(ui).displayExistingLoan(anyString());
		verify(ui).displayOverDueMessage();
		verify(ui).displayErrorMessage(anyString());
		
		assertEquals(ctl.getState(), EBorrowState.BORROWING_RESTRICTED);
		
		System.out.println("-------- End of test --------\n");
		
	}
	
	// Test swipeCard operation with overdue loans and outstanding fines
	@Test
	public void testSwipeCardOverdueLoansWithFine() {
		
		System.out.println("-------- Start test --------");
		System.out.println("Test swipeCard operation with overdue loans and fine:");
		
        // Create an overdue loan record
		IBook b = bookDAO.addBook("author1", "title1", "callNo1");
		IMember m = memberDAO.addMember("fName0", "lName0", "0001", "email0");
		m.addFine(1.0f);
		
		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		
		ILoan l = loanDAO.createLoan(m, b);
		loanDAO.commitLoan(l);
		
		cal.setTime(now);
		cal.add(Calendar.DATE, ILoan.LOAN_PERIOD + 1);
		Date checkDate = cal.getTime();		
		loanDAO.updateOverDueStatus(checkDate);
		
		ctl.initialise();
		assertEquals(ctl.getState(), EBorrowState.INITIALIZED);
		ctl.cardSwiped(1);
		
		verify(ui).setState(EBorrowState.BORROWING_RESTRICTED);
		verify(reader).setEnabled(false);
		verify(scanner, atLeast(1)).setEnabled(false);
		verify(scanner, never()).setEnabled(true);
		verify(ui).displayMemberDetails(anyInt(), anyString(), anyString());
		verify(ui).displayExistingLoan(anyString());
		verify(ui).displayOverDueMessage();
		verify(ui).displayErrorMessage(anyString());
		verify(ui).displayOutstandingFineMessage(eq(1.0f));
		
		assertEquals(ctl.getState(), EBorrowState.BORROWING_RESTRICTED);
		
		System.out.println("-------- End of test --------\n");
		
	}
	
	// Test swipeCard operation when at loan limit
	@Test
	public void testSwipeCardAtLoanLimit() {
		
		System.out.println("-------- Start test --------");
		System.out.println("Test swipeCard operation at loan limit:");
		
        // Create a maxed out loan record
		IBook[] b = new IBook[5];
		IMember m = memberDAO.addMember("fName0", "lName0", "0001", "email0");
		
		b[0]  = bookDAO.addBook("author1", "title1", "callNo1");
		b[1]  = bookDAO.addBook("author1", "title2", "callNo2");
		b[2]  = bookDAO.addBook("author1", "title3", "callNo3");
		b[3]  = bookDAO.addBook("author1", "title4", "callNo4");
		b[4]  = bookDAO.addBook("author2", "title5", "callNo5");

		for (int i = 0; i < 5; i++) {
			
			ILoan l = loanDAO.createLoan(m, b[i]);
			loanDAO.commitLoan(l);
			
		}
		
		ctl.initialise();
		assertEquals(ctl.getState(), EBorrowState.INITIALIZED);
		ctl.cardSwiped(1);
		
		verify(ui).setState(EBorrowState.BORROWING_RESTRICTED);
		verify(reader).setEnabled(false);
		verify(scanner, atLeast(1)).setEnabled(false);
		verify(scanner, never()).setEnabled(true);
		verify(ui).displayMemberDetails(anyInt(), anyString(), anyString());
		verify(ui).displayExistingLoan(anyString());
		verify(ui).displayAtLoanLimitMessage();
		verify(ui).displayErrorMessage(anyString());
		
		assertEquals(ctl.getState(), EBorrowState.BORROWING_RESTRICTED);
		
		System.out.println("-------- End of test --------\n");
		
	}
	
	// Test swipeCard operation when at loan limit with fine
	@Test
	public void testSwipeCardAtLoanLimitWithFine() {
		
		System.out.println("-------- Start test --------");
		System.out.println("Test swipeCard operation at loan limit with fine:");
		
        // Create a maxed out loan record
		IBook[] b = new IBook[5];
		IMember m = memberDAO.addMember("fName0", "lName0", "0001", "email0");
		m.addFine(1.0f);
		
		b[0]  = bookDAO.addBook("author1", "title1", "callNo1");
		b[1]  = bookDAO.addBook("author1", "title2", "callNo2");
		b[2]  = bookDAO.addBook("author1", "title3", "callNo3");
		b[3]  = bookDAO.addBook("author1", "title4", "callNo4");
		b[4]  = bookDAO.addBook("author2", "title5", "callNo5");

		for (int i = 0; i < 5; i++) {
			
			ILoan l = loanDAO.createLoan(m, b[i]);
			loanDAO.commitLoan(l);
			
		}
		
		ctl.initialise();
		assertEquals(ctl.getState(), EBorrowState.INITIALIZED);
		ctl.cardSwiped(1);
		
		verify(ui).setState(EBorrowState.BORROWING_RESTRICTED);
		verify(reader).setEnabled(false);
		verify(scanner, atLeast(1)).setEnabled(false);
		verify(scanner, never()).setEnabled(true);
		verify(ui).displayMemberDetails(anyInt(), anyString(), anyString());
		verify(ui).displayExistingLoan(anyString());
		verify(ui).displayAtLoanLimitMessage();
		verify(ui).displayErrorMessage(anyString());
		verify(ui).displayOutstandingFineMessage(eq(1.0f));
		
		assertEquals(ctl.getState(), EBorrowState.BORROWING_RESTRICTED);
		
		System.out.println("-------- End of test --------\n");
		
	}
	
	// Test swipeCard operation with maxed out fine
	@Test
	public void testSwipeCardMaxedOutFine() {
		
		System.out.println("-------- Start test --------");
		System.out.println("Test swipeCard operation with maxed out fine:");
		
		// Create member with maxed out fine
		IMember m = memberDAO.addMember("First", "Last", "1234", "abcd");
		m.addFine(10.0f);
		
		ctl.initialise();
		assertEquals(ctl.getState(), EBorrowState.INITIALIZED);
		ctl.cardSwiped(1);
		
		verify(ui).setState(EBorrowState.BORROWING_RESTRICTED);
		verify(reader).setEnabled(false);
		verify(scanner, atLeast(1)).setEnabled(false);
		verify(scanner, never()).setEnabled(true);
		verify(ui).displayMemberDetails(anyInt(), anyString(), anyString());
		verify(ui).displayExistingLoan(anyString());
		verify(ui).displayOverFineLimitMessage(10.0f);;
		verify(ui).displayErrorMessage(anyString());
		verify(ui).displayOutstandingFineMessage(eq(10.0f));
		
		assertEquals(ctl.getState(), EBorrowState.BORROWING_RESTRICTED);
		
		System.out.println("-------- End of test --------\n");
		
	}

}
