package HuffmanCoding;

/**
 * A small class for displaying any type of tree horizontally.
 * This is a rudimentary tool. Alternative, more efficient viewing tools
 * exist such as jGRAPS's canvas mode.
 * 
 * @author Mike Scott <br> Minor changes by Abraham Martinez
 * @version 1.0
 * 
 */

public class TreeDisplay {

	
    static void printTree(final TreeNode root) {
        printTree(root, "");
    }

    static void printTree(TreeNode n, String spaces) {
        if(n != null){
            printTree(n.getRight(), spaces + "  ");
            System.out.println(spaces + n.getValue());
            printTree(n.getLeft(), spaces + "  ");
        }
    }
}
