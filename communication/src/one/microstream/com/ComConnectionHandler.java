package one.microstream.com;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import one.microstream.chars.XChars;
import one.microstream.chars._charArrayRange;
import one.microstream.memory.XMemory;

public interface ComConnectionHandler<C>
{
	public ComConnectionListener<C> createConnectionListener(InetSocketAddress address);
	
	public C openConnection(InetSocketAddress address);
	
	public void prepareReading(C connection);
	
	public void prepareWriting(C connection);
	
	public void close(C connection);
	
	public void closeReading(C connection);
	
	public void closeWriting(C connection);
		
	public void read(C connction, ByteBuffer buffer);
	
	public void write(C connction, ByteBuffer buffer);
	
	public default void writeChunk(
		final C             connection  ,
		final ByteBuffer    headerBuffer,
		final ByteBuffer[]  buffers
	)
	{
		this.write(connection, headerBuffer);
		
		for(final ByteBuffer buffer : buffers)
		{
			this.write(connection, buffer);
		}
	}
	
	public void sendProtocol(C connection, ComProtocol protocol, ComProtocolStringConverter stringConverter);
	
	public ComProtocol receiveProtocol(C connection, ComProtocolStringConverter stringConverter);
		
	
	
	
	public static ComConnectionHandler.Default Default()
	{
		return new ComConnectionHandler.Default();
	}
	
	public final class Default implements ComConnectionHandler<ComConnection>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int protocolLengthDigitCount = Com.defaultProtocolLengthDigitCount();
				
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
	
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public ComConnectionListener<ComConnection> createConnectionListener(
			final InetSocketAddress address
		)
		{
			final ServerSocketChannel serverSocketChannel = XSockets.openServerSocketChannel(address);
			
			return ComConnectionListener.Default(serverSocketChannel);
		}
		
		@Override
		public ComConnection openConnection(final InetSocketAddress address)
		{
			final SocketChannel clientChannel = XSockets.openChannel(address);
			
			return new ComConnection.Default(clientChannel);
		}

		@Override
		public void prepareReading(final ComConnection connection)
		{
			// no preparation needed for SocketChannel instances
		}

		@Override
		public void prepareWriting(final ComConnection connection)
		{
			// no preparation needed for SocketChannel instances
		}

		@Override
		public void close(final ComConnection connection)
		{
			connection.close();
		}

		@Override
		public void closeReading(final ComConnection connection)
		{
			// (17.11.2018 TM)TODO: SocketChannel#shutdownInput ?
			
			// SocketChannel#close is idempotent
			this.close(connection);
		}

		@Override
		public void closeWriting(final ComConnection connection)
		{
			// (17.11.2018 TM)TODO: SocketChannel#shutdownOutput ?
			
			// SocketChannel#close is idempotent
			this.close(connection);
		}

		@Override
		public void read(final ComConnection connection, final ByteBuffer buffer)
		{
			connection.readCompletely(buffer);
		}

		@Override
		public void write(final ComConnection connection, final ByteBuffer buffer)
		{
			connection.writeCompletely(buffer);
		}
		
		@Override
		public void sendProtocol(
			final ComConnection              connection     ,
			final ComProtocol                protocol       ,
			final ComProtocolStringConverter stringConverter
		)
		{
			final ByteBuffer bufferedProtocol = Com.bufferProtocol(
				protocol                     ,
				stringConverter              ,
				this.protocolLengthDigitCount
			);
			
			this.write(connection, bufferedProtocol);
		}
		
		@Override
		public ComProtocol receiveProtocol(
			final ComConnection              connection     ,
			final ComProtocolStringConverter stringConverter
		)
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
	
}
