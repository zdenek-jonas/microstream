package one.microstream.communication.binarydynamic.test;

import java.time.Duration;

import one.microstream.communication.binarydynamic.ComBinaryDynamic;
import one.microstream.communication.types.ComChannel;
import one.microstream.communication.types.ComClient;
import one.microstream.meta.XDebug;

public class MainTestComClientDynamic
{
	public static void main(final String[] args)
	{
		
		final ComClient<?> client = ComBinaryDynamic.Foundation()
			.setClientConnectTimeout(10000)
			.createClient();
								
		
		// create a channel by connecting the client
		final ComChannel channel = client.connect(5, Duration.ofSeconds(1));
			
		final Object o = channel.receive();
		
		if(o != null)
		{
			XDebug.println("received:\n" + o.toString());
		}
		else
		{
			XDebug.println("received: null\n");
		}
		
		
		channel.send("exit");
		channel.close();
	}
}
