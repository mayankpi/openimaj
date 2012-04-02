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
package org.openimaj.hadoop.tools.twitter.token.outputmode.correlation;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.math.MathException;
import org.apache.commons.math.linear.Array2DRowFieldMatrix;
import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.hsqldb.util.CSVWriter;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.utils.TweetCountWordMap;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDFTimeSeries;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.timeseries.interpolation.IntervalSummationProcessor;
import org.openimaj.ml.timeseries.interpolation.LinearTimeSeriesInterpolation;
import org.openimaj.ml.timeseries.interpolation.util.TimeSpanUtils;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.twitter.finance.YahooFinanceData;

import com.Ostermiller.util.CSVPrinter;

public class WordValueCorrelationReducer extends Reducer<Text, BytesWritable, NullWritable, Text>{
	
	private static final long SINGLE_DAY = 60 * 60 * 24 * 1000;
	static YahooFinanceData finance;
	static Map<String, DoubleTimeSeries> financeSeries;
	private static IntervalSummationProcessor<WordDFIDF, WordDFIDFTimeSeries> interp;
	protected static synchronized void loadOptions(Reducer<Text,BytesWritable,NullWritable,Text>.Context context) throws IOException {
		if (finance == null) {
			Path financeLoc = new Path(context.getConfiguration().getStrings(CorrelateWordTimeSeries.FINANCE_DATA)[0]);
			FileSystem fs = HadoopToolsUtil.getFileSystem(financeLoc);
			finance = IOUtils.read(fs.open(financeLoc),YahooFinanceData.class);
			financeSeries = finance.seriesMapInerp(SINGLE_DAY);
			long[] times = financeSeries.get("High").getTimes();
			interp = new IntervalSummationProcessor<WordDFIDF, WordDFIDFTimeSeries>(times);
		}
	}

	private HashMap<Long, TweetCountWordMap> tweetWordMap;

	@Override
	protected void setup(Reducer<Text,BytesWritable,NullWritable,Text>.Context context) throws IOException, InterruptedException {
		loadOptions(context);
	}
	
	
	/**
	 * For each word,
	 */
	@Override
	protected void reduce(Text word, Iterable<BytesWritable> idfvalues, Reducer<Text,BytesWritable,NullWritable,Text>.Context context) throws IOException ,InterruptedException 
	{
		WordDFIDFTimeSeries wts = new WordDFIDFTimeSeries();
		for (BytesWritable bytesWritable : idfvalues) {
			WordDFIDF instance = IOUtils.deserialize(bytesWritable.getBytes(), WordDFIDF.class);
			wts.add(instance.timeperiod, instance);
		}
		interp.process(wts);
		
		double[][] tocorr = new double[2][];
		tocorr[0] = wts.doubleTimeSeries().getData();
		
		for (String ticker : finance.labels()) {
			try{
				if(!financeSeries.containsKey(ticker))continue;
				tocorr[1] = financeSeries.get(ticker).getData();
				BlockRealMatrix m = new BlockRealMatrix(tocorr);
				// Calculate and write pearsons correlation
				PearsonsCorrelation pcorr = new PearsonsCorrelation(m.transpose());
				double corr = pcorr.getCorrelationMatrix().getEntry(0, 1);
				double pval = pcorr.getCorrelationPValues().getEntry(0, 1);
				StringWriter swrit = new StringWriter();
				CSVPrinter csvp = new CSVPrinter(swrit);
				csvp.write(new String[]{word.toString(),ticker,""+corr,""+pval});
				csvp.flush();
				context.write(NullWritable.get(), new Text(swrit.toString()));
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	};
}
