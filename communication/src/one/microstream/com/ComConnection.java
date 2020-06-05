package one.microstream.com;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface ComConnection
{
	public void close();
	public void readCompletely(ByteBuffer buffer);
	public void writeCompletely(ByteBuffer buffer);
	
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
		
	}

}
