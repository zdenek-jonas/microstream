package one.microstream.com.tls;

import javax.net.ssl.SSLParameters;

public interface TLSParametersProvider
{

	SSLParameters getSSLParameters();

	/**
	 * 
	 * Provides a nearly empty SSLParameters object.
	 * 
	 * all configuration values are null except
	 *
	 * needClientAuth = true
	 *
	 */
	public final class Default implements TLSParametersProvider
	{
		@Override
		public SSLParameters getSSLParameters()
		{
			final SSLParameters sslParameters = new SSLParameters();
			sslParameters.setNeedClientAuth(true);
						
			return sslParameters;
		}
		
	}
}
