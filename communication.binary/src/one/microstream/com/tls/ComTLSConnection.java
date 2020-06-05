package one.microstream.com.tls;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLEngine;

import one.microstream.com.ComConnection;

public class ComTLSConnection implements ComConnection
{

	public ComTLSConnection(final SocketChannel channel, final SSLEngine sslEngine) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readCompletely(final ByteBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeCompletely(final ByteBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ByteBuffer read(final ByteBuffer defaultBuffer, final int timeout, final int length) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void write(final ByteBuffer buffer, final int timeout) {
		// TODO Auto-generated method stub
		
	}

}
