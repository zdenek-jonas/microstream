package one.microstream.com.tls;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
	
	/**
	 * Delay in case of an SSL unwrap buffer underflow in ms
	 */
	private static final int SSL_BUFFER_UNDERFLOW_RETRY_DELAY = 10;
	private static final int SSL_HANDSHAKE_READ_TIMEOUT       = 1000;
	
	
	private final SocketChannel channel;
	private final SSLEngine		sslEngine;

	private final ByteBuffer    sslEncyptedOut;
	private final ByteBuffer    sslEnryptedIn;
	private final ByteBuffer    sslDecrypted;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComTLSConnection(final SocketChannel channel,
		final SSLContext sslContext,
		final SSLParameters sslParameters,
		final boolean clientMode)
	{
		XDebug.println("++");
		
		try
		{
			XDebug.println("local address:  " + channel.getLocalAddress());
			XDebug.println("remote address: " + channel.getRemoteAddress());
		}
		catch (final IOException e)
		{
			throw new ComException("Failed to get connection info", e);
		}
		
		
		this.channel = channel;
		this.sslEngine = sslContext.createSSLEngine();
		this.sslEngine.setUseClientMode(clientMode);
		this.sslEngine.setSSLParameters(sslParameters);
								
		this.sslEnryptedIn    = ByteBuffer.allocate(this.sslEngine.getSession().getPacketBufferSize());
		this.sslEncyptedOut   = ByteBuffer.allocate(this.sslEngine.getSession().getPacketBufferSize());
		this.sslDecrypted     = ByteBuffer.allocate(this.sslEngine.getSession().getPacketBufferSize());
		
		try
		{
			this.doHandshake();
		}
		catch (final IOException e)
		{
			throw new ComException("TLS handshake failed ", e);
		}
		
	}

	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	
	private synchronized void read(final SocketChannel channel, final ByteBuffer buffer, final int timeout)
	{
		final ExecutorService executor = Executors.newSingleThreadExecutor();
				
		int readResult = 0;
		
		try
		{
			readResult = executor
				.submit(() -> { return channel.read(buffer); })
				.get(SSL_HANDSHAKE_READ_TIMEOUT, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException | ExecutionException | TimeoutException e)
		{
			throw new ComException("reading data during hanshake failed", e);
		}
		finally
		{
			executor.shutdownNow();
		}

		if(readResult < 0)
		{
			throw new ComException("reading data during handshake failed");
		}
	}
	
	private HandshakeStatus unwrapHandshakeData(final ByteBuffer peerNetData, final ByteBuffer peerAppData) throws IOException
	{
		SSLEngineResult.HandshakeStatus hs = this.sslEngine.getHandshakeStatus();
			 		
		if(peerNetData.position() == 0)
		{
			//final int bytesRead = this.channel.read(peerNetData);
			//XDebug.println("bytes read: " + bytesRead);
			this.read(this.channel, peerNetData, 1000);
			
		}
		
		peerNetData.flip();
				
		while(hs == HandshakeStatus.NEED_UNWRAP &&
    		peerNetData.hasRemaining())
 		{
	 		final SSLEngineResult engineResult = this.sslEngine.unwrap(peerNetData, peerAppData);
	 		hs = engineResult.getHandshakeStatus();
	 			 		
	 		XDebug.println("Unwrap status: " + engineResult.getStatus() + " bytes consumed: " + engineResult.bytesConsumed());
	 		XDebug.println("Handshake status: " + hs);
	 			 			 		 			 		
	 		final Status status = engineResult.getStatus();
	 		
	 		if(status != Status.OK)
	 		{
	 			if(status == Status.CLOSED || status == Status.BUFFER_OVERFLOW)
	 			{
	 				throw new ComException("TLS Handshake failed with engine status " + status);
	 			}
	 			
	 			if(status == Status.BUFFER_UNDERFLOW)
	 			{
	 				this.read(this.channel, peerNetData, 1000);
	 			}
	 		}
 		}
		
		peerNetData.compact();
 		
		return hs;
	}
	
	private HandshakeStatus wrapHandshakeData(final ByteBuffer appData, final ByteBuffer netData) throws IOException
	{
		netData.clear();
		final SSLEngineResult engineResult = this.sslEngine.wrap(appData, netData);
		final SSLEngineResult.HandshakeStatus hs = engineResult.getHandshakeStatus();
									
		XDebug.println("Wrap status: " + engineResult.getStatus());
		XDebug.println("Handshake status: " + hs);
		
		if(engineResult.getStatus() == SSLEngineResult.Status.OK )
		{
			netData.flip();
			this.channel.write(netData);
			netData.compact();
		}
		
		return hs;
	}
	
	private HandshakeStatus executeHandshakeTask()
	{
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
		
		return this.sslEngine.getHandshakeStatus();
	}
	
	private void doHandshake() throws IOException
	{
			
		final SSLSession session = this.sslEngine.getSession();
		final ByteBuffer handshakeData = ByteBuffer.allocate(session.getApplicationBufferSize());
		
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
	        		
	        		hs = this.unwrapHandshakeData(this.sslEnryptedIn, this.sslDecrypted);
	        			               	        			        			    	                     		
	        		break;
	        		
	        	case NEED_WRAP :
	        		
	        		XDebug.println("case NEED_WRAP");
	        		
	        		hs = this.wrapHandshakeData(handshakeData, this.sslEncyptedOut);
						        		
	        		break;
	        		
	        	case NEED_TASK :
	        		
	        		XDebug.println("case NEED_TASK");
	        			        		
	        		hs = this.executeHandshakeTask();
	        			        		
	        		break;
                	        		
				default:
					//should never happen but if so throw an exception to avoid unknown behavior during the SSL handshake
					throw new ComException("Unexpected handshake status: " + hs );
	    	}
	    		    	
	    	XDebug.println("Handshake status: " + hs);
	    }
	    
//    	XDebug.println("appData:     " + appData.remaining()     + " " + appData.position()     + " " + appData.limit() );
//    	XDebug.println("netData:     " + netData.remaining()     + " " + netData.position()     + " " + netData.limit() );
//    	XDebug.println("peerAppData: " + peerAppData.remaining() + " " + peerAppData.position() + " " + peerAppData.limit() );
//    	XDebug.println("peerNetData: " + peerNetData.remaining() + " " + peerNetData.position() + " " + peerNetData.limit() );
	}

	@Override
	public void close()
	{
		XDebug.println("++");
		//this zero sized buffer is needed for the SSLEngine to create the closing messages
		final ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
		SSLEngineResult result;
				
		this.sslEngine.closeOutbound();
		
		while(!this.sslEngine.isOutboundDone())
		{
			this.sslEncyptedOut.clear();
			try
			{
				result = this.sslEngine.wrap(emptyBuffer, this.sslEncyptedOut);
			}
			catch (final SSLException e)
			{
				throw new ComException("failed to encypt buffer", e);
			}
			
			XDebug.println("Close wrap status: " + result.getStatus());
			
			if(result.getStatus() == Status.OK)
			{
				XSockets.writeCompletely(this.channel, this.sslEncyptedOut);
			}
			
			this.sslEncyptedOut.compact();
		}
		
		XDebug.println("Closing channel");
		XSockets.closeChannel(this.channel);
	}

	@Override
	public void writeCompletely(final ByteBuffer buffer)
	{
		this.write(buffer, 0);
	}


	@Override
	public void write(final ByteBuffer buffer, final int timeout)
	{
		XDebug.println("++");
		XDebug.println("Start writing bytes: " + buffer.limit());
		
		if(!this.channel.isOpen())
		{
			throw new ComException("Can not write to closed channel!");
		}
		
		final int maxPacketSize = this.sslEngine.getSession().getPacketBufferSize();
		XDebug.println("max Packet Size: " + maxPacketSize);
		
		while(buffer.remaining() > 0)
		{
			try
			{
				final SSLEngineResult result = this.sslEngine.wrap(buffer, this.sslEncyptedOut);
				XDebug.println("wrap result: " + result.getStatus());
				this.sslEncyptedOut.flip();
			}
			catch (final SSLException e)
			{
				throw new ComException("failed to encypt buffer", e);
			}
			
			XSockets.writeCompletely(this.channel, this.sslEncyptedOut);
			this.sslEncyptedOut.clear();
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
		
		if(!this.channel.isOpen())
		{
			throw new ComException("Can not read from closed channel!");
		}
		
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
			if(this.sslDecrypted.position() == 0)
			{
				XDebug.println("read no allready decrypted data available, reading data ... ");
				
				if(this.sslEnryptedIn.position() == 0)
				{
					XDebug.println("calling readCompletely ... ");
					//XSockets.readCompletely(this.channel, this.sslDecryptBuffer);
					
					this.readInternal(this.channel, this.sslEnryptedIn);
					
				}
									
				this.sslDecrypted.clear();
				this.sslEnryptedIn.flip();
				
				try
				{
					final SSLEngineResult result = this.sslEngine.unwrap(this.sslEnryptedIn, this.sslDecrypted);
												
					if(result.getStatus() == Status.BUFFER_UNDERFLOW)
					{
						XDebug.println("BUFFER_UNDERFLOW");
						
						if(this.sslEnryptedIn.hasRemaining())
						{
							this.sslEnryptedIn.position(this.sslEnryptedIn.limit());
							this.sslEnryptedIn.limit(this.sslEnryptedIn.capacity());
							
							//XSockets.readCompletely(this.channel, this.sslDecryptBuffer);
							this.readInternal(this.channel, this.sslEnryptedIn);
							continue;
						}
						
						//Sleep some ms before retry. This is only relevant if the SocketCannel is in non-blocking mode
						try
						{
							Thread.sleep(SSL_BUFFER_UNDERFLOW_RETRY_DELAY);
						}
						catch (final InterruptedException e)
						{
							throw new ComException(e);
						}
					}
					
					this.sslDecrypted.flip();
					XDebug.println("unwrap result  : " + result.getStatus());
					XDebug.println("unwrap consumed: " + result.bytesConsumed());
					XDebug.println("unwrap bytes produced: " + result.bytesProduced());
					
					this.sslEnryptedIn.compact();
					//XDebug.printBufferStats(this.sslDecryptBuffer, "sslDecryptBuffer after compact");
				}
				catch (final SSLException e)
				{
					throw new ComException("failed to decypt buffer", e);
				}
			}
					
			final int numBytes = Math.min(length, this.sslDecrypted.limit());
			
			try
			{
				outBuffer.put(this.sslDecrypted.array(), 0, numBytes);
			}
			catch(final IndexOutOfBoundsException | BufferOverflowException e)
			{
				throw new ComException("faild to copy to out buffer", e);
			}
			
			final int newLimit = this.sslDecrypted.limit() - numBytes;
			this.sslDecrypted.position(numBytes);
			this.sslDecrypted.compact();
			this.sslDecrypted.limit(newLimit);
			
			//XDebug.printBufferStats(this.sslDecryptedBuffer, "sslDecryptedBuffer after compact");
		}
		
		XDebug.println("--");
		
		return outBuffer;
		
	}


	private void readInternal(final SocketChannel channel, final ByteBuffer buffer)
	{
		final int bytesRead;
		
		try
		{
			bytesRead = channel.read(buffer);
		}
		catch (final IOException e)
		{
			throw new ComException("failed reading from channel", e);
		}
		
		if(bytesRead < 0)
		{
			throw new ComException("reached end of stream unexpected");
		}
	}
}
