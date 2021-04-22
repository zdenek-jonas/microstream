package one.microstream.communication.tls;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;

import one.microstream.com.XSockets;
import one.microstream.communication.types.ComConnection;
import one.microstream.communication.types.ComConnectionListener;

public class ComTLSConnectionListener implements ComConnectionListener<ComConnection>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final ServerSocketChannel serverSocketChannel;
	private final SSLContext sslContext;
	private final TLSParametersProvider sslParameters;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComTLSConnectionListener(
		final ServerSocketChannel serverSocketChannel,
		final SSLContext context,
		final TLSParametersProvider tlsParameterProvider)
	{
		super();
		this.serverSocketChannel = serverSocketChannel;
		this.sslContext = context;
		this.sslParameters = tlsParameterProvider;
	}

	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public ComTLSConnection listenForConnection()
	{
		final SocketChannel channel = XSockets.acceptSocketChannel(this.serverSocketChannel);
		return new ComTLSConnection(channel, this.sslContext, this.sslParameters, false);
	}

	@Override
	public void close()
	{
		XSockets.closeChannel(this.serverSocketChannel);
	}

	@Override
	public boolean isAlive()
	{
		return this.serverSocketChannel.isOpen();
	}
}
