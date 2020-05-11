package one.microstream.com.binarydynamic;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;

public class ComHandlerNewType extends AbstractBinaryHandlerCustom<NewPersistenceTypeDefinition>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private PersistenceTypeHandlerManager<Binary> typeHandlerManager;
	private final ComTypeDefinitionBuilder typeDefintionBuilder;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComHandlerNewType(final ComTypeDefinitionBuilder	 typeDefintionBuilder)
	{
		super(NewPersistenceTypeDefinition.class,
				CustomFields(
				chars("value")
				));
		
		this.typeDefintionBuilder = typeDefintionBuilder;
	}

	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		//nopo
	}

	@Override
	public void updateState(final Binary data, final NewPersistenceTypeDefinition instance, final PersistenceLoadHandler handler)
	{
		//nopo
	}

	@Override
	public boolean hasPersistedReferences()
	{
		return false;
	}

	@Override
	public final boolean hasPersistedVariableLength()
	{
		return true;
	}
	
	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return true;
	}

	@Override
	public void store(final Binary data, final NewPersistenceTypeDefinition instance, final long objectId,
			final PersistenceStoreHandler<Binary> handler)
	{
			data.storeStringSingleValue(this.typeId(), objectId, instance.typeEntry());
	}

	@Override
	public NewPersistenceTypeDefinition create(final Binary data, final PersistenceLoadHandler handler)
	{
		final String typeEntry = String.valueOf(data.build_chars(0));
				
		final XGettingSequence<PersistenceTypeDefinition> defs = this.typeDefintionBuilder.buildTypeDefinitions(typeEntry);
		defs.forEach(d -> this.typeHandlerManager.ensureTypeHandler(d));
							
		return new NewPersistenceTypeDefinition(true);
	}

	public void setPersistenceTypeHandlerManager(final PersistenceTypeHandlerManager<Binary> typeHandlerManager)
	{
		this.typeHandlerManager = typeHandlerManager;
	}

}
