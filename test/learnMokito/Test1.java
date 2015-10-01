package learnMokito;

import static org.mockito.Mockito.*;

import java.awt.List;
import java.util.LinkedList;

import library.entities.Book;

public class Test1 {
	
	public static void main (String[] args) {
		
		LinkedList<String> mockedList = mock(LinkedList.class);
		
		mockedList.add("one");	
		mockedList.clear();
		
		verify(mockedList).add("one");
		verify(mockedList).clear();		
		
		when(mockedList.get(0)).thenReturn("first");
		when(mockedList.get(1)).thenThrow(new RuntimeException());
		
		System.out.println(mockedList.get(0));
		System.out.println(mockedList.get(999));
		
		
		
	}
	

	

}
