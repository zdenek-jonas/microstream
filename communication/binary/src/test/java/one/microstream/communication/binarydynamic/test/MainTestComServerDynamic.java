package one.microstream.communication.binarydynamic.test;

import java.nio.ByteOrder;

import one.microstream.communication.binarydynamic.ComBinaryDynamic;
import one.microstream.communication.types.ComHost;
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
