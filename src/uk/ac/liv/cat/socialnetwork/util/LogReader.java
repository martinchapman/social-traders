package uk.ac.liv.cat.socialnetwork.util;

import org.apache.log4j.Logger;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;


/**
 * 
 * @author Martin Chapman 
 */
public class LogReader implements Parameterizable  {

 	private static Logger logger = Logger
					.getLogger(LogReader.class);

	String source;
	PrintWriter writer;
	BufferedReader reader;
	File logFile;

	private LinkedList<String> searchTerms = new LinkedList<String>();

	@Override
	public void setup(ParameterDatabase parameters, Parameter base) {

	}

	public LogReader() {

		searchTerms.add("none");
		init();

	}

	public LogReader(String source) {
		
		this.source = source;

	}
	
	private void init() {

		try {
			
			logFile = new File("experiments/logs/livelog.log");

			reader = new BufferedReader(new FileReader(logFile));

		} 
		catch (FileNotFoundException ex) {

      		ex.printStackTrace();

    		}
		catch (IOException ex) {

		  	ex.printStackTrace();

		}

	}
	
	public void addSearchTerm(String term) {

		searchTerms.add(term);	

	}

	public void removeSearchTerm(String term) {

		searchTerms.remove(term);
	
	}

	public String read() throws IOException  {
		
	  String line;
	  String info;
	  String[] parsed;

	  if (searchTerms == null) return "";

	  while ((line = reader.readLine()) != null) {
		
			for (String source : searchTerms) {

			if (!(((parsed = line.split("-\\s"))[parsed.length - 1]).length() < source.length())) {

				if ((info = parsed[parsed.length - 1]).substring(0, source.length()).equals(source)) {

					return info.substring(source.length(), info.length());
	
				}

			}
		  
		  }

	  }
	
	  return "";

	}
}
