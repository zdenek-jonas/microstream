package one.microstream.com;

public interface ComChannelExceptionHandler
{
	public void handleException(Throwable exception, ComChannel channel);
	
	public static void defaultHandleException(final Throwable exception, final ComChannel channel)
	{

			channel.close();

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
