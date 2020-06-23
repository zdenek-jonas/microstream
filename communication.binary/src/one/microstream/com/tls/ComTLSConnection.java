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
import javax.net.ssl.SSLSession;

import one.microstream.com.ComConnection;
import one.microstream.com.ComException;
import one.microstream.com.XSockets;

public class ComTLSConnection implements ComConnection
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
		
	private final SocketChannel channel;
	private final SSLEngine		sslEngine;

	private final ByteBuffer    sslEncyptedOut;
	private final ByteBuffer    sslEncryptedIn;
	private final ByteBuffer    sslDecrypted;
	
	/**
	 * Timeout for blocking read operations during TLS handshake
	 */
	private final int sslHandshakeReadTimeOut;
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComTLSConnection(final SocketChannel channel,
		final SSLContext sslContext,
		final TLSParametersProvider tlsParameterProvider,
		final boolean clientMode)
	{
		this.sslHandshakeReadTimeOut = tlsParameterProvider.getHandshakeReadTimeOut();
		
		this.channel   = channel;
		this.sslEngine = sslContext.createSSLEngine();
		this.sslEngine.setUseClientMode(clientMode);
		this.sslEngine.setSSLParameters(tlsParameterProvider.getSSLParameters());
								
		this.sslEncryptedIn   = ByteBuffer.allocate(this.sslEngine.getSession().getPacketBufferSize());
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
	
	@Override
	public void readCompletely(final ByteBuffer outBuffer)
	{
		this.read(outBuffer, 1000, outBuffer.capacity());
	}
	
	@Override
	public ByteBuffer read(final ByteBuffer defaultBuffer, final int timeout, final int length)
	{
		if(!this.channel.isOpen())
		{
			throw new ComException("Can not read from closed channel!");
		}
		
		final ByteBuffer outBuffer = this.ensureOutBufferSize(defaultBuffer, length);
		
		while(outBuffer.position() < length)
		{
			if(this.sslDecrypted.position() == 0)
			{
				this.decryptPackage();
			}
			else
			{
				this.appendDecrypedData(outBuffer, length);
			}
		}
			
		return outBuffer;
	}
	
	@Override
	public void close()
	{
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
						
			if(result.getStatus() == Status.OK)
			{
				XSockets.writeCompletely(this.channel, this.sslEncyptedOut);
			}
			
			this.sslEncyptedOut.compact();
		}
		
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
		if(!this.channel.isOpen())
		{
			throw new ComException("Can not write to closed channel!");
		}
				
		while(buffer.remaining() > 0)
		{
			final SSLEngineResult result;
			
			try
			{
				result = this.sslEngine.wrap(buffer, this.sslEncyptedOut);
				this.sslEncyptedOut.flip();
			}
			catch (final SSLException e)
			{
				throw new ComException("failed to encypt buffer", e);
			}
			
			switch(result.getStatus())
			{
				case BUFFER_OVERFLOW:
					throw new ComException("Unexpected sslEngine wrap result: " + result.getStatus());
				case BUFFER_UNDERFLOW:
					throw new ComException("Unexpected sslEngine wrap result: " + result.getStatus());
				case CLOSED:
					throw new ComException("Unexpected sslEngine wrap result: " + result.getStatus());
				case OK:
					XSockets.writeCompletely(this.channel, this.sslEncyptedOut);
					break;
				default:
					throw new ComException("Unexpected sslEngine wrap result: " + result.getStatus());
			}
					
			this.sslEncyptedOut.clear();
		}
	}
	
	private synchronized void readHandshakeData(final ByteBuffer buffer)
	{
		final ExecutorService executor = Executors.newSingleThreadExecutor();
				
		int readResult = 0;
		
		try
		{
			readResult = executor
				.submit(() -> { return this.channel.read(buffer); })
				.get(this.sslHandshakeReadTimeOut, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException | ExecutionException e)
		{
			throw new ComException("reading data during hanshake failed", e);
		}
		catch(final TimeoutException e)
		{
			throw new ComException("read timeout during hanshake", e);
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
	
	private HandshakeStatus unwrapHandshakeData() throws IOException
	{
		SSLEngineResult.HandshakeStatus hs = this.sslEngine.getHandshakeStatus();
			 		
		if(this.sslEncryptedIn.position() == 0)
		{
			this.readHandshakeData(this.sslEncryptedIn);
		}
		
		this.sslEncryptedIn.flip();
				
		while(hs == HandshakeStatus.NEED_UNWRAP &&
			this.sslEncryptedIn.hasRemaining())
 		{
	 		final SSLEngineResult engineResult = this.sslEngine.unwrap(this.sslEncryptedIn, this.sslDecrypted);
	 		hs = engineResult.getHandshakeStatus();
	 			 			 			 			 		 			 		
	 		final Status status = engineResult.getStatus();
	 		
	 		if(status != Status.OK)
	 		{
	 			if(status == Status.CLOSED || status == Status.BUFFER_OVERFLOW)
	 			{
	 				throw new ComException("TLS Handshake failed with engine status " + status);
	 			}
	 			
	 			if(status == Status.BUFFER_UNDERFLOW)
	 			{
	 				this.readHandshakeData(this.sslEncryptedIn);
	 			}
	 		}
 		}
		
		this.sslEncryptedIn.compact();
 		
		return hs;
	}
	
	private HandshakeStatus wrapHandshakeData(final ByteBuffer handshakeData) throws IOException
	{
		this.sslEncyptedOut.clear();
		final SSLEngineResult engineResult = this.sslEngine.wrap(handshakeData, this.sslEncyptedOut);
		final SSLEngineResult.HandshakeStatus hs = engineResult.getHandshakeStatus();
											
		if(engineResult.getStatus() == SSLEngineResult.Status.OK )
		{
			this.sslEncyptedOut.flip();
			this.channel.write(this.sslEncyptedOut);
			this.sslEncyptedOut.compact();
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
			switch (hs)
	    	{
	        	case NEED_UNWRAP:
	        		hs = this.unwrapHandshakeData();
	        		break;
	        		
	        	case NEED_WRAP :
	        		hs = this.wrapHandshakeData(handshakeData);
	        		break;
	        		
	        	case NEED_TASK :
	        		hs = this.executeHandshakeTask();
	        		break;
                	        		
				default:
					//should never happen but if so throw an exception to avoid unknown behavior during the SSL handshake
					throw new ComException("Unexpected handshake status: " + hs );
	    	}
	    }
	}
		
	/**
	 * read network data and decrypt until one block is done
	 */
	private void decryptPackage()
	{
		boolean needMoreData = true;
		if(this.sslEncryptedIn.position() > 0)
		{
			this.sslEncryptedIn.flip();
			final SSLEngineResult result = this.unwrapData();
						
			if(result.getStatus() == Status.OK)
			{
				needMoreData = false;
				this.sslEncryptedIn.compact();
			}
			
			if(result.getStatus() == Status.BUFFER_UNDERFLOW)
			{
				this.sslEncryptedIn.position(this.sslEncryptedIn.limit());
				this.sslEncryptedIn.limit(this.sslEncryptedIn.capacity());
			}
			
			if(result.getStatus() == Status.CLOSED)
			{
				this.close();
			}
		}
							
		if(needMoreData)
		{
			this.readInternal();
		}
	}


	/**
	 * Append already decrypted data to the supplied buffer
	 * 
	 * @param outBuffer
	 * @param length
	 */
	private void appendDecrypedData(final ByteBuffer outBuffer, final int length)
	{
		this.sslDecrypted.flip();
		final int numBytes = Math.min(length, this.sslDecrypted.limit());
		
		try
		{
			outBuffer.put(this.sslDecrypted.array(), 0, numBytes);
		}
		catch(final IndexOutOfBoundsException | BufferOverflowException e)
		{
			throw new ComException("faild to copy to out buffer", e);
		}
		
		this.sslDecrypted.position(numBytes);
		this.sslDecrypted.compact();
		
	}

	/**
	 * 
	 * Read from the channel into buffer
	 * throws a ComException if the channel reached the end of stream
	 * 
	 * @param channel
	 * @param buffer
	 */
	private void readInternal()
	{
		final int bytesRead;
		
		try
		{
			bytesRead = this.channel.read(this.sslEncryptedIn);
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
	
	/**
	 * If the supplied buffer is to small to hold the required input size
	 * an new appropriate buffer is created;
	 * @param length
	 * @param defaultBuffer
	 * 
	 * @return ByteBuffer
	 */
	private ByteBuffer ensureOutBufferSize(final ByteBuffer defaultBuffer, final int length)
	{
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
		
		return outBuffer;
	}
	
	/**
	 * Unwrap data from the encrypted input buffer into the decrypted data buffer
	 * 
	 * @return SSLEngineResult
	 */
	private SSLEngineResult unwrapData()
	{
		try
		{
			return this.sslEngine.unwrap(this.sslEncryptedIn, this.sslDecrypted);
		}
		catch (final SSLException e)
		{
			throw new ComException("failed to decypt buffer", e);
		}
	}
}
