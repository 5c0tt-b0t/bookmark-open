package kj.bo.options;

import kj.bo.database.Database;
import org.apache.commons.cli.Option;

public abstract class CLIOption extends Option{

	protected CLIOption(String opt, String longOpt, boolean hasArg, String description) throws IllegalArgumentException {
		super(opt, longOpt, hasArg, description);
	}

	public abstract void execute(String[] args, Database db);

}
