package one.microstream.test.corp.logic;

import one.microstream.X;
import one.microstream.concurrency.XThreads;
import one.microstream.persistence.binary.types.BinaryEntityDataIterator;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandlerArchiving;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.Storage;
import one.microstream.storage.types.StorageBackupSetup;
import one.microstream.storage.types.StorageDataFileValidator;
import one.microstream.storage.types.StorageEntityDataValidator;
import one.microstream.storage.types.StorageFileProvider;


public class MainTestBackupStoring
{
	static final String DIRECTORY_STORAGE   = StorageFileProvider.Defaults.defaultStorageDirectory();
	static final String DIRECTORY_BACKUP    = DIRECTORY_STORAGE + "/backup";
	static final String DIRECTORY_DELETED   = DIRECTORY_BACKUP  + "/deleted";
	static final String DIRECTORY_TRUNCATED = DIRECTORY_BACKUP  + "/truncated";
	
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.Foundation(
			Storage.ConfigurationBuilder()
			.setFileEvaluator(
				// just to make testing more convenient. Not necessary for the backup itself.
				Storage.DataFileEvaluator(100, 1_000, 0.7)
			)
			.setBackupSetup(
				// the only necessary part to activate and configure backupping.
				StorageBackupSetup.New(
					Storage
					.FileProviderBuilder()
					.setBaseDirectory(DIRECTORY_BACKUP)
					.setDeletionDirectory(DIRECTORY_DELETED)
					.setTruncationDirectory(DIRECTORY_TRUNCATED)
					.setFileHandlerCreator(PersistenceTypeDictionaryFileHandlerArchiving::New)
					.createFileProvider()
				)
			)
		)
		.setDataFileValidatorCreator(
			// just to make testing more convenient. Not necessary for the backup itself.
			StorageDataFileValidator.CreatorDebugLogging(
				BinaryEntityDataIterator.Provider(),
				StorageEntityDataValidator.CreatorDebugLogging()
			)
		)
		.start()
	;
	
	static Object[] createArray(final int size)
	{
		return X.Array(Object.class, size, i -> "Element" + i);
	}

	public static void main(final String[] args)
	{
//		printTransactionsFiles();
		final Object[] array = createArray(100);
		STORAGE.root().set(array);
		Test.print("STORAGE: storing ...");
		STORAGE.store(STORAGE.root());
		
		for(int i = 0; i < 1; i++)
		{
			XThreads.sleep(500);
			STORAGE.store(array);
		}
//		STORAGE.issueFullFileCheck();
		XThreads.sleep(1000);
		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}
	

		
}
