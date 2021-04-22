package one.microstream.communication.binarydynamic.test;

import java.util.ArrayList;
import java.util.List;

public class ComplexClassNew
{
	String name = "Complex new class";
	List<Child> children = new ArrayList<>();
	
	public ComplexClassNew()
	{
		super();
		
		for(int i = 0; i < 11; i++)
		{
			this.children.add(new Child(i));
		}
		
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder(this.name + "\n");
		this.children.forEach( c-> {sb.append("\t"); sb.append(c.toString()); sb.append("\n");});
		return sb.toString();
	}
	
}
