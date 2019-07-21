package com.kj.bo.options;

import com.kj.bo.Database;

public class Add extends CLIOption {

	public Add(){
		super("a","add",true,"Add.");
	}

	@Override
	public void execute(String[] args, Database db) {
		db.add(args);
	}

}
