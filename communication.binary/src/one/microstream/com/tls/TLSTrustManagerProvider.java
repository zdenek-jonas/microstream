package one.microstream.com.tls;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import one.microstream.com.ComException;

public interface TLSTrustManagerProvider
{
	TrustManager[] get();
	
	public class Default implements TLSTrustManagerProvider
	{
		@Override
		public TrustManager[] get() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public class PKCS12 implements TLSTrustManagerProvider
	{
		private final TrustManagerFactory trustManagerFactory;
		
		public PKCS12(final Path path, final char[] password)
		{
			final KeyStore keyStore;
			
			try
			{
				keyStore = KeyStore.getInstance("pkcs12");
			}
			catch (final KeyStoreException e)
			{
				throw new ComException("failed to create KeyStore instance", e);
			}
						
			try
			{
				keyStore.load(new FileInputStream(path.toString()), password);
				
				this.trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
				
				try
				{
					this.trustManagerFactory.init(keyStore);
				}
				catch (final KeyStoreException e)
				{
					throw new ComException("failed to initializeKey ManagerFactory", e);
				}
				
			}
			catch (NoSuchAlgorithmException | CertificateException | IOException e)
			{
				throw new ComException("failed to load keys from file", e);
			}
		}

		@Override
		public TrustManager[] get()
		{
			return this.trustManagerFactory.getTrustManagers();
		}
	}
}
