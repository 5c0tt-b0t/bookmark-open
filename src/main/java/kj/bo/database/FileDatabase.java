package kj.bo.database;

import kj.bo.Entity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
enum Header {
	NUM_ENTRIES, HIGHEST_ID;

	private static final String SEPARATOR = " ";

	public static long get(Header field, String line){
		if(line == null) throw new NullPointerException("Cannot get header field.");

		final String[] values = line.split(SEPARATOR);
		if(values.length != Header.values().length)
			throw new IllegalArgumentException("Mismatch between required and received header values." +
					"\nHeadder: " + line +
					"\nRequired fields: " + Header.values().toString() +
					"\nNote: The required field sequence shown might not be correctly ordered.");

		return Long.parseLong(values[field.ordinal()]);
	}

}

// Ordinal value of the enum is used to get a value of a field of an entry.
// The sequence of the enum values should reflect the sequence of fields in an entry.
enum Field {
	// For setting values, checks for null pointer and correct object type are done in the set method.
	ID   (Long.class	, Entity::getId		, Long::parseLong				, (entity, value) -> entity.setId((long) value)),
	URL  (String.class	, Entity::getUrl	, value -> value				, (entity, value) -> entity.setUrl((String) value)),
	TAGS (String[].class, Entity::getTags	, values -> values.split(","), (entity, value) -> entity.setTags((String[]) value));

	// TAG_SEPARATOR not defined as it cannot be used when the enums are being created. It causes a forward reference.
	protected static final String SEPARATOR = " ";

	private final Class type;
	private final Function<Entity, Object> getValueFromEntity;
	private final Function<String, Object> getValueFromLine;
	private final BiConsumer<Entity, Object> entityValueSetter;

	Field(Class type,
		  Function<Entity, Object> getValueFromEntity,
		  Function<String, Object> stringToValueConverter,
		  BiConsumer<Entity, Object> entityValueSetter)
	{
		this.type = type;
		this.getValueFromEntity = getValueFromEntity;
		this.getValueFromLine = line -> stringToValueConverter.apply(line.split(SEPARATOR)[this.ordinal()]);
		this.entityValueSetter = entityValueSetter;
	}

	protected Object get(Entity entity){
		return this.getValueFromEntity.apply(entity);
	}

	protected Object get(String line){
		if(line == null) throw new NullPointerException("Cannot get entry's value.");

		if(line.split(SEPARATOR).length != Field.values().length)
			throw  new IllegalArgumentException("Mismatch between required and received entry values." +
					"\nEntry: " + line +
					"\nRequired fields: " + Field.values().toString() +
					"\nNote: The required field sequence shown might not be correctly ordered.");

		return this.getValueFromLine.apply(line);
	}

	protected void set(Entity entity, Object value){
		if (value == null)
			throw new NullPointerException();
		else if (!this.type.isInstance(value))
			throw new IllegalArgumentException("Invalid value: " + value +
					"\nRequired to be an instance of " + this.type.toString() +
					"\nReceived an instance of " + value.getClass().toString());

		this.entityValueSetter.accept(entity, value);
	}

}

public class FileDatabase implements Database {

	private final String fileName;
	private final long numOfEntries, maxId;

	private static FileDatabase singleton;

	private FileDatabase(String fileName) throws IOException{
		this.fileName = fileName;
		final BufferedReader reader = new BufferedReader(new FileReader(fileName));
		final String headder = reader.readLine();
		this.numOfEntries = Header.get(Header.NUM_ENTRIES, headder);
		this.maxId = Header.get(Header.HIGHEST_ID, headder);
		reader.close();
	}

	public FileDatabase get(String fileName) throws IOException {
		if(singleton == null) singleton = new FileDatabase(fileName);
		return singleton;
	}

	// Used when preparing to write to the db file.
	// Convert and entity to a string forming an entry of the database.
	private static String wrap(Entity entity){
		return Arrays.stream(Field.values())
				.parallel()
				.map( field -> String.valueOf(field.get(entity)))
				.collect(Collectors.joining(Field.SEPARATOR));
	}

	private static List<String> wrap(List<Entity> entities){
		return entities.parallelStream()
				.map(FileDatabase::wrap)
				.collect(Collectors.toList());
	}

	// Used when after reading the db file.
	// Converts an entry in the database file (String) to an Entity.
	private static Entity unwrap(String line){
		final Entity entity = new Entity();
		// TODO: Maybe refactor.
		Field.ID.set(entity, Field.ID.get(line));
		Field.URL.set(entity, Field.URL.get(line));
		Field.TAGS.set(entity, Field.TAGS.get(line));
		return entity;
	}

	private static List<Entity> unwrap(List<String> lines){
		return lines.parallelStream()
				.map(FileDatabase::unwrap)
				.collect(Collectors.toList());
	}

	@Override
	public void add(Entity[] entities) throws IOException {
		// NOTE: remeber to write header first.
	}

	@Override
	public void delete(long[] ids) {

	}

	@Override
	public void delete(String[] urls) {

	}

	@Override
	public List<Entity> getAll() throws IOException {
		final List<String> lines = Files.readAllLines(Paths.get(fileName));
		lines.remove(0); // Remove the header which is the first line.
		return null;
	}

	@Override
	public List<Entity> get(long[] ids) throws IOException {
		//return Collections.unmodifiableList(this.get((this.getAll()).parallelStream(), ids));
		return null;
	}

	@Override
	public List<Entity> get(Entity[] entities) throws IOException {
		return null;
	}

}
