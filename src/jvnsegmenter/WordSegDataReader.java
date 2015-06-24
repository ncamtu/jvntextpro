package jvnsegmenter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import jvntextpro.data.Sentence;
import jvntextpro.data.TWord;
import jvntextpro.util.StringUtils;

public class WordSegDataReader extends WordDataReader{
//format of POS data
	// sentences are separated by new lines
	
	public List<Sentence> readString(String dataStr){
		isTrainReading = true;
		String [] lines = dataStr.split("\n");
		List<Sentence> data = new ArrayList<Sentence>();
		
		for (String line : lines){
			String [] wordWithTags = line.split(" ");
			Sentence sent = new Sentence();
			
			for (String word : wordWithTags){
				String [] sylls = word.split("_");
				if (StringUtils.isPunc(word) || StringUtils.isAllNumber(word)){
					TWord tword = new TWord(word, "O");
					sent.addTWord(tword);
				}
				else{
					TWord tword = new TWord(sylls[0], "B_W");
					sent.addTWord(tword);
					
					for (int k = 1; k < sylls.length; ++k){
						TWord tword_ = new TWord(sylls[k], "I_W");
						sent.addTWord(tword_);
					}
				}
			}
			data.add(sent);
		}
		System.out.println(data.size());
		return data;
	}
	
	public List<Sentence> readFile(String datafile){
		try{
			//read all file to a string
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(datafile), "UTF-8"));
			
			String dataStr = "";
			String line;
			while ((line = in.readLine()) != null){
				dataStr += line + "\n";
			}
			in.close();
			
			List<Sentence> data = readString(dataStr);
			return data;
		}
		catch (Exception e){
			System.out.println(e.getMessage());
			return null;
		}		
	}
}
