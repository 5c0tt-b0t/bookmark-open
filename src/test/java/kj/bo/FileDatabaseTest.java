package kj.bo;

import junit.framework.TestCase;
import kj.bo.database.FileDatabase;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class DbTestHarness implements AutoCloseable{

	private final File file;
	private final FileDatabase db;

	protected DbTestHarness(String identifier) throws IOException {
		this.file = File.createTempFile("test", identifier);
		Files.write(file.toPath(), "0 0".getBytes());

		this.db = new FileDatabase(file.getAbsolutePath());
	}

	public FileDatabase getDb() {
		return db;
	}

	public void printFile() throws IOException {
		BufferedReader reader = Files.newBufferedReader(file.toPath());
		reader.lines().sequential().forEach(System.out::println);
		reader.close();
	}

	public boolean dbContentEquals(List<String> linesExpected) throws IOException {
		return Files.readAllLines(file.toPath()).equals(linesExpected);
	}

	@Override
	public void close() throws IOException {
		System.out.println("Final file:");
		this.printFile();
		file.delete();
	}
}

public class FileDatabaseTest {

	@Test
	public void addValidEntriesToEmptyFile() throws IOException {
		DbTestHarness harness = new DbTestHarness("AVE");

		harness.getDb().add(new Entity[]{
				new Entity("url1", new String[]{"t1", "t2", "t3"}),
				new Entity("url2", new String[]{"t1", "t2", "t3", "t4"}),
				new Entity("url3", new String[]{"t1", "t2"})
		});

		List<String> linesExpected = Arrays.asList(
				"3 3",
				"1 url1 t1,t2,t3",
				"2 url2 t1,t2,t3,t4",
				"3 url3 t1,t2"
		);

		Assert.assertTrue("Add valid entities to empty file.", harness.dbContentEquals(linesExpected));

		harness.close();
	}

	@Test
	public void addValidEntriesToUsedFile() throws IOException {
		DbTestHarness harness = new DbTestHarness("AVU");
		FileDatabase db = harness.getDb();

		db.add(new Entity[]{
				new Entity("url1", new String[]{"t1", "t2", "t3"}),
				new Entity("url2", new String[]{"t1", "t2", "t3", "t4"}),
				new Entity("url3", new String[]{"t1", "t2"})
		});

		db.add(new Entity[]{new Entity("url4", new String[]{"t1"})});

		List<String> linesExpected = Arrays.asList(
				"4 4",
				"1 url1 t1,t2,t3",
				"2 url2 t1,t2,t3,t4",
				"3 url3 t1,t2",
				"4 url4 t1"
		);

		Assert.assertTrue("Add valid entries to file with entries.", harness.dbContentEquals(linesExpected));

		harness.close();
	}

	@Test
	public void deleteAllEntires() throws IOException{
		DbTestHarness harness = new DbTestHarness("AEU");
		FileDatabase db = harness.getDb();

		db.add(new Entity[]{
				new Entity("url1", new String[]{"t1", "t2", "t3"}),
				new Entity("url2", new String[]{"t1", "t2", "t3", "t4"}),
				new Entity("url3", new String[]{"t1", "t2"})
		});

		System.out.println("Added entries:");
		harness.printFile();
		System.out.println();

		db.delete(new long[]{1,2,3});

		Assert.assertTrue("Delete all entries.", harness.dbContentEquals(Collections.EMPTY_LIST));

	}

}
