package com.kj.bo.options;

import com.kj.bo.Strategy;
import org.apache.commons.cli.Option;

import java.util.Arrays;

public class Add implements Strategy, CLIOption
{

	@Override
	public void execute(String[] args) {
		System.out.println("ADDING TO DB" + Arrays.toString(args));
	}

	@Override
	public Option getOption(){
		return Option.builder("a").longOpt("add").hasArg().desc("Add.").build();
	}
}
