package gov.loc.repository.bagit.reader;

import java.nio.file.Path;
import java.nio.file.Paths;

import gov.loc.repository.bagit.domain.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.MaliciousPathException;

public class TagFileReaderTest {

  @Test
  public void testCreateFileFromManifest() throws Exception{
    Path bagRootDir = Paths.get("foo");
    Path path = TagFileReader.createFileFromManifest(bagRootDir, "data/bar/ham.txt", Version.VERSION_0_97);
    Assertions.assertEquals(bagRootDir.resolve("data/bar/ham.txt"), path);
  }
  
  @Test
  public void testCreateFileFromManifestWithAsterisk() throws Exception{
    Path bagRootDir = Paths.get("foo");
    Path path = TagFileReader.createFileFromManifest(bagRootDir, "*data/bar/ham.txt", Version.VERSION_0_97);
    Assertions.assertEquals(bagRootDir.resolve("data/bar/ham.txt"), path);
  }
  
  @Test
  public void testCreateFileFromManifestWithURISyntax() throws Exception{
    Path bagRootDir = Paths.get("/foo");
    String uri = "file:///foo/data/bar/ham.txt";
    Path path = TagFileReader.createFileFromManifest(bagRootDir, uri, Version.VERSION_0_97);
    Assertions.assertEquals(bagRootDir.resolve("data/bar/ham.txt"), path);
  }
  
  @Test
  public void testBackslashThrowsException() throws Exception{
    Path bagRootDir = Paths.get("foo");
    Assertions.assertThrows(InvalidBagitFileFormatException.class, 
        () -> { TagFileReader.createFileFromManifest(bagRootDir, "data\\bar\\ham.txt", Version.VERSION_0_97); });
  }
  
  @Test
  public void testOutsideDataDirReferenceThrowsException() throws Exception{
    Path bagRootDir = Paths.get("foo");
    Assertions.assertThrows(MaliciousPathException.class, 
        () -> { TagFileReader.createFileFromManifest(bagRootDir, "/bar/ham.txt", Version.VERSION_0_97); });
  }
  
  @Test
  public void testRelativePathOutsideDataDirThrowsException() throws Exception{
    Path bagRootDir = Paths.get("foo");
    Assertions.assertThrows(MaliciousPathException.class, 
        () -> { TagFileReader.createFileFromManifest(bagRootDir, "../bar/ham.txt", Version.VERSION_0_97); });
  }
  
  @Test
  public void testHomeDirReferenceThrowsException() throws Exception{
    Path bagRootDir = Paths.get("foo");
    Assertions.assertThrows(MaliciousPathException.class, 
        () -> { TagFileReader.createFileFromManifest(bagRootDir, "~/bar/ham.txt", Version.VERSION_0_97); });
  }
  
  @Test
  public void testBadURIThrowsException() throws Exception{
    Path bagRootDir = Paths.get("foo");
    Assertions.assertThrows(InvalidBagitFileFormatException.class, 
        () -> { TagFileReader.createFileFromManifest(bagRootDir, "file://C:/foo^", Version.VERSION_0_97); });
  }
}
