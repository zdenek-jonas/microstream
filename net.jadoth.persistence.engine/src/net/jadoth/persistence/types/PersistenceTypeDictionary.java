package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.JadothSort;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.swizzling.types.SwizzleTypeDictionary;
import net.jadoth.util.chars.VarString;

public interface PersistenceTypeDictionary extends SwizzleTypeDictionary
{
	public XGettingTable<Long, PersistenceTypeDescription<?>> allTypes();
	
	public XGettingTable<String, PersistenceTypeDescription<?>> latestTypesByName();
	
	public XGettingTable<Long, PersistenceTypeDescription<?>> latestTypesPerTId();
	
	public XGettingTable<String, PersistenceTypeDescriptionLineage<?>> types();

	public boolean registerType(PersistenceTypeDescription<?> typeDescription);

	public boolean registerTypes(Iterable<? extends PersistenceTypeDescription<?>> typeDescriptions);

	@Override
	public PersistenceTypeDescription<?> lookupTypeByName(String typeName);

	@Override
	public PersistenceTypeDescription<?> lookupTypeById(long typeId);

	public long determineHighestTypeId();

	public void setTypeDescriptionRegistrationCallback(PersistenceTypeDescriptionRegistrationCallback callback);

	public PersistenceTypeDescriptionRegistrationCallback getTypeDescriptionRegistrationCallback();
	
	public <T> PersistenceTypeDescriptionLineage<T> ensureTypeLineage(String typeName);
	
	public <T> PersistenceTypeDescriptionLineage<T> lookupTypeLineage(String typeName);
	
	


	public static PersistenceTypeDictionary New(final PersistenceTypeDescriptionLineageProvider lineageProvider)
	{
		return new PersistenceTypeDictionary.Implementation(
			notNull(lineageProvider)
		);
	}


	public final class Implementation implements PersistenceTypeDictionary
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		// the dictionary must be enhanceable at runtime, hence it must know a type lineage provider
		private final PersistenceTypeDescriptionLineageProvider                 lineageProvider     ;
		private final EqHashTable<Long  , PersistenceTypeDescription<?>>        typesPerTypeId       = EqHashTable.New();
		private final EqHashTable<String, PersistenceTypeDescription<?>>        latestTypesPerName   = EqHashTable.New();
		private final EqHashTable<Long, PersistenceTypeDescription<?>>          latestTypesPerTypeId = EqHashTable.New();
		private final EqHashTable<String, PersistenceTypeDescriptionLineage<?>> typeLineages         = EqHashTable.New();
		private       PersistenceTypeDescriptionRegistrationCallback            registrationCallback;
		


		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(final PersistenceTypeDescriptionLineageProvider lineageProvider)
		{
			super();
			this.lineageProvider = lineageProvider;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public synchronized <T> PersistenceTypeDescriptionLineage<T> ensureTypeLineage(final String typeName)
		{
			PersistenceTypeDescriptionLineage<T> lineage = this.lookupTypeLineage(typeName);
			if(lineage != null)
			{
				return lineage;
			}
			
			lineage = this.lineageProvider.provideTypeDescriptionLineage(typeName);
			this.typeLineages.add(typeName, lineage);
			this.internalSortTypeLineages();

			return lineage;
		}
		
		@Override
		public synchronized <T> PersistenceTypeDescriptionLineage<T> lookupTypeLineage(final String typeName)
		{
			// it is the caller's responsibility to pass the proper typeName representing the specified type T.
			@SuppressWarnings("unchecked")
			final PersistenceTypeDescriptionLineage<T> lineage =
				(PersistenceTypeDescriptionLineage<T>)this.typeLineages.get(typeName)
			;
			return lineage;
		}
		
		final <T> boolean internalRegisterType(final PersistenceTypeDescription<T> typeDescription)
		{
			final PersistenceTypeDescriptionLineage<T> lineage = this.ensureTypeLineage(typeDescription.typeName());
			if(!lineage.register(typeDescription))
			{
				return false;
			}
			
			this.typesPerTypeId.add(typeDescription.typeId(), typeDescription);
			this.latestTypesPerName.put(lineage.typeName(), lineage.latest());
			
			// callback gets set externally, can be null as well, so it must be checked.
			if(this.registrationCallback != null)
			{
				this.registrationCallback.registerTypeDescription(typeDescription);
			}
			
			return true;
		}
		
		private void rebuildLatestTypesPerTypeId()
		{
			// Table must be rebuilt completely to evict the old TypeIds für (the) update type(s).
			final EqHashTable<Long, PersistenceTypeDescription<?>> table = this.latestTypesPerTypeId;
			table.clear();
			for(final PersistenceTypeDescriptionLineage<?> tdl : this.typeLineages.values())
			{
				final PersistenceTypeDescription<?> td = tdl.latest();
				if(!table.add(td.typeId(), td))
				{
					// (05.09.2017 TM)EXCP: proper exception
					throw new RuntimeException("Duplicate TypeId " + td.typeId());
				}
			}
			JadothSort.valueSort(table.keys(), Long::compareTo);
		}

		private void internalSortTypeDescriptions()
		{
			JadothSort.valueSort(this.typesPerTypeId.keys()    , Long::compareTo);
			JadothSort.valueSort(this.latestTypesPerName.keys(), JadothSort::compare);
		}
		
		private void internalSortTypeLineages()
		{
			JadothSort.valueSort(this.typeLineages.keys(), JadothSort::compare);
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public synchronized void setTypeDescriptionRegistrationCallback(
			final PersistenceTypeDescriptionRegistrationCallback callback
		)
		{
			this.registrationCallback = callback;
		}

		@Override
		public final synchronized PersistenceTypeDescriptionRegistrationCallback getTypeDescriptionRegistrationCallback()
		{
			return this.registrationCallback;
		}
		
		@Override
		public final synchronized XGettingTable<String, PersistenceTypeDescriptionLineage<?>> types()
		{
			return this.typeLineages;
		}

		@Override
		public final synchronized XGettingTable<Long, PersistenceTypeDescription<?>> allTypes()
		{
			return this.typesPerTypeId;
		}

		@Override
		public final synchronized XGettingTable<Long, PersistenceTypeDescription<?>> latestTypesPerTId()
		{
			return this.latestTypesPerTypeId;
		}
		
		@Override
		public final synchronized XGettingTable<String, PersistenceTypeDescription<?>> latestTypesByName()
		{
			return this.latestTypesPerName;
		}

		@Override
		public final synchronized boolean registerType(final PersistenceTypeDescription<?> typeDescription)
		{
			if(this.internalRegisterType(typeDescription))
			{
				this.rebuildLatestTypesPerTypeId();
				this.internalSortTypeDescriptions();
				return true;
			}
			return false;
		}

		@Override
		public synchronized boolean registerTypes(
			final Iterable<? extends PersistenceTypeDescription<?>> typeDescriptions
		)
		{
			final long oldSize = this.typesPerTypeId.size();

			for(final PersistenceTypeDescription<?> td : typeDescriptions)
			{
				this.internalRegisterType(td);
			}

			if(this.typesPerTypeId.size() != oldSize)
			{
				this.rebuildLatestTypesPerTypeId();
				this.internalSortTypeDescriptions();
				return true;
			}
			return false;
		}

		@Override
		public synchronized PersistenceTypeDescription<?> lookupTypeByName(final String typeName)
		{
			return this.latestTypesPerName.get(typeName);
		}

		@Override
		public synchronized PersistenceTypeDescription<?> lookupTypeById(final long typeId)
		{
			return this.typesPerTypeId.get(typeId);
		}

		@Override
		public synchronized long determineHighestTypeId()
		{
			long maxTypeId = -1;
			
			for(final PersistenceTypeDescription<?> type : this.typesPerTypeId.values())
			{
				if(type.typeId() >= maxTypeId)
				{
					maxTypeId = type.typeId();
				}
			}

			return maxTypeId;
		}

		@Override
		public synchronized String toString()
		{
			final VarString vc = VarString.New();

			for(final PersistenceTypeDescription<?> type : this.typesPerTypeId.values())
			{
				vc.add(type).lf();
			}

			return vc.toString();
		}
		
	}
	
	public static boolean isVariableLength(final String typeName)
	{
		switch(typeName)
		{
			case Symbols.TYPE_BYTES:
			case Symbols.TYPE_CHARS:
			case Symbols.TYPE_COMPLEX:
			{
					return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public static String fullQualifiedFieldName(
		final String declaringTypeName,
		final String fieldName
	)
	{
		return fullQualifiedFieldName(VarString.New(), declaringTypeName, fieldName).toString();
	}

	public static VarString fullQualifiedFieldName(
		final VarString vc               ,
		final String    declaringTypeName,
		final String    fieldName
	)
	{
		return vc.add(declaringTypeName).add(Symbols.MEMBER_FIELD_DECL_TYPE_SEPERATOR).add(fieldName);
	}

	public static VarString paddedFullQualifiedFieldName(
		final VarString vc                        ,
		final String    declaringTypeName         ,
		final int       maxDeclaringTypeNameLength,
		final String    fieldName                 ,
		final int       maxFieldNameLength
	)
	{
		// redundant code here to avoid unnecessary padding in normal case
		return vc
			.padRight(declaringTypeName, maxDeclaringTypeNameLength, ' ')
			.add(Symbols.MEMBER_FIELD_DECL_TYPE_SEPERATOR)
			.padRight(fieldName        , maxFieldNameLength        , ' ')
		;
	}

	// type is primarily defined by the dictionary string. Parser must guarantee to create the apropriate member types
	public static boolean isInlinedComplexType(final String typeName)
	{
		return Symbols.TYPE_COMPLEX.equals(typeName);
	}

	// type is primarily defined by the dictionary string. Parser must guarantee to create the apropriate member types
	public static boolean isInlinedVariableLengthType(final String typeName)
	{
		return Symbols.TYPE_BYTES.equals(typeName)
			|| Symbols.TYPE_CHARS.equals(typeName)
			|| isInlinedComplexType(typeName)
		;
	}



	public class Symbols
	{
		protected static final transient char   TYPE_START                       = '{';
		protected static final transient char   TYPE_END                         = '}';
		protected static final transient char   MEMBER_FIELD_DECL_TYPE_SEPERATOR = '#';
		protected static final transient char   MEMBER_TERMINATOR                = ','; // cannot be ";" as array names are terminated by it
		protected static final transient char   MEMBER_COMPLEX_DEF_START         = '(';
		protected static final transient char   MEMBER_COMPLEX_DEF_END           = ')';

		protected static final transient String KEYWORD_PRIMITIVE                = "primitive";
		protected static final transient String TYPE_CHARS                       = "[char]"   ;
		protected static final transient String TYPE_BYTES                       = "[byte]"   ;
		protected static final transient String TYPE_COMPLEX                     = "[list]"   ;
		protected static final transient int    LITERAL_LENGTH_TYPE_COMPLEX      = TYPE_COMPLEX.length();

		protected static final transient char[] ARRAY_KEYWORD_PRIMITIVE          = KEYWORD_PRIMITIVE.toCharArray();
		protected static final transient char[] ARRAY_TYPE_CHARS                 = TYPE_CHARS       .toCharArray();
		protected static final transient char[] ARRAY_TYPE_BYTES                 = TYPE_BYTES       .toCharArray();
		protected static final transient char[] ARRAY_TYPE_COMPLEX               = TYPE_COMPLEX     .toCharArray();

		public static final String typeChars()
		{
			return TYPE_CHARS;
		}

		public static final String typeBytes()
		{
			return TYPE_BYTES;
		}

		public static final String typeComplex()
		{
			return TYPE_COMPLEX;
		}



		protected Symbols()
		{
			super();
			// can be extended to access the symbols
		}

	}

	
}
