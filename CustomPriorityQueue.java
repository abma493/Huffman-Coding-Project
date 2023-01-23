package HuffmanCoding;
import java.util.Arrays;

/**
 * A custom priority queue with minor changes as needed
 * for the Huffman coding assignment for CS314. <br><br>
 * The "add" method in particular is changed in order to deal with 'equal' elements in
 * a "fair" way rather than arbitrarily as done in the canon priority queue. "Fair" protocol
 * involves removing equal elements based on FIFO removal much like a traditional queue.
 * <br><br>
 * Also, this is a good practice for heap implementation.<br> 
 * Do mind the added boolean array for the special case requirement.
 * 
 * @author Abraham Martinez
 * @version 1.0,  November 2021
 */

public class CustomPriorityQueue<E> 
	implements I_CustomPriorityQueue<E> {
	/*
	 * Flags are issued to newly added items that are duplicates within the same
	 * level of the tree (a.k.a. are siblings with an element identical) 
	 * These flags help the poll() method remove the duplicate without the flag first, 
	 * complying with FIFO.
	 */
	private Object[] heap;
	private boolean[] flags;
	private int size;
	
	public CustomPriorityQueue() {
		heap = new Object[10];
		flags = new boolean[10];
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean add(E elem) {

		if(size + 1 >= heap.length) { // resize if necessary
			heap = Arrays.copyOf(heap, heap.length * 2);
			flags = Arrays.copyOf(flags, flags.length * 2);
		}
		
		heap[size + 1] = elem; // insert as rightmost leaf
		
		// bubble/percolate up
		int index = size + 1;
		boolean found = false;
		while(!found && hasParent(index)) {
			int parent = index / 2;
			if(comparable(index, parent) < 0) {
				swap(index, parent);
				index = parent;
			} else {
				found = true;
			}
		}
		size++;
		
		// check if tree level contains a duplicate sibling. If so, flag this element. 
		int sibling = index + 1; // right node
		if(sibling < heap.length && heap[index].equals(heap[sibling])) {
			flags[index] = true; // flagged
		}
		return found;
	}

	@Override
	public E poll() {
		@SuppressWarnings("unchecked")
		E result = (E) heap[1];
		
		if(result == null) { return null; } // poll() convention
		
		heap[1] = heap[size]; // rightmost leaf becomes new root
		size--;
		// percolate down to fix ordering
		int index = 1;
		boolean found = false;
		while(!found && hasLeft(index)) {
			int left = index * 2;
			int right = index * 2 + 1;
			int child = left;
			if(hasRight(index) && comparable(right, left) < 0) {
				child = right;
			} else if(flags[left] && comparable(right, left) == 0) { // equals special case
				child = right;
			} 
			if(comparable(index, child) > 0) {
				swap(index, child);
				index = child;
			} else {
				found = true;
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private int comparable(int k, int j) {
		final Comparable<? super E> key = (Comparable<? super E>) heap[k];
		return key.compareTo((E) heap[j]);
	}
	
	private void swap(int i1, int i2) {
		@SuppressWarnings("unchecked")
		E temp = (E) heap[i1];
		heap[i1] = heap[i2];
		heap[i2] = temp;
	}
	
	private boolean hasParent(int index) {
		return index > 1;
	}
	
	private boolean hasLeft(int index) {
		return index * 2 <= size;
	}
	
	private boolean hasRight(int index) {
		return index * 2 + 1 <= size;
	}
	 
	public boolean isEmpty() {
		return size == 0;
	}
	
	public String toString() {
		String result = "[";
		if(!isEmpty()) {
			result += heap[1];
			for(int i = 2; i <= size; i++) {
				result += ", " + heap[i];
			}
		}
		return result + "]";
	}
	
}
















