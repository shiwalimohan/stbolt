package edu.umich.sbolt.language;

import java.util.Map;

import sml.Identifier;

public interface LinguisticEntity {
	public void extractLinguisticComponents(String string, Map tagsToWords);
	public Identifier translateToSoarSpeak(Map<String, Object>tagsToWords, Identifier messageId);
}
