/**
 * 
 */
package yusufs.nlp.nerid.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import yusufs.nlp.nerid.utils.XmlSequence.XMLTokenSequence;

/**
 * @author Yusuf Syaifudin
 *
 * Class ini akan menghasilkan konten yang sudah di tag.
 * TextSequence menghasilkan Sentence dimana didalamnya ada Words.
 * 
 * Sentence merupakan kelas kalimat dan Words merupakan 
 * kelas untuk memetakan kata yang menyusun kalimat.
 */
public class TextSequence {
	
	final static Logger logger = Logger.getLogger(TextSequence.class);
	final static XmlSequence xs = new XmlSequence();
	final static POSTagger pt = new POSTagger();
	
	public TextSequence() {
	}
	
	/**
	 * Melakukan proses pemecahan tag XML dan mencari POS tag sekaligus.
	 * 
	 * @param sentence
	 * @param withPunctuation apakah tanda baca diikutkan atau tidak. true = diikutkan, false = tidak. (Lihat Tokenizer.class)
	 * @return Sentence kalimat yang memiliki kalimat asli, kalimat yang sudah di tokenisasi dan kumpulan kata yang membentuk kalimat.
	 * @throws Exception
	 */
	public Sentence wordSeqWithTag(String sentence, Boolean withPunctuation) {
		// Cari sequence dari tag xml dulu
		XMLTokenSequence xts = xs.xmlTokenSequence(sentence, "OTHER", withPunctuation);
		
		// Dapatkan string yang sudah ditokenisasi.
		String tokenized = xts.getTokenizedString();
		
		// Dapatkan sequence hasil tagging.
		ArrayList<LinkedHashMap<String, String>> xmlTag = xts.getXmlSequence();
		
		// Sekarang lakukan POS tagging
		ArrayList<LinkedHashMap<String, String>> postagger = pt.recognize(tokenized, withPunctuation);
		
		ArrayList<Words> merged = new ArrayList<>();
		// Sekarang kita mendapatkan sequence dari xml tag dan pos tag, gabungkan keduanya.
		// Seharusnya keduanya memiliki ukuran yang sama.
		
		try {
			if(xmlTag.size() == postagger.size()) {
				
				// size() dari xmlTag dan postagger kini sama saja.
				for(int i=0; i<postagger.size(); i++) {
					LinkedHashMap<String, String> xmlSeq = xmlTag.get(i);
					LinkedHashMap<String, String> posSeq = postagger.get(i);
					
					Words word = new Words();
					word.setToken(xmlSeq.get("token"));
					word.setXmlTag(xmlSeq.get("tag"));
					word.setPosTag(posSeq.get("postag"));
					merged.add(word);
				}
				
				Sentence stn = new Sentence();
				stn.setOriginalSentence(sentence);
				stn.setTokenizedSentence(tokenized);
				stn.setWord(merged);
				
				return stn;
				
			}
		} catch (Exception e) {
			logger.error("Tidak dapat melakukan penggabungan tag.");
			new Exception("Tidak dapat melakukan penggabungan tag.");
		}
		
		return new Sentence();
	}

	
	/**
	 * Kelas untuk kalimat.
	 * 
	 * originalSentence = kalimat asli, sebelum dilakukan tagging.
	 * tokenizedSentence = kalimat yang sudah ditokenisasi.
	 * words = kumpulan kata yang membentuk kalimat. Lihat kelas Words.
	 * @author Yusuf Syaifudin
	 *
	 */
	public class Sentence {

		private String originalSentence;
		private String tokenizedSentence;
		private ArrayList<Words> words;
		
		/**
		 * @return the originalSentence
		 */
		public String getOriginalSentence() {
			return originalSentence;
		}

		/**
		 * @param originalSentence the originalSentence to set
		 */
		public void setOriginalSentence(String originalSentence) {
			this.originalSentence = originalSentence;
		}
		
		/**
		 * @return the tokenizedSentence
		 */
		public String getTokenizedSentence() {
			return tokenizedSentence;
		}
		
		/**
		 * @param tokenizedSentence the tokenizedSentence to set
		 */
		public void setTokenizedSentence(String tokenizedSentence) {
			this.tokenizedSentence = tokenizedSentence;
		}

		/**
		 * @return the word
		 */
		public ArrayList<Words> getWords() {
			return words;
		}

		/**
		 * @param word the word to set
		 */
		public void setWord(ArrayList<Words> word) {
			this.words = word;
		}
		
		@Override
		public String toString() {
			String out = "{originalSentence: " + getOriginalSentence() + "}, {tokenizedSentence: " 
					+ getTokenizedSentence()
					+ "}, word: " + getWords().toString();
			return out;
		}
	}
	
	/**
	 * Kumpulan kata yang membentuk kalimat.
	 * 
	 * token = kata yang ada pada kalimat itu.
	 * postag = kelas part of speech kata tersebut pada kalimat.
	 * xmlTag atau disebut juga tag NER, merupakan tag XML saat tagging dilakukan. 
	 * Jika tidak ada tag maka dianggap OTHER (sesuai spesifikasi saat melakukan normalisasi, lihat kelas XMLSequence).
	 * 
	 * @author Yusuf Syaifudin
	 *
	 */
	public class Words {
		private String token;
		private String posTag;
		private String xmlTag;
		
		/**
		 * Token atau kata.
		 * @return the token
		 */
		public String getToken() {
			return token;
		}
		
		/**
		 * @param token the token to set
		 */
		public void setToken(String token) {
			this.token = token;
		}
		
		/**
		 * Mengembalikan tag XML atau bisa disebut juga NER TAGnya.
		 * @return the xmlTag
		 */
		public String getXmlTag() {
			return xmlTag;
		}
		
		/**
		 * @param xmlTag the xmlTag to set
		 */
		public void setXmlTag(String xmlTag) {
			this.xmlTag = xmlTag;
		}
		
		

		/**
		 * Mengembalikan kelas kata dari kata tersebut.
		 * @return the lexical
		 */
		public String getPosTag() {
			return posTag;
		}

		/**
		 * @param posTag the posTag to set
		 */
		public void setPosTag(String posTag) {
			this.posTag = posTag;
		}
		
		@Override
		public String toString() {
			String out = "{token: " + getToken() + ", postag: " + getPosTag() + ", tag: " + getXmlTag() + "}";
			return out;
		}
	}
	
}
