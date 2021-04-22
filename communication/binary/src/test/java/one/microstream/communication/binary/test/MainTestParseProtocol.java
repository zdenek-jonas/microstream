package one.microstream.communication.binary.test;

import one.microstream.afs.nio.types.NioFileSystem;
import one.microstream.communication.binary.types.ComPersistenceAdaptorBinary;
import one.microstream.communication.types.Com;
import one.microstream.communication.types.ComFoundation;
import one.microstream.communication.types.ComProtocol;
import one.microstream.communication.types.ComProtocolProvider;
import one.microstream.communication.types.ComProtocolStringConverter;
import one.microstream.io.XIO;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.binary.types.BinaryPersistenceFoundation;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
import one.microstream.persistence.types.PersistenceContextDispatcher;


public class MainTestParseProtocol
{
	public static void main(final String[] args)
	{
		final BinaryPersistenceFoundation<?> pf = BinaryPersistence.Foundation()
			.setTypeDictionaryIoHandler(PersistenceTypeDictionaryFileHandler.New(
				NioFileSystem.New().ensureDirectory(
					XIO.unchecked.ensureDirectory(XIO.Path("TypeDictionary"))
				)
			))
//			.setObjectIdProvider(PersistenceObjectIdProvider.Transient())
//			.setTypeIdProvider(PersistenceTypeIdProvider.Transient())
			.setContextDispatcher(
				PersistenceContextDispatcher.LocalObjectRegistration()
			)
		;
				
		final ComFoundation.Default<?> foundation = Com.Foundation()
			.setPersistenceAdaptorCreator(ComPersistenceAdaptorBinary.Creator(pf))
		;
		
		final ComProtocolProvider<?>     protocolProvider = foundation.getProtocolProvider();
		final ComProtocol                protocol         = protocolProvider.provideProtocol(null);
		final ComProtocolStringConverter converter        = foundation.getProtocolStringConverter();
		final String                     assembled        = converter.assemble(protocol);
		System.out.println(assembled);
		
		final ComProtocol parsed     = converter.parse(assembled);
		final String      assembled2 = converter.assemble(parsed);
		System.out.println(assembled2);
	}
	
}
