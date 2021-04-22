package one.microstream.communication.binary.types;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryPersistenceRootsProvider;
import one.microstream.persistence.types.PersistenceCustomTypeHandlerRegistry;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.persistence.types.PersistenceRoots;

public class ComBinaryPersistenceRootsProvider implements BinaryPersistenceRootsProvider
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ComBinaryPersistenceRootsProvider()
	{
		super();
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public PersistenceRoots provideRoots()
	{
		//no-op
		return null;
	}

	@Override
	public PersistenceRoots peekRoots()
	{
		//no-op
		return null;
	}

	@Override
	public void updateRuntimeRoots(final PersistenceRoots runtimeRoots)
	{
		//no-op
	}

	@Override
	public void registerRootsTypeHandlerCreator(
		final PersistenceCustomTypeHandlerRegistry<Binary> typeHandlerRegistry,
		final PersistenceObjectRegistry                    objectRegistry
	)
	{
		//no-op
	}

}
