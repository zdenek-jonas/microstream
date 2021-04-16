package one.microstream.afs.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import one.microstream.afs.exceptions.AfsException;


@TestInstance(Lifecycle.PER_CLASS)
public abstract class AbstractAfsBaseTests
{
	private final static String[] SINGLE_DIR_PATH  = {"one", "microstream", "test"};
	private final static String   SINGLE_FILE_NAME = "foo.bar";
	
	private AFileSystem fileSystem;
	
	@BeforeEach
	void prepare()
	{
		this.fileSystem = this.provideFileSystem();
	}
	
	@AfterEach
	protected void cleanup()
	{
		// no-op
	}
	
	protected abstract AFileSystem provideFileSystem();
	
	protected abstract String root();
	
	@Test
	void ensurePath()
	{
		final ADirectory dir1  = this.singleDir();
		final AFile      file1 = this.singleFile();
				
		final ADirectory dir2  = this.singleDir();
		final AFile      file2 = this.singleFile();
		
		final AFile      file3 = dir2.ensureFile(SINGLE_FILE_NAME);
		final AFile      file4 = this.fileSystem.resolveFilePath(SINGLE_DIR_PATH, SINGLE_FILE_NAME);

		assertThat(dir1).isSameAs(dir2);
		assertThat(file1).isSameAs(file2);
		assertThat(file1).isSameAs(file3);
		assertThat(file1).isSameAs(file4);
	}
	
	@Test
	void resolvePath()
	{
		final String root = this.root();
				
		this.resolveFile(root, "MyFile1.txt");

		this.resolveFile("RelFileDirLevel1", "MyFile1.txt");
		this.resolveFile(root, "AbsFileDirLevel1", "MyFile1.txt");

		this.resolveFile("RelFileDirLevel1", "RelFileDirLevel2", "MyFile2.txt");
		this.resolveFile(root, "AbsFileDirLevel1", "AbsFileDirLevel2", "MyFile2.txt");

		
		this.resolveDirectory(root);

		this.resolveDirectory("RelFileDirLevel1");
		this.resolveDirectory(root, "AbsFileDirLevel1");
		this.resolveDirectory("RelFileDirLevel1", "RelFileDirLevel2");
		this.resolveDirectory(root, "AbsFileDirLevel1", "AbsFileDirLevel2");
	}
	
	void resolveFile(final String... path)
	{
		final AFile file = this.fileSystem.ensureFilePath(path);
		file.ensureExists();
				
		final AFile reresolved = this.fileSystem.resolveFilePath(path);
		
		assertThat(file).isSameAs(reresolved);
	}
	
	void resolveDirectory(final String... path)
	{
		final ADirectory dir = this.fileSystem.ensureDirectoryPath(path);
		dir.ensureExists();
				
		final ADirectory reresolved = this.fileSystem.resolveDirectoryPath(path);
		assertThat(dir).isSameAs(reresolved);
	}
	
	@Test
	void consolidate()
	{
		final ADirectory dir = this.singleDir();
		dir.ensureFile("f1").ensureExists();
		dir.ensureFile("f2").ensureExists();
		dir.ensureFile("f3").ensureExists();
						
		final int actualCount   = dir.consolidate();
		final int expectedCount = 0;
		
		assertThat(actualCount).isEqualTo(expectedCount);
	}
	
	@Test
	void create()
	{
		final AWritableFile file = this.singleFile().useWriting();
		
		assertThat(file.ensureExists()).isTrue();  // first time must return true
		assertThat(file.ensureExists()).isFalse(); // then false
	}
	
	@Test
	void downgrade()
	{
		final AFile         file   = this.singleFile();
		final AReadableFile rFile1 = file.useWriting().downgrade();
		final AReadableFile rFile2 = file.useReading();
		
		assertThat(rFile1).isSameAs(rFile2);
	}
	
	@Test
	void exclusive()
	{
		final AFile         file   = this.singleFile();
		final AWritableFile wFile1 = file.useWriting();
		final AWritableFile wFile2 = file.useWriting();
		
		assertThat(wFile1).isSameAs(wFile2);
		
		assertThatThrownBy(() -> file.useWriting("other user 2")).isInstanceOf(AfsException.class);
		assertThatThrownBy(() -> file.useReading("other user 2")).isInstanceOf(AfsException.class);
	}
	
	@Test
	void shared()
	{
		final AFile         file   = this.singleFile();
		final AReadableFile rFile1 = file.useReading();
		final AReadableFile rFile2 = file.useReading("other user 2");
		final AReadableFile rFile3 = file.useReading("other user 3");
		
		assertThat(rFile1).isNotEqualTo(rFile2);
		assertThat(rFile1).isNotEqualTo(rFile3);
		assertThat(rFile2).isNotEqualTo(rFile3);
		
		assertThatThrownBy(() -> file.useWriting()).isInstanceOf(AfsException.class);
	}
	
	@Test
	void inventorizeOnDemand()
	{
		final String[]   fileNames = {"a", "b", "c"};
		final ADirectory dir       = this.singleDir();
		for(final String fileName : fileNames)
		{
			dir.ensureFile(fileName).ensureExists();
		}
		
		final Iterator<String> iterator = Arrays.asList(fileNames).iterator();
		dir.iterateFiles(file -> assertThat(file.name()).isEqualTo(iterator.next()));
	}
	
	@Test
	void copyFile()
	{
		final ADirectory dir  = this.singleDir();
		final AFile      from = dir.ensureFile("from");
		final AFile      to   = dir.ensureFile("to");
		final ByteBuffer data = ByteBuffer.wrap(UUID.randomUUID().toString().getBytes());
				
		from.ensureExists();
		to.ensureExists();
		
		final AWritableFile wFrom = from.useWriting();
		final AWritableFile wTo   = to.useWriting();
		
		wFrom.writeBytes(data);
		data.rewind();
		
		wFrom.copyTo(wTo);
		
		final ByteBuffer actualData = wTo.readBytes();
		
		assertThat(actualData).isEqualTo(data);
	}
	
	@Test
	void moveFile()
	{
		final AFile      file   = this.singleFile();
		final ADirectory target = file.parent().parent();
		
		file.ensureExists();
		file.useWriting().moveTo(target);
		
		assertThat(target.getFile(SINGLE_FILE_NAME).exists()).isTrue();
	}
	
	@Test
	void removeRoot()
	{
		final AFile file = this.singleFile();
		
		final AReadableFile rFile = file.useReading();
		
		assertThatThrownBy(() -> this.fileSystem.removeRoot(file.toPath()[0])).isInstanceOf(AfsException.class);
		
		rFile.release();
		
		assertThatCode(() -> this.fileSystem.removeRoot(file.toPath()[0])).doesNotThrowAnyException();
	}
	
	ADirectory singleDir()
	{
		return this.fileSystem.ensureDirectoryPath(SINGLE_DIR_PATH);
	}
	
	AFile singleFile()
	{
		return this.singleDir().ensureFile(SINGLE_FILE_NAME);
	}
	
}
