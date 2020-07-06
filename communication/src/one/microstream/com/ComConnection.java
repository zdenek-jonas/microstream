package one.microstream.com;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface ComConnection
{
	public void close();
	public void readCompletely(ByteBuffer buffer);
	public void writeCompletely(ByteBuffer buffer);
	public ByteBuffer read(ByteBuffer buffer, int length);
	public void write(ByteBuffer buffer, int timeout);
	public void readUnsecure(final ByteBuffer buffer);
	public void writeUnsecured(ByteBuffer buffer);
	public void enableSecurity();
	
	public void setTimeOut(int inactivityTimeout);
	
	public class Default implements ComConnection
	{
		private final SocketChannel channel;
		private int readTimeOut = 1000;
		
		public Default(final SocketChannel channel)
		{
			super();
			this.channel = channel;
		}

		@Override
		public void close()
		{
			XSockets.closeChannel(this.channel);
		}

		@Override
		public void readCompletely(final ByteBuffer buffer)
		{
			this.read(buffer, buffer.capacity());
		}

		@Override
		public void writeCompletely(final ByteBuffer buffer)
		{
			XSockets.writeCompletely(this.channel, buffer);
		}
		
		@Override
		public ByteBuffer read(final ByteBuffer buffer, final int length)
		{
			if(this.readTimeOut > 0)
			{
				return this.readWithTimeOut(buffer, length);
			}
			
			return XSockets.read(this.channel, buffer, length);
		}
	
		public ByteBuffer readWithTimeOut(final ByteBuffer buffer, final int length)
		{
			final ExecutorService executor = Executors.newSingleThreadExecutor();
								
			try
			{
				executor
					.submit(() -> { return XSockets.read(this.channel, buffer, length); })
					.get(this.readTimeOut, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException | ExecutionException e)
			{
				throw new ComException("reading data failed", e);
			}
			catch(final TimeoutException e)
			{
				throw new ComExceptionTimeout("read timeout", e);
			}
			finally
			{
				executor.shutdownNow();
			}
			
			return buffer;
		}

		@Override
		public void write(final ByteBuffer buffer, final int timeout)
		{
			XSockets.writeFromBuffer(this.channel, buffer, timeout);
		}

		@Override
		public void enableSecurity()
		{
			//nothing to do, default connection is not encrypted;
		}

		@Override
		public void readUnsecure(final ByteBuffer buffer)
		{
			XSockets.readCompletely(this.channel, buffer);
		}
		
		@Override
		public void writeUnsecured(final ByteBuffer buffer)
		{
			XSockets.writeCompletely(this.channel, buffer);
		}

		@Override
		public void setTimeOut(final int inactivityTimeout)
		{
			this.readTimeOut = inactivityTimeout;
		}
		
	}

	

	

	

}
