package one.microstream.com.binarydynamic;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.nio.ByteOrder;
import java.util.function.Consumer;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.com.ComClient;
import one.microstream.com.ComClientChannel;
import one.microstream.com.ComConnection;
import one.microstream.com.ComHost;
import one.microstream.com.ComHostChannel;
import one.microstream.com.ComPersistenceAdaptor;
import one.microstream.com.ComPersistenceAdaptorCreator;
import one.microstream.com.ComProtocol;
import one.microstream.com.binary.ComPersistenceChannelBinary;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryPersistenceFoundation;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceContextDispatcher;
import one.microstream.persistence.types.PersistenceFoundation;
import one.microstream.persistence.types.PersistenceIdStrategy;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.persistence.types.PersistenceTypeDictionaryLoader;
import one.microstream.persistence.types.PersistenceTypeDictionaryManager;
import one.microstream.persistence.types.PersistenceTypeDictionaryStorer;
import one.microstream.persistence.types.PersistenceTypeDictionaryView;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.persistence.types.PersistenceWriteController;
import one.microstream.util.BufferSizeProvider;

public class ComPersistenceAdaptorBinaryDynamic implements ComPersistenceAdaptor<ComConnection>
{
	private final BinaryPersistenceFoundation<?> foundation;
	private final BufferSizeProvider             bufferSizeProvider;
	
	private final PersistenceIdStrategy          hostInitIdStrategy;
	private final XGettingEnum<Class<?>>         entityTypes       ;
	private final ByteOrder                      hostByteOrder     ;
	private final PersistenceIdStrategy          hostIdStrategy    ;
	
	private transient PersistenceTypeDictionary  cachedTypeDictionary;
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected ComPersistenceAdaptorBinaryDynamic(
		final BinaryPersistenceFoundation<?> foundation,
		final BufferSizeProvider             bufferSizeProvider,
		final PersistenceIdStrategy          hostInitIdStrategy,
		final XGettingEnum<Class<?>>         entityTypes,
		final ByteOrder                      hostByteOrder,
		final PersistenceIdStrategy          hostIdStrategy)
	{
		super();
		this.foundation         = foundation;
		this.bufferSizeProvider = bufferSizeProvider;
		
		this.hostInitIdStrategy = hostInitIdStrategy;
		this.entityTypes        = entityTypes       ;
		this.hostByteOrder      = hostByteOrder     ;
		this.hostIdStrategy     = hostIdStrategy    ;
	}
	
	public static ComPersistenceAdaptorBinaryDynamic New(
			final BinaryPersistenceFoundation<?> foundation        ,
			final BufferSizeProvider             bufferSizeProvider,
			final PersistenceIdStrategy          hostInitIdStrategy,
			final XGettingEnum<Class<?>>         entityTypes       ,
			final ByteOrder                      hostByteOrder,
			final PersistenceIdStrategy          hostIdStrategy
		)
		{
			return new ComPersistenceAdaptorBinaryDynamic(
				notNull(foundation)        ,
				notNull(bufferSizeProvider),
				mayNull(hostInitIdStrategy), // null for client persistence. Checked for host persistence beforehand.
				mayNull(entityTypes)       , // null for client persistence. Checked for host persistence beforehand.
				mayNull(hostByteOrder)     , // null for client persistence. Checked for host persistence beforehand.
				mayNull(hostIdStrategy)      // null for client persistence. Checked for host persistence beforehand.
			);
		}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public PersistenceFoundation<?, ?> persistenceFoundation()
	{
		return this.foundation;
	}
	
	
	@Override
	public void iterateEntityTypes(final Consumer<? super Class<?>> iterator)
	{
		this.entityTypes.iterate(iterator);
	}

	@Override
	public PersistenceIdStrategy hostInitializationIdStrategy()
	{
		return this.hostInitIdStrategy;
	}

	@Override
	public PersistenceIdStrategy hostIdStrategy()
	{
		return this.hostIdStrategy;
	}

	@Override
	public ByteOrder hostByteOrder()
	{
		return this.hostByteOrder;
	}
	
	private BufferSizeProvider bufferSizeProvider()
	{
		return this.bufferSizeProvider;
	}
			
	@Override
	public BinaryPersistenceFoundation<?> createInitializationFoundation()
	{
		final BinaryPersistenceFoundation<?> initFoundation = this.foundation.Clone();
		
		initFoundation.setContextDispatcher(
				PersistenceContextDispatcher.LocalObjectRegistration()
			)
			.setSizedArrayLengthController(
				PersistenceSizedArrayLengthController.Fitting()
			)
			.setTypeDictionaryLoader(
				createNoOpDictionaryLoader()
			)
			.setTypeDictionaryStorer(
				createNoOpTypDictionaryStorer()
			);
		
		return initFoundation;
	}

	@Override
	public PersistenceFoundation<?, ?> provideHostPersistenceFoundation(final ComConnection connection)
	{
		if(connection != null)
		{
			return this.hostConnectionFoundation(connection);
		}
		
		return this.hostConnectionFoundation();
	}
	
	private BinaryPersistenceFoundation<?> hostConnectionFoundation()
	{
		final BinaryPersistenceFoundation<?> hostFoundation = this.createInitializationFoundation();
		
		hostFoundation.setTargetByteOrder      (this.hostByteOrder());
		hostFoundation.setObjectIdProvider     (this.hostIdStrategy().createObjectIdProvider());
		hostFoundation.setTypeIdProvider       (this.hostIdStrategy().createTypeIdProvider());
		hostFoundation.setTypeMismatchValidator(Persistence.typeMismatchValidatorFailing());
		
		hostFoundation.setTypeDictionaryManager(
			PersistenceTypeDictionaryManager.Transient(
				hostFoundation.getTypeDictionaryCreator()
			)
		);
			
		return hostFoundation;
	}

	private PersistenceFoundation<?, ?> hostConnectionFoundation(final ComConnection connection)
	{
		final BinaryPersistenceFoundation<?> hostFoundation = this.hostConnectionFoundation();
				
		final PersistenceTypeHandlerManager<Binary> typeHandlerManager = hostFoundation.getTypeHandlerManager();
		typeHandlerManager.initialize();

		this.iterateEntityTypes(c ->
			typeHandlerManager.ensureTypeHandler(c)
		);
		
		final ComPersistenceChannelBinary.Default channel = ComPersistenceChannelBinary.New(
				connection,
				this.bufferSizeProvider(),
				hostFoundation,
				this.comWriteController()
			);
		
		hostFoundation.setPersistenceChannel(channel);
		
		return hostFoundation;
	}
	
	private PersistenceWriteController comWriteController()
	{
		// (06.08.2020 TM)TODO: Com Layer WriteController
		return PersistenceWriteController.Enabled();
	}
	
	@Override
	public BinaryPersistenceFoundation<?> provideClientPersistenceFoundation(final ComConnection connection,
			final ComProtocol protocol)
	{
		final BinaryPersistenceFoundation<?> clientFoundation = this.createInitializationFoundation();
				
		clientFoundation.setTargetByteOrder      (protocol.byteOrder());
		clientFoundation.setObjectIdProvider     (protocol.idStrategy().createObjectIdProvider());
		clientFoundation.setTypeIdProvider       (protocol.idStrategy().createTypeIdProvider());
				
		final PersistenceTypeDictionaryManager typeDictionaryManager = PersistenceTypeDictionaryManager.Transient(
			clientFoundation.getTypeDictionaryCreator());
		
		final PersistenceTypeDictionaryView typeDictionaryView = protocol.typeDictionary();
		typeDictionaryView.allTypeDefinitions().forEach(d -> typeDictionaryManager.registerTypeDefinition(d.value()));
				
		clientFoundation.setTypeDictionaryManager(typeDictionaryManager);
		clientFoundation.setTypeMismatchValidator(Persistence.typeMismatchValidatorFailing());
		
		final PersistenceTypeHandlerManager<Binary> typeHandlerManager = clientFoundation.getTypeHandlerManager();
		typeHandlerManager.initialize();
		
		final ComPersistenceChannelBinary.Default channel = ComPersistenceChannelBinary.New(
				connection,
				this.bufferSizeProvider(),
				clientFoundation,
				this.comWriteController()
			);
		
		clientFoundation.setPersistenceChannel(channel);
		
		return clientFoundation;
	}


	@Override
	public ComHostChannel<ComConnection> createHostChannel(
		final ComConnection connection,
		final ComProtocol protocol,
		final ComHost<ComConnection> parent)
	{
		final PersistenceManager<?> pm = this.provideHostPersistenceManager(connection);
		return new ComHostChannelDynamic<>(pm, connection, protocol, parent);
	}
	
	@Override
	public ComClientChannel<ComConnection> createClientChannel(
		final ComConnection connection,
		final ComProtocol protocol,
		final ComClient<ComConnection> parent)
	{
		final BinaryPersistenceFoundation<?> clientFoundation = this.provideClientPersistenceFoundation(connection, protocol);
		final PersistenceTypeHandlerManager<Binary> thm = clientFoundation.getTypeHandlerManager();
		
		final ComTypeDefinitionBuilder typeDefinitionBuilder = new ComTypeDefinitionBuilder(
				clientFoundation.getTypeDictionaryParser(),
				clientFoundation.getTypeDefinitionCreator(),
				clientFoundation.getTypeDescriptionResolverProvider());
		
		
		final PersistenceManager<?> pm = clientFoundation.createPersistenceManager();
		
		return new ComClientChannelDynamic<>(pm, connection, protocol, parent, thm, typeDefinitionBuilder);
		
	}
	

	public PersistenceTypeDictionary provideTypeDictionaryInternal()
	{
		final PersistenceFoundation<?, ?> initFoundation = this.createInitializationFoundation();
		
		initFoundation.setTargetByteOrder      (this.hostByteOrder());
		initFoundation.setObjectIdProvider     (this.hostIdStrategy().createObjectIdProvider());
		initFoundation.setTypeIdProvider       (this.hostIdStrategy().createTypeIdProvider());
		initFoundation.setTypeMismatchValidator(Persistence.typeMismatchValidatorFailing());
		
		initFoundation.setTypeDictionaryManager(
			PersistenceTypeDictionaryManager.Transient(
				initFoundation.getTypeDictionaryCreator()
			)
		);
			
		final PersistenceIdStrategy idStrategy = this.hostInitializationIdStrategy();
		initFoundation.setObjectIdProvider(idStrategy.createObjectIdProvider());
		initFoundation.setTypeIdProvider(idStrategy.createTypeIdProvider());

		final PersistenceTypeHandlerManager<?> typeHandlerManager = initFoundation.getTypeHandlerManager();
		typeHandlerManager.initialize();
		
		this.iterateEntityTypes(c ->
			typeHandlerManager.ensureTypeHandler(c)
		);
		
		return typeHandlerManager.typeDictionary();
	}
	
	@Override
	public PersistenceTypeDictionaryView provideTypeDictionary()
	{
		if(this.cachedTypeDictionary == null)
		{
			synchronized(this)
			{
				// recheck after synch
				if(this.cachedTypeDictionary == null)
				{
					this.cachedTypeDictionary = this.provideTypeDictionaryInternal();
				}
			}
		}
		
		return this.cachedTypeDictionary.view();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Creator methods //
	////////////////////
	
	public static ComPersistenceAdaptorCreator<ComConnection> Creator()
	{
		return Creator(
			BinaryPersistenceFoundation.New()
		);
	}
	
	public static ComPersistenceAdaptorCreator<ComConnection> Creator(
			final BinaryPersistenceFoundation<?> foundation
		)
	{
		return Creator(
			foundation,
			BufferSizeProvider.New()
		);
	}
		
	public static ComPersistenceAdaptorCreator<ComConnection> Creator(
		final BinaryPersistenceFoundation<?> foundation        ,
		final BufferSizeProvider             bufferSizeProvider
	)
	{
		return new ComPersistenceAdaptorBinaryDynamicCreator(
			notNull(foundation)        ,
			notNull(bufferSizeProvider)
		);
	}

	private static PersistenceTypeDictionaryLoader createNoOpDictionaryLoader()
	{
		return new PersistenceTypeDictionaryLoader()
		{
			@Override
			public String loadTypeDictionary()
			{
				//No OP
				return null;
			}
		};
	}
	
	private static PersistenceTypeDictionaryStorer createNoOpTypDictionaryStorer()
	{
		return new PersistenceTypeDictionaryStorer()
		{
			@Override
			public void storeTypeDictionary(final String typeDictionaryString)
			{
				// NO OP
			}
		};
	}
	
}
