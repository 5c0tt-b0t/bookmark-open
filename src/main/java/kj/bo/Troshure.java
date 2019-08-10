package kj.bo;

import kj.bo.database.Database;
import kj.bo.database.FileDatabase;
import kj.bo.options.CLIOption;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.Arrays;

public class Troshure {

    public static void main(String[] args)
    {
    	final CommandLineParser cmdLineArgs = new DefaultParser();
		CommandLine cmdLine;
    	try{
    		cmdLine = cmdLineArgs.parse(CLIOptions.get(), args);
    		Database db = new FileDatabase("");
    		for(Option opt : cmdLine.getOptions()){
				((CLIOption) opt).execute(opt.getValues(), db);
			}
		} catch(ParseException parseE){
    		System.out.println(Arrays.toString(args));
    		System.out.println("error");
    		System.out.println(parseE.toString());
		} catch (IOException io){
    		System.err.println(io.getMessage());
		}
    }
}
