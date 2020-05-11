package one.microstream.com.binarydynamic;

import one.microstream.chars.VarString;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDictionaryAssembler;

public class NewPersistenceTypeDefinition
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private String typeEntry;
	private boolean createStatus;
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public NewPersistenceTypeDefinition(final PersistenceTypeDefinition typeDefinition)
	{
		this.typeEntry = "";
		final PersistenceTypeDictionaryAssembler assembler = PersistenceTypeDictionaryAssembler.New();
		
		final VarString vc = VarString.New();
		assembler.assembleTypeDescription(vc, typeDefinition);
		this.typeEntry = vc.toString();
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public NewPersistenceTypeDefinition(final boolean b)
	{
		this.typeEntry = null;
		this.createStatus = b;
	}

	public String typeEntry()
	{
		return this.typeEntry;
	}

	public boolean createStatus()
	{
		return this.createStatus;
	}
}
