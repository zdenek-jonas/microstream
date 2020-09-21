package one.microstream.com.binarydynamic;

import one.microstream.com.ComClient;
import one.microstream.com.ComClientChannel;
import one.microstream.com.ComProtocol;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;

public class ComClientChannelDynamic<C>
	extends ComChannelDynamic<C>
	implements ComClientChannel<C>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	protected final ComClient<C> parent;
	
		
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComClientChannelDynamic(
		final PersistenceManager<?> persistenceManager,
		final C connection,
		final ComProtocol protocol,
		final ComClient<C> parent,
		final PersistenceTypeHandlerManager<Binary> typeHandlerManager,
		final ComTypeDefinitionBuilder typeDefintionBuilder
		)
	{
		super(persistenceManager, connection, protocol);
		this.parent = parent;
		this.initalizeHandlersInternal(typeHandlerManager, typeDefintionBuilder);
	}

	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	private void initalizeHandlersInternal(
		final PersistenceTypeHandlerManager<Binary> typeHandlerManager,
		final ComTypeDefinitionBuilder typeDefintionBuilder)
	{
		this.handlers.registerReceiveHandler(
			ComMessageNewType.class,
			new ComHandlerReceiveMessageNewType(
				this,
				typeHandlerManager,
				typeDefintionBuilder
				));
		
		this.handlers.registerReceiveHandler(
			ComMessageData.class,
			new ComHandlerSendReceiveMessageData(this));
		
		this.handlers.registerSendHandler(
			ComMessageData.class,
			new ComHandlerSendReceiveMessageData(this));
		
		this.handlers.registerReceiveHandler(
			ComMessageStatus.class,
			new ComHandlerReceiveMessageStatus(this));
		
		this.handlers.registerSendHandler(
			ComMessageStatus.class,
			new ComHandlerReceiveMessageStatus(this));
		
		this.handlers.registerSendHandler(
			ComMessageClientError.class,
			new ComHandlerSendMessageClientError(this));
	}


	@Override
	public final ComClient<C> parent()
	{
		return this.parent;
	}


	@Override
	public C connection()
	{
		return this.connection;
	}


	@Override
	public ComProtocol protocol()
	{
		return this.protocol;
	}
}
