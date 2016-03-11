/**
 * 
 */
package yusufs.nlp.nerid.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import yusufs.generator.randstring.RandomStringGenerator;
import yusufs.nlp.tokenizerid.Tokenizer;

/**
 * @author Yusuf Syaifudin
 *
 */
public class XmlSequence {	
	// Mendapatkan class yang akan di log
    final static Logger logger = Logger.getLogger(XmlSequence.class);
	
	// ## Constructor
	public XmlSequence() {
	}
	
	// ## normalize()
	// Mengganti konten yang sudah di tag (surrounded with html tag) 
	// dengan random string. Hal ini untuk memastikan bahwa hanya tagged content yang diambil.
	// Method ini tidak support untuk nested tag.
	public String normalize(String taggedXml) {
		// Pertama tentukan dulu pattern tag XML beserta kontennya.
		// Regex: `(<.\w+\>)[\S\s]*?(<\/.\w+>)` digunakan untuk mengambil tag beserta kontennya (tetapi tidak bisa nested tag).
		String patternNotNestedTag = "(<\\w+\\>)[\\S\\s]*?(<\\/\\w+>)";
		
		// Variable untuk menyimpan hasil replacer.
		String replacedString = taggedXml;
		
		// LinkedHashMap untuk menyimpan mapping content.
		// LinkedHashMap digunakan karena akan menyimpan urutan sesuai pada waktu proses insert.
		LinkedHashMap<String, String> mappedReplacedContent = new LinkedHashMap<>();
		
	    Pattern re = Pattern.compile(patternNotNestedTag, Pattern.MULTILINE | Pattern.COMMENTS);
	    Matcher reMatcher = re.matcher(replacedString);
	    while (reMatcher.find()) {
	    	// Untuk setiap sequence pattern yang sesuai, berarti itu ialah tag dan konten xml yang akan di replace.
	    	String toReplace = reMatcher.group();
	    	
	    	// Mendapatkan random string dengan panjang 10 karakter sebagai replacer.
	    	String randomString = RandomStringGenerator.generateRandomString(10, RandomStringGenerator.Mode.ALPHANUMERIC);
	    	// Ditambah spasi untuk memastikan string random terpisah dengan teks lain.
	    	String replacer = " " + randomString + " ";
	    	// Ganti pattern yang ada dengan random string replacer.
	    	replacedString = replacedString.replace(toReplace, replacer);
	    	
	    	// Ekstrak dulu kontennya dan pastikan telah di escape.
	    	String extractedContent = extractContent(toReplace);
	    	String escapedContent = escapeXml(extractedContent);
	    	// Setelah itu replace ke teks asli untuk disimpan.
	    	String escaped = toReplace.replace(extractedContent, escapedContent);
	    	
	    	// Simpan hasil replacing ke hashmap untuk mapping kembali.
	    	// Dengan replacer sebagai key dan toReplace (replaced content) sebagai value.
	    	mappedReplacedContent.put(randomString, escaped);
	    }
	    
	    // Sekarang, escape yang string yang sudah dipisahkan antara tag XML bawaan di-escape.
	    replacedString = escapeXml(replacedString);
	    
	    String normalized = "";
	    // Pisah berdasarkan spasi.
	 	String[] splittedString = replacedString.split("\\s");
	 	// Ulangi untuk setiap string.
	 	for(String str : splittedString) {
	 		if(!str.equals("")) {
	 			if(mappedReplacedContent.containsKey(str)) {
	 				String tmp = mappedReplacedContent.get(str);
	 				normalized += tmp;
	 			} else {
	 				normalized += str + " ";
	 			}
	 		}
	 	}
	    
	    return normalized.trim();
	}
	
	/**
	 * Menormalkan teks yang tidak memiliki tag XML di-surround dengan defaultTag
	 * @param xmlString
	 * @param defaultTag
	 * @return
	 */
	public String xmlizeText(String xmlString, String defaultTag) {
		int x = 0;
		boolean closed = false;
		
		// String kosong untuk menyimpan hasil sementara.
		String tmp = "";
		
		// String kosong untuk menyimpan hasil akhir selama proses.
		String result = "";
		
		// Dari awal karakter hingga panjang kalimat.
		for(int i = 0; i < xmlString.length(); i++) {
			if(tmp == "") {
				// String kosong, iterasi baru dimulai,
				if(xmlString.charAt(i) != '<') {
					// Jika merupakan tag pembuka (<), maka tambahkan ke tmp.
					tmp += xmlString.charAt(i);
				} else {
					// Jika bukan, tambahkan saja ke tmp terus menerus dan increment nilai x.
					x++;
					tmp += xmlString.charAt(i);
				}
			} else if(x == 0) {
				// Jika x == nol.
				if(xmlString.charAt(i) == '<') {
					// Jika karakternya merupakan (<), tambahkan ke result.
					result += "<" + defaultTag + ">" + tmp + "</" + defaultTag + ">";
					// Lalu kosongkan variable tmp.
					tmp = "";
					tmp += xmlString.charAt(i);
					x++;
				} else {
					tmp += xmlString.charAt(i);
				}
			} else {
				if(xmlString.charAt(i) == '/') {
					closed = true;
					tmp += xmlString.charAt(i);
				} else if(closed) {
					if(xmlString.charAt(i) == '>'){
						tmp += xmlString.charAt(i);
						closed = false;
						x--;
						if(x == 0) {
							result += tmp;
							tmp = "";
						}
					} else {
						tmp += xmlString.charAt(i);
					}
				} else {
					tmp += xmlString.charAt(i);
				}
			}
		}
		
		if(tmp != "") {
			result += "<" + defaultTag + ">" + tmp + "</" + defaultTag + ">";
		}
		
		logger.info(result);
		return result;
	}

	/**
	 * Mengekstrak konten dari teks xml, misal <TAG>konten</TAG> diekstrak menjadi: konten
	 * @param xmlString
	 * @return
	 */
	public String extractContent(String xmlString) {
		// Pattern untuk mengekstrak konten antara dua tag XML.
		// Konten yang diekstrak ialah dari tag pertama hingga tag penutup paling akhir, artinya 
		// jika ada string `<TAG>Konten <ANOTHERTAG>konten lain</ANOTHERTAG> dan lainnya</TAG>`
		// maka akan menghasilkan konten `Konten <ANOTHERTAG>konten lain</ANOTHERTAG> dan lainnya`.
		// Namun jika ternyata
		// Regex: `(?<=.\>).*(?=\<\/.*)`
		String regex = "(?<=\\w\\>).*(?=\\<\\/\\w+)";
		Pattern re = Pattern.compile(regex, Pattern.MULTILINE | Pattern.COMMENTS);
	    Matcher reMatcher = re.matcher(xmlString);
	    
	    // Kembalikan hanya string pertama yang ditemukan.
	    while(reMatcher.find()) {
	    	return reMatcher.group().trim();
	    }
	    
	    // Jika tidak menemukan apapun, kembalikan string kosong.
	    return "";
	}
	
	
	/**
	 * Escaping xml text
	 * @param XmlWithSpecial
	 * @return
	 */
	public String escapeXml(String XmlWithSpecial) {
		String escapedXml = StringEscapeUtils.escapeXml10(XmlWithSpecial);
		return escapedXml;
	}
	
	/**
	 * Unescape XML text
	 * @param escapedXml
	 * @return
	 */
	public String unescapeXml(String escapedXml) {
		String XmlWithSpecial = StringEscapeUtils.unescapeXml(escapedXml);
		return XmlWithSpecial;
	}
	
	// ## normalizeXml
	// Menormalisasi XML dengan wraping text yang tidak memiliki tag.
	// Misal kalimat `Saya pergi ke <LOCATION>Malang</LOCATION>` 
	// akan dijadikan sebagai `<OTHER>Saya pergi ke</OTHER><LOCATION>Malang</LOCATION>`.
	// Catatan: tag `OTHER` merupakan tag yang didefinisikan dari parameter defaultTag.
	// Konten yang ada didalam tag akan di escape dan tidak support nested tag.
	/**
	 * Menormalisasi XML dengan wraping text yang tidak memiliki tag.
	 * Misal kalimat `Saya pergi ke <LOCATION>Malang</LOCATION>` 
 	 * akan dijadikan sebagai `<OTHER>Saya pergi ke</OTHER><LOCATION>Malang</LOCATION>`.
	 * Catatan: tag `OTHER` merupakan tag yang didefinisikan dari parameter defaultTag.
	 * Konten yang ada didalam tag akan di escape dan tidak support nested tag.
	 * @param xmlWithUntaggedContent
	 * @param defaultTag
	 * @return
	 */
	public String normalizeXml(String xmlWithUntaggedContent, String defaultTag) {
		String normalizedText = normalize(xmlWithUntaggedContent);
		String normalizeXml = xmlizeText(normalizedText, defaultTag);
		return normalizeXml;
	}

	
	public ArrayList<XMLSentenceSequence> xmlSentenceSequence(String xmlWithUntaggedContent, String defaultTag, Boolean withPunct) {
		String normalized = normalizeXml(xmlWithUntaggedContent, defaultTag);
		String xml = "<XMLDOC>" + normalized + "</XMLDOC>";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
		Document doc;
		
		// Ubah tag ke format Document.
		try {
			doc = dbf.newDocumentBuilder().parse(
						new InputSource(
							new StringReader(xml)));

			// Dapatkan list dari root (*).
		    NodeList nodeList = doc.getElementsByTagName("*");
		    
		    // Variable untuk menyimpan hasil.
		    ArrayList<XMLSentenceSequence> result = new ArrayList<>();
		    // Iterate for each node.
		    for (int i=0; i<nodeList.getLength(); i++) 
		    {
		    	// Get element.
		        Element element = (Element)nodeList.item(i);
		        String tag = element.getTagName();
		        String content = element.getTextContent();
		        
		        // Jika tag == XMLDOC maka abaikan
		        if(tag != "XMLDOC") {
		        	Tokenizer token = new Tokenizer();
		        	ArrayList<String> sentences = token.extractSentence(content);
		        	for(String sentence : sentences) {
		        		XMLSentenceSequence xmlss = new XMLSentenceSequence(tag, sentence);
			        	result.add(xmlss);
		        	}
		        }
		    }
		    
		    return result;
		
		// Cetak stack trace jika terjadi error.
		} catch(SAXParseException e) {
			e.printStackTrace();
			logger.error("Kesalahan dalam membaca tag. XML mungkin tidak valid. Ingat bahwa sistem tidak mendukung nested tag.");
			logger.error("Error saat membaca teks: " + xml);
			logger.error("Error pada kolom: " + e.getColumnNumber());
			logger.error("Error pada baris: " + e.getLineNumber());
			new Exception("Kesalahan dalam membaca tag XML.");
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			logger.error("Kesalahan dalam membaca tag. XML mungkin tidak valid.");
			new Exception("Kesalahan dalam membaca tag XML.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			new Exception("Kesalahan dalam proses pembacaan tag XML.");
		}
		
		XMLSentenceSequence xss = new XMLSentenceSequence("", "");
		ArrayList<XMLSentenceSequence> result = new ArrayList<>();
		result.add(xss);
		return result ;
	}
	
	/**
	 * Mengekstrak xml ke urutan token, misal  `Saya pergi ke <LOCATION>Malang</LOCATION>` 
	 * menjadi [{token:Saya, tag:OTHER}] dan seterusnya hingga {token:Malang, tag:LOCATION}
	 * @param xmlWithUntaggedContent
	 * @param defaultTag
	 * @param withPunct
	 * @return
	 */
	public XMLTokenSequence xmlTokenSequence(String xmlWithUntaggedContent, String defaultTag, Boolean withPunct) {
		String normalized = normalizeXml(xmlWithUntaggedContent, defaultTag);
		String xml = "<XMLDOC>" + normalized + "</XMLDOC>";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
		Document doc;
		
		// Ubah tag ke format Document.
		try {
			doc = dbf.newDocumentBuilder().parse(
						new InputSource(
							new StringReader(xml)));

			// Dapatkan list dari root (*).
		    NodeList nodeList = doc.getElementsByTagName("*");
		    
		    // Variable untuk menyimpan hasil.
		    ArrayList<LinkedHashMap<String, String>> result = new ArrayList<>();
		    String tokenizedString = "";
		    // Iterate for each node.
		    for (int i=0; i<nodeList.getLength(); i++) 
		    {
		    	// Get element.
		        Element element = (Element)nodeList.item(i);
		        String tag = element.getTagName();
		        String content = element.getTextContent();
		        
		        // Jika tag == XMLDOC maka abaikan
		        if(tag != "XMLDOC") {
		    	   // Tokenisasi `content` dengan class Tokenizer.
			        Tokenizer tok = new Tokenizer();
			        ArrayList<String> tokens = tok.tokenize(content, withPunct);
			        for(String token : tokens) {
			        	LinkedHashMap<String, String> resultObject = new LinkedHashMap<>();
			        	resultObject.put("token", token);
			        	resultObject.put("tag", tag);
			        	
			        	// Simpan string yang sudah di tokenisasi
			        	tokenizedString += token + " ";
			        	// Tambahkan ke arraylist
				        result.add(resultObject);
			        }
		        }
		    }
		    return new XMLTokenSequence(tokenizedString, result);
		
		// Cetak stack trace jika terjadi error.
		} catch(SAXParseException e) {
			e.printStackTrace();
			logger.error("Kesalahan dalam membaca tag. XML mungkin tidak valid. Ingat bahwa sistem tidak mendukung nested tag.");
			logger.error("Error saat membaca teks: " + xml);
			logger.error("Error pada kolom: " + e.getColumnNumber());
			logger.error("Error pada baris: " + e.getLineNumber());
			new Exception("Kesalahan dalam membaca tag XML.");
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			logger.error("Kesalahan dalam membaca tag. XML mungkin tidak valid.");
			new Exception("Kesalahan dalam membaca tag XML.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			new Exception("Kesalahan dalam proses pembacaan tag XML.");
		}
		return new XMLTokenSequence();
	}
	
	
	public class XMLSentenceSequence {
		private String tag;
		private String sentence;
		
		public XMLSentenceSequence() {
			
		}
		
		public XMLSentenceSequence(String tag, String sentence) {
			this.tag = tag;
			this.sentence = sentence;
		}

		/**
		 * @return the tag
		 */
		public String getTag() {
			return tag;
		}

		/**
		 * @return the sentence
		 */
		public String getSentence() {
			return sentence;
		}
	}
	
	public class XMLTokenSequence {
		private String tokenizedString;
		private ArrayList<LinkedHashMap<String, String>> xmlSequence;
		
		public XMLTokenSequence() {
			
		}
		
		public XMLTokenSequence(String tokenizedString, 
				ArrayList<LinkedHashMap<String, String>> xmlSequence) {
			setTokenizedString(tokenizedString);
			setXmlSequence(xmlSequence);
		}

		public String getTokenizedString() {
			return tokenizedString;
		}

		private void setTokenizedString(String tokenizedString) {
			this.tokenizedString = tokenizedString;
		}

		/**
		 * Urutan tag xml yang ada pada kata.
		 * 
		 * Misal <X>kata</X> <Y>kata lain</Y>
		 * maka akan menghasilkan:
		 * [0] => HashMap dengan kunci
		 * 	token => kata, tag => X.
		 *  
		 * [1] => HashMap dengan kunci
		 * 	token => kata, tag => Y.
		 * 
		 * [2] => HashMap dengan kunci
		 * 	token => lain, tag => Y.
		 * 
		 * @return
		 */
		public ArrayList<LinkedHashMap<String, String>> getXmlSequence() {
			return xmlSequence;
		}

		private void setXmlSequence(ArrayList<LinkedHashMap<String, String>> xmlSequence) {
			this.xmlSequence = xmlSequence;
		}
	}
}
