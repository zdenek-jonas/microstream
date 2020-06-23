package one.microstream.com.tls;

import javax.net.ssl.SSLParameters;

public interface TLSParametersProvider
{

	SSLParameters getSSLParameters();
	
	/**
	 * provide the SSL protocol as defined in {@link <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#SSLContext">Standard Algorithm Name Documentation</a>}
	 * 
	 * @return SSL protocol
	 */
	String getSSLProtocol();

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
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private static final String TLS_PROTOCOL_STRING = "TLSv1.2";
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default()
		{
			super();
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public SSLParameters getSSLParameters()
		{
			final SSLParameters sslParameters = new SSLParameters();
			sslParameters.setNeedClientAuth(true);
						
			return sslParameters;
		}
		
		@Override
		public String getSSLProtocol()
		{
			return Default.TLS_PROTOCOL_STRING;
		}
		
	}


}
