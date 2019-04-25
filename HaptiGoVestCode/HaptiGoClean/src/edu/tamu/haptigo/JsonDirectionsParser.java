package edu.tamu.haptigo;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

public class JsonDirectionsParser
{
	@JsonIgnoreProperties(ignoreUnknown=true)
	public enum Status {OK, NOT_FOUND, ZERO_RESULTS, MAX_WAYPOINTS_EXCEEDED, INVALID_REQUEST, OVER_QUERY_LIMIT, REQUEST_DENIED, UNKNOWN_ERROR };
  //============================================REQUIRED NESTED CLASSES==============================================================================
	public static class GoogleLocation
	{
		private Double _latitude;
		private Double _longitude;

		public Double getlat() {return _latitude;}
		public Double getlng() {return _longitude;}

		public void setlat(Double input_lat) {_latitude = input_lat;}
		public void setlng(Double input_lng) {_longitude = input_lng;}
	}

	public static class Distance
	{
		private String _text;
    	private Double _value; //value represented in meters

    	public String getText() {return _text;}
    	public Double getValue() {return _value;}

    	public void setText(String input_text) {_text = input_text;}
    	public void setValue(Double input_value) {_value = input_value;}
    }

    public static class Duration
    {
    	private String _text;
    	private Double _value; //value represented in seconds

    	public String getText() {return _text;}
    	public Double getValue() {return _value;}

    	public void setText(String input_text) {_text = input_text;}
    	public void setValue(Double input_value) {_value = input_value;}
    }

    public static class Overview_Polyline
    {
    	private String _points;

		public String getPoints() {return _points;}
		public void setPoints(String input_points) {_points = input_points;}
	}

    public static class Legs
    {
   		private Distance _distance;
   		private Duration _duration;
   		private String start_address;
   		private String end_address;
   		private GoogleLocation start_location;
   		private GoogleLocation end_location;
   		private List<Steps> _steps = new ArrayList<Steps>();
   		private List<Integer> _waypoints = new ArrayList<Integer>();
   		
   		public Distance getdistance() {return _distance;}
   		public Duration getduration() {return _duration;}
   		public String getstart_address() {return start_address;}
   		public String getend_address() {return end_address;}
   		public GoogleLocation getstart_location() {return start_location;}
   		public GoogleLocation getend_location() {return end_location;}
   		public List<Steps> getsteps() {return _steps;}
   		public List<Integer> getvia_waypoint() {return _waypoints;}
   		   		
   		public void setdistance(Distance input_distance) {_distance = input_distance;}
   		public void setduration(Duration input_duration) {_duration = input_duration;}
   		public void setstart_address(String input_start) {start_address = input_start;}
   		public void setend_address(String input_end) {end_address = input_end;}
   		public void setstart_location(GoogleLocation input_start) {start_location = input_start;}
   		public void setend_location(GoogleLocation input_end) {end_location = input_end;}
   		public void setsteps(List<Steps> input_steps) {_steps = input_steps;}
   		public void setvia_waypoint(List<Integer> input_waypoints) {_waypoints = input_waypoints;}
    }

   	public static class Steps
   	{
   		private String html_instructions;
   		private Distance _distance;
   		private Duration _duration;
   		private GoogleLocation start_location;
   		private GoogleLocation end_location;
   		private String _travelMode;
   		private Overview_Polyline _polyline;

   		public String gethtml_instructions() {return html_instructions;}
   		public Distance getdistance() {return _distance;}
   		public Duration getduration() {return _duration;}
   		public GoogleLocation getstart_location() {return start_location;}
   		public GoogleLocation getend_location() {return end_location;}
   		public String gettravel_mode() {return _travelMode;}
   		public Overview_Polyline getpolyline() {return _polyline;}

   		public void sethtml_instructions(String input_html) {html_instructions = input_html;}
   		public void setdistance(Distance input_distance) {_distance = input_distance;}
   		public void setduration(Duration input_duration) {_duration = input_duration;}
   		public void setstart_location(GoogleLocation input_location) {start_location = input_location;}
   		public void setend_location(GoogleLocation input_location) {end_location = input_location;}
   		public void settravel_mode(String input_mode) {_travelMode = input_mode;}
   		public void setpolyline(Overview_Polyline input_polyline) {_polyline = input_polyline;}
   	}
  //==============================================================================================================================================
    public static class Route
    {
    	@SuppressWarnings("unused")
    	private static class Bounds
    	{
    		private GoogleLocation _northeast;
    		private GoogleLocation _southwest;

    		
			public GoogleLocation getnortheast() {return _northeast;}
    		public GoogleLocation getsouthwest() {return _southwest;}

    		public void setnortheast(GoogleLocation input_bound) { _northeast = input_bound;}
    		public void setsouthwest(GoogleLocation input_bound) { _southwest = input_bound;}
    	}

    	private String _summary, _copyrights;
    	private List<Integer> waypoint_order = new ArrayList<Integer>();
    	private Overview_Polyline _polyline;
    	private List<String> _warnings = new ArrayList<String>();;
    	private Bounds _bounds;
    	private List<Legs> _legs = new ArrayList<Legs>();
    	
    	public String getsummary() { return _summary; }
    	public String getcopyrights() { return _copyrights; }
    	public List<Integer> getwaypoint_order() {return waypoint_order; }
    	public Overview_Polyline getoverview_polyline() {return _polyline;}
    	public List<String> getwarnings() {return _warnings; }
    	public Bounds getbounds() {return _bounds;}
    	public List<Legs> getlegs() {return _legs;}
  
    	public void setsummary(String input_summary) { _summary = input_summary; }
    	public void setcopyrights(String input_copyright) { _copyrights = input_copyright; }
    	public void setwaypoint_order(List<Integer> input_order) {waypoint_order = input_order; }
    	public void setoverview_polyline(Overview_Polyline input_polyline) {_polyline = input_polyline; }
    	public void setwarnings(List<String> input_warnings) {_warnings = input_warnings; }
    	public void setbounds(Bounds input_bounds) {_bounds = input_bounds;}
    	public void setlegs(List<Legs> input_legs) {_legs = input_legs;}
    	
    }

    private Status _status;
    private List<Route> _routes = new ArrayList<Route>();

    public Status getStatus() { return _status; }
    public List<Route> getRoutes() {return _routes;}
    public List<GoogleLocation> getDirections()
    {
    	List<GoogleLocation> directions = new ArrayList<GoogleLocation>(); 			//List of directions to follow from Google
    	List<Steps> parsed_steps = new ArrayList<Steps>();				//Raw data parsed from google
    	
    	//Assumptions made below: (1) If there are multiple routes available, only pick the first one.
    	//						  (2) The complete list of directions will include all legs of the journey parsed from Google.
    	final int list_head = 0;
    	for (Legs temp_leg : _routes.get(list_head).getlegs())
    	{
    		parsed_steps.addAll(temp_leg.getsteps());
    	}
    	
    	//Create list of directions
    	for (int i = 0; i< parsed_steps.size(); i++)
    	{
    		GoogleLocation temp_location = parsed_steps.get(i).getstart_location();
    		directions.add(temp_location);
    	}
    	//Append final destination to the directions as well
    	directions.add(parsed_steps.get(parsed_steps.size()-1).getend_location());

    	return directions;
    }

    public void setStatus(Status input_status) { _status = input_status; }
    public void setRoutes(List<Route> input_routes) {_routes = input_routes; }
}