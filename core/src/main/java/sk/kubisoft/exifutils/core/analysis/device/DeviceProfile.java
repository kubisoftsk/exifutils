package sk.kubisoft.exifutils.core.analysis.device;

import java.util.List;

public class DeviceProfile {

	private String model;

	private List<DateTimeField> imageFields;

	private List<DateTimeField> videoFields;

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public List<DateTimeField> getImageFields() {
		return imageFields;
	}

	public void setImageFields(List<DateTimeField> imageFields) {
		this.imageFields = imageFields;
	}

	public List<DateTimeField> getVideoFields() {
		return videoFields;
	}

	public void setVideoFields(List<DateTimeField> videoFields) {
		this.videoFields = videoFields;
	}
}
