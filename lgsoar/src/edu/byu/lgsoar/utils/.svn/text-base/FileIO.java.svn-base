package edu.byu.lgsoar.utils;
import java.util.Scanner;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
/**
* Class for doing basic file stuff. Prevents lots of duplicate error catching code, which checkstyle dislikes.
*/
public class FileIO{
	/**
	 * 
	 * @param fileName Name of file to read
	 * @return Scanner for file
	 */
	public static Scanner fileScanner(String fileName){
		Scanner scanner = null;
		if(fileName == null)
			throw new IllegalArgumentException();
		try{
			scanner = new Scanner(new File(fileName));
		}
		catch(IOException e){
//			System.out.println(e);
			e.printStackTrace();
			return null;
		}
		return scanner;
	}
	/**
	 * 
	 * @param fileName Name of file to read
	 * @return fileReader for file
	 */
	public static FileReader fileReader(String fileName){
		if(fileName == null)
			throw new IllegalArgumentException();
		FileReader fr;
		try{
			fr = new FileReader(fileName);
		}
		catch(FileNotFoundException e)
		{
			System.out.println(fileName + " not found");
			return null;
		}
		return fr;
	}
	/**
	 * 
	 * This creates a FileWriter object for the specified file. IT ALWAYS OVERWRITES THE CURRENT FILE.
	 * @param fileName Name of file to write to
	 * @return
	 */
	public static FileWriter fileWriter(String fileName){
		FileWriter fw = null;
		try {
			fw = new FileWriter(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fw;
	}
	/**
	 * 
	 * @param fw FileWriter to close
	 * @return True if it closed normally, false if there were any errors
	 */
	public static boolean close(FileWriter fw){
		try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	/**
	 * 
	 * @param fw FileWriter to write to
	 * @param string to write
	 * @return true if the string was written, false if there were any errors
	 */
	public static boolean write(FileWriter fw, String string){
		try {
			fw.write(string);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
