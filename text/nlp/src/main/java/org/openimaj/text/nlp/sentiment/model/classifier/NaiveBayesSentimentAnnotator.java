/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.text.nlp.sentiment.model.classifier;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.bayes.NaiveBayesAnnotator;
import org.openimaj.text.nlp.sentiment.type.Sentiment;

/**
 * A {@link NaiveBayesAnnotator} for sentiment analysis.
 * 
 * @author
 * 
 * @param <SENTIMENT>
 *            {@link Sentiment} object that will be the annotation.
 */
public class NaiveBayesSentimentAnnotator<SENTIMENT extends Sentiment>
		extends
		NaiveBayesAnnotator<List<String>, SENTIMENT>
{

	private static GeneralSentimentFeatureExtractor ext;

	public NaiveBayesSentimentAnnotator(NaiveBayesAnnotator.Mode mode) {
		super(ext = new GeneralSentimentFeatureExtractor(), mode);
	}

	@Override
	public void train(
			Iterable<? extends Annotated<List<String>, SENTIMENT>> data)
	{
		final List<List<String>> rawTokens = new ArrayList<List<String>>();
		for (final Annotated<List<String>, SENTIMENT> anno : data) {
			rawTokens.add(anno.getObject());
		}

		ext.initialize(rawTokens);

		super.train(data);
	}
}
