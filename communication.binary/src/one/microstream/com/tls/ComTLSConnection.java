package one.microstream.com.tls;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import one.microstream.com.ComConnection;
import one.microstream.com.ComException;
import one.microstream.com.XSockets;
import one.microstream.meta.XDebug;

public class ComTLSConnection implements ComConnection
{
	private final SocketChannel channel;
	private final SSLEngine		sslEngine;

	private final ByteBuffer    sslEncyptBuffer;
	private final ByteBuffer    sslDecryptBuffer;
		
	private final ByteBuffer    sslDecryptedBuffer;
	
	
	public ComTLSConnection(final SocketChannel channel, final SSLEngine sslEngine)
	{
		XDebug.println("++");
		// TODO Auto-generated constructor stub
		
		this.channel = channel;
		this.sslEngine = sslEngine;
				
		this.sslEncyptBuffer    = ByteBuffer.allocate(sslEngine.getSession().getPacketBufferSize());
		this.sslDecryptBuffer   = ByteBuffer.allocate(sslEngine.getSession().getPacketBufferSize());
		this.sslDecryptedBuffer = ByteBuffer.allocate(sslEngine.getSession().getPacketBufferSize());
		
		try
		{
			this.doHandshake();
		}
		catch (final IOException e)
		{
			throw new ComException("TLS handshake failed" + e);
		}
		
	}

	private void doHandshake() throws IOException
	{
			
		final SSLSession session = this.sslEngine.getSession();
		final ByteBuffer myAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
		final ByteBuffer myNetData = ByteBuffer.allocate(session.getPacketBufferSize());
		final ByteBuffer peerAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
		final ByteBuffer peerNetData = ByteBuffer.allocate(session.getPacketBufferSize());
		
		this.sslEngine.beginHandshake();
	    SSLEngineResult.HandshakeStatus hs = this.sslEngine.getHandshakeStatus();
	    
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
	        			res = this.sslEngine.unwrap(peerNetData, peerAppData);
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
					res = this.sslEngine.wrap(myAppData, myNetData);
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
	        		
	        		final Runnable task = this.sslEngine.getDelegatedTask();
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
	        		
	        		hs = this.sslEngine.getHandshakeStatus();
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
	public void readCompletely(final ByteBuffer buffer)
	{
		//TODO: handle requested data longer then the max ssl packet buffer
		XDebug.println("Start readCompletely bytes: " + buffer.limit());
		
					
		if(this.sslDecryptedBuffer.position() == 0)
		{
			XDebug.println("readCompletely no allready decrypted data available, reading data ... ");
			
			XSockets.readCompletely(this.channel, this.sslDecryptBuffer);
			this.sslDecryptBuffer.flip();
				
			try
			{
				final SSLEngineResult result = this.sslEngine.unwrap(this.sslDecryptBuffer, this.sslDecryptedBuffer);
				XDebug.println("unwrap result: " + result.getStatus());
			}
			catch (final SSLException e)
			{
				throw new ComException("failed to decypt buffer", e);
			}
		}
		else
		{
			XDebug.println("readCompletely allready decrypted data available ... ");
		}
		
		
		final int limit = Math.min(buffer.limit(), this.sslDecryptedBuffer.limit());
		buffer.put(this.sslDecryptedBuffer.array(), 0, limit);
		this.sslDecryptedBuffer.position(limit);
		this.sslDecryptedBuffer.compact();
		
		XDebug.println("reading bytes: " + buffer.limit() + " done");
		
	}

	@Override
	public void writeCompletely(final ByteBuffer buffer)
	{
		
		XDebug.println("Start writing bytes: " + buffer.limit());
		
		final int maxPacketSize = this.sslEngine.getSession().getPacketBufferSize();
		XDebug.println("max Packet Size: " + maxPacketSize);
		
		try
		{
			final SSLEngineResult result = this.sslEngine.wrap(buffer, this.sslEncyptBuffer);
			XDebug.println("wrap result: " + result.getStatus());
			this.sslEncyptBuffer.flip();
		}
		catch (final SSLException e)
		{
			throw new ComException("failed to encypt buffer", e);
		}
		
		XSockets.writeFromBuffer(this.channel, this.sslEncyptBuffer, 1000);
		
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
