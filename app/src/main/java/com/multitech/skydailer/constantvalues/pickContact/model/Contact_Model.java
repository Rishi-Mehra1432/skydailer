package com.multitech.skydailer.constantvalues.pickContact.model;

public class Contact_Model {

	// Getter and setter for contacts
	private String contactId, contactName, contactNumber, contactPhoto;

	public Contact_Model(String contactId, String contactName,
                         String contactNumber,  String contactPhoto ) {
		this.contactId = contactId;
		this.contactName = contactName;

		this.contactNumber = contactNumber;
		this.contactPhoto = contactPhoto;

	}

	public String getContactID() {
		return contactId;
	}

	public String getContactName() {
		return contactName;
	}


	public String getContactNumber() {
		return contactNumber;
	}

	public String getContactPhoto() {
		return contactPhoto;
	}

 }
