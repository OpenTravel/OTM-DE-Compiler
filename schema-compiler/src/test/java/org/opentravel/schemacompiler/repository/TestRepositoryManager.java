package org.opentravel.schemacompiler.repository;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.TemporaryFolder;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryManager;

public class TestRepositoryManager {
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Rule
	public org.junit.rules.ErrorCollector errors = new ErrorCollector();
	private RepositoryManager repo;

	@Before
	public void beforeEachTest() throws RepositoryException {
		repo = new RepositoryManager(folder.getRoot());
	}

	public static List<String> validId() {
		return Arrays.asList(new String[] { "Valid", "Letter", "host", "", ";host", "&ole", "Id%20TEst" });
	}

	@Test
	public void testUpdateLocalRepositoryIdentityWithValidRepoId() throws RepositoryException {
		for (String valid : validId()) {
			try {
				repo.updateLocalRepositoryIdentity(valid, "ValidName");
			} catch (RepositoryException re) {
				errors.addError(re);
			}
		}
	}

	public static List<String> invalidId() {
		return Arrays.asList(new String[] { ":Inalid", "@Letter" });
	}

	@Test
	public void testUpdateLocalRepositoryIdentityWithInvalidRepoId() throws RepositoryException {
		for (String invalid : invalidId()) {
			try {
				repo.updateLocalRepositoryIdentity(invalid, "ValidName");
				errors.addError(new RuntimeException("Id: " + invalid + ", should be invalid!"));
			} catch (RepositoryException re) {
			}
		}
	}

}
