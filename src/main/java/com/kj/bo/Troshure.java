package com.kj.bo;

import com.kj.bo.options.CLIOption;
import org.apache.commons.cli.*;

import java.util.Arrays;

public class Troshure {

    public static void main( String[] args )
    {
    	final CommandLineParser cmdLineArgs = new DefaultParser();
		CommandLine cmdLine;
    	try{
    		cmdLine = cmdLineArgs.parse(CLIOptions.get(), args);
    		Database db = new FileDatabase();
    		for(Option opt : cmdLine.getOptions()){
				((CLIOption) opt).execute(opt.getValues(), db);
			}
		} catch(ParseException e){
    		System.out.println(Arrays.toString(args));
    		System.out.println("error");
    		System.out.println(e.toString());
		}
    }
}
