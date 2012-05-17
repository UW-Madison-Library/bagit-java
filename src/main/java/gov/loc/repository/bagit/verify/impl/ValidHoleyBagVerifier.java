package gov.loc.repository.bagit.verify.impl;

import java.io.InputStream;
import java.text.MessageFormat;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.utilities.BagVerifyResult;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import gov.loc.repository.bagit.verify.Verifier;

/**
 * Verifies the structure of a bag, but ignores the payload directory.
 * This verifier can be used prior to fetching a holey bag, and will
 * ensure that the bag is structured correctly.
 */
public class ValidHoleyBagVerifier extends LongRunningOperationBase implements Verifier
{
	private static final Log log = LogFactory.getLog(ValidHoleyBagVerifier.class);
	
	private BagVerifyResult result;
	private Bag bag;
	
	@Override
	public BagVerifyResult verify(Bag bag) 
	{
		this.bag = bag;
		this.result = new BagVerifyResult(true);
		
		log.trace("Checking for bag declaration.");
		if (bag.getBagItTxt() == null)
			this.fail("Bag does not have {0}.", bag.getBagConstants().getBagItTxt());				

		log.trace("Checking for at least one payload manifest.");
		if (bag.getPayloadManifests().isEmpty())
			this.fail("Bag does not have any payload manifests.");
		
		log.trace("Confirming version specified matches version in declaration.");
		if (bag.getBagItTxt() != null && !bag.getBagConstants().getVersion().versionString.equals(bag.getBagItTxt().getVersion()))
			this.fail("Version is not {0}.", bag.getBagConstants().getVersion());				

		log.trace("Checking for fetch.txt.");
		if (bag.getFetchTxt() == null)
			this.fail("Bag does not have {0}.", bag.getBagConstants().getFetchTxt());				
		
		log.info("Completion check: " + result.toString());
		
		return this.result;
	}
	
	protected void checkManifest(Manifest manifest)
	{
		int manifestTotal = manifest.keySet().size();
		int manifestCount = 0;
		
		for(String filepath : manifest.keySet()) 
		{
			if (this.isCancelled()) return;
			
			manifestCount++;
			
			this.progress("verifying files in manifest exist", filepath, manifestCount, manifestTotal);
			
			BagFile bagFile = bag.getBagFile(filepath);
			
			if (bagFile == null || ! bagFile.exists())
			{
				this.fail("File {0} in manifest {1} missing from bag.", filepath, manifest.getFilepath());
			}
			else
			{
				String fixity = manifest.get(filepath);
				InputStream stream = bagFile.newInputStream();
				
				try
				{
                    if (!MessageDigestHelper.fixityMatches(stream, manifest.getAlgorithm(), fixity))
                    {
                        this.fail("Fixity failure in manifest {0}: {1}", manifest.getFilepath(), filepath);
                    }
				}
				finally
				{
					IOUtils.closeQuietly(stream);
				}
			}
		}
	}
	
	private void fail(String format, Object...args)
	{
		this.fail(MessageFormat.format(format, args));
	}
	
	private void fail(String message)
	{
		log.trace(message);
		this.result.setSuccess(false);
		this.result.addMessage(message);
	}
}