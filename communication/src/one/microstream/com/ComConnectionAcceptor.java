package one.microstream.com;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;


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
		final ComChannelExceptionHandler    exceptionHandler       ,
		final ComPeerIdentifier             peerIdentifier
	)
	{
		
		return new ComConnectionAcceptor.Default<>(
			notNull(protocolProvider)       ,
			notNull(protocolStringConverter),
			notNull(connectionHandler)      ,
			notNull(persistenceAdaptor)     ,
			notNull(channelAcceptor)        ,
			notNull(exceptionHandler)       ,
			notNull(peerIdentifier)
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
		private final ComPeerIdentifier			 peerIdentifier         ;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final ComProtocolProvider<C>     protocolProvider       ,
			final ComProtocolStringConverter protocolStringConverter,
			final ComConnectionHandler<C>    connectionHandler      ,
			final ComPersistenceAdaptor<C>   persistenceAdaptor     ,
			final ComHostChannelAcceptor<C>  channelAcceptor        ,
			final ComChannelExceptionHandler exceptionHandler		,
			final ComPeerIdentifier			 peerIdentifier
		)
		{
			super();
			this.protocolProvider        = protocolProvider       ;
			this.protocolStringConverter = protocolStringConverter;
			this.connectionHandler       = connectionHandler      ;
			this.persistenceAdaptor      = persistenceAdaptor     ;
			this.channelAcceptor         = channelAcceptor        ;
			this.channelExceptionHandler = exceptionHandler       ;
			this.peerIdentifier          = peerIdentifier         ;
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
						
			try
			{
				this.validiateClient(connection);
											
				this.connectionHandler.enableSecurity(connection);
				
				final ComProtocol protocol = this.protocolProvider.provideProtocol(connection);
								
				this.connectionHandler.sendProtocol(connection, protocol, this.protocolStringConverter);
				
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
			catch(final Throwable e)
			{
				//TODO: Log this somewhere
				this.connectionHandler.close(connection);
			}
																	
		}

		private void validiateClient(final C connection)
		{
			final ByteBuffer expectedIdentifer = this.peerIdentifier.getBuffer();
			final ByteBuffer clientIdentifierBuffer = ByteBuffer.allocate(expectedIdentifer.capacity());
			this.connectionHandler.receiveClientIdentifer(connection, clientIdentifierBuffer);
			clientIdentifierBuffer.flip();
												
			if(expectedIdentifer.compareTo(clientIdentifierBuffer) != 0)
			{
				throw new ComException("invalid peer identifier");
			}

		}
		
	}
	
}
