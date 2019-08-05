package kj.bo.database;

import javax.imageio.event.IIOWriteProgressListener;
import java.io.IOException;
import java.util.List;

public interface Database {

	void add(String[] urls) throws IOException;

	void delete(long[] ids) throws IOException;

	void delete(String[] urls) throws IOException;

	List<String> getAll() throws IOException;

	List<String> get(long[] ids) throws IOException;
}
