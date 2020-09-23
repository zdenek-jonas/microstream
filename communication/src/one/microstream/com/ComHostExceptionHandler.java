package one.microstream.com;

import one.microstream.meta.XDebug;

public interface ComHostExceptionHandler<C>
{
	public void handleException(Throwable exception, ComChannel channel);
	public void handleConnectException(Throwable exception, C connection);

	
	public static void defaultHandleException(final Throwable exception, final ComChannel channel)
	{
		channel.close();
	}
	
	public static <C> ComHostExceptionHandler<C> New(final ComConnectionHandler<C> connectionHandler)
	{
		return new ComHostExceptionHandler.Default<>(connectionHandler);
	}
	
	public final class Default<C> implements ComHostExceptionHandler<C>
	{
		private final ComConnectionHandler<C> connectionHandler;
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final ComConnectionHandler<C> connectionHandler)
		{
			super();
			this.connectionHandler = connectionHandler;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void handleException(final Throwable exception, final ComChannel channel)
		{
			XDebug.println("Handled channel exception");
			exception.printStackTrace();
			ComHostExceptionHandler.defaultHandleException(exception, channel);
		}


		@Override
		public void handleConnectException(final Throwable exception, final C connection)
		{
			//XDebug.println("Handled connection exception");
			//exception.printStackTrace();
			try
			{
				this.connectionHandler.close(connection);
			}
			catch(final Exception e)
			{
				//Do nothing if closing fails
			}
		}
		
	}
}
