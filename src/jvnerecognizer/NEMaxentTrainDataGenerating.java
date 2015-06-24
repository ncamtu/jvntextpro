package jvnerecognizer;

import java.io.File;
import java.util.Vector;

import jvnsegmenter.BasicContextGenerator;
import jvnsegmenter.ConjunctionContextGenerator;
import jvnsegmenter.RegexContextGenerator;
import jvnsegmenter.VietnameseContextGenerator;
import jvnsegmenter.WordTrainGenerating;
import jvntextpro.data.TaggingData;
import jvntextpro.data.TrainDataGenerating;

import org.w3c.dom.Element;

public class NEMaxentTrainDataGenerating extends TrainDataGenerating{
	/** The model dir. */
	String modelDir;
	
	/**
	 * Instantiates a new word train generating.
	 *
	 * @param modelDir the model dir
	 */
	public NEMaxentTrainDataGenerating(String modelDir){
		this.modelDir = modelDir;
		init();
	}
	
	/* (non-Javadoc)
	 * @see jvntextpro.data.TrainDataGenerating#init()
	 */
	@Override
	public void init() {
		// TODO Auto-generated method stub
		reader = new NEDataReader(true);			
		tagger = new TaggingData();
		
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
				tagger.addContextGenerator(contextGen);
		}
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String [] args){
		  //tagging
		if (args.length != 2){
			System.out.println("NETrainDataGenerating [Model Dir] [File/Folder]");
			System.out.println("Generating training data for NE with FlexCRFs++ or jvnmaxent (in JVnTextPro)");
			System.out.println("Model Dir: directory containing featuretemple file and LexiconStorage");
			System.out.println("Input File/Folder: file/folder name containing data manually tagged for training");
			return;
		}
		
		NEMaxentTrainDataGenerating trainGen = new NEMaxentTrainDataGenerating(args[0]);
		
		File input = new File(args[1]);
		if (input.isFile()){
			trainGen.generateTrainData(args[1], args[1]);
		}
		else if (input.isDirectory()){
			File [] infiles = input.listFiles();
			for (int i = 0; i < infiles.length; ++i){
				File infile = infiles[i];
				if (infile.getName().endsWith("tagged"))
					continue;
				trainGen.generateTrainData(infile.getPath(), infile.getPath());
			}
		}
	}
}
