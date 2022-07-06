package Helpers;

public class APIMethods {
	public final static String METHOD_WITHOUT_PETID = "/owners/{ownerId}/pets";
	public final static String METHOD_WITH_PETID = "/owners/{ownerId}/pets/{petId}";
	public final static String METHOD_VETS = "/vets";
	public final static String METHOD_VISIT = "/owners/{ownerId}/pets/{petId}/visits";
	public final static String METHOD_OWNER_WITHOUT_OWNERID = "/owners";
	public final static String METHOD_OWNER_WITH_OWNERID = "/owners/{ownerId}";
}
