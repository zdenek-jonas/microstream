package one.microstream.java.util;

import java.util.Comparator;
import java.util.TreeSet;

import one.microstream.X;
import one.microstream.collections.old.OldCollections;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerTreeSet extends AbstractBinaryHandlerCustomCollection<TreeSet<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_COMPARATOR =                                                      0;
	static final long BINARY_OFFSET_ELEMENTS   = BINARY_OFFSET_COMPARATOR + Binary.objectIdByteLength();



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<TreeSet<?>> typeWorkaround()
	{
		return (Class)TreeSet.class; // no idea how to get ".class" to work otherwise
	}
	
	private static <E> Comparator<? super E> getComparator(final TreeSet<E> instance)
	{
		return XMemory.accessComparator(instance);
	}
	
	@SuppressWarnings("unchecked")
	private static <E> Comparator<? super E> getComparator(
		final Binary                 bytes  ,
		final PersistenceLoadHandler handler
	)
	{
		return (Comparator<? super E>)handler.lookupObject(bytes.get_long(BINARY_OFFSET_COMPARATOR));
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerTreeSet()
	{
		super(
			typeWorkaround(),
			BinaryCollectionHandling.simpleArrayPseudoFields(
				pseudoField(Comparator.class, "comparator")
			)
		);
		
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final TreeSet<?>              instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		final long contentAddress = bytes.storeSizedIterableAsList(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			handler
		);
		
		bytes.store_long(
			contentAddress + BINARY_OFFSET_COMPARATOR,
			handler.apply(getComparator(instance))
		);
	}
	
	@Override
	public final TreeSet<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new TreeSet<>(
			getComparator(bytes, handler)
		);
	}

	@Override
	public final void update(final Binary bytes, final TreeSet<?> instance, final PersistenceLoadHandler handler)
	{
		instance.clear();
		
		/*
		 * Tree collections don't use hashing, but their comparing logic still uses the elements' state,
		 * which might not yet be available when this method is called. Hence the detour to #complete.
		 */
		final Object[] elementsHelper = new Object[getElementCount(bytes)];
		bytes.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, handler, elementsHelper);
		bytes.registerHelper(instance, elementsHelper);
	}
	
	@Override
	public final void complete(final Binary bytes, final TreeSet<?> instance, final PersistenceLoadHandler handler)
	{
		OldCollections.populateSetFromHelperArray(instance, bytes.getHelper(instance));
	}

	@Override
	public final void iterateInstanceReferences(final TreeSet<?> instance, final PersistenceFunction iterator)
	{
		iterator.apply(getComparator(instance));
		Persistence.iterateReferencesIterable(iterator, instance);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_COMPARATOR));
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}