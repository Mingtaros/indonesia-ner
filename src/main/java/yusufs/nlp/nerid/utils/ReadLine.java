/**
 * 
 */
package yusufs.nlp.nerid.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * @author Yusuf Syaifudin
 *
 */
public class ReadLine {
	
	final static Logger logger = Logger.getLogger(ReadLine.class);

	private File file;
	/**
	 * 
	 */
	public ReadLine(File file) {
		this.setFile(file);
	}
	
	public ArrayList<String> read() throws Exception {
		FileReader fileReader = new FileReader(file);
		try (BufferedReader br = new BufferedReader(fileReader)) {
		    String line;
		    ArrayList<String> string_line = new ArrayList<>();
		    while ((line = br.readLine()) != null) {
		       // process the line.
		    	string_line.add(line);
		    }
		    
		    return string_line;
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public String readToString() throws Exception {
		FileReader fileReader = new FileReader(file);
		try (BufferedReader br = new BufferedReader(fileReader)) {
		    String line;
		    String string_line = "";
		    while ((line = br.readLine()) != null) {
		       // process the line.
		    	string_line += line;
		    }
		    
		    return string_line;
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return this.file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}
	
	

}
