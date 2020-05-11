package one.microstream.com.binarydynamic;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.exceptions.BaseException;
import one.microstream.meta.XDebug;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;

public class ComHandlerReceiveMessageNewType implements ComHandlerReceive<ComMessageNewType>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceTypeHandlerManager<Binary> typeHandlerManager;
	private final ComTypeDefinitionBuilder 				typeDefintionBuilder;
	private final ComChannelDynamic<?>			 		comChannel;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComHandlerReceiveMessageNewType(
		final ComChannelDynamic<?>					comClientChannelDynamic,
		final PersistenceTypeHandlerManager<Binary> typeHandlerManager,
		final ComTypeDefinitionBuilder 				typeDefintionBuilder
	)
	{
		super();
		this.comChannel 				= comClientChannelDynamic;
		this.typeHandlerManager   		= typeHandlerManager;
		this.typeDefintionBuilder		= typeDefintionBuilder;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public Object processMessage(final ComMessageNewType message)
	{
		final String typeEntry = message.typeEntry();
		//final String typeEntry = "§%$&/(\"%";
		XDebug.println("got: " + typeEntry);
				
		try
		{
			final XGettingSequence<PersistenceTypeDefinition> defs = this.typeDefintionBuilder.buildTypeDefinitions(typeEntry);
			defs.forEach(d -> this.typeHandlerManager.ensureTypeHandler(d));
		}
		catch(final BaseException e)
		{
			XDebug.println("sending error messege");
			this.comChannel.send(new ComMessageClientError(e));
			this.comChannel.close();
			throw e;
		}
		
		XDebug.println("sending answer OK");
		this.comChannel.send(new ComMessageStatus(true));
		
		return null;
	}
	
	@Override
	public Object processMessage(final Object messageObject)
	{
		final ComMessageNewType message = (ComMessageNewType)messageObject;
		return this.processMessage(message);
	}

	@Override
	public boolean continueReceiving()
	{
		return true;
	}
}
