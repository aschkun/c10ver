package gartham.c10ver.data.autosave;

import java.io.File;

import gartham.c10ver.data.JSONType;
import gartham.c10ver.utils.DataUtils;

public class JSONTypeSave extends JSONType {

	private final File saveLocation;

	public File getSaveLocation() {
		return saveLocation;
	}

	public JSONTypeSave(File saveLocation) {
		super(DataUtils.loadObj(saveLocation));
		this.saveLocation = saveLocation;
	}

	@Override
	public void change() {
		super.change();
		DataUtils.save(getProperties(), saveLocation);
	}
}
