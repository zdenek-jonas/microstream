package one.microstream.com.binarydynamic.test;

import one.microstream.com.ComChannel;
import one.microstream.com.ComClient;
import one.microstream.com.binarydynamic.ComBinaryDynamic;
import one.microstream.meta.XDebug;

public class MainTestComClientDynamic
{
	public static void main(final String[] args)
	{
		
		final ComClient<?> client = ComBinaryDynamic.Client();
		
		// create a channel by connecting the client
		final ComChannel channel = client.connect();
		
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
	}
}
