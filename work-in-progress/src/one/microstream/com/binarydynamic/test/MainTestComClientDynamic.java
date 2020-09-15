package one.microstream.com.binarydynamic.test;

import java.time.Duration;

import one.microstream.com.ComChannel;
import one.microstream.com.ComClient;
import one.microstream.com.binarydynamic.ComBinaryDynamic;
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
