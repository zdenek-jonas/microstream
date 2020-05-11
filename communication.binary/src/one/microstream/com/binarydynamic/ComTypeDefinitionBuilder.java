package one.microstream.com.binarydynamic;

import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionCreator;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberCreator;
import one.microstream.persistence.types.PersistenceTypeDescription;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionResolver;
import one.microstream.persistence.types.PersistenceTypeDescriptionResolverProvider;
import one.microstream.persistence.types.PersistenceTypeDictionaryBuilder;
import one.microstream.persistence.types.PersistenceTypeDictionaryEntry;
import one.microstream.persistence.types.PersistenceTypeDictionaryParser;

public class ComTypeDefinitionBuilder
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceTypeDictionaryParser 				typeDictionaryParser;
	private final PersistenceTypeDefinitionCreator 				typeDefinitionCreator;
	private final PersistenceTypeDescriptionResolverProvider 	typeDescriptionResolverProvider;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComTypeDefinitionBuilder(
		final PersistenceTypeDictionaryParser 				typeDictionaryParser,
		final PersistenceTypeDefinitionCreator 				typeDefinitionCreator,
		final PersistenceTypeDescriptionResolverProvider	typeDescriptionResolverProvider)
	{
		super();
		this.typeDictionaryParser			 = typeDictionaryParser;
		this.typeDefinitionCreator			 = typeDefinitionCreator;
		this.typeDescriptionResolverProvider = typeDescriptionResolverProvider;
			
	}

	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public XGettingSequence<PersistenceTypeDefinition> buildTypeDefinitions(final String typeEntry)
	{
		final PersistenceTypeDescriptionResolver typeResolver = this.typeDescriptionResolverProvider.provideTypeDescriptionResolver();
		
		final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries = this.typeDictionaryParser.parseTypeDictionaryEntries(typeEntry);
		
		final XGettingTable<Long, PersistenceTypeDictionaryEntry> uniqueTypeIdEntries = PersistenceTypeDictionaryBuilder.ensureUniqueTypeIds(entries);
		
		final PersistenceTypeDefinitionMemberCreator memberCreator =
			PersistenceTypeDefinitionMemberCreator.New(uniqueTypeIdEntries.values(), typeResolver)
		;
						
		final BulkList<PersistenceTypeDefinition> typeDefs = BulkList.New(uniqueTypeIdEntries.size());
		for(final PersistenceTypeDescription e : uniqueTypeIdEntries.values())
		{

			final EqHashEnum<PersistenceTypeDefinitionMember> allMembers =
				EqHashEnum.New(PersistenceTypeDescriptionMember.identityHashEqualator())
			;
			final EqHashEnum<PersistenceTypeDefinitionMember> instanceMembers =
				EqHashEnum.New(PersistenceTypeDescriptionMember.identityHashEqualator())
			;
			
			PersistenceTypeDictionaryBuilder.buildDefinitionMembers(memberCreator, e, allMembers, instanceMembers);
			
			final String   runtimeTypeName = typeResolver.resolveRuntimeTypeName(e);
			final Class<?> type            = runtimeTypeName == null
				? null
				: typeResolver.tryResolveType(runtimeTypeName)
			;
			
			final PersistenceTypeDefinition typeDef = this.typeDefinitionCreator.createTypeDefinition(
				e.typeId()     ,
				e.typeName()   ,
				runtimeTypeName,
				type           ,
				allMembers     ,
				instanceMembers
			);
			
			typeDefs.add(typeDef);
		}
		
		return typeDefs;
		
	}


}
