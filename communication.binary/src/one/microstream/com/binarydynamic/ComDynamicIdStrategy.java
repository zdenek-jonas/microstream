package one.microstream.com.binarydynamic;

import one.microstream.persistence.types.PersistenceIdStrategy;
import one.microstream.persistence.types.PersistenceObjectIdStrategy;
import one.microstream.persistence.types.PersistenceTypeIdStrategy;

public class ComDynamicIdStrategy implements PersistenceIdStrategy
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static ComDynamicIdStrategy New(final long startingObjectId)
	{
		return new ComDynamicIdStrategy(
			PersistenceTypeIdStrategy.Transient() ,
			PersistenceObjectIdStrategy.Transient(startingObjectId)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceTypeIdStrategy.Transient   typeIdStrategy  ;
	private final PersistenceObjectIdStrategy.Transient objectIdStrategy;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	ComDynamicIdStrategy(
		final PersistenceTypeIdStrategy.Transient   typeIdStrategy  ,
		final PersistenceObjectIdStrategy.Transient objectIdStrategy
	)
	{
		super();
		this.typeIdStrategy   = typeIdStrategy  ;
		this.objectIdStrategy = objectIdStrategy;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public PersistenceObjectIdStrategy.Transient objectIdStragegy()
	{
		return this.objectIdStrategy;
	}

	@Override
	public PersistenceTypeIdStrategy.Transient typeIdStragegy()
	{
		return this.typeIdStrategy;
	}

}
