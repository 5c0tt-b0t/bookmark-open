package kj.bo.database;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/*
Each field in the file is separated with a space.
The last line is an entry, not a trailing \n.
The first line indicates the number of entries and the highest id:
NUM_ENTRIES HIGHEST_ID

Format of an entry:
ID URL Tags
5 https://www.duckduckgo.com search,privacy

Example of a database file.
3 10
3 https://www.youtube.com videos,streaming,google
5 https://www.duckduckgo.com search,privacy
10 https://www.bbc.com news
*/

public class FileDatabase implements Database {

	// String because the .split method of String requires it.
	private static final String SEPARATOR = " ", TAG_SEPARATOR = ",";

	private static FileDatabase singleton;

	private final String fileName;
	private final long numOfEntries;
	private final long maxId;

	private enum HeadderField {NUM_ENTRIES, HIGHEST_ID;};
	private static class Headder extends EnumMap<HeadderField, Object> {
		public Headder(String headder){
			super(HeadderField.class);
			final String[] values = headder.split(SEPARATOR);
			final HeadderField[] fields = HeadderField.values();
			for(int k = 0; k < fields.length; k++){
				Object value = fields[k];
				switch(fields[k]){
					case HIGHEST_ID:
					case NUM_ENTRIES: value = Long.parseLong(values[k]); break;
				}
				this.put(fields[k], value);
			}
		}
	}

	// Ordinal value of the enum is used to get a value of a field of an entry.
	// The sequence of the enum values should reflect the sequence of fields in an entry.
	private static enum Field {ID, URL, TAGS;};
	private static Object getField(Field field, String line){
		try{
			String value = line.split(SEPARATOR)[field.ordinal()];
			switch(field){
				case ID:  return Long.parseLong(value);
				case URL: return value;
				case TAGS: return value.split(TAG_SEPARATOR); // String[] returned
			}
		} catch (NullPointerException | NumberFormatException e){
			System.err.println("Could not parse: " + line + ". " + e.getMessage());
		}
		return line;
	}

	private FileDatabase(String fileName) throws IOException{
		this.fileName = fileName;
		final BufferedReader reader = new BufferedReader(new FileReader(fileName));
		final Headder headder = new Headder(reader.readLine());
		this.numOfEntries = (long) headder.get(HeadderField.NUM_ENTRIES);
		this.maxId = (long) headder.get(HeadderField.HIGHEST_ID);
		reader.close();
	}

	public FileDatabase get(String fileName) throws IOException{
		if(singleton == null) singleton = new FileDatabase(fileName);
		return singleton;
	}

	@Override
	public void add(String[] urls) throws IOException {
		// TODO: create entry and then add to db.
		List<String> entries = Arrays.asList(urls);
		Files.write(Paths.get(fileName), Arrays.asList(urls), Charset.forName("UTF-8"), StandardOpenOption.CREATE,StandardOpenOption.WRITE);
	}

	@Override
	public void delete(long[] ids) {

	}

	@Override
	public void delete(String[] urls) {

	}

	@Override
	public List<String> getAll() throws IOException {
		final List<String> lines = Files.readAllLines(Paths.get(fileName));
		lines.remove(0); // Remove the headder which is the first line.
		return Collections.unmodifiableList(lines);
	}

	@Override
	public List<String> get(long[] ids) throws IOException {
		return Collections.unmodifiableList(this.get((this.getAll()).parallelStream(), ids));
	}

	public List<String> getSequential(long[] ids) throws IOException {
		return Collections.unmodifiableList(this.get(this.getAll().stream().sequential(), ids));
	}

	private List<String> get(Stream<String> stream, long[] ids) {
		List<Long> processedIds = LongStream.of(ids)
				.filter( id -> id <= maxId)
				.collect(() -> new ArrayList<Long>(), (ArrayList<Long> array, long id) -> array.add(id), (arr1, arr2) -> arr1.addAll(arr2));

		return stream
				.filter( line -> processedIds.contains((long) getField(Field.ID, line)))
				.collect(Collectors.toList());
	}
}
