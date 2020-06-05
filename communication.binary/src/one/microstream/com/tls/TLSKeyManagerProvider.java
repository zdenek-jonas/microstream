package one.microstream.com.tls;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

import one.microstream.com.ComException;

public interface TLSKeyManagerProvider
{
	KeyManager[] get();
	
	/*
	 * use system default key manager
	 */
	public class Default implements TLSKeyManagerProvider
	{
		public Default()
		{
			super();
		}

		@Override
		public KeyManager[] get()
		{
			return null;
		}
	}
	
	public class PKCS12 implements TLSKeyManagerProvider
	{
		private final KeyManagerFactory keyManagerFactory;
		
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
				
				this.keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
				
				try
				{
					this.keyManagerFactory.init(keyStore, password);
				}
				catch (UnrecoverableKeyException | KeyStoreException e)
				{
					throw new ComException("failed to initializeKey ManagerFactory", e);
				}
				
			}
			catch (NoSuchAlgorithmException | CertificateException | IOException e)
			{
				throw new ComException("failed to load keys from file", e);
			}
			finally
			{
				Arrays.fill(password, '0');
			}
		}

		@Override
		public KeyManager[] get()
		{
			return this.keyManagerFactory.getKeyManagers();
		}
		
	}
}
