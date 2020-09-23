package one.microstream.com.binarydynamic;

import one.microstream.com.ComClient;
import one.microstream.com.ComClientChannel;
import one.microstream.com.ComProtocol;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceTypeHandlerEnsurer;
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
		final ComTypeDefinitionBuilder typeDefintionBuilder,
		final PersistenceTypeHandlerEnsurer<Binary> typeHandlerEnsurer
		)
	{
		super(persistenceManager, connection, protocol);
		this.parent = parent;
		this.initalizeHandlersInternal(typeHandlerManager, typeDefintionBuilder, typeHandlerEnsurer);
	}

	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	private void initalizeHandlersInternal(
		final PersistenceTypeHandlerManager<Binary> typeHandlerManager,
		final ComTypeDefinitionBuilder typeDefintionBuilder, final PersistenceTypeHandlerEnsurer<Binary> typeHandlerEnsurer)
	{
		this.handlers.registerReceiveHandler(
			ComMessageNewType.class,
			new ComHandlerReceiveMessageNewType(
				this,
				typeHandlerManager,
				typeDefintionBuilder,
				typeHandlerEnsurer
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
			ComMessageClientTypeMismatch.class,
			new ComHandlerSendMessageClientTypeMismatch(this));
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
