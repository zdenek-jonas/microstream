package one.microstream.com.binarydynamic;

import one.microstream.com.ComException;

public class ComExceptionTypeMismatch extends ComException
{
	private final long typeId;
	final Class<?> type;
	
	public ComExceptionTypeMismatch(final long typeId, final Class<?> type)
	{
		super(String.format("local type %s does not match to remote type with type id %d!",
			type.getName(),
			typeId
		));
		
		this.typeId = typeId;
		this.type = type;
	}

	protected long getTypeId()
	{
		return this.typeId;
	}

	protected Class<?> getType()
	{
		return this.type;
	}

}
