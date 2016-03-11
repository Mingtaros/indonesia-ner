/**
 * 
 */
package yusufs.nlp.nerid;

/**
 * @author Yusuf Syaifudin
 *
 */
public class Config {
	
	/**
	 * Constructor
	 * Load a config from file to make system easy to control
	 */
	public Config() {
		
	}

	/**
	 * Entitas yang akan dicari.
	 */
	public static final String[] NERTAG = {"PERSON", "LOCATION", "ORGANIZATION", "TIME", "QUANTITY", "OTHER"};
	
	/**
	 * Part of Speech Tag yang akan dicari atau sebagai acuan.
	 */
	public static final String[] POSTAG = {"OP", "CP", "GM", ";", ":", 
		"\"", ".", ",", "-", "...", 
		"JJ", "RB", "NN", "NNP", "NNG", 
		"VBI", "VBT", "IN", "MD", "CC", 
		"SC", "DT", "UH", "CDO", "CDC", 
		"CDP", "CDI", "PRP", "WP", "PRN", 
		"PRL", "NEG", "SYM", "RP", "FW"};
	
	/**
	 * Part of Speech Tag yang akan dicari atau sebagai acuan.
	 */
	public static final String[] EXTENDED_POSTAG = {"OP", "CP", "GM", ";", ":", 
		"\"", ".", ",", "-", "...", 
		"JJ", "RB", "NN", "NNAC", "NNFC", 
		"VBI", "VBT", "IN", "MD", "CC", 
		"SC", "DT", "UH", "CDO", "CDC", 
		"CDP", "CDP1", "CDP2", "CDP3", "CDP4", 
		"CDPM", "CDPR", "CDI", "PRP", "WP", 
		"PRN", "PRL", "NEG", "SYM", "RP", 
		"FW", "FWAC", "FWFC", "START", "END"};
	
	
}
