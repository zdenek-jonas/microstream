package one.microstream.com.binarydynamic;

import one.microstream.com.ComChannel;
import one.microstream.com.ComProtocol;
import one.microstream.meta.XDebug;
import one.microstream.persistence.types.PersistenceManager;

public abstract class ComChannelDynamic<C> implements ComChannel
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	protected final PersistenceManager<?> persistenceManager;
	protected final C                     connection;
	protected final ComProtocol           protocol;
	protected final ComHandlerRegistry    handlers = new ComHandlerRegistry.Default();

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComChannelDynamic(
		final PersistenceManager<?> persistenceManager,
		final C                     connection,
		final ComProtocol           protocol)
	{
		this.connection         = connection;
		this.persistenceManager = persistenceManager;
		this.protocol           = protocol;
	}

	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	//Bypass Handlers to avoid recursion if called inside an handler ...
	public Object requestUnhandled(final Object object)
	{
		this.persistenceManager.store(object);
		return this.persistenceManager.get();
	}
	
	@Override
	public final void send(final Object graphRoot)
	{
		ComHandlerSend<?> handler = null;
		
		if(graphRoot != null)
		{
			handler = this.handlers.lookupSend(graphRoot.getClass());
		}
		
		if(handler != null )
		{
			handler.sendMessage(graphRoot);
		}
		else
		{
			this.persistenceManager.store(new ComMessageData(graphRoot));
		}
	}

	@Override
	public final Object receive()
	{
		Object received = null;
		
		while(null == received)
		{
			received = this.persistenceManager.get();
			this.persistenceManager.objectRegistry().clear();
			XDebug.println("received data...");

			final ComHandlerReceive<?> handler = this.handlers.lookupReceive(received.getClass());
			if(handler != null )
			{
				received = handler.processMessage(received);
				XDebug.println("received handled object: " + received);
				if(!handler.continueReceiving())
				{
					return received;
				}
					
			}
			else
			{
				XDebug.println("received unhandled object");
			}
		}
		
		return received;
	}

	@Override
	public final void close()
	{
		this.persistenceManager.close();
	}
}
