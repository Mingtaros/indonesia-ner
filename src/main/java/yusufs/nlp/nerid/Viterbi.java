/**
 * Algoritma viterbi.
 */
package yusufs.nlp.nerid;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import yusufs.nlp.nerid.utils.TextSequence.Sentence;
import yusufs.nlp.nerid.utils.TextSequence.Words;

/**
 * @author Yusuf Syaifudin
 *
 */
public class Viterbi {
	
	final static Logger logger = Logger.getLogger(Viterbi.class);
	
	final static String[] NERTAG = Config.NERTAG;

	/**
	 * Melakukan perhitungan dengan menggunakan algoritma viterbi dan menghasilkan Sentence
	 * @param sentence
	 * @param nermodel
	 * @return
	 */
	public Sentence decode(Sentence sentence, NERModel nermodel) {

		ArrayList<Words> words = sentence.getWords();		
		
		// Setting matrix untuk menyimpan hasil perhitungan.
		double[][] matrix = new double[NERTAG.length][words.size()]; 
		
		// Variable untuk menyimpan setiap hasil perhitungan.
		HashMap<String, Double> savedState = new HashMap<>();
		
		// For dari 0 hingga panjang kalimat.
		for(int i=0; i<words.size(); i++) {
			
			HashMap<String, Double> temp = new HashMap<>();
			
			// For dari 0 hingga panjang NERTAG (untuk semua NER TAG yang ada).
			for(int j=0; j<NERTAG.length; j++) {
				// observedState = tag NER yang akan dicari, yaitu semua kemungkinan tag.
				String nertag = NERTAG[j];
				
				// Jika i == 0 maka awal kalimat, dan gunakan start probability.
				if(i==0) {
					String prevLex = "START";
					String currLex = words.get(0).getPosTag();
					String nextLex = "";
					
					try {
						nextLex = words.get(1).getPosTag();
					} catch (Exception e) {
						nextLex = "END";
					}
					
					double startProb = nermodel.getStartProb(nertag);
					double emissionProb = nermodel.getEmissionProb(nertag, prevLex, currLex, nextLex);
					
					double probability = startProb * emissionProb;
					
					matrix[j][i] = probability;
					temp.put(nertag, probability);
					
				} else {
					double max = 0;
					
					for(String ner : NERTAG) {
						double sigma = savedState.get(ner) * nermodel.getTransitionProb(ner, nertag);
						
						if(sigma >= max) {
							max = sigma;
						}
					}
					
					String prevLexTag = words.get(i - 1).getPosTag();
					String currentLexTag = words.get(i).getPosTag();
					
					String nextLexTag = "";
					if(i < words.size() - 1) {
						nextLexTag = words.get(i + 1).getPosTag();
					} else {
						nextLexTag = "END";
					}
					
					double probability =  max * nermodel.getEmissionProb(nertag, prevLexTag, currentLexTag, nextLexTag);
					
					matrix[j][i] = probability;
					temp.put(nertag, probability);
				}
			}
			savedState = temp;
		}
		
		// Highest probability backward.
		int[] sequenceIndex = new int[words.size()];
		
		for(int i = words.size() - 1; i >= 0; i--) {
			int stateIndex = 0;
			
			for(int j=0; j < NERTAG.length; j++) {
				if(matrix[j][i] > matrix[stateIndex][i]) {
					stateIndex = j;
				}
			}
			sequenceIndex[i] = stateIndex;
			
			words.get(i).setXmlTag(NERTAG[stateIndex]);
		}
		
		return sentence;
	}
}
