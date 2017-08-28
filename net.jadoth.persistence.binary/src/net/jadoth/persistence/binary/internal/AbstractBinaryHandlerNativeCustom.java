package net.jadoth.persistence.binary.internal;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.persistence.types.PersistenceTypeHandlerCustom;


public abstract class AbstractBinaryHandlerNativeCustom<T>
extends AbstractBinaryHandlerNative<T>
implements PersistenceTypeHandlerCustom<Binary, T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerNativeCustom(
		final long                                                         typeId,
		final Class<T>                                                     type  ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> fields
	)
	{
		super(typeId, type, fields);
	}

}
