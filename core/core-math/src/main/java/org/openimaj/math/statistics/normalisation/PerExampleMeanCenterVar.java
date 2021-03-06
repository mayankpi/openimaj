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
package org.openimaj.math.statistics.normalisation;

import org.openimaj.math.util.DoubleArrayStatsUtils;
import org.openimaj.util.array.ArrayUtils;

/**
 * Subtract the mean of each example vector from itself and divide by the
 * standard deviation to normalise the vector such that it has unit variance. A
 * regularisation term can be optionally included in the divisor.
 * <p>
 * Only use if the data is stationary (i.e., the statistics for each data
 * dimension follow the same distribution).
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class PerExampleMeanCenterVar implements Normaliser {
	double eps = 10.0 / 255.0;

	/**
	 * Construct with the given variance regularisation term. Setting to zero
	 * disables the regulariser.
	 *
	 * @param eps
	 *            the variance normalisation regularizer (each dimension is
	 *            divided by sqrt(var + eps).
	 */
	public PerExampleMeanCenterVar(double eps) {
		this.eps = eps;
	}

	@Override
	public double[] normalise(double[] vector) {
		final double mean = DoubleArrayStatsUtils.mean(vector);
		final double var = DoubleArrayStatsUtils.var(vector);
		vector = ArrayUtils.subtract(vector, mean);
		vector = ArrayUtils.divide(vector, Math.sqrt(var + eps));

		return vector;
	}

	@Override
	public double[][] normalise(double[][] data) {
		final double[][] out = new double[data.length][];
		for (int c = 0; c < out.length; c++)
			out[c] = normalise(data[c]);
		return out;
	}
}
