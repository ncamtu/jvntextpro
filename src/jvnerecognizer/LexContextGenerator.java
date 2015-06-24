package jvnerecognizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.w3c.dom.Element;

import jflexcrf.Option;
import jvnsegmenter.BasicContextGenerator;
import jvnsegmenter.ConjunctionContextGenerator;
import jvntextpro.data.Sentence;
import jvntextpro.util.StringUtils;

public class LexContextGenerator extends BasicContextGenerator {

	protected static HashMap<String, HashSet<String>> lexicons = new HashMap<String, HashSet<String>>();
	
	protected static HashMap<String, String> name2abbv = new HashMap<String, String>();
	
	protected static HashMap<String, Vector<LexCPair>> name2cpobject = new HashMap<String, Vector<LexCPair>>();
	
	public static void init(String lexiconStorage)
	{
		System.out.println("Initializing lexicons");
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(lexiconStorage + File.separator + "list.manifest"));			

			String line;
			while ((line = reader.readLine()) != null){
				if (line.equals("")) continue;
				if (lexicons.containsKey(line)) continue;
				
				loadLexicon(line, lexiconStorage);
			}
			
			reader.close();
		}
		catch (Exception e){
			System.err.println("Error while reading manifest in lexiconStorage dir");
			return;
		}
	}	
	
	protected static boolean loadLexicon(String name, String path){
		try {			
			String [] strs = name.split("[ \t]+");
			name = strs[0];
			String abbv = strs[1];
			
			path = path + File.separator +  name + ".txt";
			System.out.print("Loading " + path + "\t: ");
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
						new FileInputStream(path), "UTF-8"));
			
			HashSet<String> list; 
			if (lexicons.containsKey(name)){
				list = lexicons.get(name);
			}
			else {
				list = new HashSet<String>();
				lexicons.put(name,list);
				name2abbv.put(name, abbv);
			}
			
			String line;
			while ((line = in.readLine()) != null){				
				list.add(line.trim().toLowerCase());
				//System.out.println(line);
			}
			
			System.out.println(list.size() + " entries loaded");							
			in.close();
			return true;		
		}
		catch (IOException ioe){
			System.err.println("Error while loading " + name);
			return false;
		}
	}
	
	protected static boolean isAnEntryOf(String name, String word){
		if (!lexicons.containsKey(name)){
			return false;
		}
		else {
			return lexicons.get(name).contains(word.toLowerCase());
		}
	}
	
	protected static boolean containAnEntryOf(String name, String word){
		
		if (!lexicons.containsKey(name))
			return false;

		if (isAnEntryOf(name, word))
			return true;
		
		HashSet<String> list= lexicons.get(name);		
		
		StringTokenizer tk = new StringTokenizer(word, " \t");
		String [] tkArray = new String [tk.countTokens()];
		
		//System.out.println(word);
		int i = 0;
		while (tk.hasMoreTokens())
			tkArray[i++] = tk.nextToken();
		
		for (int count = 1; count < tkArray.length; ++count){
			String tkConj = "";
			
			for (int j = 0; j < tkArray.length - count; ++j){
				tkConj +=  tkArray[j] + " ";
			}
			
			//System.out.println(tkConj.trim().toLowerCase());
			if (list.contains(tkConj.trim().toLowerCase()))
				return true;
		}
		return false;
	}	
	
	public LexContextGenerator(Element node){
		readFeatureParameters(node);		
		
		for (int it = 0; it < cpnames.size(); ++it){			
			String cp = cpnames.get(it);
			Vector<Integer> paras = this.paras.get(it);
			
			if (cp.startsWith("and")){
				Vector<LexCPair> lcps = parseAndPhase(cp, paras);				
				name2cpobject.put(cp, lcps);
			}
		}
	}
	
	@Override
	public String[] getContext(Sentence sent, int pos) {
		List<String> cps = new ArrayList<String>();
		for (int it = 0; it < cpnames.size(); ++it){			
			String cp = cpnames.get(it);
			Vector<Integer> paras = this.paras.get(it);
			String cpvalue = "";
			
			if (cp.startsWith("in") || cp.startsWith("has")){
				cpvalue = getContextOfSingleCP(sent, pos, new LexCPair(cp, paras));
				if (!cpvalue.isEmpty())
					cps.add(cpvalue);
			}
			else if (cp.startsWith("and")){
				Vector<LexCPair> lcps = name2cpobject.get(cp);
				
				if (lcps == null)
					cpvalue = "";
				else {
					cpvalue = "a:";
					for (int i = 0; i < lcps.size(); ++i){
						String sub_cpvalue = getContextOfSingleCP(sent, pos, lcps.get(i));
						if (sub_cpvalue.isEmpty()){
							cpvalue = "";
							break;
						}
						cpvalue = cpvalue + sub_cpvalue + ":";
					}
				}
				if (!cpvalue.isEmpty()){
					cpvalue = cpvalue.substring(0, cpvalue.length() - 1);
					cps.add(cpvalue);
				}
			}
		}
		String [] ret = new String[cps.size()];	
		return cps.toArray(ret);
	}
	
	protected String getContextOfSingleCP(Sentence sent, int pos, LexCPair lcp){
		String cpvalue = "";
		String cpname = lcp.name;
		Vector<Integer> paras = lcp.para;
		
		//get the words and paras
		String suffix = "";			
		String word = "";
		for (int i = 0; i < paras.size(); ++i) {
			if (pos + paras.get(i) < 0 || pos + paras.get(i)>= sent.size()){
				return "";
			}

			suffix += paras.get(i) + ":";
			word += sent.getWordAt(pos + paras.get(i)) + " ";			
		}					
		word = word.replaceAll("_", " ").trim();
		
		// parse the lexicon cp
		StringTokenizer tk = new StringTokenizer(cpname, "_");
        String lookupType;
        
        if (!tk.hasMoreTokens()) return "";
        else lookupType = tk.nextToken();        
        if (!tk.hasMoreTokens()) return "";
        
    	String lexiconName = tk.nextToken();    	
    	if (!lexicons.containsKey(lexiconName)) return "";
    	
    	if (lookupType.equalsIgnoreCase("in")){
    		if (isAnEntryOf(lexiconName, word))
    			cpvalue = "i:" + name2abbv.get(lexiconName) + ":" + suffix;
    	}
    	else if (lookupType.equalsIgnoreCase("has")){
    		if (!StringUtils.isAllSyllableInitCaped(word))
    			return "";
    		if (containAnEntryOf(lexiconName, word))
    			cpvalue = "h:" + name2abbv.get(lexiconName) + ":" + suffix;
    	}
		return cpvalue;
	}
		
	/**
	 * Parse an `and` phase to extract context predicate
	 *  
	 * @param cpname eg. and([npara]_in_[list1],[npara]_has_[list2])
	 * in_[list1]; has_[list2] are single cps
	 * npara: the number of parameters in the paras applied to this single cps
	 * @param paras the corresponding parameters
	 * @return a set of single context predicate template with parameters
	 * requirement: sum of npara of single cps (in and phase) must equals the size of paras 
	 * */
	protected Vector<LexCPair> parseAndPhase(String cpname, Vector<Integer> paras){
		StringTokenizer tk = new StringTokenizer(cpname, "(,)");
		if (!tk.nextToken().equalsIgnoreCase("and"))
			return null;
		
		try{
			Vector<LexCPair> ret = new Vector<LexCPair>();
			int cur_pos_paras = 0;
			while (tk.hasMoreTokens()){
				String npara_cp = tk.nextToken();
				int pos = StringUtils.findFirstOf(npara_cp, "_", 0);
				int npara = Integer.parseInt(npara_cp.substring(0, pos));
				
				String cp = npara_cp.substring(pos + 1);				
				Vector<Integer> cpparas = new Vector<Integer>();
				for (int j = cur_pos_paras; j < cur_pos_paras + npara; ++j)
					cpparas.add(paras.get(j));
				
				LexCPair lcp = new LexCPair(cp, cpparas);
				cur_pos_paras += npara;
				ret.add(lcp);
			}
			return ret;
		}
		catch (Exception ex){
			System.out.println("Sum of npara of single cps does not equal the size of paras " + ex.getMessage());
			return null;
		}
	}
}

class LexCPair {
	  public String name;
	  public Vector<Integer> para;
	  public LexCPair(String name, Vector<Integer>  para) { 
		  this.name = name; this.para = para;
	  }
}
