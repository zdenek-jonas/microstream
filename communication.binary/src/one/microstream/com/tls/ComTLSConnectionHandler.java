package one.microstream.com.tls;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import one.microstream.com.ComConnection;
import one.microstream.com.ComConnectionHandler;
import one.microstream.com.ComConnectionListener;
import one.microstream.com.ComException;
import one.microstream.com.ComProtocol;
import one.microstream.com.ComProtocolStringConverter;
import one.microstream.com.XSockets;
import one.microstream.meta.XDebug;

public class ComTLSConnectionHandler implements ComConnectionHandler<ComConnection> {

	private final static boolean SERVER_MODE = false;
	private final static boolean CLIENT_MODE = false;
	
	private final SSLContext context;
	private final String tlsProtocol = "TLSv1.2";

	@Override
	public ComConnectionListener<ComConnection> createConnectionListener(final InetSocketAddress address) {
		XDebug.println("++");
		
		final ServerSocketChannel serverSocketChannel = XSockets.openServerSocketChannel(address);
		return new ComTLSConnectionListener(serverSocketChannel, this.createSSLEngine(SERVER_MODE));
	}


	@Override
	public ComTLSConnection openConnection(final InetSocketAddress address) {
		XDebug.println("++");
		
		final SocketChannel clientChannel = XSockets.openChannel(address);
		return new ComTLSConnection(clientChannel, this.createSSLEngine(CLIENT_MODE));
	}

	@Override
	public void prepareReading(final ComConnection connection) {
		XDebug.println("++");
		// TODO Auto-generated method stub

	}

	@Override
	public void prepareWriting(final ComConnection connection) {
		XDebug.println("++");
		// TODO Auto-generated method stub

	}

	@Override
	public void close(final ComConnection connection) {
		XDebug.println("++");
		// TODO Auto-generated method stub

	}

	@Override
	public void closeReading(final ComConnection connection) {
		XDebug.println("++");
		// TODO Auto-generated method stub

	}

	@Override
	public void closeWriting(final ComConnection connection) {
		XDebug.println("++");
		// TODO Auto-generated method stub

	}

	@Override
	public void read(final ComConnection connection, final ByteBuffer buffer) {
		XDebug.println("++");
		// TODO Auto-generated method stub

	}

	@Override
	public void write(final ComConnection connection, final ByteBuffer buffer) {
		XDebug.println("++");
		// TODO Auto-generated method stub

	}

	@Override
	public void sendProtocol(final ComConnection connection, final ComProtocol protocol,
			final ComProtocolStringConverter stringConverter) {
		XDebug.println("++");
		// TODO Auto-generated method stub

	}

	@Override
	public ComProtocol receiveProtocol(final ComConnection connection, final ComProtocolStringConverter stringConverter) {
		XDebug.println("++");
		// TODO Auto-generated method stub
		return null;
	}

	public static ComConnectionHandler<ComConnection> New()
	{
		XDebug.println("++");
		return new ComTLSConnectionHandler();
	}
	
	private ComTLSConnectionHandler()
	{
		super();
		XDebug.println("++");
				
		try
		{
			this.context = SSLContext.getInstance("TLSv1.2");
		}
		catch (final NoSuchAlgorithmException e)
		{
			throw new ComException("Failed get SSLContextInstance for " + this.tlsProtocol, e);
		}
		
		try
		{
			this.context.init(null, null, null);
		}
		catch (final KeyManagementException e)
		{
			throw new ComException("Failed to init SSLContext", e);
		}
		
	}
		
	private SSLEngine createSSLEngine(final boolean clientMode)
	{
		final SSLEngine engine = this.context.createSSLEngine();
		engine.setUseClientMode(clientMode);
		
		return engine;
	}
}
