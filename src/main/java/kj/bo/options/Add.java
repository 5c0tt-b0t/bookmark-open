package kj.bo.options;

import kj.bo.database.Database;

public class Add extends CLIOption {

	public Add(){
		super("a","add",true,"Add.");
	}

	@Override
	public void execute(String[] args, Database db) {

		//db.add(args);
	}

}
