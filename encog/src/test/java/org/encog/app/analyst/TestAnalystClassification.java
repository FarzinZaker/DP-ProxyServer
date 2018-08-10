/*
 * Encog(tm) Core v3.4 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core
 
 * Copyright 2008-2016 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package org.encog.app.analyst;

import java.io.File;

import org.encog.util.TempDir;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;
import org.encog.util.file.FileUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestAnalystClassification {
	public final TempDir TEMP_DIR = new TempDir();

	@Test
	public void testClassification() throws Exception {
		File rawFile = TEMP_DIR.createFile("simple.csv");
		File egaFile = TEMP_DIR.createFile("simple.ega");
		File outputFile = TEMP_DIR.createFile("simple_output.csv");

		FileUtil.copyResource("org/encog/data/simple.csv", rawFile);
		FileUtil.copyResource("org/encog/data/simple-c.ega", egaFile);

		EncogAnalyst analyst = new EncogAnalyst();
		analyst.addAnalystListener(new ConsoleAnalystListener());
		analyst.load(egaFile);

		analyst.executeTask("task-full");
		
		ReadCSV csv = new ReadCSV(outputFile.toString(),true,CSVFormat.ENGLISH);
		while(csv.next()) {
			Assert.assertEquals(csv.get(3), csv.get(4));
		}
		
		Assert.assertEquals(4, analyst.getScript().getFields().length );
		Assert.assertEquals(3, analyst.getScript().getFields()[3].getClassMembers().size());
		
		csv.close();
	}

	@After
	public void tearDown() throws Exception {
		TEMP_DIR.dispose();
	}
}