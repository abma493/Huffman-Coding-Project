package HuffmanCoding;

/**
 * The interface for a simple, custom Priority Queue. 
 * The contract methods are limited to the scope
 * of the Huffman Coding Assignment requirements.
 * 
 * @author Abraham Martinez, November 2021
 * @version 1.0
 */

public interface I_CustomPriorityQueue<E> {
	
	/**
	 *@return the size of this queue.
	 *@param none
	 */
	public int size();
	
	
	/**
	 * @return true if element is successfully added, false otherwise.
	 * @param elem != null
	 */
	public boolean add(E elem);
	
	
	/**
	 * @return the head of this queue, null otherwise.
	 * Unlike remove() [not implemented in this custom interface], 
	 * this method does not throw an exception if the head of this
	 * queue is empty.
	 */
	public E poll();
	
}
