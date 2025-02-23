package one.microstream.collections.types;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */



import java.util.function.Consumer;

/**
 * 
 *
 */
public interface XProcessingSequence<E> extends XRemovingSequence<E>, XGettingSequence<E>, XProcessingCollection<E>
{
	public interface Factory<E>
	extends XRemovingSequence.Factory<E>, XGettingSequence.Factory<E>, XProcessingCollection.Factory<E>
	{
		@Override
		public XProcessingSequence<E> newInstance();
	}



	public E removeAt(long index); // remove and retrieve element at index or throw IndexOutOfBoundsException if invalid

//	@Override
//	public E fetch(); // remove and retrieve first or throw IndexOutOfBoundsException if empty (fetch ~= first)
	public E pop();   // remove and retrieve last  or throw IndexOutOfBoundsException if empty (stack conceptional pop)

//	@Override
//	public E pinch(); // remove and retrieve first or null if empty (like forcefull extraction from collection's base)
	public E pick();  // remove and retrieve last  or null if empty (like easy extraction from collection's end)


	@Override
	public XProcessingSequence<E> toReversed();

	public <C extends Consumer<? super E>> C moveSelection(C target, long... indices);

	@Override
	public XGettingSequence<E> view(long fromIndex, long toIndex);

}
