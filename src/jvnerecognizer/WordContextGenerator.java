package jvnerecognizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;

import jvnsegmenter.SyllableContextGenerator;
import jvntextpro.data.Sentence;
import jvntextpro.util.StringUtils;

public class WordContextGenerator extends SyllableContextGenerator {

	public WordContextGenerator(Element node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String[] getContext(Sentence sent, int pos) {
	List<String> cps = new ArrayList<String>();
		
		for (int it = 0; it < cpnames.size(); ++it){			
			String cp = cpnames.get(it);
			Vector<Integer> paras = this.paras.get(it);
			String cpvalue = "";
			if (cp.equals("initial_cap")){
				cpvalue = ic(sent,pos,paras.get(0));
			}
			else if (cp.equals("all_cap")){
				cpvalue = ac(sent, pos, paras.get(0));
			}
			else if (cp.equals("mark")){
				cpvalue = mk(sent, pos, paras.get(0));
			}
			else if (cp.equals("first_obsrv")){
				if (pos + paras.get(0) == 0)
					cpvalue = "fi:" + paras.get(0);
			}
			else if (cp.equals("contain_percent_sign")){
				cpvalue = cpct(sent, pos, paras.get(0));
			}
			else if (cp.equals("contain_slash_sign")){
				cpvalue = cslash(sent, pos, paras.get(0));
			}
			else if (cp.equals("contain_comma_sign")){
				cpvalue = ccomma(sent, pos, paras.get(0));
			}
			else if (cp.endsWith("all_cap_and_digit")){
				cpvalue = ccnd(sent, pos, paras.get(0));
			}
			else if (cp.endsWith("all_syll_init_caped")){
				cpvalue = asic(sent,pos,paras.get(0));
			}
			
			if (!cpvalue.equals("")) cps.add(cpvalue);
		}
		String [] ret = new String[cps.size()];		
		return cps.toArray(ret);
	}
	
	protected String asic(Sentence sent, int pos, int i){
		String cp;
		if (0 <= (pos + i) && (pos + i) < sent.size()){
			String word = sent.getWordAt(pos + i);
			cp = "asic:" + i;		
			
			if (!StringUtils.isAllSyllableInitCaped(word)){
				cp = "";
			}				
			
		}
		else cp = "";
		return cp;
	}
	protected String cpct(Sentence sent, int pos, int i){
		String cp;
		if (0 <= (pos + i) && (pos + i) < sent.size()){
			String word = sent.getWordAt(pos + i);
			cp = "cpct:" + i;		
			
			if (word.indexOf("%") == -1){
				cp = "";
			}				
			
		}
		else cp = "";
		return cp;
	}
	
	protected String cslash(Sentence sent, int pos, int i){
		String cp;
		if (0 <= (pos + i) && (pos + i) < sent.size()){
			String word = sent.getWordAt(pos + i);
			cp = "cs:" + i;		
			
			if (word.indexOf("/") == -1){
				cp = "";
			}				
		}
		else cp = "";
		return cp;
	}
	
	protected String ccomma(Sentence sent, int pos, int i){
		String cp;
		if (0 <= (pos + i) && (pos + i) < sent.size()){
			String word = sent.getWordAt(pos + i);
			cp = "cm:" + i;		
			
			if (word.indexOf(":") == -1){
				cp = "";
			}				
		}
		else cp = "";
		return cp;
	}
	
	protected String ccnd(Sentence sent, int pos, int i){
		String cp;
		if (0 <= (pos + i) && (pos + i) < sent.size()){
			String word = sent.getWordAt(pos + i);
			cp = "ccnd:" + i;		
			
			if (!StringUtils.isAllCap(word) || !StringUtils.containLetterAndDigit(word)){
				cp = "";
			}				
		}
		else cp = "";
		return cp;
	}
}
