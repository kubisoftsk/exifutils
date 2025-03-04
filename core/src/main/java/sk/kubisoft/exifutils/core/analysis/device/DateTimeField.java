package sk.kubisoft.exifutils.core.analysis.device;

public class DateTimeField {

	private String dateField;

	private boolean localTime;

	private String offsetField;

	public String getDateField() {
		return dateField;
	}

	public void setDateField(String dateField) {
		this.dateField = dateField;
	}

	public boolean isLocalTime() {
		return localTime;
	}

	public void setLocalTime(boolean localTime) {
		this.localTime = localTime;
	}

	public String getOffsetField() {
		return offsetField;
	}

	public void setOffsetField(String offsetField) {
		this.offsetField = offsetField;
	}
}
