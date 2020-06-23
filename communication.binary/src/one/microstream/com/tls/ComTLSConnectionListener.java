package one.microstream.com.tls;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;

import one.microstream.com.ComConnection;
import one.microstream.com.ComConnectionListener;
import one.microstream.com.ComException;
import one.microstream.com.XSockets;
import one.microstream.meta.XDebug;

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
		try
		{
			XDebug.println("listening for connection at: " + this.serverSocketChannel.getLocalAddress());
		}
		catch (final IOException e)
		{
			throw new ComException("failed to get local address",e);
		}
		final SocketChannel channel = XSockets.acceptSocketChannel(this.serverSocketChannel);
		return new ComTLSConnection(channel, this.sslContext, this.sslParameters, false);
	}

	@Override
	public void close()
	{
		XSockets.closeChannel(this.serverSocketChannel);
	}
}
