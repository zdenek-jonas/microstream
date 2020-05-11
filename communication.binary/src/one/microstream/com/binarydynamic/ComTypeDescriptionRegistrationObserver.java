package one.microstream.com.binarydynamic;

import one.microstream.com.ComChannel;
import one.microstream.meta.XDebug;
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
		XDebug.println("observeTypeDefinitionRegistration: " + typeDefinition.typeId() + " " + typeDefinition.runtimeTypeName());
									
		this.comChannel.send(new ComMessageNewType(typeDefinition));
		
		XDebug.println("store new type def done " + typeDefinition.typeId() + "\n");
	}
}
