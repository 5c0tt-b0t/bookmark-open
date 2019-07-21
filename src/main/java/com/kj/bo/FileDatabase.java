package com.kj.bo;

public class FileDatabase implements Database{

	public FileDatabase(String fileName){

	}

	@Override
	public void add(String[] urls) {
		System.out.println("Adding to DB.");
	}

	@Override
	public void delete(long[] ids) {
		System.out.println("Deleting from DB.");
	}

	@Override
	public void get(long[] ids) {
		System.out.println("Getting from DB");
	}
}
