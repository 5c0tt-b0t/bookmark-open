package kj.bo.database;

import kj.bo.Entity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
The first line, the metadata header as refered to it in code,
indicates the number of entries and the highest id:
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

/*
IMPORTANT NOTE:
The defined enums specify the fields of the metadata header and each entry in the database.
The defined sequence of values in each enum definition should accurately represent the line
respective line in the database file. For example:
	enum MetadataFields {NUM_ENTRIES, HIGHEST_ID;};
for a metadata header line:
	10 15
where 10 is the number of entries and 15 is the highest id.

The sequence should correspond to the sequence in line written or read.
*/

/**
 * Metadata header fields of the database file.
 *  The header is the first line in the database and these are its fields.
 * <li>{@link #NUM_ENTRIES}</li>
 * <li>{@link #HIGHEST_ID}</li>
 */
enum MetadataFields {NUM_ENTRIES, HIGHEST_ID;};

/**
 * Reprentation of the database's metadata header, composed of fields as specified in {@link MetadataFields}
 */
class MetadataHeader {

	private static final String SEPARATOR = " ";

	/**
	 * Maps each field, specified by {@link MetadataFields}, of the metadata header to its value.
	 */
	private EnumMap<MetadataFields, Long> map = new EnumMap<>(MetadataFields.class);
	private static MetadataHeader singleton;

	/**
	 *
	 * @param line Metadata header line which should be the first line in the file.
	 * @exception IllegalArgumentException Malformed metadata header line. It specifies more or less fields than required.
	 */
	private MetadataHeader(String line){
		final String[] values = line.split(SEPARATOR);
		final MetadataFields[] fields = MetadataFields.values();

		if(values.length != fields.length)
			throw new IllegalArgumentException("Mismatch between required and received metadata header values." +
				"\nHeadder: " + line +
				"\nRequired fields: " + fields.toString() +
				"\nNote: The required field sequence shown might not be correctly ordered.");

		// TODO: Test if there are synchronisation problems when using a parallel stream.
		IntStream.range(0, values.length)
				.sequential()
				.forEach( k -> map.put(fields[k], Long.parseLong(values[k])));
	}

	/**
	 * Returns a MetadataHeader object representing the metadata header string provided.<br><br>
	 * <p>The metadata header line should be the first line in the database file.</p>
	 * <p><b>NOTE:</b>
	 * Future calls to this method with different parameter
	 * will result in the same old unchanged MetadataHeader object to be returned.</p>
	 * @param metadata header First line in the database file.
	 * @return Header object representing the metadata header string.
	 * @exception IllegalArgumentException Malformed metadata header line. It specifies more or less fields than required.
	 */
	protected static MetadataHeader get(String metadataheader){
		if(singleton == null) singleton = new MetadataHeader(metadataheader);
		return singleton;
	}

	/**
	 * Use to update the object to maintain validity after the addition or deletion of entries.
	 * @param entriesChanged
	 */
	protected void update(long entriesChanged) {
		this.map.put(MetadataFields.NUM_ENTRIES, map.get(MetadataFields.NUM_ENTRIES) + entriesChanged);
		this.map.put(MetadataFields.HIGHEST_ID, map.get(MetadataFields.HIGHEST_ID) + entriesChanged);
	}

	/**
	 * Returns a String representation of the MetadataHeader,
	 * valid for writing the metadata header of the database file.
	 * @return String entry representing the Header object.
	 */
	protected String wrap(){
		// Do not use a parallel stream as the sting produced has to be ordered.
		return Stream.of(MetadataFields.values())
				.sequential()
				.map( field -> String.valueOf(this.map.get(field)))
				.collect(Collectors.joining(SEPARATOR));
	}

	/**
	 * Returns the appropriate value specified in the metadata header, for the field given.
	 * @param field Field for which to get the value for.
	 * @return Appropriate value for the field in the metadata header.
	 */
	protected long get(MetadataFields field) {
		return this.map.get(field);
	}

}

/**
 * Specifies fields of an entry of the database.
 */
enum Field {
	// For setting values; checks for null pointer and correct object type are done in the set method.
	/* Each field is defined as follows:
		fieldName(
			type of the value,
			getter for the field in the Entity class,
			Parser function to convert a string to the appropriate type of the field,
			setter for the field in the Entity class.
		);
	*  The sequence in which the fields are defined should correspond to how an entry line in the file, is stored.
	*/
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
	 * Returns the value of a field by extracting it from an Entity object.
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

/**
 * Database stored as a file designed to work Entity objects.<br><br>
 * Suited for a CLI application or a program
 * which exits after performing a single operation on the database.
 */
public class FileDatabase implements Database {

	/**
	 * Threshold after which more efficient algorithms are to be used.
	 * Using such algorithms on small data sets might result in slower performance.
	 */
	private static final long BULK_THRESHOLD = 1000000;

	private final String fileName;
	private final Path filePath;
	private final MetadataHeader metadataHeader;

	private static FileDatabase singleton;

	private FileDatabase(String fileName) throws IOException{
		this.fileName = fileName;
		this.filePath = Paths.get(fileName);

		final BufferedReader reader = new BufferedReader(new FileReader(fileName));
		this.metadataHeader = MetadataHeader.get(reader.readLine());
		reader.close();
	}

	/**
	 * Returns a FileDatabase object allowing interaction with a file database.<br><br>
	 * <p><b>NOTE:</b>
	 * Future calls to this method with different file path will return
	 * the same old unmodified FileDatabase object.</p>
	 * @param filePath Path to the database file.
	 * @return FileDatabase object.
	 * @throws IOException In case there is an error opening the file.
	 */
	public FileDatabase get(String filePath) throws IOException {
		if(singleton == null) singleton = new FileDatabase(filePath);
		return singleton;
	}

	/**
	 * Given an entity, a String entry will be returned which can be written to the database file.
	 * @param entity
	 * @return Entry to be written to the database file.
	 */
	private static String wrap(Entity entity){
		// NOTE: Ids have to be set appropriately before wrapping the entities.
		// Order ahas to be maintained so that each value is in the correct position.
		// TODO: Check that Field.values() returns values in order.
		return Arrays.stream(Field.values())
				.sequential()
				.map( field -> String.valueOf(field.get(entity)))
				.collect(Collectors.joining(Field.SEPARATOR));
	}

	/**
	 * Given a list of entities, a list of entries writable to the database will be returned.<br>
	 * The entries returned are in ascending order of id.
	 * @param entities List of entities.
	 * @return List of entities writable to the database file.
	 */
	private static List<String> wrap(List<Entity> entities){
		// TODO: Check performance difference between this stream implementation and a for loop.
		return entities.parallelStream()
				.map(FileDatabase::wrap)
				.sorted(Comparator.comparingLong(line -> (long) Field.ID.get((String) line)))
				.collect(Collectors.toList());
	}

	/**
	 * Returns an Entity object represented by the entry String.
	 * @param entry Database entry from which to extract the values.
	 * @return Returns an Entity object represented by the entry String.
	 */
	private static Entity unwrap(String entry){
		final Entity entity = new Entity();
		for(Field field : Field.values()){
			field.set(entity, field.get(entry));
		}
		return entity;
	}

	/**
	 * Given a list of database entries a list of entities representing them is returned.<br><br>
	 * <p><b>NOTE:</b>
	 * The returned list is not guaranteed to be ordered by id.</p>
	 * @param entries
	 * @return
	 */
	private static List<Entity> unwrap(List<String> entries){
		return entries.parallelStream()
				.map(FileDatabase::unwrap)
				.collect(Collectors.toList());
	}

	/**
	 * The method reads the database and returns all entries within it.<br><br>
	 * <p><b>NOTE:</b>
	 * The metadata header of the database (the first line) is not included in the list as it is not an entry.</p>
	 * @return List of all entries in the database file.
	 * @throws IOException In case the database file cannot be read.
	 */
	private List<String> getEntries() throws IOException{
		final List<String> lines =  Files.readAllLines(filePath);
		lines.remove(0); // Remove metadata header.
		return lines;
	}

	private TreeMap<Long, String> getTreeMapOfEntries() throws IOException {
		return new TreeMap<>(
				this.getEntries().stream()
						.collect(Collectors.toMap(ln -> (long) Field.ID.get(ln), Function.identity()))
		);
	}

	@Override
	public void add(Entity[] entities) throws IOException {
		if(entities.length == 0) return;

		// TODO: Handle overflow of id in case a long variable cannot store it anymore.

		// Setting ids so that valid ids are assigned.
		// The ids set, start 1 higher than the highest id already present in the file.
		final long startFromId = metadataHeader.get(MetadataFields.HIGHEST_ID) + 1;
		for(int k = 0; k < entities.length; k++) {
			entities[k].setId(startFromId + k);
		}

		// The metadata header is ignored by getEntries().
		List<String> lines = this.getEntries();

		// Adding metadata header.
		metadataHeader.update(entities.length);
		lines.add(0, metadataHeader.wrap());

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
		final long highestId = metadataHeader.get(MetadataFields.HIGHEST_ID);

		long[] idsToFind = ids;
		// TODO: Set BULK_THRESHOLD an appropriate number.
		if(idsToFind.length >= BULK_THRESHOLD){
			Arrays.parallelSort(idsToFind);
			final int k = Arrays.binarySearch(idsToFind,highestId);

			// All IDs higher than the highesId are removed.
			if (k != idsToFind.length) {
				// If the array contains value higher than highestId.
				// TODO: Check that the array after copyOfRange has the correct values.
				idsToFind = Arrays.copyOfRange(idsToFind, 0, k);
			}

			// TODO: Remove duplicates
		}

		final TreeMap<Long, String> entries = this.getTreeMapOfEntries();

		List<Entity> entities = new ArrayList<>();
		for(long id : idsToFind){
			if(entries.containsKey(id)) entities.add(unwrap(entries.get(id)));
		}

		return Collections.unmodifiableList(entities);
	}

	@Override
	public Entity[] get(Entity[] entities) throws IOException {
		final TreeMap<Long, String> entries = this.getTreeMapOfEntries();

		Arrays.stream(entities).parallel()
				.forEach(entity -> {
					final long id = entity.getId();
					final String entry = entries.get(id);
					entity.setUrl((String) Field.URL.get(entry));
					entity.setTags((String[]) Field.TAGS.get(entry));
				});

		return entities;
	}

}
