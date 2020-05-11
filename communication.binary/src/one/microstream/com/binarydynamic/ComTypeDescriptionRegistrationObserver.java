package one.microstream.com.binarydynamic;

import one.microstream.com.ComChannel;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionRegistrationObserver;

public class ComTypeDescriptionRegistrationObserver implements PersistenceTypeDefinitionRegistrationObserver
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
		
	private final ComChannel comChannel;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComTypeDescriptionRegistrationObserver(final ComChannel comChannel)
	{
		super();
		this.comChannel = comChannel;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
				
	@Override
	public void observeTypeDefinitionRegistration(final PersistenceTypeDefinition typeDefinition)
	{
		this.comChannel.send(new ComMessageNewType(typeDefinition));
	}
}
