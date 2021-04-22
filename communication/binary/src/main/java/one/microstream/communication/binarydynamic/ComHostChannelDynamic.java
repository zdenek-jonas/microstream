package one.microstream.communication.binarydynamic;

import one.microstream.communication.types.ComHost;
import one.microstream.communication.types.ComHostChannel;
import one.microstream.communication.types.ComProtocol;
import one.microstream.persistence.types.PersistenceManager;

public class ComHostChannelDynamic<C>
	extends ComChannelDynamic<C>
	implements ComHostChannel<C>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	protected final ComHost<C> parent;
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComHostChannelDynamic(
		final PersistenceManager<?> persistenceManager,
		final C connection,
		final ComProtocol protocol,
		final ComHost<C> parent)
	{
		super(persistenceManager, connection, protocol);
		this.parent = parent;
		
		final ComTypeDescriptionRegistrationObserver observer = new ComTypeDescriptionRegistrationObserver(this);
		this.persistenceManager.typeDictionary().setTypeDescriptionRegistrationObserver(observer);
		this.initalizeHandlersInternal();
		
	}

	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private void initalizeHandlersInternal()
	{
		this.handlers.registerSendHandler(
			ComMessageNewType.class,
			new ComHandlerSendMessageNewType(
				this));
		
		this.handlers.registerReceiveHandler(
			ComMessageClientTypeMismatch.class,
			new ComHandlerReceiveMessageClientTypeMismatch(this));
		
		this.handlers.registerReceiveHandler(
			ComMessageStatus.class,
			new ComHandlerReceiveMessageStatus(this));
		
		this.handlers.registerSendHandler(
			ComMessageStatus.class,
			new ComHandlerReceiveMessageStatus(this));
		
		this.handlers.registerReceiveHandler(
			ComMessageData.class,
			new ComHandlerSendReceiveMessageData(this));
			
		this.handlers.registerSendHandler(
			ComMessageData.class,
			new ComHandlerSendReceiveMessageData(this));
	}
	
	@Override
	public final ComHost<C> parent()
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
