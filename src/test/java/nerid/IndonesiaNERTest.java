/**
 * 
 */
package nerid;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;

import org.junit.Test;

import yusufs.nlp.nerid.IndonesiaNER;
import yusufs.nlp.nerid.utils.TextSequence.Sentence;
import yusufs.nlp.nerid.utils.TextSequence.Words;

/**
 * @author Yusuf Syaifudin
 *
 */
public class IndonesiaNERTest {
	
	
	IndonesiaNER iner = new IndonesiaNER(IndonesiaNER.MODEL.YUSUFS);
	
	public IndonesiaNERTest() {
		// logging
		// org.apache.log4j.BasicConfigurator.configure();
	}

	@Test
	public void testModel() {
		try {
			String test = iner.testEmbeddedModel(new File("./resources/ner/data_test.txt"));
			System.out.println(test);
			assertEquals(test, "{\"dataCounter\":{\"OTHER\":9526,\"LOCATION\":213,\"ORGANIZATION\":323,\"PERSON\":679,\"QUANTITY\":39,\"TIME\":287},\"total\":11067,\"correct\":1277,\"missing\":264,\"wrong\":853,\"truePositive\":1277,\"trueNegative\":8673,\"falsePositive\":853,\"falseNegative\":264,\"accuracy\":0.8990693051414114,\"recall\":0.8286826735885788,\"precision\":0.5995305164319249,\"totalKalimat\":512}");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testPrediction() {
		ArrayList<Sentence> predicted = iner.predictWithEmbeddedModel("Jokowi pergi ke Singapura.", true);
	
		// just one sentence = one array
		ArrayList<Words> kalimat = predicted.get(0).getWords();
		
		for(Words kal : kalimat) {
			if(kal.getToken().equals("Jokowi")) {
				assertEquals(kal.getXmlTag(), "PERSON");
			} else if(kal.getToken().equals("Singapura")) {
				assertEquals(kal.getXmlTag(), "LOCATION");
			} else {
				assertEquals(kal.getXmlTag(), "OTHER");
			}
		}
		
		System.out.println(kalimat);
	}

}
