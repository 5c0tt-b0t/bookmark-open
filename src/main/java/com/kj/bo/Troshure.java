package com.kj.bo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import java.util.Arrays;

public class Troshure
{
    public static void main( String[] args )
    {
    	final CommandLineParser cmdLineArgs = new DefaultParser();
		CommandLine cmdLine;
    	try{
    		cmdLine = cmdLineArgs.parse(CLIOptions.getOptions(),args);
    		System.out.println(Arrays.toString(cmdLine.getOptions()));
		} catch(ParseException e){
    		System.out.println(Arrays.toString(args));
    		System.out.println("error");
    		System.out.println(e.toString());
		}
    }
}
