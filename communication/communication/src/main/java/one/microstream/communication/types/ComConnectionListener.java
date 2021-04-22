package one.microstream.communication.types;

import static one.microstream.X.notNull;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import one.microstream.com.XSockets;

public interface ComConnectionListener<C>
{
	public C listenForConnection();
	
	public void close();
	
	public boolean isAlive();
	
	public static ComConnectionListener.Default Default(final ServerSocketChannel serverSocketChannel)
	{
		return new ComConnectionListener.Default(
			notNull(serverSocketChannel)
		);
	}
	
	public final class Default implements ComConnectionListener<ComConnection>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ServerSocketChannel serverSocketChannel;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final ServerSocketChannel serverSocketChannel)
		{
			super();
			this.serverSocketChannel = serverSocketChannel;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final ComConnection listenForConnection()
		{
			final SocketChannel channel = XSockets.acceptSocketChannel(this.serverSocketChannel);
			return new ComConnection.Default(channel);
		}

		@Override
		public final void close()
		{
			XSockets.closeChannel(this.serverSocketChannel);
		}

		@Override
		public boolean isAlive()
		{
			return this.serverSocketChannel.isOpen();
		}
		
	}

	
	
}
