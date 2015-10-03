package library;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import library.daos.BookDAO;
import library.daos.LoanMapDAO;
import library.daos.MemberMapDAO;
import library.interfaces.EBorrowState;
import library.interfaces.IBorrowUI;
import library.interfaces.daos.IBookDAO;
import library.interfaces.daos.ILoanDAO;
import library.interfaces.daos.IMemberDAO;
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
		
		assertEquals(ctl.getState(), EBorrowState.CREATED);		
		ctl.initialise();
		verify(display).getDisplay();
		verify(ui).setState(EBorrowState.INITIALIZED);
		verify(reader).setEnabled(true);
		verify(scanner).setEnabled(false);
		assertEquals(ctl.getState(), EBorrowState.INITIALIZED);
		
	}
	
	// Test swipeCard operation when not restricted
	@Test
	public void testSwipeCardNotRestricted() {
		
		memberDAO.addMember("First", "Last", "1234", "abcd");
		
		ctl.initialise();
		assertEquals(ctl.getState(), EBorrowState.INITIALIZED);
		ctl.cardSwiped(1);
		verify(ui).setState(EBorrowState.SCANNING_BOOKS);
		verify(reader).setEnabled(false);
		verify(scanner).setEnabled(true);
		assertEquals(ctl.getState(), EBorrowState.SCANNING_BOOKS);
		
	}

}
