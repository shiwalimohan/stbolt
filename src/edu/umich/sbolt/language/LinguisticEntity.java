package edu.umich.sbolt.language;

import java.util.Map;

import sml.Identifier;

public abstract class LinguisticEntity {
	public abstract void extractLinguisticComponents(String string, Map<String, Object> tagsToWords);
	public abstract void translateToSoarSpeak(Identifier id, String connectingString);
}
