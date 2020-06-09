package one.microstream.com.tls;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import one.microstream.chars.XChars;
import one.microstream.chars._charArrayRange;
import one.microstream.com.Com;
import one.microstream.com.ComConnection;
import one.microstream.com.ComConnectionHandler;
import one.microstream.com.ComConnectionListener;
import one.microstream.com.ComException;
import one.microstream.com.ComProtocol;
import one.microstream.com.ComProtocolStringConverter;
import one.microstream.com.XSockets;
import one.microstream.memory.XMemory;
import one.microstream.meta.XDebug;

public class ComTLSConnectionHandler implements ComConnectionHandler<ComConnection>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final int protocolLengthDigitCount = Com.defaultProtocolLengthDigitCount();
	
	private final static boolean SERVER_MODE = false;
	private final static boolean CLIENT_MODE = true;
	
	private final SSLContext context;
	private final String tlsProtocol = "TLSv1.2";

	private final TLSKeyManagerProvider   keyManagerProvider;
	private final TLSTrustManagerProvider trustManagerProvider;
	
	
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
	public void read(final ComConnection connection, final ByteBuffer buffer)
	{
		XDebug.println("++");
		connection.readCompletely(buffer);

	}

	@Override
	public void write(final ComConnection connection, final ByteBuffer buffer)
	{
		XDebug.println("++");
		connection.writeCompletely(buffer);
	}

	@Override
	public void sendProtocol(final ComConnection connection, final ComProtocol protocol,
			final ComProtocolStringConverter stringConverter)
	{
		XDebug.println("++");
		
		final ByteBuffer bufferedProtocol = Com.bufferProtocol(
				protocol                     ,
				stringConverter              ,
				this.protocolLengthDigitCount
			);

		this.write(connection, bufferedProtocol);
	}

	@Override
	public ComProtocol receiveProtocol(final ComConnection connection, final ComProtocolStringConverter stringConverter)
	{
		final ByteBuffer lengthBuffer = XMemory.allocateDirectNative(this.protocolLengthDigitCount);
		this.read(connection, lengthBuffer);
		
//		XDebug.printDirectByteBuffer(lengthBuffer);
		
		// buffer position must be reset for the decoder to see the bytes
		lengthBuffer.position(0);
		final String lengthDigits = XChars.standardCharset().decode(lengthBuffer).toString();
		final int    length       = Integer.parseInt(lengthDigits);
		
		final ByteBuffer protocolBuffer = XMemory.allocateDirectNative(length - this.protocolLengthDigitCount);
		this.read(connection, protocolBuffer);
		
		// buffer position must be reset to after the separator for the decoder to see the bytes
		protocolBuffer.position(1);
		final char[] protocolChars = XChars.standardCharset().decode(protocolBuffer).array();
		
		return stringConverter.parse(_charArrayRange.New(protocolChars));
	}

	public static ComConnectionHandler<ComConnection> New(final TLSKeyManagerProvider keyManagerProvider, final TLSTrustManagerProvider trustManagerProvider)
	{
		XDebug.println("++");
		return new ComTLSConnectionHandler(keyManagerProvider, trustManagerProvider);
	}
	
	private ComTLSConnectionHandler(final TLSKeyManagerProvider keyManagerProvider, final TLSTrustManagerProvider trustManagerProvider)
	{
		super();
		XDebug.println("++");
		
		this.keyManagerProvider   = keyManagerProvider;
		this.trustManagerProvider = trustManagerProvider;
				
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
			this.context.init(
				this.keyManagerProvider.get(),
				this.trustManagerProvider.get(),
				null
			);
		}
		catch (final KeyManagementException e)
		{
			throw new ComException("Failed to init SSLContext", e);
		}
		
	}
		
	private SSLEngine createSSLEngine(final boolean clientMode)
	{
		final SSLParameters defaults = this.context.getDefaultSSLParameters();
		final SSLEngine engine = this.context.createSSLEngine();
		engine.setUseClientMode(clientMode);
		
		return engine;
	}
}
