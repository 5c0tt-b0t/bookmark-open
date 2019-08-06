package kj.bo.database;

import kj.bo.Entity;

import java.io.IOException;
import java.util.List;

public interface Database {

	void add(Entity[] entities) throws IOException;

	void delete(long[] ids) throws IOException;

	void delete(String[] urls) throws IOException;

	List<Entity> getAll() throws IOException;

	List<Entity> get(long[] ids) throws IOException;

	// Mutates entries filling them with data. Entities are required to have an id.
	List<Entity> get(Entity[] entities) throws IOException;
}
