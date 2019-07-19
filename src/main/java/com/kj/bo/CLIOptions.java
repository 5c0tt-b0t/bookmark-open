package com.kj.bo;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class CLIOptions {

	public static Options getOptions(){
		Options options = new Options();

		Option add = Option.builder("a")
				.longOpt("add")
				.hasArg()
				.desc("Add a url.")
				.build();
		options.addOption(add);

		Option delete = Option.builder("d")
				.longOpt("delete")
				.hasArg()
				.desc("Delete url with specified id.")
				.build();
		options.addOption(delete);

		Option help = Option.builder("h")
				.longOpt("help")
				.desc("Help menu.")
				.build();
		options.addOption(help);
		return options;
	}

}
