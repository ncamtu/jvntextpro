package jvnerecognizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStreamWriter;
public class NERecognizing {
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String [] args){
		displayCopyright();        
        if (!checkArgs(args)) {
            displayHelp();
            return;
        }
        
      //get model dir
        String modelDir = args[1];
        MaxentRecognizer recognizer = new MaxentRecognizer(modelDir);
        
      //tagging
        try {
        	System.out.println(args[2]);
	        if (args[2].equalsIgnoreCase("-inputfile")){
	        	System.out.println(args[3]);
	        	File inputFile = new File(args[3]);
	        	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
	        			new FileOutputStream(inputFile.getPath() + ".ne"), "UTF-8"));
	        	
	        	String result = recognizer.nerecognize(inputFile);
	        	
	        	writer.write(result);
	        	writer.close();
	        }
	        else{ //input dir
	        	String inputDir = args[3];
	        	 if (inputDir.endsWith(File.separator)) {
		                inputDir = inputDir.substring(0, inputDir.length() - 1);
		            }
		            
		            File dir = new File(inputDir);
		            String[] children = dir.list(new FilenameFilter() {
		                public boolean accept(File dir, String name) {
		                    return name.endsWith(".wseg");
		                }
		            });    
		            
		            for (int i = 0; i < children.length; i++) {
		            	System.out.println("Perform NE Recognition " + children[i]);
		            	String filename = inputDir + File.separator + children[i];
			                if ((new File(filename)).isDirectory()) {
			                    continue;
			            }
			                
			            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				        			new FileOutputStream(filename + ".ne"), "UTF-8"));
			             
			            writer.write(recognizer.nerecognize(new File(filename)));
			             
			            writer.close();
		            }
	        }
        }
        catch (Exception e){
        	System.out.println("Error while perform NE Recognition");
        	System.out.println(e.getMessage());
        	e.printStackTrace();
        }
	}
	
	/**
	 * Check args.
	 *
	 * @param args the args
	 * @return true, if successful
	 */
	public static boolean checkArgs(String[] args) {        
        if (args.length < 4) {
            return false;
        }
        
        if (args[0].compareToIgnoreCase("-modeldir") != 0) {
            return false;
        }
        
        if (!(args[2].compareToIgnoreCase("-inputfile") == 0 ||
                args[2].compareToIgnoreCase("-inputdir") == 0)) {
            return false;
        }
        
        return true;
    }
	
	/**
	 * Display copyright.
	 */
	public static void displayCopyright() {
        System.out.println("Vietnamese Named Entity Recognition:");
        System.out.println("\tusing Maximum Entropy");
        System.out.println();
    }
    
    /**
     * Display help.
     */
    public static void displayHelp() {
        System.out.println("Usage:");
        System.out.println("\tCase 1: NERecognizing -modeldir <model directory> -inputfile <input data file>");
        System.out.println("\tCase 2: NERecognizing -modeldir <model directory> -inputdir <input data directory>");
        System.out.println("Where:");        
        System.out.println("\t<model directory> is the directory contain the model and option files");
        System.out.println("\t<input data file> is the file containing input sentences that need to");
        System.out.println("\tbe processed (each sentence on a line)");
        System.out.println("\t<input data directory> is the directory containing multiple input data files (.tkn)");
        System.out.println();
    }
}
