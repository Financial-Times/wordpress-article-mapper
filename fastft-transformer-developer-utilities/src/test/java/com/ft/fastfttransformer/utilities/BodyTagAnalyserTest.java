package com.ft.fastfttransformer.utilities;


import org.junit.Test;

import com.ft.fastfttransformer.utilities.AggregateTagReport;
import com.ft.fastfttransformer.utilities.BodyTagAnalyser;

public class BodyTagAnalyserTest {
	
	private BodyTagAnalyser bodyTagAnalyser = new BodyTagAnalyser();

	@Test
	public void shouldProvideADetailedTagReport() throws Exception {
		AggregateTagReport tagReport = bodyTagAnalyser.analyseBodyTags();
		System.out.println("****" + tagReport);
	}
	
}
