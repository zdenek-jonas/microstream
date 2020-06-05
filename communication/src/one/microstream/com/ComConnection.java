package one.microstream.com;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface ComConnection
{
	public void close();
	public void readCompletely(ByteBuffer buffer);
	public void writeCompletely(ByteBuffer buffer);
	public ByteBuffer read(ByteBuffer buffer, int timeout, int length);
	public void write(ByteBuffer buffer, int timeout);
	
	public class Default implements ComConnection
	{
		private final SocketChannel channel;
		
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
			XSockets.readCompletely(this.channel, buffer);
		}

		@Override
		public void writeCompletely(final ByteBuffer buffer)
		{
			XSockets.writeCompletely(this.channel, buffer);
		}

		@Override
		public ByteBuffer read(final ByteBuffer buffer, final int timeout, final int length)
		{
			return XSockets.readIntoBufferKnownLength(this.channel, buffer, timeout, length);
		}

		@Override
		public void write(final ByteBuffer buffer, final int timeout)
		{
			XSockets.writeFromBuffer(this.channel, buffer, timeout);
		}
		
	}

	

	

}
