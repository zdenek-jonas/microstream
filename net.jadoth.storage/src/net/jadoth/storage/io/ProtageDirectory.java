package net.jadoth.storage.io;

import java.util.function.Supplier;

import net.jadoth.collections.types.XGettingTable;

public interface ProtageDirectory extends ProtageIoElement
{
	
	public XGettingTable<String, ? extends ProtageFile> files();
	
	public ProtageFile createFile(String fileName);
	
	public default boolean contains(final ProtageFile file)
	{
		return this.contains(file.name());
	}
	
	public boolean contains(String fileName);
	
	
	/**
	 * A simple deadlock prevention strategy:<br>
	 * - directories are locked before files
	 * - directories with higher identifiers are locked before those with lower identifier
	 * - files with higher identifiers are locked before those with lower identifier
	 * 
	 * @param directory1
	 * @param directory2
	 * @param logic
	 */
	public static ProtageWritableFile executeLocked(
		final ProtageWritableDirectory      directory1,
		final ProtageWritableDirectory      directory2,
		final Supplier<ProtageWritableFile> logic
	)
	{
		synchronized(directory1)
		{
			// deadlock-prevention strategy
			if(directory1.identifier().compareTo(directory2.identifier()) >= 0)
			{
				synchronized(directory2)
				{
					return logic.get();
				}
			}
		}
		
		synchronized(directory2)
		{
			synchronized(directory1)
			{
				return logic.get();
			}
		}
	}
	
}
