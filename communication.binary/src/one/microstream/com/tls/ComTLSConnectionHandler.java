package one.microstream.com.tls;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

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
	
	private final static boolean TLS_CLIENT_MODE = true;
		
	private final SSLContext context;
	
	private final TLSKeyManagerProvider   keyManagerProvider;
	private final TLSTrustManagerProvider trustManagerProvider;
	private final TLSParametersProvider   tlsParameterProvider;
	private final SecureRandomProvider    randomProvider;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private ComTLSConnectionHandler(
		final TLSKeyManagerProvider   keyManagerProvider,
		final TLSTrustManagerProvider trustManagerProvider,
		final TLSParametersProvider   tlsParameterProvider,
		final SecureRandomProvider    randomProvider)
	{
		super();
		XDebug.println("++");
		
		this.tlsParameterProvider = tlsParameterProvider;
		this.keyManagerProvider   = keyManagerProvider;
		this.trustManagerProvider = trustManagerProvider;
		this.randomProvider       = randomProvider;
				
		try
		{
			this.context = SSLContext.getInstance(tlsParameterProvider.getSSLProtocol());
		}
		catch (final NoSuchAlgorithmException e)
		{
			throw new ComException("Failed get SSLContextInstance for " + tlsParameterProvider.getSSLProtocol(), e);
		}
		
		try
		{
			this.context.init(
				this.keyManagerProvider.get(),
				this.trustManagerProvider.get(),
				this.randomProvider.get()
			);
		}
		catch (final KeyManagementException e)
		{
			throw new ComException("Failed to init SSLContext", e);
		}
	}
	
	public static ComConnectionHandler<ComConnection> New(
		final TLSKeyManagerProvider   keyManagerProvider,
		final TLSTrustManagerProvider trustManagerProvider,
		final TLSParametersProvider   tlsParameterProvider,
		final SecureRandomProvider    randomProvider)
	{
		XDebug.println("++");
		return new ComTLSConnectionHandler(keyManagerProvider, trustManagerProvider, tlsParameterProvider, randomProvider);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public ComConnectionListener<ComConnection> createConnectionListener(final InetSocketAddress address) {
		XDebug.println("++");
		
		final ServerSocketChannel serverSocketChannel = XSockets.openServerSocketChannel(address);
		return new ComTLSConnectionListener(serverSocketChannel, this.context, this.tlsParameterProvider.getSSLParameters());
	}


	@Override
	public ComTLSConnection openConnection(final InetSocketAddress address) {
		XDebug.println("++");
		
		final SocketChannel clientChannel = XSockets.openChannel(address);
		return new ComTLSConnection(clientChannel, this.context, this.tlsParameterProvider.getSSLParameters(), TLS_CLIENT_MODE);
	}

	@Override
	public void prepareReading(final ComConnection connection)
	{
		// no action required
	}

	@Override
	public void prepareWriting(final ComConnection connection)
	{
		// no action required
	}

	@Override
	public void close(final ComConnection connection)
	{
		connection.close();
	}

	@Override
	public void closeReading(final ComConnection connection)
	{
		//closing read only not supported
		connection.close();
	}

	@Override
	public void closeWriting(final ComConnection connection)
	{
		//closing write only not supported
		connection.close();
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


}
