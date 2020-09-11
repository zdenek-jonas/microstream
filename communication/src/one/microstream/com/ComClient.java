package one.microstream.com;

import java.net.InetSocketAddress;
import java.time.Duration;

public interface ComClient<C>
{
	public ComClientChannel<C> connect() throws ComException;
	
	ComClientChannel<C> connect(int retries, Duration retryDelay) throws ComException;

	public InetSocketAddress hostAddress();
	
	
	
	public static <C> ComClientCreator<C> Creator()
	{
		return ComClientCreator.New();
	}
	
	public static <C> ComClient.Default<C> New(
		final InetSocketAddress          hostAddress       ,
		final ComConnectionHandler<C>    connectionHandler ,
		final ComProtocolStringConverter protocolParser    ,
		final ComPersistenceAdaptor<C>   persistenceAdaptor,
		final int                        inactivityTimeOut
	)
	{
		return new ComClient.Default<>(
			hostAddress       ,
			connectionHandler ,
			protocolParser    ,
			persistenceAdaptor,
			inactivityTimeOut
		);
	}
	
	public final class Default<C> implements ComClient<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final InetSocketAddress          hostAddress       ;
		private final ComConnectionHandler<C>    connectionHandler ;
		private final ComProtocolStringConverter protocolParser    ;
		private final ComPersistenceAdaptor<C>   persistenceAdaptor;
		private final ComPeerIdentifier          peerIdentifier = ComPeerIdentifier.New();
		private final int                        inactivityTimeOut;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final InetSocketAddress          hostAddress       ,
			final ComConnectionHandler<C>    connectionHandler ,
			final ComProtocolStringConverter protocolParser    ,
			final ComPersistenceAdaptor<C>   persistenceAdaptor,
			final int                        inactivityTimeOut
		)
		{
			super();
			this.hostAddress        = hostAddress       ;
			this.connectionHandler  = connectionHandler ;
			this.protocolParser     = protocolParser    ;
			this.persistenceAdaptor = persistenceAdaptor;
			this.inactivityTimeOut  = inactivityTimeOut ;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final InetSocketAddress hostAddress()
		{
			return this.hostAddress;
		}
		
		@Override
		public ComClientChannel<C> connect() throws ComException
		{
			return this.connect(0, Duration.ZERO);
		}
		
		@Override
		public ComClientChannel<C> connect(final int retries, final Duration retryDelay) throws ComException
		{
			final C                   conn     = this.connectionHandler.openConnection(this.hostAddress, retries, retryDelay);
			
			this.connectionHandler.sendClientIdentifer(conn, this.peerIdentifier.getBuffer());
			this.connectionHandler.enableSecurity(conn);
			
			final ComProtocol         protocol = this.connectionHandler.receiveProtocol(conn, this.protocolParser);
			this.connectionHandler.setInactivityTimeout(conn, this.inactivityTimeOut);
			
			final ComClientChannel<C> channel  = this.persistenceAdaptor.createClientChannel(conn, protocol, this);
			
			return channel;
		}
		
	}
	
}
