package one.microstream.communication.binarydynamic.test;

public class Child
{
	String name = "Child new class";
	int id;
	
	public Child(final int id)
	{
		super();
		this.name = this.name + " " + id;
		this.id = id;
	}
	
	@Override
	public String toString()
	{
		return this.name + " id: " + this.id;
	}
	
	
}