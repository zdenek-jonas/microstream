package one.microstream.com.tls;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import one.microstream.com.ComConnection;
import one.microstream.com.ComConnectionListener;
import one.microstream.com.XSockets;

public class ComTLSConnectionListener implements ComConnectionListener<ComConnection>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final ServerSocketChannel serverSocketChannel;
	private final SSLContext sslContext;
	private final SSLParameters sslParameters;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComTLSConnectionListener(
		final ServerSocketChannel serverSocketChannel,
		final SSLContext context,
		final SSLParameters sslParameters)
	{
		super();
		this.serverSocketChannel = serverSocketChannel;
		this.sslContext = context;
		this.sslParameters = sslParameters;
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
}
