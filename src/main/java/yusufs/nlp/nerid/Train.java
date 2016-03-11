/**
 * 
 */
package yusufs.nlp.nerid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import yusufs.nlp.tokenizerid.Tokenizer;
import yusufs.nlp.nerid.utils.ReadLine;
import yusufs.nlp.nerid.utils.TextSequence;
import yusufs.nlp.nerid.utils.TextSequence.Sentence;

/**
 * @author Yusuf Syaifudin
 *
 */
public class Train {
	
	final static Logger logger = Logger.getLogger(Train.class);

	/**
	 * Melakukan training terhadap data. Setiap teks akan otomatis dipecah menjadi kalimat sesuai tokenizer
	 * 
	 * @param trainingData
	 * @param withPunctuation
	 * @return
	 * @throws Exception
	 */
	public NERModel doTrain(String trainingData, Boolean withPunctuation) throws Exception {
		logger.info("Memulai proses training data.");
		
		// Pertama memecah teks kedalam kalimat-kalimat.
		Tokenizer tokenizer = new Tokenizer();
		ArrayList<String> sentences = tokenizer.extractSentence(trainingData);
		
		// Untuk setiap kalimat, cari tag-nya baik xml tag (PERSON, ORGANIZATION, LOCATION, dll) 
		// ataupun Part of Speech tag.
		ArrayList<Sentence> sentencesArray = new ArrayList<>();
		TextSequence ws = new TextSequence();
		for(String sentence : sentences) {
		    Sentence sen;
			try {
				// Akan menghasilkan kumpulan token + ner tag + pos tag nya.
				sen = ws.wordSeqWithTag(sentence, withPunctuation);
				// Tambahkan setiap hasil training ke variable wordSeq.
				sentencesArray.add(sen);
			} catch (Exception e) {
				logger.error("Gagal dalam melakukan training data.");
				e.printStackTrace();
				throw new Exception("Gagal dalam melakukan training data.");
			}
		}
		logger.info(sentencesArray.size() + " kalimat telah ditemukan.");
		
		// Sekarang kita telah memiliki semua kalimat yang telah di ekstrak XML dan POSnya.
		// Lalu lakukan perhitungan.
		Counter counter = new Counter(sentencesArray);
		HashMap<String, Double> startProbability = counter.probabilityOfStartXmlTag;
		HashMap<String, HashMap<String, Double>> transitionProbability = counter.probabilityBetweenXmlTag;
		
		// Emission probability menggunakan probabilitas tag xml dengan melihat kelas katanya, kelas kata sebelumnya dan kelas kata berikutnya.
		HashMap<String, HashMap<String, Double>> previousLexicalClass = counter.probabilityOfCurrentXmlAndPrevLex;
		HashMap<String, HashMap<String, Double>> currentLexicalClass = counter.probabilityOfCurrentXmlAndCurrentLex;
		HashMap<String, HashMap<String, Double>> nextLexicalClass = counter.probabilityOfCurrentXmlAndNextLex;
		
		// Kalikan masing-masing probability.
		// Misal, untuk mendapatkan probability dari NN_VBI_NNP dengan NN merupakan kelas kata sebelum, 
		// VBI merupakan kelas katanya, NNP kelas kata sesudahnya.
		// Maka akan dikalikan probability `previousLexicalClass` untuk NN, `currentLexicalClass` untuk VBI dan `nextLexicalClass` untuk NNP.
		// Artinya, semua kemungkinan akan dikalikan.
		
		// Model dan simpan ke model.
		NERModel model = new NERModel(startProbability, transitionProbability, previousLexicalClass, currentLexicalClass, nextLexicalClass);
		logger.info("Selesai melakukan training data.");
		
		return model;
	}
	
	
	/**
	 * Melakukan pelatihan dengan mengganggap input/masukan yang ada yaitu satu baris dianggap satu kalimat
	 * @param trainingData
	 * @param withPunctuation
	 * @return
	 * @throws Exception
	 */
	public NERModel doTrainReadLine(File trainingData, Boolean withPunctuation) throws Exception {
		logger.info("Memulai proses training data.");
		
		// Pertama baca dulu baris per baris, anggap saja satu baris itu satu kalimat.
		ReadLine file = new ReadLine(trainingData);
		ArrayList<String> sentences;
		try {
			sentences = file.read();
		} catch (Throwable e1) {
			throw new Exception(e1.getMessage());
		}
		
		// Untuk setiap kalimat, cari tag-nya baik xml tag (PERSON, ORGANIZATION, LOCATION, dll) 
		// ataupun Part of Speech tag.
		ArrayList<Sentence> sentencesArray = new ArrayList<>();
		TextSequence ws = new TextSequence();
		for(String sentence : sentences) {
		    Sentence sen;
			try {
				// Akan menghasilkan kumpulan token + ner tag + pos tag nya.
				sen = ws.wordSeqWithTag(sentence, withPunctuation);
				// Tambahkan setiap hasil training ke variable wordSeq.
				sentencesArray.add(sen);
			} catch (Exception e) {
				logger.error("Gagal dalam melakukan training data.");
				e.printStackTrace();
				throw new Exception("Gagal dalam melakukan training data.");
			}
		}
		logger.info(sentencesArray.size() + " kalimat telah ditemukan.");
		
		// Sekarang kita telah memiliki semua kalimat yang telah di ekstrak XML dan POSnya.
		// Lalu lakukan perhitungan.
		Counter counter = new Counter(sentencesArray);
		HashMap<String, Double> startProbability = counter.probabilityOfStartXmlTag;
		HashMap<String, HashMap<String, Double>> transitionProbability = counter.probabilityBetweenXmlTag;
		
		// Emission probability menggunakan probabilitas tag xml dengan melihat kelas katanya, kelas kata sebelumnya dan kelas kata berikutnya.
		HashMap<String, HashMap<String, Double>> previousLexicalClass = counter.probabilityOfCurrentXmlAndPrevLex;
		HashMap<String, HashMap<String, Double>> currentLexicalClass = counter.probabilityOfCurrentXmlAndCurrentLex;
		HashMap<String, HashMap<String, Double>> nextLexicalClass = counter.probabilityOfCurrentXmlAndNextLex;
		
		// Kalikan masing-masing probability.
		// Misal, untuk mendapatkan probability dari NN_VBI_NNP dengan NN merupakan kelas kata sebelum, 
		// VBI merupakan kelas katanya, NNP kelas kata sesudahnya.
		// Maka akan dikalikan probability `previousLexicalClass` untuk NN, `currentLexicalClass` untuk VBI dan `nextLexicalClass` untuk NNP.
		// Artinya, semua kemungkinan akan dikalikan.
		
		// Model dan simpan ke model.
		NERModel model = new NERModel(startProbability, transitionProbability, previousLexicalClass, currentLexicalClass, nextLexicalClass);
		logger.info("Selesai melakukan training data.");
		
		return model;
	}
	
	
	/**
	 * Menyimpan ner model ke dalam sebuah file json.
	 * @param nerModel
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public File saveModel(NERModel nerModel, File filename) throws IOException {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
		String json = gson.toJson(nerModel);
 
		try (FileOutputStream fop = new FileOutputStream(filename)) {
 
			// if file doesn't exists, then create it
			if (!filename.exists()) {
				filename.createNewFile();
			}
 
			// get the content in bytes
			byte[] contentInBytes = json.getBytes();
 
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
 
			return filename;
 
		} catch (IOException e) {
			e.printStackTrace();
			return filename;
		}
		
		
	}
	
}
