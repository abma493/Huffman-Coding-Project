
package HuffmanCoding;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Main runner class for the Compression/Decompression process of files
 * through the employment of Huffman coding. <br><br>
 * Based on a program by Owen Astrachan.
 * 
 * @author Abraham Martinez
 * 
 * @version 2.0 November 2021
 * 
 */
public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer 			  myViewer;
    private Map<Character, String> lookupTable;
    private int[] 						  freq;
    private int 			  bitsUncompressed;
    private int 				bitsCompressed;
    private int 							 i; 
    private int 				  headerFormat;
    private TreeNode    				  root; 
    private boolean   			  requestForce;

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * @param in is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     * header to use, standard count format, standard tree format, or
     * possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
    	this.headerFormat = headerFormat;
    	freq = buildFrequencyTable(in);
    	root = buildHuffmanTree(freq); 
        lookupTable = buildLookupTable(root);
        int operation = bitsUncompressed - bitsCompressed;
        requestForce = (operation < 0);
        if(requestForce) {
        	myViewer.showMessage("Compressed file has " + Math.abs(operation) +
        							" more bits than the uncompressed file. \n" + 
        								"Select \"force\" compression option to compress.");
        }
        return operation; 
    }
	
	// Builds the character frequency table off from the InputStream provided.
    private int[] buildFrequencyTable(InputStream in) throws IOException {
		BitInputStream bitIS = new BitInputStream(in);
		final int[] freqResult = new int[ALPH_SIZE + 1]; // includes val 256 (PSEUDO_EOF)
		int read = 0;
		while(read != -1) {
			read = bitIS.read(); // 8-bit chunks
			if(read != -1) {
				freqResult[read]++;
				bitsUncompressed += read;
			}
		}
    	if(bitsUncompressed == 0) {
    		bitIS.close();
    		throw new IOException("preprocess failed. No bytes to read in file. ");
    	}
		bitIS.close();
		return freqResult;
	}
    
    // Builds the Huffman tree to be used by encoder/decoder procedures.
    private TreeNode buildHuffmanTree(int[] freq) {
		CustomPriorityQueue<TreeNode> cspQueue = new CustomPriorityQueue<>();
		for(char i = 0; i < ALPH_SIZE + 1; i++) {
			if(freq[i] > 0) {
				cspQueue.add(new TreeNode(i, freq[i]));
			} else if(i == PSEUDO_EOF) { // Ensure P_EOF with freq of 1.
				cspQueue.add(new TreeNode(i, 1));
			}
		} /* Expect to have a laid out flattened plane of nodes. (forest) */
		assert cspQueue.size() > 0;
		while(cspQueue.size() > 1) {
			final TreeNode left = cspQueue.poll();
			final TreeNode right = cspQueue.poll();
			final TreeNode parent = new TreeNode(left, 0, right); // '0' in ASCII represents null. 
			cspQueue.add(parent);
		}
		
		return cspQueue.poll();
	}
    
    // Character to binary encoding. (Recursive)
	private Map<Character, String> buildLookupTable(TreeNode root) {
		final Map<Character, String> lookupTable = new HashMap<>();
		buildLookupTableHelper(root, "", lookupTable);
		return lookupTable;
	}
	
	// A recursive helper for looking up characters and creating their respective binary encoding.
	private void buildLookupTableHelper(final TreeNode node, final String code,
			final Map<Character,String> lookupTable) {
		
		if(!node.isLeaf()) {
			buildLookupTableHelper(node.getLeft(), code + '0', lookupTable);
			buildLookupTableHelper(node.getRight(), code + '1', lookupTable);
		} else {
			lookupTable.put((char) node.getValue(), code);
			bitsCompressed += readCodeBits(code, node.getValue());
		}
	}
	
	// Reads each mapped value's encoding bits and add to total Bits in compression. 
	private int readCodeBits(final String code, final int value) {
		int totalBitsInCode = 0;
		for(int i = 0; i < code.length(); i++) {
			totalBitsInCode++;
		}
		return totalBitsInCode * freq[value];
	}

	/**
	 * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br> pre: <code>preprocessCompress</code> must be called before this method
     * @param in is the stream being compressed (NOT a BitInputStream)
     * @param out is bound to a file/stream to which bits are written
     * for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     * If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
    	int bitsWrit = 0;
    	if(!requestForce || force) {
    		bitsWrit = BITS_PER_INT * 2; 
    		BitInputStream bitIS = new BitInputStream(in);
    		BitOutputStream bitOS = new BitOutputStream(out);
    		bitOS.writeBits(BITS_PER_INT, MAGIC_NUMBER); // Header 1 
    		if(headerFormat == STORE_TREE) {
    			bitOS.writeBits(BITS_PER_INT, STORE_TREE); // Indicator of fmt
    			bitsWrit += standardTreeFormat(bitOS);  
    		} else if(headerFormat == STORE_COUNTS) {
    			bitOS.writeBits(BITS_PER_INT, STORE_COUNTS); // Indicator of fmt 
    			bitsWrit += standardCountFormat(bitOS); 
    		} 	
    		int read = 0; // read/compress rest of the file
    		while(read != -1) {
    			read = bitIS.read();
    			if(read != -1) {
    				String huffCode = lookupTable.get((char) read); 
    				for(int j = 0; j < huffCode.length(); j++) {
    					bitsWrit++;
    					bitOS.writeBits(1, huffCode.charAt(j));
    				}
    			}
    		}
    		bitIS.close();
    		String EOFHuffCode = lookupTable.get((char) PSEUDO_EOF);
    		for(int k = 0; k < EOFHuffCode.length(); k++) { // END (prevent padding w/ P_EOF)
    			bitsWrit++;
    			bitOS.writeBits(1, EOFHuffCode.charAt(k));
    		}
 
    	}
        return bitsWrit; // Bits in Compressed file.
    }    
    
    // Standard Count Format (value 256 [P_EOF] not included; freq of 1 warranted.)
    private int standardCountFormat(BitOutputStream bitOS) {
    	for(int i = 0; i < ALPH_SIZE; i++) { 
    		bitOS.writeBits(BITS_PER_INT, freq[i]);
    	}
    	return ALPH_SIZE * BITS_PER_INT;
    }
    
    // Standard Tree Format (Recursive)
    private int standardTreeFormat(BitOutputStream bitOS) { 	
    	String embeddedHuffTree = huffmanTreeCode(root, "");
    	int treeSizeInBits = embeddedHuffTree.toCharArray().length;
    	bitOS.writeBits(BITS_PER_INT, treeSizeInBits);
    	
    	for(int k = 0; k < embeddedHuffTree.length(); k++) {
    		bitOS.writeBits(1, embeddedHuffTree.charAt(k));
    	}
    	return BITS_PER_INT + treeSizeInBits;
    }

    // Employs preorder traveral to find the code of the tree. (for STF)
    private String huffmanTreeCode(TreeNode node, String treeFmtCode) {
    	if(node == null) {
    		return "";
    	}   	
    	if(node.isLeaf()) {
    		return 1 + BinaryConvert.toBinary(node.getValue());
    	} 
    	return 0 + huffmanTreeCode(node.getLeft(), treeFmtCode) +
    			huffmanTreeCode(node.getRight(), treeFmtCode);
    }
    
    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     * @param in is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException { 
    	BitInputStream bIS = new BitInputStream(in);
    	BitOutputStream bOS = new BitOutputStream(out);
    	int magic = bIS.readBits(BITS_PER_INT);
    	if(magic != MAGIC_NUMBER) { 
    		bIS.close(); bOS.close();
    		throw new IOException("Error reading compressed file. \n" +
					"File did not start.");	
    	}
    	int fmtType = bIS.readBits(BITS_PER_INT);
    	if(fmtType == STORE_COUNTS) {
    		storeCountDecompression(bIS, bOS);
    	} else if(fmtType == STORE_TREE) {
    		storeTreeDecompression(bIS, bOS);
    	} else { 
    		// for the sake of this assignment, we omit STORE_CUSTOM option
    		bIS.close(); bOS.close();
    		throw new IOException("Error reading compressed file. \n" + 
    										"File missing format type.");
    	}
	    return bitsCompressed; 
    }
    
    // Decompresses the file using the Standard Count Format embedded in the compressed file.
	private void storeCountDecompression(BitInputStream bIS, BitOutputStream bOS) throws IOException {
		assert freq == null;
    	freq = new int[ALPH_SIZE + 1];
    	int counter = 0;
    	while(counter < ALPH_SIZE) {
    		int read = bIS.readBits(BITS_PER_INT);
    		freq[counter] = read;
    		counter++;
    	} // Freq table created.
    	final TreeNode root = buildHuffmanTree(freq);
    	readTree(bIS, bOS, root);
    }
	
	// Decompresses the file using the Standard Tree Format embedded in the compressed file. 
    private void storeTreeDecompression(BitInputStream bIS, BitOutputStream bOS) throws IOException {
    	int limit = bIS.readBits(BITS_PER_INT); // Size of the tree in bits
    	String treeCode = ""; // binary representation of the tree. (Written w/ preorder traversal)
    	int bit = 0;
    	while(bit < limit) {
    		treeCode += bIS.readBits(1);
    		bit++;
    	}
		root = buildTreeSTF(root, treeCode, limit);
		readTree(bIS, bOS, root);
	}
    
    /** Recursively builds the tree by reading bit-by-bit and traversing preorder. 
     * This tree does not take into account frequencies for nodes, as they don't matter in
     * this particular way of rebuilding the tree.
     * @return the root node of the built tree
     * @throws IOException if a non-binary digit is found. 
     */
    private TreeNode buildTreeSTF(TreeNode root, String treeCode, int limit) throws IOException  {
    	if(i < limit) {
    		char bit = treeCode.charAt(i);
    		i++;
    		if(bit == '0') { // null node (non-leaf)
    			root = new TreeNode(0, 0);
    			root.setLeft(buildTreeSTF(root.getLeft(), treeCode, limit));
    			bit = treeCode.charAt(i);
    			root.setRight(buildTreeSTF(root.getRight(), treeCode, limit));
    		} else if (bit == '1') { // leaf value
    			String value = "";
    			for(int i = 0; i < 9; i++) {
    				value += treeCode.charAt(this.i);
    				this.i++;
    			}
    			return new TreeNode(BinaryConvert.toDecimal(value), 0);
    		} else {
    			throw new IOException("Error reading file. Corrupted bit found.");
    		}
    	}
    	return root;
	}

    
    // Reads the tree using both formats, it just needs a root linking a Huffman tree.
	private void readTree(BitInputStream bIS, BitOutputStream bOS, final TreeNode root) throws IOException {
		TreeNode current = root;
    	int bit = bIS.readBits(1);
    	boolean done = false;
    	while(!done) {
    		if(bit == -1) { // A proper .hf should never end without P_EOF
    			throw new IOException("Error reading compressed file. \n" + 
    									"unexpected end of input. "
    										+ "	no PSEUDO_EOF value.");
    		}
    		while(!current.isLeaf()) {
    			if(bit == 1) {
    				current = current.getRight();
    			} else if(bit == 0) {
    				current = current.getLeft();
    			} 
    			bit = bIS.readBits(1);
    		}
    		bOS.write(current.getValue());
    		done = (current.getValue() == PSEUDO_EOF);
    		current = root;
    	}
	}

	public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

}







