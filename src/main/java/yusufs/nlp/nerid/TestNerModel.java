/**
 * 
 */
package yusufs.nlp.nerid;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import yusufs.nlp.nerid.utils.ReadLine;
import yusufs.nlp.nerid.utils.TextSequence;
import yusufs.nlp.nerid.utils.TextSequence.Sentence;
import yusufs.nlp.nerid.utils.TextSequence.Words;

/**
 * @author yusuf
 *
 */
public class TestNerModel {
	final static Logger logger = Logger.getLogger(TestNerModel.class);
	
	// getter setter
	private HashMap<String, Integer> dataCounter = new HashMap<>();
	private int total = 0;
	private int correct = 0;
	private int missing = 0;
	private int wrong = 0;
	private int truePositive = 0; // true positive jika di data test itu merupakan entitas (NE) dan dideteksi sebagai NE juga
	private int trueNegative = 0; // true negative jika di data test itu bukan entitas (OTHER) dan dideteksi sebagai OTHER juga
	private int falsePositive = 0; // False Positive ketika sebenarnya di data testing OTHER tetapi dideteksi sebagai entitas (NE)
	private int falseNegative = 0; // False Negative ketika sebenarnya di data tes merupakan entitas (NE) tetapi dideteksi sebagai OTHER
	
	
	private double accuracy = 0.0;
	private double recall = 0.0;
	private double precision = 0.0;
	
	private int totalKalimat = 0;
	public TestNerModel() {
		
	}
	
	public TestNerModel(HashMap<String, Integer> datacounter, int correct, int missing, int wrong,
			int truePositive, int trueNegative, int falsePositive, int falseNegative, int jumlahkalimat) {
		this.dataCounter = datacounter;
		
		this.total = truePositive + trueNegative + falsePositive + falseNegative;
		
		this.correct = correct;
		this.missing = missing;
		this.wrong = wrong;
		
		this.truePositive = truePositive;
		this.trueNegative = trueNegative;
		this.falsePositive = falsePositive;
		this.falseNegative = falseNegative;
		
		this.accuracy = ((double) (truePositive + trueNegative) / (double) (truePositive + trueNegative + falsePositive + falseNegative));
		this.recall = ((double) (truePositive) / (double) (truePositive + falseNegative));
		this.precision = ((double) (truePositive) / (double) (truePositive + falsePositive));
		
		this.totalKalimat = jumlahkalimat;
	}
	
	/**
	 * @return the dataCounter
	 */
	public HashMap<String, Integer> getDataCounter() {
		return dataCounter;
	}
	
	/**
	 * 
	 * @return the total
	 */
	public int getTotal() {
		return total;
	}
	
	/**
	 * 
	 * @return the correct
	 */
	public int getCorrect() {
		return correct;
	}
	
	/**
	 * 
	 * @return the missing
	 */
	public int getMissing() {
		return missing;
	}
	
	/**
	 * 
	 * @return the wrong
	 */
	public int getWrong() {
		return wrong;
	}
	
	/**
	 * 
	 * @return the truePositive
	 */
	public int getTruePositive() {
		return truePositive;
	}
	
	/**
	 * 
	 * @return the trueNegative
	 */
	public int getTrueNegative() {
		return trueNegative;
	}
	
	/**
	 * 
	 * @return the falsePositive
	 */
	public int getFalsePositive() {
		return falsePositive;
	}
	
	/**
	 * 
	 * @return the falseNegative
	 */
	public int getFalseNegative() {
		return falseNegative;
	}
	
	/**
	 * 
	 * @return the accuracy
	 */
	public double getAccuracy() {
		return accuracy;
	}
	
	/**
	 * 
	 * @return the recall
	 */
	public double getRecall() {
		return recall;
	}
	
	/*
	 * 
	 * @return the precision
	 */
	public double getPrecision() {
		return precision;
	}
	
	
	/**
	 * 
	 * @return the totalKalimat
	 */
	public int getTotalKalimat() {
		return totalKalimat;
	}

	/**
	 * Test NER model with data test
	 * @param datatest
	 * @throws Exception 
	 */
	public TestNerModel doTest(File testData, NERModel nermodel) throws Exception {
		
		// Pertama baca dulu baris per baris, anggap saja satu baris itu satu kalimat.
		ReadLine file = new ReadLine(testData);
		ArrayList<String> sentences;
		try {
			sentences = file.read();
		} catch (Throwable e1) {
			throw new Exception(e1.getMessage());
		}
		HashMap<String, Integer> counter = new HashMap<>();
		
		TextSequence ws = new TextSequence();
		int counterCorrect = 0;
		int counterMissing = 0;
		int counterWrong = 0;
		
		int countertp = 0;
		int countertn = 0;
		int counterfp = 0;
		int counterfn = 0;
		
		int counterkalimat = 0;
		
		for(String sentence : sentences) {
			counterkalimat = counterkalimat + 1;
			Sentence kalimatDataTes;
			try {
				kalimatDataTes = ws.wordSeqWithTag(sentence, true);
			} catch (Exception e) {
				logger.error("Gagal dalam membaca data uji.");
				e.printStackTrace();
				throw new Exception("Gagal dalam membaca data uji.");
			}
			
			// lakukan prediksi kalimat
			sentence = sentence.trim();
			Prediction predictNer = new Prediction();
			Sentence kalimatHasilPrediksi = predictNer.predictSentence(sentence, true, nermodel);
			
			if(kalimatDataTes.getWords().size() == kalimatHasilPrediksi.getWords().size()) {
				for(int i=0; i<kalimatDataTes.getWords().size(); i++) {
					// Hitung jumlah datanya dari data test
					Words wordsDataPrediksi = kalimatHasilPrediksi.getWords().get(i);
					Words wordsDataTes = kalimatDataTes.getWords().get(i);
					
					if(counter.containsKey(wordsDataTes.getXmlTag())) {
						int increment = counter.get(wordsDataTes.getXmlTag()) + 1;
						counter.put(wordsDataTes.getXmlTag(), increment);
					} else {
						counter.put(wordsDataTes.getXmlTag(), 1);
					}
					
					// perbandingkan
					// missing = jika data test != OTHER && hasil prediksi == OTHER
					// correct = jika data test == hasil prediksi
					// wrong = data test == OTHER && hasil prediksi == yang lain
					
					if(wordsDataTes.getXmlTag() != "OTHER") {
						if(wordsDataPrediksi.getXmlTag() != "OTHER") {
							counterCorrect = counterCorrect + 1; // correct
							// bukan other dan diprediksi bukan other <- true positive
							countertp = countertp + 1;
						} else {
							counterMissing = counterMissing + 1; // missing
							// bukan other tapi dideteksi sebagai other <- false negative
							counterfn = counterfn + 1; 
						}
					} else {
						if(wordsDataPrediksi.getXmlTag() != "OTHER") {
							counterWrong = counterWrong + 1; // wrong
							// sebenarnya other, tapi dideteksi sebagai entitas (bukan other) <- false positive
							counterfp = counterfp + 1;
						} else {
							// sebenarnya other, dan dideteksi sebagai other <- true negatove
							countertn = countertn + 1;
						}
					}
				}
			} else {
				logger.error("Data uji tidak dapat diperbandingkan.");
				throw new Exception("Data uji tidak dapat diperbandingkan.");
			}			
			
		}
		
		dataCounter = counter;
		correct = counterCorrect;
		missing = counterMissing;
		wrong = counterWrong;
		
		truePositive = countertp;
		trueNegative = countertn;
		falsePositive = counterfp;
		falseNegative = counterfn;
		
		return new TestNerModel(dataCounter, correct, missing, wrong, 
				countertp, countertn, counterfp, counterfn, counterkalimat);
	}
	
}
