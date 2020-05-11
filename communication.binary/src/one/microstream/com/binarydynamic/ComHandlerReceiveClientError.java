package one.microstream.com.binarydynamic;

import one.microstream.com.ComChannel;

public class ComHandlerReceiveClientError implements ComHandlerReceive<ComMessageClientError>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final ComChannel channel;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComHandlerReceiveClientError(final ComChannel connection)
	{
		this.channel = connection;
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public Object processMessage(final ComMessageClientError message)
	{
		this.channel.close();
		return message;
	}

	@Override
	public Object processMessage(final Object received)
	{
		final ComMessageClientError message = (ComMessageClientError)received;
		return this.processMessage(message);
	}
}
