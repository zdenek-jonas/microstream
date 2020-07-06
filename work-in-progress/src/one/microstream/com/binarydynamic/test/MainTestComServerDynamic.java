package one.microstream.com.binarydynamic.test;

import java.nio.ByteOrder;

import one.microstream.com.ComHost;
import one.microstream.com.binarydynamic.ComBinaryDynamic;
import one.microstream.meta.XDebug;

public class MainTestComServerDynamic
{
	public static void main(final String[] args)
	{
		final ComHost<?> host = ComBinaryDynamic.Foundation()
			.setHostByteOrder(ByteOrder.BIG_ENDIAN)
			.setHostChannelAcceptor(hostChannel ->
			{
				hostChannel.send(new ComplexClassNew());
				
				final Object o = hostChannel.receive();
				XDebug.println("HOST RECEIVED: " + o.toString());
			})
			.setInactivityTimeout(6000)
			.createHost()
		;

		// run the host, making it constantly listen for new connections and relaying them to the logic
		host.run();
	}
}
