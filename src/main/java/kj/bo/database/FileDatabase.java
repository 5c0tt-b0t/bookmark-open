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


/**
 * Header fields of the database file
 * <li>{@link #NUM_ENTRIES}</li>
 * <li>{@link #HIGHEST_ID}</li>
 */
enum HeaderField{NUM_ENTRIES, HIGHEST_ID;};
// TODO: Rename Header as it can be misleading. It does not specify the columns.


/**
 * Reprentation of the database's header, composed of fields as specified in {@link HeaderField}
 */
class Header {

	private static final String SEPARATOR = " ";

	/**
	 * Maps each field, specified by {@link HeaderField}, of the header to its value.
	 */
	private EnumMap<HeaderField, Long> map = new EnumMap<>(HeaderField.class);
	private static Header singleton;

	/**
	 *
	 * @param line
	 * @exception IllegalArgumentException Malformed header line. It specifies more or less fields than required.
	 */
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

	/**
	 * Returns a Header object representing the header string provided.<br><br>
	 * <p><b>NOTE:</b>
	 * Future calls to this method with different parameter
	 * will result in the same old unchanged Header object to be returned.</p>
	 * @param header First line in the database file.
	 * @return Header object representing the header string.
	 * @exception IllegalArgumentException Malformed header line. It specifies more or less fields than required.
	 */
	protected static Header get(String header){
		if(singleton == null) singleton = new Header(header);
		return singleton;
	}

	/**
	 * Use to update Header object to maintain validity after the addition or deletion of entries.
	 * @param entriesChanged
	 */
	protected void update(long entriesChanged) {
		this.map.put(HeaderField.NUM_ENTRIES, map.get(HeaderField.NUM_ENTRIES) + entriesChanged);
		this.map.put(HeaderField.HIGHEST_ID, map.get(HeaderField.HIGHEST_ID) + entriesChanged);
	}

	/**
	 * Returns a String representation of the Header object
	 * valid for writing the header of the database file.
	 * @return String entry representing the Header object.
	 */
	protected String wrap(){
		// Do not use a parallel stream as the sting produced has to be ordered.
		return Stream.of(HeaderField.values())
				.sequential()
				.map( field -> String.valueOf(this.map.get(field)))
				.collect(Collectors.joining(SEPARATOR));
	}

	/**
	 * Returns the appropriate value specified in the header, for the field given.
	 * @param field Field for which to get the value for.
	 * @return
	 */
	protected long get(HeaderField field) {
		return this.map.get(field);
	}

}

// Ordinal value of the enum is used to get a value of a field of an entry.
// The sequence of the enum values should reflect the sequence of fields in an entry.
/**
 * Specifies fields of an entry of the database.
 */
enum Field {
	// For setting values; checks for null pointer and correct object type are done in the set method.
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

	/**
	 * Returns the value of a field by extracting it from an entity object.
	 * <br><br>
	 * <p><b>NOTE:</b>
	 * You will need to cast the returned value appropriately.<br>
	 *     <p>Example: {@code (long) Field.ID.get(entity)}</p>
	 * See {@see Field} enum definition for further information about which type to cast to.</p>
	 * @param entity Entity object.
	 * @return Appropriate value for the field.
	 */
	protected Object get(Entity entity){
		return this.getValueFromEntity.apply(entity);
	}

	/**
	 * Returns the value of the specified field by extracting it from an entry of the database file.
	 * <br><br>
	 * <p><b>NOTE:</b>
	 * You will need to cast the returned value appropriately.<br>
	 *     <p>Example: {@code (long) Field.ID.get(entry)}</p>
	 * See {@see Field} enum definition for further information about which type to cast to.</p>
	 * @param entry Single row or line of the database file specifying an entry.
	 * @return Appropriate value for the field.
	 * @exception IllegalArgumentException Malformed entry line. It specifies more or less fields than required.
	 */
	protected Object get(String entry){
		if(entry == null) throw new NullPointerException("Cannot get entry's value.");

		if(entry.split(SEPARATOR).length != Field.values().length)
			throw  new IllegalArgumentException("Mismatch between required and received entry values." +
					"\nEntry: " + entry +
					"\nRequired fields: " + Field.values().toString() +
					"\nNote: The required field sequence shown might not be correctly ordered.");
		return this.getValueFromLine.apply(entry);
	}


	/**
	 * Set the value corresponding to a field, of an entity.
	 * <p>This method does not allow a null value to be set.</p>
	 * @param entity Entity to set the value of.
	 * @param value Value to set.
	 * @exception NullPointerException If the value parameter is null.
	 * @exception IllegalArgumentException If the value's type is invalid for the field.<br>
	 * 			E.g. value of type String for ID field.
	 */
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
