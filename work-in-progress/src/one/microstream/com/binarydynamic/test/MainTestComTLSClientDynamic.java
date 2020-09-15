package one.microstream.com.binarydynamic.test;

import java.nio.file.Paths;
import java.time.Duration;

import one.microstream.com.ComChannel;
import one.microstream.com.ComClient;
import one.microstream.com.binarydynamic.ComBinaryDynamic;
import one.microstream.com.tls.ComTLSConnectionHandler;
import one.microstream.com.tls.SecureRandomProvider;
import one.microstream.com.tls.TLSKeyManagerProvider;
import one.microstream.com.tls.TLSParametersProvider;
import one.microstream.com.tls.TLSTrustManagerProvider;
import one.microstream.meta.XDebug;

public class MainTestComTLSClientDynamic
{
	public static void main(final String[] args)
	{
		
		final ComClient<?> client = ComBinaryDynamic.Foundation()
			.setConnectionHandler(ComTLSConnectionHandler.New(
				new TLSKeyManagerProvider.PKCS12(
					Paths.get("C:/Users/HaraldGrunwald/DevTSL/v2/client_key_store.pks"),
					new char[] {'m','i','c','r','o','s','t','r','e','a','m'}),
				new TLSTrustManagerProvider.PKCS12(
					Paths.get("C:/Users/HaraldGrunwald/DevTSL/v2/client_trust_store.pks"),
					new char[] {'m','i','c','r','o','s','t','r','e','a','m'}),
				new TLSParametersProvider.Default(),
				new SecureRandomProvider.Default()
			))
			.setClientConnectTimeout(10000)
			.createClient();
								
		
		// create a channel by connecting the client
		final ComChannel channel = client.connect(5, Duration.ofMillis(1000));
		
		final Object o = channel.receive();
		
		if(o != null)
		{
			XDebug.println("received:\n" + o.toString());
		}
		else
		{
			XDebug.println("received: null\n");
		}
		
		final Object o2 = channel.receive();
		
		if(o2 != null)
		{
			XDebug.println("received:\n" + o2.toString());
		}
		else
		{
			XDebug.println("received: null\n");
		}
		
		
		channel.send("exit");
		channel.close();
	}
}
