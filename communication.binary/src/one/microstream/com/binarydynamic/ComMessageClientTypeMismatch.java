package one.microstream.com.binarydynamic;

public class ComMessageClientTypeMismatch extends ComMessageStatus
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final long typeId;
	private final Class<?> type;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComMessageClientTypeMismatch(final long typeId, final Class<?> type)
	{
		super(false);
		this.typeId = typeId;
		this.type = type;
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	protected long getTypeId()
	{
		return this.typeId;
	}


	protected Class<?> getType()
	{
		return this.type;
	}
}
