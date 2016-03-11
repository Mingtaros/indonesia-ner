/**
 * 
 */
package yusufs.nlp.nerid;

import java.io.File;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import yusufs.nlp.nerid.utils.TextSequence.Sentence;

/**
 * @author Yusuf Syaifudin
 *
 */
public class IndonesiaNER {
	
	private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	
	public static enum MODEL {
	    FACHRI, YUSUFS 
	}

	
	private NERModel nerModel;
	/**
	 * 
	 */
	public IndonesiaNER(MODEL embeddedModel) {
		NERModel model = new NERModel();
		
		switch (embeddedModel) {
		case FACHRI:
			this.nerModel = model.loadModel(new File("./resources/model/ner_model_fachri.json"));
			break;

		case YUSUFS:
			this.nerModel = model.loadModel(new File("./resources/model/ner_model_yusufs.json"));
			break;
		default:
			this.nerModel = model.loadModel(new File("./resources/model/ner_model_yusufs.json"));
			break;
		}
	}
	
	
	/**
	 * Melakukan prediksi teks dengan menggunakan model yang sudah ada
	 * @param textToPredict
	 * @param withPunctuation
	 * @return
	 */
	public ArrayList<Sentence> predictWithEmbeddedModel(String textToPredict, Boolean withPunctuation) {
		Prediction prediction = new Prediction();
		return prediction.predict(textToPredict, withPunctuation, this.nerModel);
	}
	
	/**
	 * Melakukan test terhadap model yang telah ada dengan data dari inputan
	 * @param dataTest
	 * @return
	 * @throws Exception
	 */
	public String testEmbeddedModel(File dataTest) throws Exception {
		TestNerModel testModel = new TestNerModel();
		TestNerModel nertest = testModel.doTest(dataTest, this.nerModel);
		
		String json = gson.toJson(nertest);
		
		return json;
	}

}
