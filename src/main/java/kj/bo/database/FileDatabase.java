package kj.bo.database;

import kj.bo.Entity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

enum HeaderField{NUM_ENTRIES, HIGHEST_ID;};
class Header {

	private static final String SEPARATOR = " ";

	private EnumMap<HeaderField, Long> map = new EnumMap<>(HeaderField.class);
	private static Header singleton;

	private Header(String line){
		final String[] values = line.split(SEPARATOR);
		final HeaderField[] fields = HeaderField.values();

		if(values.length != fields.length)
			throw new IllegalArgumentException("Mismatch between required and received header values." +
				"\nHeadder: " + line +
				"\nRequired fields: " + fields.toString() +
				"\nNote: The required field sequence shown might not be correctly ordered.");

		// TODO: Test if there are synchronisation problems when using a parallel stream.
		IntStream.range(0, values.length)
				.sequential()
				.forEach( k -> map.put(fields[k], Long.parseLong(values[k])));
	}

	protected static Header get(String headder){
		if(singleton == null) singleton = new Header(headder);
		return singleton;
	}

	protected void update(long entriesChanged) {
		this.map.put(HeaderField.NUM_ENTRIES, map.get(HeaderField.NUM_ENTRIES) + entriesChanged);
		this.map.put(HeaderField.HIGHEST_ID, map.get(HeaderField.HIGHEST_ID) + entriesChanged);
	}

	protected String wrap(){
		// Do not use a parallel stream as the sting produced has to be ordered.
		return Stream.of(HeaderField.values())
				.sequential()
				.map( field -> String.valueOf(this.map.get(field)))
				.collect(Collectors.joining(SEPARATOR));
	}

	protected long get(HeaderField field) {
		return this.map.get(field);
	}

}
// Ordinal value of the enum is used to get a value of a field of an entry.
// The sequence of the enum values should reflect the sequence of fields in an entry.
enum Field {
	// For setting values, checks for null pointer and correct object type are done in the set method.
	ID   (Long.class	, Entity::getId		, Long::parseLong				, (entity, value) -> entity.setId((long) value)),
	URL  (String.class	, Entity::getUrl	, value -> value				, (entity, value) -> entity.setUrl((String) value)),
	TAGS (String[].class, Entity::getTags	, values -> values.split(",")	, (entity, value) -> entity.setTags((String[]) value));

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

	private static final long BULK_THRESHOLD = 100000000;

	private final String fileName;
	private final Header header;

	private static FileDatabase singleton;

	private FileDatabase(String fileName) throws IOException{
		this.fileName = fileName;
		final BufferedReader reader = new BufferedReader(new FileReader(fileName));
		this.header = Header.get(reader.readLine());
		reader.close();
	}

	public FileDatabase get(String fileName) throws IOException {
		if(singleton == null) singleton = new FileDatabase(fileName);
		return singleton;
	}

	// Used when preparing to write to the db file.
	// Convert and entity to a string forming an entry of the database.
	private static String wrap(Entity entity){
		// NOTE: Ids have to be set appropriately before wrapping the entities.
		// Order ahas to be maintained so that each value is in the correct position.
		// TODO: Check that Field.values() returns values in order.
		return Arrays.stream(Field.values())
				.sequential()
				.map( field -> String.valueOf(field.get(entity)))
				.collect(Collectors.joining(Field.SEPARATOR));
	}

	private static List<String> wrap(List<Entity> entities){
		return entities.parallelStream()
				.map(FileDatabase::wrap)
				.sorted(Comparator.comparingLong(line -> (long) Field.ID.get((String) line)))
				.collect(Collectors.toList());
	}

	// Used after reading the db file.
	// Converts an entry in the database file (String) to an Entity.
	private static Entity unwrap(String line){
		final Entity entity = new Entity();
		// TODO: Maybe refactor.
		Field.ID.set(entity, Field.ID.get(line));
		Field.URL.set(entity, Field.URL.get(line));
		Field.TAGS.set(entity, Field.TAGS.get(line));
		return entity;
	}

	// Id ordering is not guaranteed for the returned list.
	private static List<Entity> unwrap(List<String> lines){
		return lines.parallelStream()
				.map(FileDatabase::unwrap)
				.collect(Collectors.toList());
	}

	@Override
	public void add(Entity[] entities) throws IOException {
		if(entities.length == 0) return;

		// TODO: Handle overflow of id in case a long variable cannot store it anymore.
		// Setting ids so that valid ids are assigned.
		// The ids set, start 1 higher than the highest id already present in the file.
		final long startFromId = header.get(HeaderField.HIGHEST_ID) + 1;
		for(int k = 0; k < entities.length; k++) {
			entities[k].setId(startFromId + k);
		}

		List<String> lines = this.getEntries(); // Header is already removed from the getEntries values.

		// Adding header.
		header.update(entities.length);
		lines.add(0, header.wrap());

		// Adding new entries.
		lines.addAll(wrap(Arrays.asList(entities)));

		Files.write(Paths.get(fileName), lines,
				StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.WRITE);
	}

	@Override
	public void delete(long[] ids) {

	}

	@Override
	public void delete(String[] urls) {

	}

	// Returns all lines in the db file except the header.
	private List<String> getEntries() throws IOException{
		final List<String> lines =  Files.readAllLines(Paths.get(fileName));
		lines.remove(0); // Remove header.
		return lines;
	}

	@Override
	public List<Entity> getAll() throws IOException {
		final List<String> lines = this.getEntries();
		return Collections.unmodifiableList(lines
				.parallelStream()
				.map(FileDatabase::unwrap)
				.collect(Collectors.toList()));
	}

	@Override
	public List<Entity> get(final long[] ids) throws IOException {
		final long highestId = header.get(HeaderField.HIGHEST_ID);

		long[] idsToFind = ids;
		// TODO: Set BULK_THRESHOLD an appropriate number.
		if(idsToFind.length >= BULK_THRESHOLD){
			Arrays.parallelSort(idsToFind);
			final int k = Arrays.binarySearch(idsToFind,highestId);
			if (k != idsToFind.length) {
				// If the array contains value higher than highestId.
				// TODO: Check that the array after copyOfRange has the correct values.
				idsToFind = Arrays.copyOfRange(idsToFind, 0, k);
			}
		}

		TreeMap<Long, String> entries = new TreeMap<>(
				this.getEntries().stream()
						.collect(Collectors.toMap(ln -> (long) Field.ID.get(ln), Function.identity()))
		);

		List<Entity> entities = new ArrayList<>();
		for(long id : idsToFind){
			if(entries.containsKey(id)) entities.add(unwrap(entries.get(id)));
		}

		return Collections.unmodifiableList(entities);
	}

	@Override
	public List<Entity> get(final Entity[] entities) throws IOException {
		// Needs to mutate entries. Fill entries with their respective data.
		return null;
	}

}
