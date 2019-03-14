//Author: Kainoa Giomi

package Project2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Compresses and decompresses files using the standard Huffman algorithm for encoding and decoding
 */
public class HuffmanSubmit implements Huffman {
	////256 because there are 256 unique and valid characters
	public final int CHAR = 256;
	
	/**
	 * Nested Node class for building Huffman trees with
	 */
	class Node
	{
		
		private char ch;
	    private int freq;
	    public Node left, right;
	    /**
	     * Constructor for a node of Huffman tree
	     * 
	     * @param ch the character
	     * @param freq the frequency of the character
	     * @param left the left child of the node
	     * @param right the right child of the node
	     */
	    public Node(char ch, int freq, Node left, Node right) {
            this.ch   = ch;
            this.freq  = freq;
            this.left  = left;
            this.right = right;
        }
	    /**
	     * Checks whether or not a given node is a leaf
	     * 
	     * @return true if node is a leaf; otherwise false
	     */
	    public boolean isLeaf() {
	    	if(left == null && right == null)
	    		return true;
	    	return false;
	    }
	    /**
	     * Gets the frequency of a character referenced by the node
	     * 
	     * @return the frequency of the character
	     */
	    public int getFreq() {
	    	return this.freq;
	    }
	    /**
	     * Gets the character referenced by the node
	     * 
	     * @return the character
	     */
	    public char getCharacter() {
	    	return this.ch;
	    }
	}
	
	/**
	 * Nested comparator class for sorting the priority queue of nodes before being converted into a Huffman tree
	 */
	class freqComparator implements Comparator<Node>{ 
		/**
		 * Comparing two nodes with one another by their respective frequencies
		 */
        public int compare(Node n1, Node n2) { 
            return n1.freq - n2.freq;    
        } 
        	
    } 
	
	/**
	 * Finds all the characters in the Huffman tree and retrieves each character's respective unique Huffman code
	 * 
	 * @param st the array of Huffman codes, where the index is the character code (0 - 255)
	 * @param n the node
	 * @param s the binary Huffman code
	 */
	private void huffmanCoder(String[] st, Node n, String s) {
        if (!n.isLeaf()) {
            huffmanCoder(st, n.left,  s + '0');
            huffmanCoder(st, n.right, s + '1');
        }
        else {
            st[n.getCharacter()] = s;
        }
    }
	
	/**
	 *     Encodes the input file using Huffman Coding. Produces two files 
	 *    
	 *     @param inputFile the name of the input file to be encoded 
	 *     @param outputFile the name of the output file, which is a binary file
	 *     @param freqFile the file which stores the frequency of each byte 
	 */
	public void encode(String inputFile, String outputFile, String freqFile){
		//Takes input from inputFile and adds each char to an ArrayList of chars
		BinaryIn in = new BinaryIn(inputFile);
		//chars is an array of all the characters in the inputFile
		ArrayList<Character> charArray = new ArrayList<Character>();
		//chars is an array of unique characters in the inputFile
		char[] chars = new char[CHAR];
		int index = 0;
		//reads characters from inputFile until the end of the file
		while(!in.isEmpty()) {
			charArray.add(in.readChar());
		}
		
		//Utilizes Map data structure for holding each character and frequency of the character found in the inputFile
		Map <Character, Integer> map = new HashMap<Character, Integer>();
		for(char c: charArray) {
			//If character is already in map, increment frequency by one
			if(map.containsKey(c)) {
				map.put(c, new Integer(map.get(c) + 1));
			}
			//Else, add the new character to the map and the chars array and increment the index of chars
			else {
				map.put(c, new Integer(1));
				chars[index] = c;
				index++;
			}
		}
		
		//Outputs the frequency file, which contains the frequencies of each unique character
		BinaryOut out = new BinaryOut(freqFile);
		int i =0;
		//iterates for all the characters in chars
		while(i < index) {
			String s = Integer.toBinaryString(chars[i]);
			//padding 0's on binary strings with length less than 8
			while(s.length() < 8)
				s = "0" + s;
			//writes the binary code for the respective character to the freqFile
			out.write(s);
			out.write(':');
			//writes the frequency for the respective character to the freqFile
			out.write(Integer.toString(map.get(chars[i])));
			out.write(" ");
			i++;
			out.flush();
		}
		
		//Priority queue holding all the nodes sorted in ascending ordered by frequency
		PriorityQueue<Node> pq = new PriorityQueue<Node>(index, new freqComparator());
		for(int j = 0; j < index; j++) {
			Node n = new Node(chars[j], map.get(chars[j]), null, null);
			pq.add(n);
		}
         
		//builds the Huffman tree from the Priority queue, which is achieved once the size of the queue is 1
		while(pq.size() > 1) {
			Node left = pq.poll();
			Node right = pq.poll();
			Node parent = new Node('\0', left.getFreq() + right.getFreq(), left, right);
			pq.add(parent);
		}
		//Sets the newly built Huffman tree to Node tree
		Node tree = pq.poll();
		
		//String array codes holds Huffman codes corresponding to index in chars
		//String array codesByASC holds Huffman codes corresponding to the code of the char
		String[] codes = new String[index], codesByASC = new String[CHAR];
		String s = "";
		//Finds all Huffman codes of characters in the Huffman tree node and places them in codesByASC
		huffmanCoder(codesByASC, tree, s);
		//Places Huffman codes in codesByASC and places them in codes by the index which corresponds with the index of the character in chars
		for(int j = 0; j < index; j++) {
			codes[j] = codesByASC[chars[j]];
		}
		
		//Writes encoded outputFile
		BinaryOut enc = new BinaryOut(outputFile);	
		//for every character in the inputFile
		for(char c : charArray){
			int k = 0;
			//finds character equivalent in chars array
			while(chars[k] != c) {
				k++;
			}
			//writes Huffman code onto outputFile
			for (int m = 0; m < codes[k].length(); m++) {
                if (codes[k].charAt(m) == '0') {
                    enc.write('0');
                }
                else{
                    enc.write('1');
                }
			} 
			enc.flush();
		}
	}

	/**
	 *     Decodes the encoded input file using Huffman decoding.  
	 *    
	 *     @param inputFile the name of the input file to be decoded
	 *     @param outputFile he name of the output file 
	 *     @param freqFile the freqFile produced after encoding
	 */
	public void decode(String inputFile, String outputFile, String freqFile){
		//Finds frequency of each character from freqFile
		BinaryIn in = new BinaryIn(freqFile);
		//charArray the strings representing the unique characters from the freqFile
		String[] charArray = new String[CHAR];
		//freqString the strings representing frequencies of characters from the freqFile
		String[] freqString = new String[CHAR];
		//i represents colons, j represents spaces
		int index = 0, i = 0, j = 0;
		String s = "";
		//reads characters from freqFile until the end of the file
		while(!in.isEmpty()) {
			String input = in.readChar() + "";
			//Same number of colons and spaces found
			if(i == j) {
				if(input.equals(":")) {
					//character finished and added to charArray, number of colons found is incremented
					charArray[index] = s;
					s = "";
					i++;
				}
				else
					//add readChar() to current string 
					s += input;
			//More colons found than spaces
			}
			else if(i > j) {
				if(input.equals(" ")) {
					//frequency finished and added to freqString, number of spaces found is inremented
					freqString[index] = s;
					s = "";
					index++;
					j++;
				}
				else
					//add readChar() to current string 
					s += input;
			}
		}
		//converts strings of characters and frequencies from arrays made above to an array of characters and integers, respectively
		char[] chars = new char[index];
		int[] freq = new int[index];
		for(int k = 0; k < index; k++) {
			chars[k] = (char)Integer.parseInt(charArray[k], 2);
			freq[k] = Integer.parseInt(freqString[k]);
		}
		
		//Priority queue holding all the nodes sorted in ascending ordered by frequency
		PriorityQueue<Node> pq = new PriorityQueue<Node>(index, new freqComparator());
		for(int k = 0; k < index; k++) {
			Node n = new Node(chars[k], freq[k], null, null);
			pq.add(n);
		}
        
		//builds the Huffman tree from the Priority queue, which is achieved once the size of the queue is 1
		while(pq.size() > 1) {
			Node left = pq.poll();
			Node right = pq.poll();
			Node parent = new Node('\0', left.getFreq() + right.getFreq(), left, right);
			pq.add(parent);
		}
		//Sets the newly built Huffman tree to Node tree
		Node tree = pq.poll();
		
		//String array codes holds Huffman codes corresponding to index in chars
		//String array codesByASC holds Huffman codes corresponding to the code of the char
		String[] codes = new String[index], codesByASC = new String[CHAR];
		//Finds all Huffman codes of characters in the Huffman tree node and places them in codesByASC
		huffmanCoder(codesByASC, tree, s);
		//Places Huffman codes in codesByASC and places them in codes by the index which corresponds with the index of the character in chars
		for(int k = 0; k < index; k++) {
			codes[k] = codesByASC[chars[k]];
		}
		
		//Takes in the encoded file made from the encode method and decodes it, writing the decoded file in outputFile
		BinaryIn in2 = new BinaryIn(inputFile);
		BinaryOut dec = new BinaryOut(outputFile);
		String binaryHuff = "";
		while(!in2.isEmpty()) {
			//adds 0's and 1's from encoded file to binaryHuff until it matches a Huffman code of one of the unique characters
			binaryHuff += in2.readChar();
			int k = 0;
			for(String code : codes) {
				//if Huffman code found, binaryHuff is cleared and the corresponding character is printed in the output file
				if(code.equals(binaryHuff)) {
					dec.write(chars[k]);
					dec.flush();
					binaryHuff = "";
					freq[k]--;
				}
				k++;
			}
		}
	}

	/**
	 * Main function for HuffmanSubmit.java, tests encoding and decoding methods
	 */
	public static void main(String[] args) {
		Huffman  huffman = new HuffmanSubmit();
		//The decoded versions of ur.jpg and alice30.txt are the same as the originals
		huffman.encode("ur.jpg", "ur.enc", "freq.txt");
		huffman.decode("ur.enc", "ur_dec.jpg", "freq.txt");
		
		huffman.encode("alice30.txt", "ur.enc", "freq.txt");
		huffman.decode("ur.enc", "alice30_dec.txt", "freq.txt");
	}

}
