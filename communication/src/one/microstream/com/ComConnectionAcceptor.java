package one.microstream.com;

import static one.microstream.X.notNull;

import one.microstream.meta.XDebug;


/**
 * Logic to greet/authenticate the client, exchange metadata, create a {@link ComChannel} instance.
 * Potentially in another, maybe even dedicated thread.
 * 
 * @author TM
 *
 */
public interface ComConnectionAcceptor<C>
{
	public ComProtocolProvider<C> protocolProvider();
	
	public void acceptConnection(C connection, ComHost<C> parent);
	
	
	
	public static <C> ComConnectionAcceptorCreator<C> Creator()
	{
		return ComConnectionAcceptorCreator.New();
	}
	
	public static <C> ComConnectionAcceptor<C> New(
		final ComProtocolProvider<C>        protocolProvider       ,
		final ComProtocolStringConverter    protocolStringConverter,
		final ComConnectionHandler<C>       connectionHandler      ,
		final ComPersistenceAdaptor<C>      persistenceAdaptor     ,
		final ComHostChannelAcceptor<C>     channelAcceptor		   ,
		final ComChannelExceptionHandler    exceptionHandler
	)
	{
		
		return new ComConnectionAcceptor.Default<>(
			notNull(protocolProvider)       ,
			notNull(protocolStringConverter),
			notNull(connectionHandler)      ,
			notNull(persistenceAdaptor)     ,
			notNull(channelAcceptor)        ,
			notNull(exceptionHandler)
		);
	}
	
	public final class Default<C> implements ComConnectionAcceptor<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ComProtocolProvider<C>     protocolProvider       ;
		private final ComProtocolStringConverter protocolStringConverter;
		private final ComConnectionHandler<C>    connectionHandler      ;
		private final ComPersistenceAdaptor<C>   persistenceAdaptor     ;
		private final ComHostChannelAcceptor<C>  channelAcceptor        ;
		private final ComChannelExceptionHandler channelExceptionHandler;
				
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final ComProtocolProvider<C>     protocolProvider       ,
			final ComProtocolStringConverter protocolStringConverter,
			final ComConnectionHandler<C>    connectionHandler      ,
			final ComPersistenceAdaptor<C>   persistenceAdaptor     ,
			final ComHostChannelAcceptor<C>  channelAcceptor        ,
			final ComChannelExceptionHandler exceptionHandler
		)
		{
			super();
			this.protocolProvider        = protocolProvider       ;
			this.protocolStringConverter = protocolStringConverter;
			this.connectionHandler       = connectionHandler      ;
			this.persistenceAdaptor      = persistenceAdaptor     ;
			this.channelAcceptor         = channelAcceptor        ;
			this.channelExceptionHandler = exceptionHandler       ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final ComProtocolProvider<C> protocolProvider()
		{
			return this.protocolProvider;
		}
		
		@Override
		public final void acceptConnection(final C connection, final ComHost<C> parent)
		{
			// note: things like authentication could be done here in a wrapping implementation.
			
			final ComProtocol protocol = this.protocolProvider.provideProtocol(connection);
			
			try
			{
				this.connectionHandler.sendProtocol(connection, protocol, this.protocolStringConverter);
			}
			catch(final Throwable e)
			{
				XDebug.println("Protokoll exchange failed: \n" + e);
			}
				
			final ComHostChannel<C> channel = this.persistenceAdaptor.createHostChannel(connection, protocol, parent);
							
			try
			{
				this.channelAcceptor.acceptChannel(channel);
			}
			catch(final Throwable e)
			{
				this.channelExceptionHandler.handleException(e, channel);
			}

		}
		
	}
	
}
