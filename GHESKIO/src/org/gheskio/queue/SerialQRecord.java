package org.gheskio.queue;

import java.util.StringTokenizer;
import java.sql.Date;
import java.io.*;

public class SerialQRecord {

	public java.sql.Date receive_time = null;
	public String receive_ip = null;

	public long record_id = 0;
        public String app_name = "";
        public String token_id = "";
        public long event_time = 0;
        public String event_type = "";
        public String station_id = "";
        public String facility_id = "";
        public String worker_id = "";
        public String comments = "";

	public String nextDateString = "";

	// these come in the form:
	/* 			SimpleQRecord.COLUMN_TOKEN_ID + ", " +
                                SimpleQRecord.COLUMN_EVENT_TIME + ", " +
                                SimpleQRecord.COLUMN_COMMENTS + ", " +
                                SimpleQRecord.COLUMN_EVENT_TYPE + ", " +
                                SimpleQRecord.COLUMN_WORKER_ID + ", " +
                                SimpleQRecord.COLUMN_STATION_ID + ", " +
                                SimpleQRecord.COLUMN_FACILITY_ID 
	*/

	/** constructor allows for two forms: if remoteAddr is null,
	 * this is a test record from the maps.geography.uc.edu test site
	 * otherwise, the remoteAddress is from the servlet, and the time is now. 
	 */
	public SerialQRecord(String serializedSQR, String remoteAddr) throws Exception {
		// deal with null fields
		serializedSQR = serializedSQR.replaceAll("\\|", "| ");

		StringTokenizer st = new StringTokenizer(serializedSQR, "|", false);

		// XXX - must be compatible with: org.gheskio.queue.SimpleQRecord.toString()

		if (remoteAddr == null) {
			
			String receiveTimeString = st.nextToken().trim();
			try {
				java.util.Date receive_timeDate = new java.util.Date(receiveTimeString);
				receive_time = new java.sql.Date(receive_timeDate.getTime());
			} catch (Exception e) {
				receive_time = new java.sql.Date(new java.util.Date().getTime());
			}

			receive_ip = st.nextToken().trim();

		} else {
			receive_ip = remoteAddr;
			receive_time = new java.sql.Date(new java.util.Date().getTime());
		}

		app_name = st.nextToken().trim();

		// throw an exception here if mismatch..?
		String versionId = st.nextToken();

		token_id = st.nextToken().trim();
		String eventTimeString = st.nextToken().trim();

		// XXX gotta get this straight for MS SQLserver
		event_time = Long.parseLong(eventTimeString);
		java.util.Date nextDate = new java.util.Date(event_time);

		comments = st.nextToken().trim();
		event_type = st.nextToken().trim();
		worker_id = st.nextToken().trim();
		station_id = st.nextToken().trim();
		facility_id = st.nextToken().trim();
	}

	public static void usage() {
		System.out.println("usage: java org.gheskio.SerialQRecord <file to parse>");
	}

	public static void main(String args[]) {

		try {
			FileReader fr = new FileReader(args[0]);
			BufferedReader br = new BufferedReader(fr);
			String nextLine = br.readLine() ;
			while (nextLine != null) {
				try {
					SerialQRecord sqr = new SerialQRecord(nextLine, null);
				} catch (Exception e) {
					System.out.println("problem: " + nextLine);
				}
				nextLine = br.readLine() ;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
