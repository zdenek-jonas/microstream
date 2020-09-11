package one.microstream.com;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Duration;

import one.microstream.chars.XChars;
import one.microstream.chars._charArrayRange;
import one.microstream.memory.XMemory;

public interface ComConnectionHandler<C>
{
	///////////////////////////////////////////////////////////////////////////
	// interface methods //
	//////////////////////
	
	public ComConnectionListener<C> createConnectionListener(InetSocketAddress address);
	
	public C openConnection(InetSocketAddress address);
	
	public C openConnection(InetSocketAddress hostAddress, int retries, Duration retryDelay);
	
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
		
	public void setInactivityTimeout(C connection, int inactivityTimeout);
	
	public void sendClientIdentifer(C connection, ByteBuffer buffer);
	
	public void receiveClientIdentifer(final C connection, final ByteBuffer buffer);
	
	public void enableSecurity(C connection);
	
	public static ComConnectionHandler.Default Default()
	{
		return new ComConnectionHandler.Default();
	}
	
	public class Default implements ComConnectionHandler<ComConnection>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int protocolLengthDigitCount = Com.defaultProtocolLengthDigitCount();
				
			
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default()
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
		public ComConnection openConnection(final InetSocketAddress address, final int retries, final Duration retryDelay)
		{
			int tries = 0;
			
			do
			{
				try
				{
					tries++;
					return this.openConnection(address);
				}
				catch(final Exception connectException)
				{
					if(tries <= retries)
					{
						try
						{
							Thread.sleep(retryDelay.toMillis());
						}
						catch (final InterruptedException interruptedException)
						{
							throw new ComException("Connect to " + address + " failed", interruptedException);
						}
					}
					else
					{
						throw new ComException("Connect to " + address + " failed", connectException);
					}
				}
			}
			while(tries <= retries);
			
			//Should not be reached. If a connection can't be opened an exception should have been thrown already
			throw new ComException("Connect to " + address + " failed");
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
		public void sendClientIdentifer(final ComConnection connection, final ByteBuffer buffer)
		{
			connection.writeUnsecured(buffer);
		}
		
		@Override
		public void receiveClientIdentifer(final ComConnection connection, final ByteBuffer buffer)
		{
			connection.readUnsecure(buffer);
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


		@Override
		public void enableSecurity(final ComConnection connection)
		{
			//The default Connection is not encrypted, nothing to do
		}


		@Override
		public void setInactivityTimeout(final ComConnection connection, final int inactivityTimeout)
		{
			connection.setTimeOut(inactivityTimeout);
		}

	}

	
	
}
