package jvnerecognizer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import jvntextpro.data.*;

public class NEDataReader extends DataReader {

	/** The is train reading. */
	private boolean isTrainReading = false;
	
		/** The tags. */
	protected String [] tags = {"per", "loc", "org", "time", "misc", "num", "cur", "pct", "O"};
	
	
	/*Constructors
	 * (non-Javadoc)
	 * @see jvntextpro.data.DataReader#readFile(java.lang.String)
	 */
	public NEDataReader(){
		//do nothing
		isTrainReading = false;
	}
	
	public NEDataReader(boolean isTrainReading){
		this.isTrainReading = isTrainReading;
	}
	
	/**
	 * read files
	 */
	@Override
	public List<Sentence> readFile(String datafile) {
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(datafile), "UTF-8"));
			
			String dataStr = "";
			String line = "";
			while ((line = reader.readLine()) != null){
				dataStr += line + "\n";
			}
			
			reader.close();
			
			return readString(dataStr);
		}
		catch (Exception ex){
			System.out.println("read data fail " + ex.getMessage());
			return null;
		}
	}

	@Override
	public List<Sentence> readString(String dataStr) {
		// TODO Auto-generated method stub
		String [] lines = dataStr.split("\n");
		List<Sentence> data = new ArrayList<Sentence>();
		
		for (String line:lines){
			if (line.isEmpty())
				continue;
			Sentence sentence = new Sentence();
			
			if (isTrainReading){
				int curpos = 0;			
				NamedEntity nent;
				boolean hasEntity = false;			
				
				while ((nent = NamedEntity.getNextEntity(line, curpos)) != null){
					hasEntity = true;
									
					String aheadStr = line.substring(curpos, nent.beginIdx);
					
					StringTokenizer tk = new StringTokenizer(aheadStr, " {}");
					while (tk.hasMoreTokens()){
						String token = tk.nextToken();
						sentence.addTWord(token,"O");
					}
					
					StringTokenizer entTk = new StringTokenizer(nent.instance, " {}");
					boolean beginToken = true;
					if (entTk.hasMoreTokens()){
						while (entTk.hasMoreTokens()){
							if (beginToken) {
								sentence.addTWord(entTk.nextToken(), "B_" + nent.type);
								beginToken = false;
							}
							else
								sentence.addTWord(entTk.nextToken(), "I_" + nent.type);
						}
					}
					curpos = nent.endIdx + 1;
				}
				
				if (curpos < line.length()){			
					String remain = line.substring(curpos, line.length());
					
					StringTokenizer tk = new StringTokenizer(remain, " ");
					while (tk.hasMoreTokens()){					
						sentence.addTWord(tk.nextToken(),"O");					
					}
					
					if (hasEntity){
						data.add(sentence);
					}
				}
			}// end read training data				
			else {
				StringTokenizer tk = new StringTokenizer(line," ");
				while (tk.hasMoreTokens())
					sentence.addTWord(tk.nextToken(), null);
				data.add(sentence);
			}					
		}
		
		return data;
	}

}

class NamedEntity{
	public String type; //time, per,org, loc
	public String instance; 
	public int beginIdx;
	public int endIdx;	
	
	public static NamedEntity getNextEntity (String data, int curpos){
		NamedEntity nent = null;
		
		int beginOpenTag = data.indexOf("<", curpos);
		int endOpenTag = data.indexOf(">", beginOpenTag);
		
		if (beginOpenTag == -1 || endOpenTag == -1 )
			return null;		
		
		String openTag = data.substring(beginOpenTag + 1, endOpenTag).toLowerCase();
		String closeTag = "</" + openTag + ">";
		
		int closeTagIdx = data.indexOf(closeTag, endOpenTag + 1);
		
		if (closeTagIdx == -1)
			return null;
		
		nent = new NamedEntity();
		
		nent.beginIdx = beginOpenTag;
		nent.endIdx = closeTagIdx + closeTag.length();
		nent.instance = data.substring(endOpenTag + 1, closeTagIdx).trim();
		nent.type = openTag;
		
		return nent;
	}
}

