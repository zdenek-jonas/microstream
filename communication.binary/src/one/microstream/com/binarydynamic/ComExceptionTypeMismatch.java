package one.microstream.com.binarydynamic;

import one.microstream.com.ComException;

public class ComExceptionTypeMismatch extends ComException
{

	public ComExceptionTypeMismatch(final long typeId, final Class<?> type)
	{
		super(String.format("local type %s does not match to remote type with type id %d!",
			type.getName(),
			typeId
		));
	}

}
