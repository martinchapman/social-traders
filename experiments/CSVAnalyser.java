import java.io.File;
import java.util.Arrays;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.BufferedReader;
import java.util.List;
import java.util.LinkedList;

public class CSVAnalyser extends Thread {

	
	public static void main(String args[]) {

      CSVAnalyser csvAnalyser = new CSVAnalyser(args[0], args[1]);
	  csvAnalyser.start();
	  
	}

	String path;
	String criteria;
	

	public CSVAnalyser(String path, String criteria) {
		
		this.path = path;
		this.criteria = criteria;
		printResults("", false);		

	}

	public void run() {
		
		String lastLine = null;

		File loc = new File(path);
		
		File files[] = loc.listFiles();	

		final List<String> stopWords = Arrays.asList("GD"); 

		double cumTraderAverages = 0.0;

		LinkedList<Double> dayAverages = new LinkedList<Double>();
		LinkedList<Double> gameAverages = new LinkedList<Double>();

		for (int i = 0; i < files.length; i++)
		{

			try {
			
				StreamTokenizer st = new StreamTokenizer(new FileReader(files[i]));
				st.commentChar('_');
				
				while (st.nextToken() != StreamTokenizer.TT_EOF) {

					if (st.ttype == StreamTokenizer.TT_WORD ) {            

						if (st.sval.contains("Day")) {

							dayAverages.add(cumTraderAverages / 20);

							cumTraderAverages = 0.0;

						}

						if (!(st.sval).contains("GD")) {

			       			lastLine = st.sval;
							
						}

					} 
					else if (st.ttype == StreamTokenizer.TT_NUMBER) {

						if (lastLine.contains(criteria))
						{

							cumTraderAverages = cumTraderAverages + st.nval;
							

						}

			    		} 

				}

				double cumDayAverages = 0.0;

				for (double day: dayAverages) {

					cumDayAverages = cumDayAverages + day;

				}

				gameAverages.add(cumDayAverages / 29);

				dayAverages.clear();

				printResults(files[i].toString() + " | Average " + criteria + ": " + gameAverages.getLast(), true);

			} catch (FileNotFoundException ex) {

            		ex.printStackTrace();

	        } catch (IOException ex) {

            		ex.printStackTrace();

        		}			

		}		

	}

	private void printResults(String content, Boolean append) {

		System.out.println(content);

		try {
	
			PrintWriter out = new PrintWriter(new FileWriter("summary.txt", append));
			out.println(content);
			out.flush();
		       
		} catch (IOException e) {

		    e.printStackTrace();
	   
		}

	}

}
