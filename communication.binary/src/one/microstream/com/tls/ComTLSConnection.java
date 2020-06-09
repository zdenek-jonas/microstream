package one.microstream.com.tls;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLSession;

import one.microstream.com.ComConnection;
import one.microstream.com.ComException;
import one.microstream.meta.XDebug;

public class ComTLSConnection implements ComConnection
{

	private final SocketChannel channel;

	public ComTLSConnection(final SocketChannel channel, final SSLEngine sslEngine)
	{
		XDebug.println("++");
		// TODO Auto-generated constructor stub
		
		this.channel = channel;
				
		try
		{
			this.doHandshake(sslEngine);
		}
		catch (final IOException e)
		{
			throw new ComException("TLS handshake failed" + e);
		}
		
	}

	private void doHandshake(final SSLEngine engine) throws IOException
	{
			
		final SSLSession session = engine.getSession();
		final ByteBuffer myAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
		final ByteBuffer myNetData = ByteBuffer.allocate(session.getPacketBufferSize());
		final ByteBuffer peerAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
		final ByteBuffer peerNetData = ByteBuffer.allocate(session.getPacketBufferSize());
		
		engine.beginHandshake();
	    SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
	    
	    while (hs != SSLEngineResult.HandshakeStatus.FINISHED &&
	            hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING)
	    {
	    	XDebug.println("Handshake status: " + hs);
	    	
	    	switch (hs)
	    	{
	        	case NEED_UNWRAP:
	        		
	        		XDebug.println("case NEED_UNWRAP");
	        		
	        		final int bytesRead = this.channel.read(peerNetData);
	        		XDebug.println("bytes read: " + bytesRead);
	        		
	        		peerNetData.flip();
	        		
	        		/*
	        		 * in some cases the input buffer may contain more the one handshake message to be unwrapped
	        		 * before the next read is done.
	        		 */
	        		SSLEngineResult res;
	        		do
	        		{
	        			res = engine.unwrap(peerNetData, peerAppData);
	        			hs = res.getHandshakeStatus();
	        			
	        			XDebug.println("Unwrap status: " + res.getStatus());
	        			XDebug.println("Handshake status: " + hs);
	 	               
	 	               
	        		}
	        		while(hs == HandshakeStatus.NEED_UNWRAP &&
	        			peerNetData.hasRemaining() &&
	        			res.getStatus() == Status.OK);
	        		
	        		peerNetData.compact();
	               	        			        			    	                     		
	        		break;
	        		
	        	case NEED_WRAP :
	        		
	        		XDebug.println("case NEED_WRAP");
					myNetData.clear();
					res = engine.wrap(myAppData, myNetData);
					hs = res.getHandshakeStatus();
												
					XDebug.println("Wrap status: " + res.getStatus());
					XDebug.println("Handshake status: " + hs);
					
					if(res.getStatus() == SSLEngineResult.Status.OK )
					{
						myNetData.flip();
						this.channel.write(myNetData);
					}
	        		
	        		break;
	        		
	        	case NEED_TASK :
	        		XDebug.println("case NEED_TASK");
	        		
	        		final Runnable task = engine.getDelegatedTask();
	        		if(task != null)
	        		{
	        			final Thread engineTask = new Thread(task);
	        			engineTask.start();
	        			try
	        			{
							engineTask.join();
						}
	        			catch (final InterruptedException e)
	        			{
	        				throw new ComException("Error in SSLEngine handshake task ", e);
						}
	        		}
	        		
	        		hs = engine.getHandshakeStatus();
	        		XDebug.println("Handshake status: " + hs);
	        		
	        		break;
	        		
				default:
					XDebug.println("case DEFAULT??");
					break;
	    	}
	    		    	
	    	XDebug.println("Handshake status: " + hs);
	    }
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
