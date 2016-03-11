/**
 * 
 */
package yusufs.nlp.nerid;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import yusufs.nlp.tokenizerid.Tokenizer;
import yusufs.nlp.nerid.utils.TextSequence;
import yusufs.nlp.nerid.utils.TextSequence.Sentence;

/**
 * @author Yusuf Syaifudin
 *
 */
public class Prediction {
	
	final static Logger logger = Logger.getLogger(Prediction.class);
			
	public Prediction() {
		
	}
	
	/**
	 * Melakukan prediksi label entitas terhadap data teks dengan menggunakan model yang diberikan
	 * @param data
	 * @param withPunctuation
	 * @param nermodel
	 * @return ArrayList<Sentence>
	 */
	public ArrayList<Sentence> predict(String data, Boolean withPunctuation, NERModel nermodel) {
		Tokenizer tokenizer = new Tokenizer();
		TextSequence ws = new TextSequence();
		Viterbi viterbi = new Viterbi();
		
		ArrayList<Sentence> allPredictedSentence = new ArrayList<>();
		
		// Pertama, lakukan ekstraksi kalimat.
		ArrayList<String> sentences = tokenizer.extractSentence(data);
		
		// Untuk setiap kalimat yang ditemukan, lakukan tokensisasi 
		// untuk memastikan kalimat sudah normal (tanda baca dipisah menjadi token tersendiri). 
		for(String sentence : sentences) {			
			try {
				Sentence sentenceWithWordSeq = ws.wordSeqWithTag(sentence, withPunctuation);
				Sentence predictedSentence = viterbi.decode(sentenceWithWordSeq, nermodel);
				allPredictedSentence.add(predictedSentence);
				
			} catch (Exception e) {
				logger.error("Gagal melakukan prediksi.");
				e.printStackTrace();
				new Exception("Gagal melakukan prediksi.");
			}
			
		}
		
		return allPredictedSentence;
	}
	
	/**
	 * Memprediksi label entitas terhadap hanya satu kalimat saja
	 * @param sentence
	 * @param withPunctuation
	 * @param nermodel
	 * @return
	 */
	public Sentence predictSentence(String sentence, Boolean withPunctuation, NERModel nermodel) {
		TextSequence ws = new TextSequence();
		Viterbi viterbi = new Viterbi();
		
		Sentence predictedSentenceResult = null;
		
		// Anggap string itu kalimat.
		// Untuk setiap kalimat yang ditemukan, lakukan tokensisasi 
		// untuk memastikan kalimat sudah normal (tanda baca dipisah menjadi token tersendiri). 
		try {
			Sentence sentenceWithWordSeq = ws.wordSeqWithTag(sentence, withPunctuation);
			Sentence predictedSentence = viterbi.decode(sentenceWithWordSeq, nermodel);
			
			predictedSentenceResult = predictedSentence;
			
		} catch (Exception e) {
			logger.error("Gagal melakukan prediksi.");
			e.printStackTrace();
			new Exception("Gagal melakukan prediksi.");
		}
		
		return predictedSentenceResult;
	}
	
}
