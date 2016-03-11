/**
 * 
 */
package yusufs.nlp.nerid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import yusufs.nlp.nerid.utils.TextSequence.Sentence;
import yusufs.nlp.nerid.utils.TextSequence.Words;

/**
 * @author Yusuf Syaifudin
 *
 */
public class Counter {
	
	final static Logger logger = Logger.getLogger(Counter.class);
	
	public Counter() {
		
	}
	
	public Counter(ArrayList<Sentence> sentences) {
		countNow(sentences);
	}
	
	/**
	 * Menghitung probabilitas awal, transisi antar tag dan peluang bersyarat dengan melihat kelas kata,
	 * baik kelas kata sebelum, sesudah ataupun kelas kata milik kata itu sendiri.
	 * 
	 * @param sentences ArrayList dari kalimat (dapat juga disebut kumpulan kalimat).
	 * @return
	 */
	public void countNow(ArrayList<Sentence> sentences) {
		// Mari lakukan perhitungan.
		logger.info("Mulai melakukan perhitungan probabilitas.");
		HashMap<String, Integer> countStartTag = new HashMap<>();
		HashMap<String, HashMap<String, Integer>> countTransitionXmlTagToNextXmlTag = new HashMap<>();
		
		HashMap<String, HashMap<String, Integer>> countPairOfCurrentXmlAndPrevLex = new HashMap<>();
		HashMap<String, HashMap<String, Integer>> countPairOfCurrentXmlAndCurrentLex = new HashMap<>();
		HashMap<String, HashMap<String, Integer>> countPairOfCurrentXmlAndNextLex = new HashMap<>();

		// ### Inisialisasi
		// Pertama, masukkan semua tag NER dan Part of Speech yang akan dicari,
		// dengan mendapatkan valuenya dari class Config.
		// Kemudian men-declare value-nya dengan 0;
		String[] NERTAG = Config.NERTAG;
		String[] POSTAG = Config.EXTENDED_POSTAG;

		
		for(String nertag : NERTAG) {
			countStartTag.put(nertag, 0);
			countTransitionXmlTagToNextXmlTag.put(nertag, new HashMap<String, Integer>());
			countPairOfCurrentXmlAndPrevLex.put(nertag,  new HashMap<String, Integer>());
			countPairOfCurrentXmlAndCurrentLex.put(nertag,  new HashMap<String, Integer>());
			countPairOfCurrentXmlAndNextLex.put(nertag,  new HashMap<String, Integer>());
			
			for(String ner : NERTAG) {
				countTransitionXmlTagToNextXmlTag.get(nertag).put(ner, 0);
			}
			
			for(String postag : POSTAG) {
				countPairOfCurrentXmlAndPrevLex.get(nertag).put(postag, 0);
				countPairOfCurrentXmlAndCurrentLex.get(nertag).put(postag, 0);
				countPairOfCurrentXmlAndNextLex.get(nertag).put(postag, 0);
			}
		}

		
		// Pertama iterasi wordSeq.
		for(int i=0; i<sentences.size(); i++) {
			
			// `sentences` berisi `Sentence` dan  berisi `ArrayList<Words>` 
			// yang merupakan sequence kata yang menyusun kalimat (sentence).
			// Jadi pada intinya, iterasi ini melakukan iterasi untuk setiap kalimat dan setiap kata.
			// Sentences -> Sentence -> diiterasi -> Words -> Word diiterasi -> sequence kata yang menyusun kalimat.
			Sentence s = sentences.get(i);
			ArrayList<Words> word = s.getWords();
			
			// ### PROBABILITAS AWAL
			// Lakukan perhitungan probabilitas awal.
			// Probabilitas awal = jumlah distinct tag yang ada di awal kata/semua tag di awal kata.
			// Yang dihitung ialah tag xml nya saja.
			// Dapatkan tag xml kata pada awal kalimat.
			String startTag = word.get(0).getXmlTag() == null ? "" : word.get(0).getXmlTag();				
			
			if(countStartTag.containsKey(startTag)) {
				// Jika hashmap telah memiliki kunci (key) itu, maka increment value-nya.
				int val = countStartTag.get(startTag);
				countStartTag.put(startTag, val + 1);
			} else { 
				// Jika belum punya, tambahkan saja sebagai key dan berikan default value = 1.
				countStartTag.put(startTag, 1);
			}
			
			
			// Iterasi untuk setiap kata.
			for(int j=0; j<word.size(); j++) {
				
				String currentXmlTag = word.get(j).getXmlTag();
				String currentLexicalTag = word.get(j).getPosTag();
				/* String currentToken = word.get(j).getToken(); */
				
				/* String prevXmlTag = ""; */
				String prevLexicalTag = "START";
				/* String prevToken = ""; */
				// Jika lebih dari nol, pasti memiliki index dibelakangnya dan seharusnya tidak IndexOutOfBound
				if(j > 0) {
					/* prevXmlTag = word.get(j-1).getXmlTag(); */
					prevLexicalTag = word.get(j-1).getPosTag();
					/* prevToken = word.get(j-1).getToken(); */
				}
				
				String nextXmlTag = "";
				String nextLexicalTag = "END";
				/* String nextToken = ""; */
				// Jika ukurannya kurang dari ukuran array dikurangi 1, maka pasti punya index lagi di depannya.
				if(j < word.size() - 1) {
					nextXmlTag = word.get(j+1).getXmlTag();
					nextLexicalTag = word.get(j+1).getPosTag();
					/* nextToken = word.get(j+1).getToken(); */
				}
				
				
				// ### TRANSITION PROBABILITY
				// Yaitu menghitung jumlah transisi tag satu ke tag lainnya.
				// Jika hashmap telah memiliki kunci (key) itu, 	
				if(countTransitionXmlTagToNextXmlTag.containsKey(currentXmlTag)) {
					// maka dapatkan dulu nilainya, yaitu berupa HashMap<String, Integer>.
					HashMap<String, Integer> nextTransition = countTransitionXmlTagToNextXmlTag.get(currentXmlTag);
					
					// Sebelumnya, cek untuk memastikan bahwa nextXmlTag bukan null.
					if(nextXmlTag != "") {
						// Jika bukan null, lalu coba cek apakah tag xml selanjutnya sudah dijadikan key
						// oleh HashMap nextTransition.
						
						if(nextTransition.containsKey(nextXmlTag)) {
							// Jika sudah dimiliki, maka increment valuenya.
							int value = nextTransition.get(nextXmlTag);
							nextTransition.put(nextXmlTag, value + 1);
						} else {
							// Jika melum memiliki, tambahkan key dan default valuenya ialah 1.
							nextTransition.put(nextXmlTag, 1);
						}
						
						// Lalu masukkan ke countTransitionToNextTag.
						countTransitionXmlTagToNextXmlTag.put(currentXmlTag, nextTransition);
					}
					
				} else {
					// Jika hashmap belum punya kunci itu, insert dulu dengan value null.
					countTransitionXmlTagToNextXmlTag.put(currentXmlTag, new HashMap<String, Integer>());
				}
			
				
				// ### Menghitung pasangan tag xml dengan tag leksikal
				// Untuk emission probability.
				// 
				// #### Hitung antara tag xml dengan leksikal kata sebelumnya.
				if(countPairOfCurrentXmlAndPrevLex.containsKey(currentXmlTag)) {
					HashMap<String, Integer> prev = countPairOfCurrentXmlAndPrevLex.get(currentXmlTag);
					
					if(prev.containsKey(prevLexicalTag)) {
						int count = prev.get(prevLexicalTag);
						prev.put(prevLexicalTag, count + 1);
					} else {
						prev.put(prevLexicalTag, 1);
					}
					
					countPairOfCurrentXmlAndPrevLex.put(currentXmlTag, prev);
				} else {
					countPairOfCurrentXmlAndPrevLex.put(currentXmlTag, new HashMap<String, Integer>());
				}
				
				
				// #### Hitung antara tag xml dengan leksikal dirinya sendiri.
				if(countPairOfCurrentXmlAndCurrentLex.containsKey(currentXmlTag)) {
					HashMap<String, Integer> current = countPairOfCurrentXmlAndCurrentLex.get(currentXmlTag);
					
					if(current.containsKey(currentLexicalTag)) {
						int count = current.get(currentLexicalTag);
						current.put(currentLexicalTag, count + 1);
					} else {
						current.put(currentLexicalTag, 1);
					}
					
					countPairOfCurrentXmlAndCurrentLex.put(currentXmlTag, current);
				} else {
					countPairOfCurrentXmlAndCurrentLex.put(currentXmlTag, new HashMap<String, Integer>());
				}
				
				
				// #### Hitung antara tag xml dengan leksikal kata sesudahnya.
				if(countPairOfCurrentXmlAndNextLex.containsKey(currentXmlTag)) {
					HashMap<String, Integer> next = countPairOfCurrentXmlAndNextLex.get(currentXmlTag);
					
					if(next.containsKey(nextLexicalTag)) {
						int count = next.get(nextLexicalTag);
						next.put(nextLexicalTag, count + 1);
					} else {
						next.put(nextLexicalTag, 1);
					}
					
					countPairOfCurrentXmlAndNextLex.put(currentXmlTag, next);
				} else {
					countPairOfCurrentXmlAndNextLex.put(currentXmlTag, new HashMap<String, Integer>());
				}
				
			} // Akhir dari iterasi kata.
			
		} // Akhir dari iterasi wordSeq.
		
		
		// Hitung probabilitasnya
		// ### Probabilitas awal.
		// Hitung pembaginya dulu, yaitu jumlahan semua tag yang ditemukan.
		int startProbDivider = 0;
		for(Entry<String, Integer> countStartTagEntry : countStartTag.entrySet()) {
			int value = countStartTagEntry.getValue();
			startProbDivider = startProbDivider + value;
		}
		
		
		// Sekarang lakukan pembagian untuk setiap tag yang ditemukan, 
		// ini akan menghasilkan nilai probabilitas awal.
		HashMap<String, Double> startProb = new HashMap<>();
		for(Entry<String, Integer> countStartTagEntry : countStartTag.entrySet()) {
			String key = countStartTagEntry.getKey();
			int value = countStartTagEntry.getValue();			
			double probability = (double) value / (double) startProbDivider;
			
			if(Double.isNaN(probability)) {
				probability = 0;
			}
			
			startProb.put(key, probability);
		}
		
		
		// ### Probabilitas transisi
		// Cari pembaginya dulu, yaitu jumlahan seluruh nilai pada tag yang ada.
		// Misal, pembagi untuk tag OTHER = sekian, tag PERSON = sekian
		HashMap<String, Integer> transitionDivider = new HashMap<>();
		for(Entry<String, HashMap<String, Integer>> entry : countTransitionXmlTagToNextXmlTag.entrySet()) {

			String key = entry.getKey();
			HashMap<String, Integer> value = entry.getValue();
			
			// Hitung untuk masing-masing tag (key).
			int increment = 0;
			for(Entry<String, Integer> valueEntry : value.entrySet()) {
				int val = valueEntry.getValue();
				increment = increment + val;
			}
			
			// Simpan.
			transitionDivider.put(key, increment);			
		}
		
		
		// Menghitung probabilitas transisi.
		HashMap<String, HashMap<String, Double>> transitionProb = new HashMap<>();
		for(Entry<String, HashMap<String, Integer>> entry : countTransitionXmlTagToNextXmlTag.entrySet()) {

			String parentkey = entry.getKey();
			HashMap<String, Integer> value = entry.getValue();
			
			// Sekarang hitung probabilitasnya untuk masing-masing transisi ke tag selanjutnya.
			// Misal, OTHER ke PERSON = jumlah tag transisi dari OTHER ke PERSON / pembagi dari tag OTHER (jumlahan dari tag OTHER).
			HashMap<String, Double> nextTransProb = new HashMap<>();
			for(Entry<String, Integer> valueEntry : value.entrySet()) {
				
				// Dapatkan key dan valuenya, value berupa jumlah kemunculan tag itu.
				String key = valueEntry.getKey();
				int val = valueEntry.getValue();
				
				// Dapatkan pembaginya.
				int pembagi = transitionDivider.get(parentkey);
				
				// Lakukan perhitungan.
				double probability = (double) val / (double) pembagi;
				
				if(Double.isNaN(probability)) {
					probability = 0;
				}
				
				// Simpan dengan tag ke i+1 sebagai key dan probability sebagai value.
				nextTransProb.put(key, probability);
			}
			
			// Untuk setiap key-value dari probability yang dihitung, masukkan ke transition prob.
			transitionProb.put(parentkey, nextTransProb);
		}
		
		
		// ### Probabilitas bersyarat.
		// Hitung pembagi untuk probabilitas antara tag xml kata sekarang dengan tag leksikal kata sebelumnya.
		// Hitung dulu pembaginya, yaitu jumlahan dari semuanya.
		HashMap<String, Integer> dividerOfCurrentXmlAndPrevLex = new HashMap<>();
		for(Entry<String, HashMap<String, Integer>> entry : countPairOfCurrentXmlAndPrevLex.entrySet()) {
			String key = entry.getKey();
			HashMap<String, Integer> value = entry.getValue();
			
			int increment = 0;
			for(Entry<String, Integer> valueEntry : value.entrySet()) {
				int val = valueEntry.getValue();
				increment = increment + val;
			}
			
			dividerOfCurrentXmlAndPrevLex.put(key, increment);
		}
		
		
		// Hitung probabilitasnya.
		HashMap<String, HashMap<String, Double>> probabilityOfCurrentXmlAndPrevLex = new HashMap<>();
		for(Entry<String, HashMap<String, Integer>> entry : countPairOfCurrentXmlAndPrevLex.entrySet()) {
			String parentKey = entry.getKey();
			HashMap<String, Integer> parentValue = entry.getValue();
			
			HashMap<String, Double> currPrevTransition = new HashMap<String, Double>();
			for(Entry<String, Integer> parentValueEntry : parentValue.entrySet()) {
				String key = parentValueEntry.getKey();
				int value = parentValueEntry.getValue();
				
				int divider = dividerOfCurrentXmlAndPrevLex.get(parentKey);
				
				double probability = (double) value / (double) divider;
				
				if(Double.isNaN(probability)) {
					probability = 0;
				}
				
				currPrevTransition.put(key, probability);
			}
			probabilityOfCurrentXmlAndPrevLex.put(parentKey, currPrevTransition);
		}
		
		
		// Hitung pembagi untuk current tag dengan leksikal tag.
		HashMap<String, Integer> dividerOfCurrentXmlAndCurrentLex = new HashMap<String, Integer>();
		for(Entry<String, HashMap<String, Integer>> entry : countPairOfCurrentXmlAndCurrentLex.entrySet()) {
			String key = entry.getKey();
			HashMap<String, Integer> value = entry.getValue();
			
			int increment = 0;
			for(Entry<String, Integer> valueEntry : value.entrySet()) {
				int val = valueEntry.getValue();
				increment = increment + val;
			}
			
			dividerOfCurrentXmlAndCurrentLex.put(key, increment);
		}
		
		
		// Perhitungan probabilitas.
		HashMap<String, HashMap<String, Double>> probabilityOfCurrentXmlAndCurrentLex = new HashMap<>();
		for(Entry<String, HashMap<String, Integer>> entry : countPairOfCurrentXmlAndCurrentLex.entrySet()) {
			String parentKey = entry.getKey();
			HashMap<String, Integer> parentValue = entry.getValue();
			
			HashMap<String, Double> curr = new HashMap<String, Double>();
			for(Entry<String, Integer> parentValueEntry : parentValue.entrySet()) {
				String key = parentValueEntry.getKey();
				int value = parentValueEntry.getValue();
				
				int divider = dividerOfCurrentXmlAndCurrentLex.get(parentKey);
				
				double probability = (double) value / (double) divider;
				
				if(Double.isNaN(probability)) {
					probability = 0;
				}
				curr.put(key, probability);
			}
			probabilityOfCurrentXmlAndCurrentLex.put(parentKey, curr);
		}
		
		
		// Mencari pembagi curent tag dengan leksikal kata sesudahnya
		HashMap<String, Integer> dividerOfCurrentXmlAndNextLex = new HashMap<>();
		for(Entry<String, HashMap<String, Integer>> entry : countPairOfCurrentXmlAndNextLex.entrySet()) {
			String key = entry.getKey();
			HashMap<String, Integer> value = entry.getValue();
			
			int increment = 0;
			for(Entry<String, Integer> valueEntry : value.entrySet()) {
				int val = valueEntry.getValue();
				increment = increment + val;
			}
			
			dividerOfCurrentXmlAndNextLex.put(key, increment);
		}
		
		
		// Hitung probabilitasnya.
		HashMap<String, HashMap<String, Double>> probabilityOfCurrentXmlAndNextLex = new HashMap<>();
		for(Entry<String, HashMap<String, Integer>> entry : countPairOfCurrentXmlAndNextLex.entrySet()) {
			String parentKey = entry.getKey();
			HashMap<String, Integer> parentValue = entry.getValue();
			
			HashMap<String, Double> currNextTransition = new HashMap<String, Double>();
			for(Entry<String, Integer> parentValueEntry : parentValue.entrySet()) {
				String key = parentValueEntry.getKey();
				int value = parentValueEntry.getValue();
				
				int divider = dividerOfCurrentXmlAndNextLex.get(parentKey);

				double probability = (double) value / (double) divider;
				
				if(Double.isNaN(probability)) {
					probability = 0;
				}
				currNextTransition.put(key, probability);
			}
			probabilityOfCurrentXmlAndNextLex.put(parentKey, currNextTransition);
		}
		
		
		// Start probability.
		logger.debug("Tag pada awal kata yang ditemukan: " + countStartTag);
		logger.debug("Pembagi probabilitas awal: " + startProbDivider);
		logger.debug("Probabilitas awal: " + startProb);
		this.countStartTag = countStartTag;
		this.dividerOfStartXmlTag = startProbDivider;
		this.probabilityOfStartXmlTag = startProb;
		
		// Transition probability.
		logger.debug("Tag transisi yang ditemukan: " + countTransitionXmlTagToNextXmlTag);
		logger.debug("Pembagi tag transisi: " + transitionDivider);
		logger.debug("Probabilitas transisi: " + transitionProb);
		this.countTransitionBetweenXmlTag = countTransitionXmlTagToNextXmlTag;
		this.dividerOfTransitionBetweenXmlTag = transitionDivider;
		this.probabilityBetweenXmlTag = transitionProb;

		// Emission probability antara tag xml ke tag leksikal sebelumnya.
		logger.debug("Jumlah antara xml dengan leksikal sebelumnya: " + countPairOfCurrentXmlAndPrevLex);
		logger.debug("Pembagi bersyarat antara tag xml dengan leksikal sebelumnya: " + dividerOfCurrentXmlAndPrevLex);
		logger.debug("Probabilitas bersyarat antara tag xml dengan leksikal sebelumnya: " + probabilityOfCurrentXmlAndPrevLex);
		this.countPairOfCurrentXmlAndPrevLex = countPairOfCurrentXmlAndPrevLex;
		this.dividerOfCurrentXmlAndPrevLex = dividerOfCurrentXmlAndPrevLex;
		this.probabilityOfCurrentXmlAndPrevLex = probabilityOfCurrentXmlAndPrevLex;
		
		// Emission probability antara tag xml dengan leksikalnya.
		logger.debug("Jumlah antara xml dengan leksikalnya: " + countPairOfCurrentXmlAndCurrentLex);
		logger.debug("Pembagi bersyarat antara tag xml dengan leksikalnya: " + dividerOfCurrentXmlAndCurrentLex);
		logger.debug("Probabilitas bersyarat antara tag xml dengan leksikalnya: " + probabilityOfCurrentXmlAndCurrentLex);
		this.countPairOfCurrentXmlAndCurrentLex = countPairOfCurrentXmlAndCurrentLex;
		this.dividerOfCurrentXmlAndCurrentLex = dividerOfCurrentXmlAndCurrentLex;
		this.probabilityOfCurrentXmlAndCurrentLex = probabilityOfCurrentXmlAndCurrentLex;		
		
		// Emission probability antara tag xml dengan leksikal sesudahnya.
		logger.debug("Jumlah antara xml dengan leksikal sesudahnya: " + countPairOfCurrentXmlAndNextLex);
		logger.debug("Pembagi bersyarat antara tag xml dengan leksikal sesudahnya: " + dividerOfCurrentXmlAndNextLex);
		logger.debug("Probabilitas bersyarat antara tag xml dengan leksikal sesudahnya: " + probabilityOfCurrentXmlAndNextLex);
		logger.info("Selesai melakukan perhitungan probabilitas.");
		this.countPairOfCurrentXmlAndNextLex = countPairOfCurrentXmlAndNextLex;
		this.dividerOfCurrentXmlAndNextLex = dividerOfCurrentXmlAndCurrentLex;
		this.probabilityOfCurrentXmlAndNextLex = probabilityOfCurrentXmlAndNextLex;
	}
	
	// Variabel untuk kelas lain mendapatkan hasil perhitungan.
	
	/**
	 * Jumlah masing-masing tag XML di awal kata.
	 */
	public HashMap<String, Integer> countStartTag;
	
	/**
	 * Jumlah transisi antara tag XML (key hashmap pertama) dengan tag XML kata selanjutnya (key hashmap kedua). 
	 */
	public HashMap<String, HashMap<String, Integer>> countTransitionBetweenXmlTag;
	
	/**
	 * Jumlah transisi antara tag XML (key hashmap pertama) dengan tag kelas kata sebelumnya (key hashmap kedua).
	 */
	public HashMap<String, HashMap<String, Integer>> countPairOfCurrentXmlAndPrevLex;
	
	/**
	 * Jumlah transisi antara tag XML (key hashmap pertama) dengan tag kelas katanya (key hashmap kedua).
	 */
	public HashMap<String, HashMap<String, Integer>> countPairOfCurrentXmlAndCurrentLex;
	
	/**
	 * Jumlah transisi antara tag XML (key hashmap pertama) dengan tag kelas kata selanjutnya (key hashmap kedua).
	 */
	public HashMap<String, HashMap<String, Integer>> countPairOfCurrentXmlAndNextLex;
	
	/**
	 * Pembagi tag awal untuk mencari start probability.
	 */
	public int dividerOfStartXmlTag;
	
	/**
	 * Pembagi transisi antara tag xml.
	 */
	public HashMap<String, Integer> dividerOfTransitionBetweenXmlTag;
	
	/**
	 * Pembagi peluang bersyarat (xml dengan kelas kata sebelumnya).
	 */
	public HashMap<String, Integer> dividerOfCurrentXmlAndPrevLex;
	
	/**
	 * Pembagi peluang bersyarat (xml dengan kelas kata miliknya).
	 */
	public HashMap<String, Integer> dividerOfCurrentXmlAndCurrentLex;
	
	/**
	 * Pembagi peluang bersyarat (xml dengan kelas kata sesudahnya).
	 */
	public HashMap<String, Integer> dividerOfCurrentXmlAndNextLex;
	
	/**
	 * Nilai probabilitas awal dengan key sebagai tag XML dan value sebagai nilai probabilitasnya.
	 */
	public HashMap<String, Double> probabilityOfStartXmlTag;
	
	/**
	 * Nilai probabilitas transisi dengan key sebagai tag XML itu dan value hashmap, dengan
	 * key sebagai tag XML selanjutnya dan value sebagai nilai probabilitasnya.
	 */
	public HashMap<String, HashMap<String, Double>> probabilityBetweenXmlTag;
	
	/**
	 * Nilai probabilitas bersyarat (kelas kata sebelumnya) dengan key sebagai tag XML itu dan value hashmap, dengan
	 * key sebagai tag leksikal sebelum kata itu dan value sebagai nilai probabilitasnya.
	 */
	public HashMap<String, HashMap<String, Double>> probabilityOfCurrentXmlAndPrevLex;
	
	/**
	 * Nilai probabilitas bersyarat (kelas kata dirinya sendiri) dengan key sebagai tag XML itu dan value hashmap, dengan
	 * key sebagai tag leksikal kata itu dan value sebagai nilai probabilitasnya.
	 */
	public HashMap<String, HashMap<String, Double>> probabilityOfCurrentXmlAndCurrentLex;
	
	/**
	 * Nilai probabilitas bersyarat (kelas kata sesudahnya) dengan key sebagai tag XML itu dan value hashmap, dengan
	 * key sebagai tag leksikal sesudah kata itu dan value sebagai nilai probabilitasnya.
	 */
	public HashMap<String, HashMap<String, Double>> probabilityOfCurrentXmlAndNextLex;

}
