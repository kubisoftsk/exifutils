package sk.kubisoft.exifutils.core.config.model;

public class ExifUtilsConfiguration {

	private ExifToolConfig exifTool;

	private DateTimeConfig dateTime;

	private RenameConfig rename;

	public ExifToolConfig getExifTool() {
		return exifTool;
	}

	public void setExifTool(ExifToolConfig exifTool) {
		this.exifTool = exifTool;
	}

	public DateTimeConfig getDateTime() {
		return dateTime;
	}

	public void setDateTime(DateTimeConfig dateTime) {
		this.dateTime = dateTime;
	}

	public RenameConfig getRename() {
		return rename;
	}

	public void setRename(RenameConfig rename) {
		this.rename = rename;
	}
}