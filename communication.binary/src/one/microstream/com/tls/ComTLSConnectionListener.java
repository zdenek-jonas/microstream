package one.microstream.com.tls;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLEngine;

import one.microstream.com.ComConnection;
import one.microstream.com.ComConnectionListener;
import one.microstream.com.XSockets;

public class ComTLSConnectionListener implements ComConnectionListener<ComConnection>
{

	private final ServerSocketChannel serverSocketChannel;
	private final SSLEngine sslEngine;

	public ComTLSConnectionListener(
		final ServerSocketChannel serverSocketChannel,
		final SSLEngine sslEngine)
	{
		super();
		this.serverSocketChannel = serverSocketChannel;
		this.sslEngine = sslEngine;
	}

	@Override
	public ComTLSConnection listenForConnection()
	{
		final SocketChannel channel = XSockets.acceptSocketChannel(this.serverSocketChannel);
		return new ComTLSConnection(channel, this.sslEngine);
	}

	@Override
	public void close()
	{
		XSockets.closeChannel(this.serverSocketChannel);
	}

}
