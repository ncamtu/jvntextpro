package jvntextpro.util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Vector;

// read sentences in and split them into n folds
// each sentence is in one line
// where n is specified
public class CrossValGenerator {
	//---------------------
	// member data
	//---------------------
	private int n;	
	
	// one string is one sentence
	Vector<String> data;
	
	//---------------------
	// member methods
	//---------------------
	public CrossValGenerator(int nfold){
		this.n = nfold;
		data = new Vector<String>();
	}
	
	public void readData(String folder){
		File dir = new File(folder);
		File [] files = dir.listFiles();
		
		for (File file : files){
			if (!file.getName().endsWith("tagged")) continue;
			
			try {
				System.out.println("Reading ... " + file.getName());
				BufferedReader in = new BufferedReader(new InputStreamReader(
						new FileInputStream(file.getAbsolutePath()), "UTF-8"));
				
				String line;
				String sentence = "";
				while ((line = in.readLine()) != null){
					if (line.trim().isEmpty()){
						if (!sentence.trim().isEmpty()){
							data.add(sentence.trim());
						}
						sentence = "";
					}
					
					sentence += line + "\n";
				}
				
				if (!sentence.trim().isEmpty()){
					data.add(sentence.trim());
				}								
				in.close();
			}
			catch (Exception ex){
				System.out.println("Error:" + ex.getMessage());
			}
		}
	}

	private int [] crossvalindGenerate(){
		int N = data.size();
		int[] a = new int[N];

		// insert integers 0..N-1
		for (int i = 0; i < N; i++)
			a[i] = i;

		// shuffle
		for (int i = 0; i < N; i++) {
		    int r = (int) (Math.random() * (i+1));     // int between 0 and i
		    int swap = a[r];
		    a[r] = a[i];
		    a[i] = swap;
		}
		
		return a;
	}
	
	
	public void crossvalDataGenerate(String outputDir){
		int nsentperfold = (int) Math.ceil(data.size()/n);
		int [] a = crossvalindGenerate();
		
		for (int i = 0; i < this.n; ++i){
			Vector<String> train = new Vector<String>();
			Vector<String> test = new Vector<String>();
			
			System.out.println("Getting fold " + (i + 1));
			
			for (int j = 0; j < a.length; ++j){
				if (j >= i * nsentperfold && j < (i+1) * nsentperfold){
					test.add(data.get(a[j]));
				}
				else train.add(data.get(a[j]));
			}
			
			//writing this fold
			try{
				//write train
				BufferedWriter trainout = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(outputDir + File.separator + "train" + (i+1) + ".tagged" ), "UTF-8"));
				
				for (int j = 0; j < train.size(); ++j){
					trainout.write(train.get(j) + "\n\n");
				}
				trainout.close();
				
				//write test
				BufferedWriter testout = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(outputDir + File.separator + "test" + (i+1) + ".tagged" ), "UTF-8"));
				
				for (int j = 0; j < test.size(); ++j){
					testout.write(test.get(j) + "\n\n");
				}
				
				testout.close();				
			}
			catch (Exception e){
				
			}
		}
		
	}
	
	//-----------------------------
	public static void main(String [] args){
		CrossValGenerator gen = new CrossValGenerator(5);
		gen.readData(args[0]);
		gen.crossvalDataGenerate(args[1]);
	}
	
}
