package HuffmanCoding;

/**
 * A class that converts a given integer to a binary representation (9-bit) as a 
 * <code> String. </code> 
 * @author Abigail Paden, Abraham Martinez
 * @version 1.0
 *
 */

public class BinaryConvert {
	
	private static final int[] binaryTable = {256, 128, 64, 32, 16, 8, 4, 2, 1};
	
	/**@return A string representation of the number's 9-bit binary code */
	public static String toBinary(int number) {
		String result = "";
		for(int i : binaryTable) {
			if(number < i) {
				result += "0";
			} else { // i < number
				number -= i;
				result += "1";
				
			}
		}
		return result;
	}
	
	/**@return The Integer representation of the 9-bit binary code */
	public static int toDecimal(String code) {
		int expectedCodeLength = 9;
		if(code == null || code.length() < expectedCodeLength) {
			throw new IllegalArgumentException("Code length is less than 9 bits.");
		} 
		int result = 0;
		for(int i = 0; i < code.length(); i++) {
			if(code.charAt(i) == '1') {
				 result += binaryTable[i]; 
			}
		}		
		return result;		
	}
	
}






