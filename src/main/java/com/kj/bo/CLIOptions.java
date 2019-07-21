package com.kj.bo;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class CLIOptions {

	private static Options options;

	public static Options get(){
		if(options == null){
			options = initOptions();
		}

		return options;
	}

	private static Options initOptions(){
		Options opt = new Options();

		Option delete = Option.builder("d")
				.longOpt("delete")
				.hasArg()
				.desc("Delete url with specified id.")
				.build();
		opt.addOption(delete);

		Option help = Option.builder("h")
				.longOpt("help")
				.desc("Help menu.")
				.build();
		opt.addOption(help);

		Option add = Option.builder("a")
				.longOpt("add")
				.hasArg()
				.desc("Add.")
				.build();
		opt.addOption(add);

		return opt;
	}

}
