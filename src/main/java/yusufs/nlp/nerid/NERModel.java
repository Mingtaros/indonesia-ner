/**
 * 
 */
package yusufs.nlp.nerid;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Yusuf Syaifudin
 *
 */
public class NERModel {

	final static Logger logger = Logger.getLogger(NERModel.class);
	
	private final HashMap<String, Double> startProbability;
	private final HashMap<String, HashMap<String, Double>> transitionProbability;
	
	private final HashMap<String, HashMap<String, Double>> previousLexicalClass;
	private final HashMap<String, HashMap<String, Double>> currentLexicalClass;
	private final HashMap<String, HashMap<String, Double>> nextLexicalClass;
		
	public NERModel() {
		this.startProbability = null;
		this.transitionProbability = null;
		this.previousLexicalClass = null;
		this.currentLexicalClass = null;
		this.nextLexicalClass = null;
	}
	
	public NERModel(HashMap<String, Double> startProbability,
			HashMap<String, HashMap<String, Double>> transitionProbability,
			HashMap<String, HashMap<String, Double>> previousLexicalClass,
			HashMap<String, HashMap<String, Double>> currentLexicalClass,
			HashMap<String, HashMap<String, Double>> nextLexicalClass) {
		this.startProbability = startProbability;
		this.transitionProbability = transitionProbability;
		this.previousLexicalClass = previousLexicalClass;
		this.currentLexicalClass = currentLexicalClass;
		this.nextLexicalClass = nextLexicalClass;
	}

	/**
	 * @return the startProbability
	 */
	public HashMap<String, Double> getStartProbability() {
		return startProbability;
	}

	/**
	 * @return the transitionProbability
	 */
	public HashMap<String, HashMap<String, Double>> getTransitionProbability() {
		return transitionProbability;
	}

	/**
	 * @return the previousLexicalClass
	 */
	public HashMap<String, HashMap<String, Double>> getPreviousLexicalClass() {
		return previousLexicalClass;
	}

	/**
	 * @return the currentLexicalClass
	 */
	public HashMap<String, HashMap<String, Double>> getCurrentLexicalClass() {
		return currentLexicalClass;
	}

	/**
	 * @return the nextLexicalClass
	 */
	public HashMap<String, HashMap<String, Double>> getNextLexicalClass() {
		return nextLexicalClass;
	}
	
	/**
	 * Mendapatkan probabilitas awal untuk key yang diberikan.
	 * @param key
	 * @return
	 */
	public double getStartProb(String key) {
		return startProbability.get(key) == null ? 0 : startProbability.get(key);
	}
	
	/**
	 * Mendapatkan probabilitas transisi dari key (tag XML) ke tag XML selanjutnya.
	 * @param from
	 * @param to
	 * @return
	 */
	public double getTransitionProb(String from, String to) {
		return transitionProbability.get(from).get(to) == null ? 0 : transitionProbability.get(from).get(to);
	}
	
	/**
	 * Mendapatkan probabilitas bersyarat, yaitu dengan melihat kelas kata sebelum, miliknya dan sesudah.
	 * @param nertag
	 * @param prevLexTag
	 * @param currentLexTag
	 * @param nextLexTag
	 * @return
	 */
	public double getEmissionProb(String nertag, String prevLexTag, String currentLexTag, String nextLexTag) {
		double prevProb = previousLexicalClass.get(nertag).get(prevLexTag) == null ? 0 : previousLexicalClass.get(nertag).get(prevLexTag);
		double currentProb = currentLexicalClass.get(nertag).get(currentLexTag) == null ? 0 : currentLexicalClass.get(nertag).get(currentLexTag);
		double nextProb = nextLexicalClass.get(nertag).get(nextLexTag) == null ? 0 : nextLexicalClass.get(nertag).get(nextLexTag);
		
		return prevProb * currentProb * nextProb;
 	}
	
	
	/**
	 * Membaca model dari file json yang telah dibuat
	 * @param modelfile
	 * @return
	 */
	public NERModel loadModel(File modelfile) {		 
		try {
  
			Path path = Paths.get(modelfile.getAbsolutePath());

		    String stringFromFile = java.nio.file.Files.lines(path).collect(
		            Collectors.joining());
		    
		    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
			NERModel model = gson.fromJson(stringFromFile, NERModel.class);
			return model;
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Gagal membaca model.");
			new Exception("Gagal membaca model.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Gagal membaca model.");
			new Exception("Gagal membaca model.");
		}
		return this;
	}
	
}
