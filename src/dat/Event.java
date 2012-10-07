package dat;


import java.util.UUID;

import dat.utils.TypedMap;
import dat.utils.FormattingUtils;

public class Event {

	private String id;
	
	private long time;
	
	private String type;
	
	private TypedMap attributes;
	
	
	/**
	 * Constructor
	 * 
	 * @param time
	 * @param type
	 * @param attributes
	 */
	public Event(String id, long time, String type, TypedMap attributes) {
		super();
		this.id = id;
		this.time = time;
		this.type = type;
		this.attributes = attributes;
	}

	public Event() {
		super();
		this.id = UUID.randomUUID().toString();
	}
	
	/**
	 * Constructor with random generated ID
	 * 
	 * @param time
	 * @param type
	 * @param attributes
	 */
	public Event(long time, String type, TypedMap attributes) {
		this(UUID.randomUUID().toString(),time,type,attributes);
	}
	
	/**
	 * Constructor with random generated ID
	 * @param time
	 * @param type
	 */
	public Event(long time, String type) {
		this(UUID.randomUUID().toString(),time,type);
	}

	public Event(String id, long time, String type) {
		this(id, time, type, new TypedMap());
	}

	
	/**
	 * Returns the id of the event
	 * 
	 * @return
	 */
	public String getId(){
		return this.id;
	}
	/**
	 * Get the time at which this event will be triggered.
	 * 
	 * @return the event's triggering time.
	 */
	public long getTime() {
		return time;
	}


	/**
	 * The event's type.
	 * 
	 * @return a String with the event type.
	 */
	public String getType() {
		return type;
	}



	public TypedMap getAttributes() {
		return attributes;
	}

    
	
	
	
	public void setId(String id) {
		this.id = id;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setAttributes(TypedMap attributes) {
		this.attributes = attributes;
	}

	public static void setFormat(String format) {
		Event.format = format;
	}





	private static String format = "{type=%s}{id=%s}{attributes=%s}";
	
	public String toString(){
		
		return String.format(format, type,id,FormattingUtils.mapToString(attributes));
	}
	
	
	
}
