package one.microstream.com.tls;

import java.security.SecureRandom;

public interface SecureRandomProvider
{
	public SecureRandom get();
	
	/**
	 *  returns a null secureRandom to use the system default
	 *
	 */
	public final class Default implements SecureRandomProvider
	{
		@Override
		public SecureRandom get()
		{
			//to use system default return null
			return null;
		}
		
	}
}
