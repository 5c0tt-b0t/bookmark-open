package com.kj.bo.options;

import com.kj.bo.Strategy;
import org.apache.commons.cli.Option;

public class Delete implements Strategy, CLIOption{

	@Override
	public void execute(String[] args) {
		System.out.println("Delete url.");
	}

	@Override
	public Option getOption() {
		return Option.builder("d").longOpt("delete").hasArg().desc("delete url").build();
	}
}
