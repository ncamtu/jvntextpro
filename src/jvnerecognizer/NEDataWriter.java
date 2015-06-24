package jvnerecognizer;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import jvntextpro.data.DataWriter;
import jvntextpro.data.Sentence;

public class NEDataWriter extends DataWriter {

	@Override
	public void writeFile(List lblSeqs, String filename) {
		String ret = writeString(lblSeqs);
		try{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "UTF-8"));
			out.write(ret);
			out.close();
		}
		catch (Exception e){
			
		}
	}

	@Override
	/* example of switching states
	 * 	per -> per
		per -> loc
		per -> O
		O -> per
		O->O
	 */
	public String writeString(List lblSeqs) {
		String outStr = "";
		for (int i = 1; i < lblSeqs.size(); ++i){
			Sentence sent = (Sentence) lblSeqs.get(i);
			
			String line = "";
			String inEntity = "O";
			String entity = "";
			for (int j = 0; j < sent.size(); ++j){
				String curTag = sent.getTagAt(j);
				if (!inEntity.equalsIgnoreCase("O")){
					if(curTag.equalsIgnoreCase(inEntity)){
						entity += " " + sent.getWordAt(j);
					}
					else {
						if (curTag.equalsIgnoreCase("O")){
							line += "<" + inEntity + ">" + entity + "</" + inEntity + ">";
							entity = "";
							line += sent.getWordAt(j) + " ";
						}
						else{
							line += "<" + inEntity + ">" + entity + "</" + inEntity + ">";							
							entity = sent.getWordAt(j);
						}
					}
				}
				else {
					if (!curTag.equalsIgnoreCase("O"))
						entity = sent.getWordAt(j);
					else {//O->O
						line += sent.getWordAt(j) + " ";
					}
				}
				inEntity = curTag;
			}
			outStr += line + "\n";
		}
		return outStr;
	}

}
