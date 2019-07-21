package com.kj.bo;

public interface Database {

	void add(String[] urls);

	void delete(long[] ids);

	void get(long[] ids);
}
