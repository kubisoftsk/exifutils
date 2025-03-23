package sk.kubisoft.exifutils.core.analysis.device;

import java.util.List;
import java.util.Map;

public class DeviceProfile {

	private String name;

	private Map<String, String> tags;

	private List<DateTimeField> imageFields;

	private List<DateTimeField> videoFields;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
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
