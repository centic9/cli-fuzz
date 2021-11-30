package org.dstadler.cli.fuzz;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;

class FuzzTest {
	@Test
	public void test() {
		FuzzedDataProvider provider = mock(FuzzedDataProvider.class);

		Fuzz.fuzzerTestOneInput(provider);

		when(provider.consumeRemainingAsString()).thenReturn("abc");

		Fuzz.fuzzerTestOneInput(provider);
	}

	@Test
	public void testLog() {
		// should not be logged
		Logger LOG = LogManager.getLogger(FuzzTest.class);
		LOG.atError().log("Test log output which should not be visible -----------------------");
	}

	@Disabled("Local test for verifying a slow run")
	@Test
	public void testSlowUnit() {
		//Fuzz.fuzzerTestOneInput(FileUtils.readFileToByteArray(new File("slow-unit-0a0b0ce97bb332cd9f8fde03e03840768a81d29d")));
	}
}