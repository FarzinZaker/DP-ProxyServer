package ceraslabs.hogna.workload;

import java.net.*;

import java.io.*;
import java.util.*;

import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class Clients extends Thread
{
    boolean debug = false;

    double meantime = 0.0; // mean time of the login response times

    double totaltime = 0.0; // the total value used for mean time calculation

    double variance = 0.0; // variance of the login response times

    int runs = 10; // number of runs or sample numbers of the response
    // times

    double thinkTimeMean = 1000.0; // mean value of the think time (exponential
    // distribution)

    double[] time; // the response times of the individual runs
    String url = "http://localhost:8080";
    
	String[] urlParams = {}; // the parameters for the url

    Random thinkRand;

    int clusterSize = 1;
    int succ = runs;
    int okCode = 0;
    int errorCode = 0;

    // Becomes true when there is a request to stop the execution of this thread.
    // When a stop is requested, no more runs for this client.
    boolean m_bStopRequested = false;

    /**
     * @param runs
     * @param thinkTimeMean
     * @param url
     */
    public Clients(int runs, double thinkTimeMean, String url, String[] urlParams) {
        super();
        this.runs = runs;
        this.thinkTimeMean = thinkTimeMean;
        this.url = url;
        succ = runs;
        thinkRand = new Random();
        time = new double[runs];
        this.urlParams = urlParams; 
    }

    public void setClusterSize(int size) {
        clusterSize = size;

    }

    public double getMeanTime() {
        return meantime;
    }

    public double getSuccessRate() {
        return succ / runs;
    }

    public double getOKRate() {
        return okCode / runs;
    } //

    public double getVariance() {
        return variance;
    }

    public Clients() {

        super();
        // runs = noOfRuns;
        // thinkTimeMean = thinkTimeInMs;
        thinkRand = new Random();

        time = new double[runs];
    }

	public double getThinkTime(double mean)
	{
		double thinkTime = 0.0;

		if (mean > 0.0)
		{
			//thinkTime = 0 - mean * Math.log(1 - thinkRand.nextDouble());
			thinkTime = 2.0 / 3.0 * mean * (1 + thinkRand.nextDouble());
		}
		else if (mean < 0.0)
		{
			System.out.println("mean is less than 0.0");
			return -1.0;
        }
		return thinkTime;
	}

	/*
	 * Request that this thread should stop. The thread will not stop immediately,
	 * but no more HTTP Requests will be made.
	 */
	public void RequestStop()
	{
		this.m_bStopRequested = true;
	}
	
	public int GetCountRequestsMade()
	{
		return this.okCode + this.errorCode;
	}
	
	public int GetCountRequestsSuccessful()
	{
		return this.okCode;
	}
	
	public int GetCountRequestsFailed()
	{
		return this.errorCode;
	}
	
	/*
	 * Generates at random some string values for the parameters of the web request.
	 * <sWebAddress>?<sHttpRequestParams>=<RandomString>
	 */
	private URL CreateUrl(String sWebAddress, String[] sHttpRequestParams)
	{
		URL url = null;

		RandomStringGenerator randomString = new RandomStringGenerator(10);
		try
		{
			String sUrlBuilder = sWebAddress;
			if (sHttpRequestParams.length > 0)
			{
				char connector = '?';
				for (String sHttpRequestParam : sHttpRequestParams)
				{
					sUrlBuilder += connector + sHttpRequestParam + "=" + randomString.nextString();
					connector = '&';
				}
			}	
			url = new URL(sUrlBuilder);
		}
		catch (Exception ex)
		{
			System.out.println("URL is unavailable");
			ex.printStackTrace();
		}
		
		return url;
	}

	public void run()
	{
		URL ur = null;
		HttpURLConnection hc = null;
		String cookie = null;
		totaltime = 0.0;

		// the random string generator

		for (int i = 0; i < runs; i++)
		{
			// check for a stop request
			if (this.m_bStopRequested == true)
			{
		        Trace.WriteLine(TraceLevel.INFO, "Acknowledge the stop request. Cleaning and then terminate.");
				break;
			}
			
			// access the sign on page
			cookie = null;
			ur = this.CreateUrl(url, urlParams);

			try
			{
				double t = getThinkTime(thinkTimeMean);
				//double t = thinkTimeMean;

				if (t < 0.0)
				{
					System.out.println("Error: Think time is < 0");
					System.exit(1);
				}
				Thread.sleep((long) t); // add think time here

				// start the timer
				long t1 = System.currentTimeMillis();

				//sun.net.www.http.HttpClient client;
				//HTTPConnection con;

				hc = (HttpURLConnection) ur.openConnection();

				hc.setRequestMethod("GET");
				InputStream is = hc.getInputStream();
				int ret = 0;
				byte[] buf = new byte[1024];
				while ((ret = is.read(buf)) > 0)
				{
					//System.out.println(buf);
				}
				is.close();

				//BufferedReader in = new BufferedReader(new InputStreamReader(hc.getInputStream()));
				//in.close();
				if (hc.getResponseCode() == 200)
					okCode++;
				else
					errorCode++;

				// record the time
				time[i] = System.currentTimeMillis() - t1;
				totaltime = totaltime + time[i];
			}
			catch (Exception ex)
			{
				try
				{
					int respCode = hc.getResponseCode();
					BufferedReader in = new BufferedReader(new InputStreamReader(hc.getErrorStream()));
					String response = null;
					// read the response body
					while ((response = in.readLine()) != null)
					{
						System.out.println(response);
					}
					// close the errorstream
					in.close();
				}
				catch(IOException ex1)
				{
					// deal with the exception
					ex1.printStackTrace();
					succ--;
				}

				// System.out.println("login error");
				ex.printStackTrace();
				succ--;
			}

			hc.disconnect();
        }
		if (debug)
		{
			System.out.println("loop done");
		}
		// compute the statistics across all invocations
		// but only if at least one invocation was successful
		if (succ > 0)
		{
			meantime = totaltime / succ;
			variance = 0.0;
			for (int j = 0; j < succ; j++)
			{
				// it is assumed that if the
				// response
				// time is 0, something went wrong!
				if (time[j] > 0.0)
				{
					variance += (time[j] - meantime) * (time[j] - meantime);
				}
			}
			variance =Math.sqrt(variance / succ);
		}
		if (debug)
		{
            System.out.println("meantime: " + meantime + " variance: " + variance);
		}
	}
}
