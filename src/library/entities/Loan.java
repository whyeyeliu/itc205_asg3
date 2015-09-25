package library.entities;

import java.util.Date;

import library.interfaces.entities.IBook;
import library.interfaces.entities.ILoan;
import library.interfaces.entities.IMember;

public class Loan implements ILoan{

	@Override
	public void commit(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void complete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isOverDue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkOverDue(Date currentDate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IMember getBorrower() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IBook getBook() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return 0;
	}

}
