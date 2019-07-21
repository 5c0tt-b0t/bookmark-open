package com.kj.bo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Option;

import java.util.Arrays;

public class Troshure {


    public static void main( String[] args )
    {
    	final CommandLineParser cmdLineArgs = new DefaultParser();
		CommandLine cmdLine;
    	try{
    		cmdLine = cmdLineArgs.parse(CLIOptions.get(), args);
    		Arrays.stream(cmdLine.getOptions()).forEachOrdered( (Option opt) -> {
					final String[] arguments = opt.getValues();
					try {
						System.out.println("-" + opt.getOpt() + ", " + arguments.length + " args: " + Arrays.toString(arguments));
					} catch(NullPointerException noArgsError){
						System.out.println("-" + opt.getOpt() +  ", no args");
					}
    		});
		} catch(ParseException e){
    		System.out.println(Arrays.toString(args));
    		System.out.println("error");
    		System.out.println(e.toString());
		}
    }
}
