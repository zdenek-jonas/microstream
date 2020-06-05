package one.microstream.com.binarydynamic.test;

import java.nio.ByteOrder;
import java.nio.file.Paths;

import one.microstream.com.ComHost;
import one.microstream.com.binarydynamic.ComBinaryDynamic;
import one.microstream.com.tls.ComTLSConnectionHandler;
import one.microstream.com.tls.TLSKeyManagerProvider;
import one.microstream.com.tls.TLSTrustManagerProvider;
import one.microstream.meta.XDebug;

public class MainTestComServerDynamic
{
	public static void main(final String[] args)
	{
		final ComHost<?> host = ComBinaryDynamic.Foundation()
			.setHostByteOrder(ByteOrder.BIG_ENDIAN)
			.setConnectionHandler(ComTLSConnectionHandler.New(
					new TLSKeyManagerProvider.PKCS12(Paths.get("C:/Users/HaraldGrunwald/DevTSL/host.pks"), new char[] {'m','i','c','r','o','s','t','r','e','a','m'}),
					new TLSTrustManagerProvider.Default()
				))
			.setHostChannelAcceptor(hostChannel ->
			{
				hostChannel.send(new ComplexClassNew());
				
				final Object o = hostChannel.receive();
				XDebug.println("HOST RECEIVED: " + o.toString());
			})
			.createHost()
		;

		// run the host, making it constantly listen for new connections and relaying them to the logic
		host.run();
	}
}
