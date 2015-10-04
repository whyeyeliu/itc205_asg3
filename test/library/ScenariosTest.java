package library;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Calendar;
import java.util.Date;

import library.daos.BookDAO;
import library.daos.BookHelper;
import library.daos.LoanHelper;
import library.daos.LoanMapDAO;
import library.daos.MemberHelper;
import library.daos.MemberMapDAO;
import library.interfaces.EBorrowState;
import library.interfaces.IBorrowUI;
import library.interfaces.daos.IBookDAO;
import library.interfaces.daos.ILoanDAO;
import library.interfaces.daos.IMemberDAO;
import library.interfaces.entities.IBook;
import library.interfaces.entities.ILoan;
import library.interfaces.entities.IMember;
import library.interfaces.hardware.ICardReader;
import library.interfaces.hardware.IDisplay;
import library.interfaces.hardware.IPrinter;
import library.interfaces.hardware.IScanner;
import library.panels.borrow.ABorrowPanel;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ScenariosTest {
	
	private IBorrowUI ui;
	private ICardReader reader;
	private IScanner scanner;
	private IPrinter printer; 
	private IDisplay display;
	private BorrowUC_CTL ctl;
	
	private static IBookDAO bookDAO;
	private static IMemberDAO memberDAO;
	private static ILoanDAO loanDAO;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		bookDAO = new BookDAO(new BookHelper());
		memberDAO = new MemberMapDAO(new MemberHelper());
		loanDAO = new LoanMapDAO(new LoanHelper());
		
		IBook[] book = new IBook[15];
		IMember[] member = new IMember[6];
			
		book[0]  = bookDAO.addBook("author1", "title1", "callNo1");
		book[1]  = bookDAO.addBook("author1", "title2", "callNo2");
		book[2]  = bookDAO.addBook("author1", "title3", "callNo3");
		book[3]  = bookDAO.addBook("author1", "title4", "callNo4");
		book[4]  = bookDAO.addBook("author2", "title5", "callNo5");
		book[5]  = bookDAO.addBook("author2", "title6", "callNo6");
		book[6]  = bookDAO.addBook("author2", "title7", "callNo7");
		book[7]  = bookDAO.addBook("author2", "title8", "callNo8");
		book[8]  = bookDAO.addBook("author3", "title9", "callNo9");
		book[9]  = bookDAO.addBook("author3", "title10", "callNo10");
		book[10] = bookDAO.addBook("author4", "title11", "callNo11");
		book[11] = bookDAO.addBook("author4", "title12", "callNo12");
		book[12] = bookDAO.addBook("author5", "title13", "callNo13");
		book[13] = bookDAO.addBook("author5", "title14", "callNo14");
		book[14] = bookDAO.addBook("author5", "title15", "callNo15");
			
		member[0] = memberDAO.addMember("fName0", "lName0", "0001", "email0");
		member[1] = memberDAO.addMember("fName1", "lName1", "0002", "email1");
		member[2] = memberDAO.addMember("fName2", "lName2", "0003", "email2");
		member[3] = memberDAO.addMember("fName3", "lName3", "0004", "email3");
		member[4] = memberDAO.addMember("fName4", "lName4", "0005", "email4");
		member[5] = memberDAO.addMember("fName5", "lName5", "0006", "email5");
		
		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
				
		//create a member with overdue loans		
		for (int i=0; i<2; i++) {
			ILoan loan = loanDAO.createLoan(member[1], book[i]);
			loanDAO.commitLoan(loan);
		}
		cal.setTime(now);
		cal.add(Calendar.DATE, ILoan.LOAN_PERIOD + 1);
		Date checkDate = cal.getTime();		
		loanDAO.updateOverDueStatus(checkDate);
			
		//create a member with maxed out unpaid fines
		member[2].addFine(10.0f);
		
		//create a member with maxed out loans
		for (int i=2; i<7; i++) {
			ILoan loan = loanDAO.createLoan(member[3], book[i]);
			loanDAO.commitLoan(loan);
		}
			
		//a member with a fine, but not over the limit
		member[4].addFine(5.0f);
			
		//a member with a couple of loans but not over the limit
		for (int i=7; i<9; i++) {
			ILoan loan = loanDAO.createLoan(member[5], book[i]);
			loanDAO.commitLoan(loan);
		}		
		
	}

	@Before
	public void setUp() throws Exception {
		
		this.ui = mock(ABorrowPanel.class);
		this.reader = mock(ICardReader.class);
		this.scanner = mock(IScanner.class);
		this.printer = mock(IPrinter.class);
		this.display = mock(IDisplay.class);
		
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
		this.ctl = null;
		
	}

	// Test the happy day scenario
	@Test
	public void testHappyDayScenario() {
		
		System.out.println("-------- Start test --------");
		System.out.println("Test the happy day scenario:");
		ctl.initialise();
		ctl.cardSwiped(1);
		ctl.bookScanned(10);
		ctl.scansCompleted();
		ctl.loansConfirmed();
		
		verify(ui).setState(EBorrowState.COMPLETED);
		verify(reader, atLeast(3)).setEnabled(false);
		verify(scanner, atLeast(3)).setEnabled(false);
		verify(printer).print(anyString());
		
		// Check that the correct book has been added to a loan for the member
		assertEquals(memberDAO.getMemberByID(1).getLoans().get(0).getBook().getID(), 10);		
		assertEquals(ctl.getState(), EBorrowState.COMPLETED);
		
		System.out.println("-------- End of test --------\n");
		
	}
	
	// Test the reject books scenario
	@Test
	public void testRejectScenario() {
		
		System.out.println("-------- Start test --------");
		System.out.println("Test the reject books scenario:");
		ctl.initialise();
		ctl.cardSwiped(1);
		ctl.bookScanned(10);
		ctl.loansRejected();
		
		verify(ui, times(2)).setState(EBorrowState.SCANNING_BOOKS);
		verify(reader, times(1)).setEnabled(true);
		verify(reader, times(2)).setEnabled(false);
		verify(scanner, times(2)).setEnabled(true);
		verify(scanner, times(1)).setEnabled(false);
		verify(printer, never()).print(anyString());
		
		// Check that pending loan display has been cleared
		verify(ui, times(2)).displayPendingLoan("");
		
		assertEquals(ctl.getState(), EBorrowState.SCANNING_BOOKS);
		
		System.out.println("-------- End of test --------\n");
		
	}
	
	// Test borrowing restricted scenario
	@Test
	public void testRestrictedScenario() {
		
		System.out.println("-------- Start test --------");
		System.out.println("Test the borrowing restricted scenario:");
		ctl.initialise();
		ctl.cardSwiped(2);
		ctl.cancelled();
		
		verify(ui).setState(EBorrowState.CANCELLED);
		verify(reader, times(2)).setEnabled(false);
		verify(reader, times(1)).setEnabled(true);
		verify(scanner, times(3)).setEnabled(false);
		verify(scanner, never()).setEnabled(true);
				
		assertEquals(ctl.getState(), EBorrowState.CANCELLED);
		
		System.out.println("-------- End of test --------\n");
		
	}

}
