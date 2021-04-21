package one.microstream.persistence.binary.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.junit.jupiter.api.Test;

public class ObjectCopierTests
{
	@Test
	void copyEquality()
	{
		final Object original = this.createObject();
		
		final Object copy  = ObjectCopier.New().copy(original);
		final Object copy2 = ObjectCopier.New().copy(original);
		
		assertThat(copy).isEqualTo(original);
		assertThat(copy).isEqualTo(copy2);
	}


	private Object createObject()
	{
		final Map<Object, Object> map = new HashMap<>();
		
		final List<LocalDate> list = new ArrayList<>();
		list.add(LocalDate.now());
		list.add(LocalDate.now().plus(1, ChronoUnit.DAYS));
		map.put("a", list);
		
		map.put("b", new Random().nextDouble());
		
		final List<Object> list2 = new ArrayList<>();
		list2.add(list);
		list2.add(new Customer("Jon Doe"));
		map.put("c", list2);
		
		return map;
	}
	
	
	static class Customer implements Serializable
	{
		private final String name;
		
		public Customer(final String name)
		{
			super();
			this.name = name;
		}

		public String name()
		{
			return this.name;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(this.name);
		}

		@Override
		public boolean equals(
			final Object obj
		)
		{
			if(this == obj)
			{
				return true;
			}
			if(obj == null || !(obj instanceof Customer))
			{
				return false;
			}
			final Customer other = (Customer)obj;
			return Objects.equals(this.name, other.name);
		}
		
	}
	
}
