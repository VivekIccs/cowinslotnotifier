package com.corona.cowinslotnotifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.corona.cowinslotnotifier.Notifier.Notifier;
import com.corona.cowinslotnotifier.Properties.ConfigProperties;
import com.corona.cowinslotnotifier.util.ParameterStringBuilder;

public class SlotFinder {
	Set<JSONObject> sessionsWithAvailableSlots = null;
    StringBuilder message;
    int availableDose1 = 0;
    int availableDose2 = 0;

    String findSlotsInTheDistrict() {
        String districtId = ConfigProperties.getProperty(ConfigProperties.DISTRICT_ID);
        String date = getTodayDate();
        System.out.println(date);
        String allSessions = getSlotsFromCowin(districtId, date);
        filterSessionsWithAvailableSlots(allSessions);
        makeMessage();

        date = getTomorrowDate();
        System.out.println(date);
        allSessions = getSlotsFromCowin(districtId, date);
        filterSessionsWithAvailableSlots(allSessions);
        makeMessage();

        return "";
    }

    private void makeMessage() {
    	message = new StringBuilder();
    	int count = sessionsWithAvailableSlots.size();
    	if (count > 0) {
		    sessionsWithAvailableSlots.forEach((JSONObject session) -> {
    		    /*System.out.println(session.get("vaccine") + "          "
            		//+ session.get("date") + "\n"
            		+ "Loc : " + session.get("name") + "   "
            		//+ "Add : " + session.get("address") + "\n"
            		+ "Pin : " + session.get("pincode") + "   "
            		+ "Age : " + session.get("min_age_limit") + "   "
            		+ "Dose1 : " + session.get("available_capacity_dose1") + "           "
            		//+ "Dose2 : " + session.get("available_capacity_dose2") + "\n"
            		);*/
    		    message.append(String.format(
    		    		  "! %1$-10s   Loc : %2$-35s   Pin : %3$-6s   Age : %4$-2s   Dose1 : %5$-4s   Dose2 : %6$-4s   Fee_Type : %7$-4s   Fee : %8$-4s !\n", 
    		    		  session.get("vaccine"), session.get("name"), session.get("pincode"), session.get("min_age_limit"),
    		    		  session.get("available_capacity_dose1"), session.get("available_capacity_dose2"), session.get("fee_type"),
    		    		  session.get("fee")));
            });
		    Notifier.displayNotificationTray("Dose1 available at " + count + " locations");
    	}
		System.out.println(message);
	}

	private void filterSessionsWithAvailableSlots(String allSlots) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(allSlots);
            JSONArray sessions = (JSONArray) jsonObject.get("sessions");
            Set<JSONObject> sessionsWithAvailableSlots = new HashSet<JSONObject>();
            availableDose1 = availableDose2 = 0;
            
            for (int i = 0; i < sessions.size(); i++) {
                JSONObject session = (JSONObject) sessions.get(i);
                if(isDoseOneAvailable(session)) {
                	availableDose1++;
                    sessionsWithAvailableSlots.add(session);
                }
                if(isDoseTwoAvailable(session)) {
                	availableDose2++;
                	sessionsWithAvailableSlots.add(session);
                }
            }
            filterByAgeGroup(sessionsWithAvailableSlots);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void filterByAgeGroup(Set<JSONObject> sessions) {
    	sessionsWithAvailableSlots = new HashSet<JSONObject>();
    	sessions.forEach((JSONObject session) -> {
    		if (filter18(session)) {
    			sessionsWithAvailableSlots.add(session);
    		}
    		if (filter45(session)) {
    			sessionsWithAvailableSlots.add(session);
    		}
        });
    	System.out.println(sessionsWithAvailableSlots.size());
	}

	private boolean filter45(JSONObject session) {
		String filterAge45 = ConfigProperties.getProperty("age_45_filter");
        if (filterAge45.equals("true")) {
            String minAgeLimit = session.get("min_age_limit").toString();
            if (45 == Integer.parseInt(minAgeLimit)) {
                return true;
            }
        }
        return false;
	}

	private boolean filter18(JSONObject session) {
		String filterAge18 = ConfigProperties.getProperty("age_18_filter");
        if (filterAge18.equals("true")) {
            String minAgeLimit = session.get("min_age_limit").toString();
            if (18 == Integer.parseInt(minAgeLimit)) {
                return true;
            }
        }
        return false;
	}

	private boolean isDoseTwoAvailable(JSONObject session) {
        String doseTwoProp = ConfigProperties.getProperty("dose_two_filter");
        if (doseTwoProp.equals("true")) {
            String availableCapacityDose = session.get("available_capacity_dose2").toString();
            if (20 < Integer.parseInt(availableCapacityDose)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDoseOneAvailable(JSONObject session) {
        String doseOneProp = ConfigProperties.getProperty("dose_one_filter");
        if (doseOneProp.equals("true")) {
            String availableCapacityDose = session.get("available_capacity_dose1").toString();
            if (20 < Integer.parseInt(availableCapacityDose)) {
                return true;
            }
        }
        return false;
    }

    private String getSlotsFromCowin(String districtID, String date) {
        StringBuffer response = new StringBuffer();
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("district_id", districtID);
            //parameters.put("district_id", "363");
            parameters.put("date", date);
            String paramString = ParameterStringBuilder.getParamsString(parameters);
            URL url = new URL("https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/findByDistrict" + "?" + paramString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);

            int responseCode = con.getResponseCode();
            String readLine = null;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                while ((readLine = in.readLine()) != null) {
                    response.append(readLine);
                }
                in.close();
            } else {
                System.out.println("REQUEST FAILED WITH ERROR_CODE : " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    private String getTodayDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        return formatter.format(date);
    }

    private String getTomorrowDate() {
    	SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
    	Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = calendar.getTime();
        return formatter.format(tomorrow);
    }
}
