package kj.bo.options;

import kj.bo.database.Database;

public class Delete extends CLIOption{

	public Delete(){
		super("d","delete",true,"delete url");
	}

	@Override
	public void execute(String[] args, Database db) {
		long[] ids = new long[args.length];
		for(int k=0; k < args.length; k++){
			try {
				ids[k] = Long.parseLong(args[k]);
			} catch (NumberFormatException notANumber){
				System.err.println(args[k] + " not an id.");
			}
		}
		db.delete(ids);
	}

}
