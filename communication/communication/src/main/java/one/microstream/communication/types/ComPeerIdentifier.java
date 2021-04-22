package one.microstream.communication.types;

import java.nio.ByteBuffer;

import one.microstream.chars.XChars;

public interface ComPeerIdentifier
{

	public static ComPeerIdentifier New()
	{
		return new ComPeerIdentifier.Default();
	}

	public ByteBuffer getBuffer();
	
	public class Default implements ComPeerIdentifier
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private final String peerIdentifierString = "Microstream OGC Client";
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default()
		{
			super();
		}

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public ByteBuffer getBuffer()
		{
			return XChars.standardCharset().encode(this.peerIdentifierString);
		}
		
	}
	
}
