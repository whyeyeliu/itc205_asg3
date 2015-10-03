package learnMokito;

import static org.mockito.Mockito.*;

import java.awt.List;
import java.util.LinkedList;

import org.junit.Test;

import library.entities.Book;

public class Test1 {
	
	@Test
	public void test() {
		
		LinkedList<String> mockedList = mock(LinkedList.class);
		
		mockedList.add("one");	
		mockedList.clear();
		
		verify(mockedList, atLeast(1)).add("one");
		verify(mockedList).clear();		
		
		when(mockedList.get(0)).thenReturn("one");
		when(mockedList.get(1)).thenThrow(new RuntimeException());
		
		System.out.println(mockedList.get(0));
		System.out.println(mockedList.get(999));
		
		
		
	}
	

	

}
