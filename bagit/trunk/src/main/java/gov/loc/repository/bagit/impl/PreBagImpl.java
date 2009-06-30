package gov.loc.repository.bagit.impl;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.PreBag;
import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.transformer.Completer;
import gov.loc.repository.bagit.transformer.impl.DefaultCompleter;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;

public class PreBagImpl implements PreBag {

	private static final Log log = LogFactory.getLog(PreBagImpl.class);
	
	BagFactory bagFactory;
	File dir;
	
	public PreBagImpl(BagFactory bagFactory) {
		this.bagFactory = bagFactory;
	}
	
	@Override
	public File getFile() {
		return this.dir;
	}

	@Override
	public Bag makeBagInPlace(Version version, boolean retainBaseDirectory) {
		return this.makeBagInPlace(version, retainBaseDirectory, new DefaultCompleter(this.bagFactory));

	}

	@Override
	public Bag makeBagInPlace(Version version, boolean retainBaseDirectory, Completer completer) {
		log.info(MessageFormat.format("Making a bag in place at {0}", this.dir));
		File dataDir = new File(this.dir, this.bagFactory.getBagConstants(version).getDataDirectory());
		log.trace("Data directory is " + dataDir);
		try {
			//If there is no data direct
			if (! dataDir.exists()) {
				log.trace("Data directory does not exist");
				//If retainBaseDirectory
				File moveToDir = dataDir;
				if (retainBaseDirectory) {
					//Create new base directory in data directory
					moveToDir = new File(dataDir, this.dir.getName());
					//Move contents of base directory to new base directory
				}
				log.trace("Move to dir is " + moveToDir);
				for(File file : this.dir.listFiles()) {
					if (! file.equals(dataDir)) {
						FileUtils.moveToDirectory(file, moveToDir, true);
					}
				}
				
			} else if (! dataDir.isDirectory()) {
				throw new RuntimeException(MessageFormat.format("{0} is not a directory", dataDir));
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		//Create a bag
		Bag bag = this.bagFactory.createBag(this.dir, version, LoadOption.BY_PAYLOAD_FILES);
		//Complete the bag
		bag = bag.makeComplete(completer);
		//Write the bag
		return bag.write(new FileSystemWriter(this.bagFactory), this.dir);
	}

	@Override
	public void setFile(File dir) {
		if (! dir.exists()) {
			throw new RuntimeException(MessageFormat.format("{0} does not exist", dir));
		}
		if (! dir.isDirectory()) {
			throw new RuntimeException(MessageFormat.format("{0} is not a directory", dir));
		}
		this.dir = dir;
	}

}