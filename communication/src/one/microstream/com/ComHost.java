package one.microstream.com;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.net.InetSocketAddress;

/**
 * Host type to listen for new connections and relay them to logic for further processing,
 * potentially in another, maybe even dedicated thread.
 * 
 * @author TM
 *
 */
public interface ComHost<C> extends Runnable
{
	public InetSocketAddress address();
	
	public ComProtocolProvider<C> protocolProvider();
	
	/**
	 * Listens for incoming connections and relays them for processing.
	 */
	public void acceptConnections();
	
	@Override
	public void run();
	
	public void stop();
	
	public boolean isListening();
	
	
	
	public static <C> ComHost<C> New(
		final InetSocketAddress        address           ,
		final ComConnectionHandler<C>  connectionHandler ,
		final ComConnectionAcceptor<C> connectionAcceptor
	)
	{
		return new ComHost.Default<>(
			mayNull(address)           ,
			notNull(connectionHandler) ,
			notNull(connectionAcceptor)
		);
	}
	
	public final class Default<C> implements ComHost<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final InetSocketAddress        address           ;
		private final ComConnectionHandler<C>  connectionHandler ;
		private final ComConnectionAcceptor<C> connectionAcceptor;
		
		private transient ComConnectionListener<C> liveConnectionListener;
		private volatile boolean stopped;
		
		
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final InetSocketAddress        address           ,
			final ComConnectionHandler<C>  connectionHandler ,
			final ComConnectionAcceptor<C> connectionAcceptor
		)
		{
			super();
			this.address            = address           ;
			this.connectionHandler  = connectionHandler ;
			this.connectionAcceptor = connectionAcceptor;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final InetSocketAddress address()
		{
			return this.address;
		}

		@Override
		public final ComProtocolProvider<C> protocolProvider()
		{
			return this.connectionAcceptor.protocolProvider();
		}

		@Override
		public void run()
		{
			// the whole method may not be synchronized, otherweise a running host could never be stopped
			synchronized(this)
			{
				if(this.isListening())
				{
					// if the host is already running, this method must abort here.
					return;
				}
				
				this.liveConnectionListener = this.connectionHandler.createConnectionListener(this.address);
			}
			if(!this.stopped)
			{
				this.acceptConnections();
			}
		}
		
		@Override
		public synchronized void stop()
		{
			this.stopped = true;
			
			if(this.liveConnectionListener == null)
			{
				return;
			}
			
			this.liveConnectionListener.close();
			this.liveConnectionListener = null;
		}

		@Override
		public synchronized boolean isListening()
		{
			
			if(this.liveConnectionListener != null)
			{
				return this.liveConnectionListener.isAlive();
			}
			
			return false;
		}

		@Override
		public void acceptConnections()
		{
			// repeatedly accept new connections until stopped.
			while(!this.stopped)
			{
				synchronized(this)
				{
					if(!this.isListening())
					{
						break;
					}
					
					this.synchAcceptConnection();
				}
			}
		}
		
		private void synchAcceptConnection()
		{
			final C connection;
			
			try
			{
				connection = this.liveConnectionListener.listenForConnection();
			}
			catch(final ComException e)
			{
				//intentional, don't stop the host if a connection attempt failed
				e.printStackTrace();
				return;
			}
			
			this.connectionAcceptor.acceptConnection(connection, this);
		}
	}
	
	
	
	public static <C> ComHostCreator<C> Creator()
	{
		return ComHostCreator.New();
	}
	
}
