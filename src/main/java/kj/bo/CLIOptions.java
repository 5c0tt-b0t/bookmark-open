package kj.bo;

import kj.bo.options.Add;
import kj.bo.options.Delete;
import org.apache.commons.cli.Options;

public class CLIOptions {

	private static Options options;

	public static Options get(){
		if(options == null){
			initOptions();
		}

		return options;
	}

	private static void initOptions(){
		options = new Options();

		options.addOption(new Delete());
		options.addOption(new Add());
	}

}
