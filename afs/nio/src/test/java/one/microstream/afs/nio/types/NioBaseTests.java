package one.microstream.afs.nio.types;

import java.io.IOException;
import java.nio.file.FileSystem;

import com.google.common.jimfs.Jimfs;

import one.microstream.afs.types.AFileSystem;
import one.microstream.afs.types.AbstractAfsBaseTests;

public class NioBaseTests extends AbstractAfsBaseTests
{
	FileSystem jimfs;
	
	@Override
	protected AFileSystem provideFileSystem()
	{
		return NioFileSystem.New(
			this.jimfs = Jimfs.newFileSystem()
		);
	}
	
	@Override
	protected String root()
	{
		return this.jimfs.getRootDirectories().iterator().next().toString();
	}
	
	@Override
	protected void cleanup()
	{
		super.cleanup();
		
		try
		{
			this.jimfs.close();
		}
		catch(final IOException e)
		{
			// swallow
		}
	}
}
