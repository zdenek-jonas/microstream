package one.microstream.com;

import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;

public interface ComChannelExceptionHandler
{
	public void handleException(Throwable exception, ComChannel channel);
	
	public static void defaultHandleException(final Throwable exception, final ComChannel channel)
	{
		if(exception instanceof PersistenceExceptionTransfer
			|| exception instanceof ComException)
		{
			channel.close();
		}
		else
		{
			throw new ComException(exception);
		}
	}
	
	public static ComChannelExceptionHandler New()
	{
		return new ComChannelExceptionHandler.Default();
	}
	
	public final class Default implements ComChannelExceptionHandler
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void handleException(final Throwable exception, final ComChannel channel)
		{
			ComChannelExceptionHandler.defaultHandleException(exception, channel);
		}
		
	}
}
