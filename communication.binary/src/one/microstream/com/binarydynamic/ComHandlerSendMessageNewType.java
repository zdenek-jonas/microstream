package one.microstream.com.binarydynamic;

public class ComHandlerSendMessageNewType implements ComHandlerSend<ComMessageNewType>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final ComChannelDynamic<?> comChannel;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComHandlerSendMessageNewType(
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
	public Void sendMessage(final ComMessageNewType message)
	{
		final ComMessageStatus answer = (ComMessageStatus)this.comChannel.requestUnhandled(message);
				
		if(answer instanceof ComMessageClientTypeMismatch)
		{
			throw new ComExceptionTypeMismatch(
				((ComMessageClientTypeMismatch) answer).getTypeId(),
				((ComMessageClientTypeMismatch) answer).getType()
			);
		}
				
		return null;
	}
	
	@Override
	public Object sendMessage(final Object messageObject)
	{
		final ComMessageNewType message = (ComMessageNewType)messageObject;
		return this.sendMessage(message);
	}
		

}
