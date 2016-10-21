// # Part of Speech Tagger
// Merupakan file untuk melakukan tagging part of speech 
// menggunakan bantuan library dari NLP ITB ["HMM Based Part-of-Speech Tagger for Bahasa Indonesia"](http://mail.informatika.org/~ayu/2010postagger.pdf) 
// oleh Alfan Farizki Wicaksono dan Ayu Purwarianti tahun 2010 

package yusufs.nlp.nerid.utils;

import NLP_ITB.POSTagger.HMM.Decoder.MainTagger;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import yusufs.nlp.tokenizerid.Tokenizer;

/**
 *
 * @author Yusuf Syaifudin
 */
public class POSTagger {
    private final MainTagger mainTagger;
    final static Logger logger = Logger.getLogger(POSTagger.class);
    
    private final String FIRST_CAPITAL_PATTERN = "[A-Z]+.*";
    private final String ALL_CAPITAL_PATTERN = "[A-Z]+";
    private final String ALL_DIGIT = "\\d+";
    private final String RANGED_NUMBER = "\\d+-\\d+";
    
    // ## Constructor
    public POSTagger() {
        int lm = 1;
        int affix = 1;
        int pass2 = 0;
        int lex = 1;
        
        // Pastikan folder postagger/* beserta isinya sudah ada di current path saat Anda memanggil .jar.
        // Misal Anda ada di folder `D:\kuliah\Skripsi\Progam\skripsi` dan memanggil jar dengan 
        // `java -jar target\skripsi_yusufs-1.0-SNAPSHOT.jar`, maka pastikan Anda telah menaruh folder postagger di folder skripsi.
        String lexicon = "./resources/postagger/Lexicon.trn";
        String ngram = "./resources/postagger/Ngram.trn";

        mainTagger = new MainTagger(lexicon, ngram, lm,
                3, 3,
                0, affix,
                false, 0.2,
                pass2, 500.0,
                lex);
    }
    
    
    // Tagging kalimat
    // Pastikan sudah di tokenisasi
    /**
     * Menghasilkan array hashmap, dengan kunci token dan postag.
     * @param sentence
     * @param withPunct
     * @param extendedTag
     * @return ArrayList<LinkedHashMap<String, String>>
     */
    public ArrayList<LinkedHashMap<String, String>> recognize(String sentence, Boolean withPunct, Boolean extendedTag) {
    	Tokenizer tk = new Tokenizer();
    	String tokenize = tk.tokenizeToString(sentence, withPunct);
        ArrayList<String> taggedSentence = mainTagger.taggingStr(tokenize);
        
        // Menghindari kasus khusus, dimana string sentence yang dimasukkan hanya terdiri dari satu token.
        // Sehingga menghasilkan pos tagger yang kosong.
        // Kasus ini bisa terjadi karena pada saat proses extractSentence misalnya ada kalimat:
        // `1. Ketuhanan Yang Maha Esa.` maka akan menghasilkan dua kalimat yaitu `1.` dan `Ketuhanan Yang Maha Esa.`
        // pada saat `1.` dicari POS tagnya maka akan kosong, lalu menjadikan kegagalan sistem, 
        // maka dibuat kondisi ini agar tidak error.
        if(taggedSentence.size() == 0) {
        	ArrayList<LinkedHashMap<String, String>> ret = new ArrayList<>();
        	LinkedHashMap<String, String> defaultPostag = new LinkedHashMap<>();
        	
        	// Defaultnya ialah leksikalnya SYM (simbol).
        	// Jika merupakan alpha only, maka dianggap FW (Foreign Word).
        	// Jika numeric atau alpha numeric maka dianggap CDP (Primary Numerals).
        	String getPOSTag = "";
        	if(isAlpha(tokenize.trim())) {
        		getPOSTag = "FW";
        	} else if(isNumeric(tokenize.trim())) {
        		getPOSTag = "CDP";
        	} else if(isAlphaNumeric(tokenize.trim())) {
        		getPOSTag = "CDP";
        	} else {
        		getPOSTag = "SYM";
        	}

        	getPOSTag = extendTag(tokenize.trim(), getPOSTag);
        	
        	defaultPostag.put("token", tokenize.trim());
        	defaultPostag.put("postag", getPOSTag);
        	ret.add(defaultPostag);
        	return ret;
        }
        
        ArrayList<LinkedHashMap<String, String>> output = new ArrayList<>();
        
        // Iterasi untuk setiap yang sudah di tagging (menjadi token tersendiri).
        for(String token : taggedSentence) {
        	// Ekstrak kata dan tag yang dipisah oleh tanda /.
        	String word = token.replaceFirst("(.*)/(.+)$", "$1");
        	String postag = token.replaceFirst("(.*)/(.+)$", "$2");
        	
        	// Lalu simpan ke variable tmp bertipe LinkedHashMap untuk memastikan sesuai urutan.
        	LinkedHashMap<String, String> tmp = new LinkedHashMap<>();

            if(extendedTag == true) {
                postag = extendTag(word, postag);
            }
        	
            tmp.put("token", word);
            tmp.put("postag", postag);
            
            output.add(tmp);
        }
        
        logger.info(tokenize);
        logger.info(output);
        return output;
    }

    /**
     * Menghasilkan array hashmap, dengan kunci token dan postag.
     * @param sentence
     * @param withPunct
     * @return
     */
    public ArrayList<LinkedHashMap<String, String>> recognize(String sentence, Boolean withPunct) {
        return recognize(sentence, withPunct, true);
    }


    /**
     * Cek apakah string berisi hanya huruf.
     * @param word
     * @return
     */
    private boolean isAlpha(String word) {
        return word.matches("[a-zA-Z]+");
    }
    
    /**
     * Cek apakah hanya berisi angka.
     * @param word
     * @return
     */
    private boolean isNumeric(String word) {
    	return word.matches("^\\d+$");
    }
    
    /**
     * Cek apakah berisi angka dan huruf.
     * @param word
     * @return
     */
    private boolean isAlphaNumeric(String word) {
    	return word.matches("^[\\w\\W0-9]*$");
    }
    
    /**
     * Meng-extend tag agar lebih akurat.
     * 
     * @param word
     * @param tag
     * @return
     */
    private String extendTag(String word, String tag) {
        if (tag.startsWith("NN")) {
            return nounExtend(word, tag);
        } else if (tag.startsWith("FW")) {
            return foreignWordExtend(word, tag);
        } else if (tag.startsWith("CDP")) {
            return numeralExtend(word, tag);
        }
        
        return tag;
    }
    
    
    /**
     * Jika kata terdiri dari huruf kapital semua, tambahkan AC (All Capital) dibelakang tag.
     * Jika kata dimulai dengan huruf kapital, tambahkan FC (First Capital).
     * 
     * sehingga akan menjadi NNAC atau NNFC, otherwise return NN.
     * 
     * @param word
     * @param tag
     * @return
     */
    private String nounExtend(String word, String tag) {
        if(word.matches(ALL_CAPITAL_PATTERN)) {
            tag+="AC";
        } else if(word.matches(FIRST_CAPITAL_PATTERN)) {
            tag+="FC";
        }
        return tag;
    }

    
    /**
     * Jika kata terdiri dari digit dengan panjang lebih dari 4, maka tambahkan tag M (More).
     * Jika kata terdiri dari digit kurang dari 4, tambahkan jumlah digit itu.
     * Jika kata merupakan ranged number, misal 1900-2000 maka tambahkan R (Range) pada tag.
     * 
     * Sehingga akan: CDP1, CDP2, CDP3, CDP4, CDPM, CDPR.
     * 
     * @param word
     * @param tag
     * @return
     */
    private String numeralExtend(String word, String tag) {
        if(word.matches(ALL_DIGIT)){
            if(word.length() > 4){
                tag += "M";
            }else{
                tag += String.valueOf(word.length());
            }
        }else if(word.matches(RANGED_NUMBER)){
            tag += "R";
        }
        return tag;
    }

    
    /**
     * Jika kata terdiri dari huruf kapital semua, tambahkan AC (All Capital) dibelakang tag.
     * Jika kata dimulai dengan huruf kapital, tambahkan FC (First Capital).
     * 
     * sehingga akan menjadi FWAC atau FWFC, otherwise return FW.
     * 
     * @param word
     * @param tag
     * @return
     */
    private String foreignWordExtend(String word, String tag) {
        if(word.matches(ALL_CAPITAL_PATTERN)){
            tag+="AC";
        }else if(word.matches(FIRST_CAPITAL_PATTERN)){
            tag+="FC";
        }
        return tag;
    }
}
