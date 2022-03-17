
package one.microstream.integrations.cdi.types.extension;

/*-
 * #%L
 * microstream-integrations-cdi
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;

import one.microstream.integrations.cdi.exceptions.CDIExceptionStorage;
import one.microstream.integrations.cdi.types.Storage;
import one.microstream.reflect.XReflect;
import one.microstream.storage.types.StorageManager;


/**
 * Storage Discovery Bean to CDI extension to register an entity with {@link one.microstream.integrations.cdi.types.Storage}
 * annotation
 */
class StorageBean<T> extends AbstractBean<T>
{
	private final Class<T>        type      ;
	private final Set<Type>       types     ;
	private final Set<Annotation> qualifiers;
	
	protected StorageBean(final BeanManager beanManager, final Class<T> type)
	{
		super(beanManager);
		this.type       = type;
		this.types      = Collections.singleton(type);
		this.qualifiers = new HashSet<>();
		this.qualifiers.add(AnnotationLiteralUtil.DEFAULT_ANNOTATION);
		this.qualifiers.add(AnnotationLiteralUtil.ANY_ANNOTATION);
	}
	
	@Override
	public Class<T> getBeanClass()
	{
		return this.type;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T create(final CreationalContext<T> context)
	{
		final StorageManager manager = this.getInstance(StorageManager.class);
		final Object         root    = manager.root();
		T                    entity;
		if(Objects.isNull(root))
		{
			entity = XReflect.defaultInstantiate(this.type);
			manager.setRoot(entity);
			manager.storeRoot();
		}
		else
		{
			if(this.type.isInstance(root))
			{
				entity = (T)root;
			}
			else
			{
				throw new CDIExceptionStorage(this.type, root.getClass());
			}
		}
		return entity;
	}
	
	@Override
	public Set<Class<? extends Annotation>> getStereotypes()
	{
		return Collections.singleton(Storage.class);
	}
	
	@Override
	public Set<Type> getTypes()
	{
		return this.types;
	}
	
	@Override
	public Set<Annotation> getQualifiers()
	{
		return this.qualifiers;
	}
	
	@Override
	public String getId()
	{
		return this.type.getName() + " @Storage";
	}
	
	@Override
	public String toString()
	{
		return "StorageBean{"
			+
			"type="
			+ this.type
			+
			", types="
			+ this.types
			+
			", qualifiers="
			+ this.qualifiers
			+
			'}';
	}
}