package com.kj.bo;

import java.util.Arrays;

public class FileDatabase implements Database{

	@Override
	public void add(String[] urls) {
		System.out.println("Adding to DB.");
		System.out.println(Arrays.toString(urls));
	}

	@Override
	public void delete(long[] ids) {
		System.out.println("Deleting from DB.");
		System.out.println(Arrays.toString(ids));
	}

	@Override
	public void get(long[] ids) {
		System.out.println("Getting from DB");
		System.out.println(Arrays.toString(ids));
	}
}
