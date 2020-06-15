package one.microstream.com.tls;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;

import one.microstream.com.ComConnection;
import one.microstream.com.ComConnectionListener;
import one.microstream.com.XSockets;

public class ComTLSConnectionListener implements ComConnectionListener<ComConnection>
{

	private final ServerSocketChannel serverSocketChannel;
	private final SSLContext sslContext;

	public ComTLSConnectionListener(
		final ServerSocketChannel serverSocketChannel,
		final SSLContext context)
	{
		super();
		this.serverSocketChannel = serverSocketChannel;
		this.sslContext = context;
	}

	@Override
	public ComTLSConnection listenForConnection()
	{
		final SocketChannel channel = XSockets.acceptSocketChannel(this.serverSocketChannel);
		return new ComTLSConnection(channel, this.sslContext, false);
	}

	@Override
	public void close()
	{
		XSockets.closeChannel(this.serverSocketChannel);
	}
}
