package one.microstream.com.binarydynamic;

public class ComHandlerSendMessageClientError implements ComHandlerSend<ComMessageClientError>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final ComChannelDynamic<?> comChannel;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComHandlerSendMessageClientError(
		final ComChannelDynamic<?> channel
	)
	{
		super();
		this.comChannel = channel;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
		
	@Override
	public Void sendMessage(final ComMessageClientError message)
	{
		this.comChannel.persistenceManager.store(message);
		return null;
	}
	
	@Override
	public Object sendMessage(final Object messageObject)
	{
		final ComMessageClientError message = (ComMessageClientError)messageObject;
		return this.sendMessage(message);
	}
	
}
