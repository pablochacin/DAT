package dat;


import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import dat.network.NodeAddress;
import dat.utils.TypedMap;
import dat.utils.FormattingUtils;

/**
 * 
 * @author Pablo Chacin
 *
 */
public class Message implements Serializable {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String algorithm;
	
	private String id;
	
	private String type;
	
	private NodeAddress sender;
	
	private NodeAddress destination;
	
	private TypedMap attributes;


	
	/**
	 * Constructor
	 * 
	 * @param id
	 * @param algorithm
	 * @param type
	 * @param sender
	 * @param attributes
	 */
	public Message(String id, String algorithm, String type,
			NodeAddress sender, NodeAddress destination,Map<String,Object> attributes) {

		this.id = id;
		this.algorithm = algorithm;
		this.type = type;
		this.sender = sender;
		this.destination = destination;
		this.attributes = new TypedMap();
		this.attributes.putAll(attributes);
	}

	/**
	 * Convenience constructor with minimum parameters used for creating custom Message subclasses 
	 * (see {@link Node#sendMessage(NodeAddress, Message)}
	 * 
	 * @param id
	 * @param type
	 * @param attributes
	 */
	public Message(String id, String type,TypedMap attributes){
		this.id = id;
		this.type = type;
		this.attributes = attributes;
		
	}
	
	/**
	 * Convenience constructor used for creating custom Message subclasses (see {@link Node#sendMessage(NodeAddress, Message)}
	 * 
	 */
	public Message(){
		this.id = UUID.randomUUID().toString();
		this.attributes = new TypedMap();
		this.type = this.getClass().getSimpleName();
	}
	
	public Message(String id, String type) {
		this(id,type,new TypedMap());
	}

	/**
	 * Returns the message's unique id
	 * 
	 * @return a String with the Id of the message
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the message type, which is algorithm dependent. By convention,the name
	 * takes the form of point separated qualifiers, starting with the name of the algorithm.
	 * 
	 * Example: algorithm.action.request, algorithm.action.reply.
	 * 
	 * @return a String with the type of the message
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns the network address of the node where the message was originated
	 * 
	 * @return the {@link NodeAddress} of the originator of the message
	 */
	public NodeAddress getSender() {
		return sender;
	}

	/**
	 * 
	 * @return a TypedMap with the attributes of the message
	 */
	public TypedMap getAttributes() {
		return attributes;
	}
	
	public String getAlgorithm(){
		return algorithm;
	}
	
	
	public NodeAddress getDestination(){
		return destination;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setSender(NodeAddress sender) {
		this.sender = sender;
	}

	public void setDestination(NodeAddress destination) {
		this.destination = destination;
	}

	/**
	 * Sets a group of attributes. Already existing attributes are preserved unless 
	 * override by new attributes.
	 * 
	 * @param attributes
	 */
	public void setAttributes(Map<String,Object> attributes) {
		this.attributes.putAll(attributes);
	}
		
	
	/**
	 * Format used to 
	 */
	private static String format = "{sender=%s}{destination=%s}{algorithm=%s}{type=%s}{id=%s}{attributes=%s}";
	
	public String toString(){
		
		String attributes = FormattingUtils.mapToString(getAttributes());

		return String.format(format, sender.getLocation(),destination.getLocation(),algorithm,type,id,attributes);

	}
	
	public Long getLong(String attribute){
		return attributes.getLong(attribute);
	}
	
	public void setLong(String attribute,Long value){
		attributes.putLong(attribute,value);
	}

	public Double getDouble(String attribute){
		return attributes.getDouble(attribute);
	}
	
	public void setDouble(String attribute,Double value){
		attributes.putDouble(attribute,value);
	}

	public Boolean getBoolean(String attribute){
		return attributes.getBoolean(attribute);
	}
	
	public void setBoolean(String attribute,Boolean value){
		attributes.putBoolean(attribute,value);
	}

	public String getString(String attribute){
		return attributes.getString(attribute);
	}
	
	public void setString(String attribute,String value){
		attributes.putString(attribute,value);
	}

	public Object getObject(String attribute){
		return attributes.getObject(attribute);
	}
	
	public void setObject(String attribute,Object value){
		attributes.putObject(attribute,value);
	}

	public Integer getInteger(String attribute){
		return attributes.getInteger(attribute);
	}
	
	public void setInteger(String attribute,Integer value){
		attributes.putInteger(attribute,value);
	}

	public byte[] getByteArray(String attribute){
		return attributes.getByteArray(attribute);
	}
	
	public void setByteArray(String attribute,byte[] value){
		attributes.putByteArray(attribute,value);
	}

}
