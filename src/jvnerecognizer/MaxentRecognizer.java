package jvnerecognizer;


import java.io.File;
import java.util.List;
import java.util.Vector;
import org.w3c.dom.Element;
import jmaxent.Classification;
import jvnpostag.POSDataWriter;
import jvnsegmenter.BasicContextGenerator;
import jvnsegmenter.ConjunctionContextGenerator;
import jvnsegmenter.RegexContextGenerator;
import jvnsegmenter.VietnameseContextGenerator;
import jvntextpro.data.DataReader;
import jvntextpro.data.DataWriter;
import jvntextpro.data.Sentence;
import jvntextpro.data.TaggingData;

public class MaxentRecognizer {
	DataReader reader = new NEDataReader();
	DataWriter writer = new NEDataWriter();
	//DataWriter writer = new POSDataWriter();
	TaggingData dataTagger = new TaggingData();
	Classification classifier;
	
	public MaxentRecognizer(String modelDir){
		init(modelDir);
	}
	
	/**
	 * Inits the.
	 *
	 * @param modelDir the model dir
	 */
	public void init(String modelDir) {
		//Read feature template file
		//Read feature template file
		String templateFile = modelDir + File.separator + "featuretemplate.xml";
		Vector<Element> nodes = BasicContextGenerator.readFeatureNodes(templateFile); 
		
		for (int i = 0; i < nodes.size(); ++i){
			Element node = nodes.get(i);
			String cpType = node.getAttribute("value");
			BasicContextGenerator contextGen = null;
			
			if (cpType.equals("Conjunction")){
				ConjunctionContextGenerator.name = "word_conj";
				ConjunctionContextGenerator.cpprefix = "w";
				
				contextGen = new ConjunctionContextGenerator(node);
			}
			else if (cpType.equals("Lexicon")){
				contextGen = new LexContextGenerator(node);
				LexContextGenerator.init(modelDir + File.separator + "LexiconStorage"); 
			}
			else if (cpType.equals("Regex")){
				contextGen = new RegexContextGenerator(node);
			}
			else if (cpType.equals("WordFeature")){
				contextGen = new WordContextGenerator(node);
			}
			else if (cpType.equals("ViSyllableFeature")){
				contextGen = new VietnameseContextGenerator(node);
			}
			
			if (contextGen != null)
				dataTagger.addContextGenerator(contextGen);
		}
		
		//create context generators
		classifier = new Classification(modelDir);
	}

	public String nerecognize(String inStr){
		List<Sentence> data = reader.readString(inStr);
		for (int i = 0; i < data.size(); ++i){
        	
    		Sentence sent = data.get(i);
    		for (int j = 0; j < sent.size(); ++j){
    			String [] cps = dataTagger.getContext(sent, j);
    			String label = classifier.classify(cps);
    			sent.getTWordAt(j).setTag(label);
    		}
    	}
		
		return writer.writeString(data);
	}
	
	public String nerecognize(File file){
		List<Sentence> data = reader.readFile(file.getPath());
		for (int i = 0; i < data.size(); ++i){
        	
    		Sentence sent = data.get(i);
    		for (int j = 0; j < sent.size(); ++j){
    			String [] cps = dataTagger.getContext(sent, j);
    			String label = classifier.classify(cps);
    			sent.getTWordAt(j).setTag(label);
    		}
    	}
		
		return writer.writeString(data);
	}
	public void setDataReader(DataReader reader){
		this.reader = reader;
	}
	
	public void setDataWriter(DataWriter writer){
		this.writer = writer;
	}
}
