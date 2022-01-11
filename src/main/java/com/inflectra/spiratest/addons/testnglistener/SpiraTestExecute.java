package com.inflectra.spiratest.addons.testnglistener;

import java.net.URL;
import java.util.Date;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.google.gson.*;

/**
 * This defines the 'SpiraTestExecute' class that provides the Java facade
 * for calling the REST web service exposed by SpiraTest
 * 
 * @author		Inflectra Corporation
 * @version		4.0.0
 *
 */
public class SpiraTestExecute {
	private static final String WEB_SERVICE_SUFFIX = "/Services/v3_0/ImportExport.svc?WSDL";
	/**
	 * The URL appended to the base URL to access REST. Note that it ends with a slash
	 */
	private static final String REST_SERVICE_URL = "/services/v5_0/RestService.svc/";

	public String url;
	public String userName;
	public String token;
	public int projectId;

	/**
	 * Performs an HTTP POST request ot the specified URL
	 *
	 * @param input The URL to perform the query on
	 * @param body  The request body to be sent
	 * @return An InputStream containing the JSON returned from the POST request
	 * @throws IOException
	 */
	public static String httpPost(String input, String body) throws IOException {
		URL url = new URL(input);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		//allow sending a request body
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);

		//have the connection send and retrieve JSON
		connection.setRequestProperty("accept", "application/json; charset=utf-8");
		connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		OutputStream os = connection.getOutputStream();
		os.write(body.getBytes());
		os.flush();
		os.close();

		int responseCode = connection.getResponseCode();

		String httpResponse = "";

		//getting the response
		if (100 <= responseCode && responseCode <= 399) {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			httpResponse = response.toString();
		}
		return httpResponse;
	}


	/**
	 * Records a test run
	 *
	 * @param testCaseId        The test case being executed
	 * @param releaseId         The release being executed against (optional)
	 * @param testSetId         The test set being executed against (optional)
	 * @param executionStatusId The status of the test run (pass/fail/not run)
	 * @param runnerName        The name of the automated testing tool
	 * @param runnerTestName    The name of the test as stored in JUnit
	 * @param runnerAssertCount The number of assertions
	 * @param runnerMessage     The failure message (if appropriate)
	 * @param runnerStackTrace  The error stack trace (if any)
	 * @param endDate           When the test run ended
	 * @param startDate         When the test run started
	 * @return ID of the new test run
	 */
	public int recordTestRun(int testCaseId, Integer releaseId, Integer testSetId, Date startDate,
							  Date endDate, int executionStatusId, String runnerName, String runnerTestName, int runnerAssertCount,
							  String runnerMessage, String runnerStackTrace) {
		String url = this.url + REST_SERVICE_URL + "projects/" + this.projectId + "/test-runs/record?username=" + this.userName + "&api-key=" + this.token;

		Gson gson = new Gson();


		//create the body of the request
		String body = "{\"TestRunFormatId\": 1, \"RunnerName\": \"" + runnerName;
		body += "\", \"RunnerTestName\": \"" + cleanApiText(cleanText(runnerTestName)) + "\",";
		body += "\"RunnerStackTrace\": " + cleanText(gson.toJson(runnerStackTrace)) + ",";
		body += "\"StartDate\": \"" + formatDate(startDate) + "\", " + "\"EndDate\": \"" + formatDate(endDate) + "\",";
		body += "\"ExecutionStatusId\": " + executionStatusId + ",\"RunnerAssertCount\": " + runnerAssertCount;
		body += ",\"RunnerMessage\": \"" + cleanApiText(cleanText(runnerMessage)) + "\",";
		body += "\"TestCaseId\": " + testCaseId;

		if(releaseId != null && releaseId != -1) {
			body += ", \"ReleaseId\": " + releaseId;
		}
		if(testSetId != null && testSetId != -1) {
			body += ", \"TestSetId\": " + testSetId;
		}

		body += "}";

		String httpResponse;
		int testRunId = 0;

		//send the request
		try {
			httpResponse = httpPost(url, body);
			JsonObject jsonObject = JsonParser.parseString(httpResponse).getAsJsonObject();
			testRunId = jsonObject.get("TestRunId").getAsInt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return testRunId;
	}

	/**
	 * Turn the date into the format readable by Spira
	 * @param d
	 * @return
	 */
	private static String formatDate(Date d) {
		return "/Date(" + d.getTime() + "-0000)/";
	}

	/**
	 * Send a test run to Spira from the info in the given test run
	 *
	 * @return ID of the new test run
	 */
	public void recordTestRun(TestRun testRun) {
		Date now = new Date();
		recordTestRun(testRun.testCaseId, testRun.releaseId == -1 ? null : testRun.releaseId,
				testRun.testSetId == -1 ? null : testRun.testSetId, now, now, testRun.executionStatusId,
				"JUnit", testRun.testName, 1, testRun.message, testRun.stackTrace);
	}

	/**
	 * Removes any invalid characters of strings being sent through the API
	 *
	 * @param text
	 * @return
	 */
	public String cleanApiText(String text) {
		if (text == null) {
			return null;
		} else {
			String result = text.replaceAll("\\\\+", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("\n", "  ");
			return result;
		}
	}

	/**
	 * Removes any invalid XML contract characters from a string before being used in a SOAP call
	 *
	 * @param text
	 * @return
	 */
	public String cleanText(String text) {
		if (text == null) {
			return null;
		}
		return text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
	}

}