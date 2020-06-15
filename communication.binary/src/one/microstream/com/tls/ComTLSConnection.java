package one.microstream.com.tls;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

import one.microstream.com.ComConnection;
import one.microstream.com.ComException;
import one.microstream.com.XSockets;
import one.microstream.meta.XDebug;

public class ComTLSConnection implements ComConnection
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final SocketChannel channel;
	private final SSLEngine		sslEngine;

	private final ByteBuffer    sslEncyptBuffer;
	private final ByteBuffer    sslDecryptBuffer;
	private final ByteBuffer    sslDecryptedBuffer;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComTLSConnection(final SocketChannel channel,
		final SSLContext sslContext,
		final SSLParameters sslParameters,
		final boolean clientMode)
	{
		XDebug.println("++");
		
		this.channel = channel;
		this.sslEngine = sslContext.createSSLEngine();
		this.sslEngine.setUseClientMode(clientMode);
		this.sslEngine.setSSLParameters(sslParameters);
								
		this.sslEncyptBuffer    = ByteBuffer.allocate(this.sslEngine.getSession().getPacketBufferSize());
		this.sslDecryptBuffer   = ByteBuffer.allocate(this.sslEngine.getSession().getPacketBufferSize());
		this.sslDecryptedBuffer = ByteBuffer.allocate(this.sslEngine.getSession().getPacketBufferSize());
		
		try
		{
			this.doHandshake();
		}
		catch (final IOException e)
		{
			throw new ComException("TLS handshake failed" + e);
		}
		
	}

	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
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
	public void close()
	{
		//XSockets.closeChannel(this.channel);
		final ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
		SSLEngineResult result;
		
		this.sslEngine.closeOutbound();
		
		while(!this.sslEngine.isOutboundDone())
		{
			try
			{
				result = this.sslEngine.wrap(emptyBuffer, this.sslEncyptBuffer);
			}
			catch (final SSLException e)
			{
				throw new ComException("failed to encypt buffer", e);
			}
			
			XDebug.println("Close wrap status: " + result.getStatus());
			
			if(result.getStatus() == Status.OK)
			{
				XSockets.writeCompletely(this.channel, this.sslEncyptBuffer);
			}
			
			this.sslEncyptBuffer.compact();
		}
		
		XSockets.closeChannel(this.channel);
	}

	@Override
	public void writeCompletely(final ByteBuffer buffer)
	{
		XDebug.println("++");
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
		this.sslEncyptBuffer.clear();
	}


	@Override
	public void write(final ByteBuffer buffer, final int timeout)
	{
		XDebug.println("++");
		XDebug.println("Start writing bytes: " + buffer.limit());
		
		final int maxPacketSize = this.sslEngine.getSession().getPacketBufferSize();
		XDebug.println("max Packet Size: " + maxPacketSize);
		
		while(buffer.remaining() > 0)
		{
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
			this.sslEncyptBuffer.clear();
		}
	}
	
	@Override
	public void readCompletely(final ByteBuffer outBuffer)
	{
		this.read(outBuffer, 1000, outBuffer.capacity());
	}
	
	@Override
	public ByteBuffer read(final ByteBuffer defaultBuffer, final int timeout, final int length)
	{
		XDebug.println("++");
		XDebug.println("Start read bytes: " + length);
		
		final ByteBuffer outBuffer;
		
		if(defaultBuffer.capacity() < length)
		{
			outBuffer = ByteBuffer.allocateDirect(length);
		}
		else
		{
			outBuffer = defaultBuffer;
			outBuffer.clear();
		}
	
			
		while(outBuffer.position() < length)
		{
			if(this.sslDecryptedBuffer.position() == 0)
			{
				XDebug.println("read no allready decrypted data available, reading data ... ");
				
				if(this.sslDecryptBuffer.position() == 0)
				{
					XDebug.println("calling readCompletely ... ");
					XSockets.readCompletely(this.channel, this.sslDecryptBuffer);
				}
									
				this.sslDecryptedBuffer.clear();
				this.sslDecryptBuffer.flip();
				
				try
				{
					final SSLEngineResult result = this.sslEngine.unwrap(this.sslDecryptBuffer, this.sslDecryptedBuffer);
												
					if(result.getStatus() == Status.BUFFER_UNDERFLOW)
					{
						XDebug.println("BUFFER_UNDERFLOW");
						
						if(this.sslDecryptBuffer.hasRemaining())
						{
							this.sslDecryptBuffer.position(this.sslDecryptBuffer.limit());
							this.sslDecryptBuffer.limit(this.sslDecryptBuffer.capacity());
							
							XSockets.readCompletely(this.channel, this.sslDecryptBuffer);
							continue;
						}
						
						throw new ComException("SSL Engine decrypt BUFFER_UNDERFLOW");
					}
					
					this.sslDecryptedBuffer.flip();
					XDebug.println("unwrap result  : " + result.getStatus());
					XDebug.println("unwrap consumed: " + result.bytesConsumed());
					XDebug.println("unwrap bytes produced: " + result.bytesProduced());
					
					this.sslDecryptBuffer.compact();
					XDebug.printBufferStats(this.sslDecryptBuffer, "sslDecryptBuffer after compact");
				}
				catch (final SSLException e)
				{
					throw new ComException("failed to decypt buffer", e);
				}
			}
					
			final int numBytes = Math.min(length, this.sslDecryptedBuffer.limit());
			
			try
			{
				outBuffer.put(this.sslDecryptedBuffer.array(), 0, numBytes);
			}
			catch(final IndexOutOfBoundsException | BufferOverflowException e)
			{
				throw new ComException("faild to copy to out buffer", e);
			}
			
			final int newLimit = this.sslDecryptedBuffer.limit() - numBytes;
			this.sslDecryptedBuffer.position(numBytes);
			this.sslDecryptedBuffer.compact();
			this.sslDecryptedBuffer.limit(newLimit);
			
			XDebug.printBufferStats(this.sslDecryptedBuffer, "sslDecryptedBuffer after compact");
		}
		
		XDebug.println("--");
		
		return outBuffer;
		
	}
}
