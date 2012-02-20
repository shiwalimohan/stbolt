package edu.umich.sbolt.language;

import java.util.Map;

import sml.Agent;
import sml.Identifier;

public interface LinguisticEntity {
	public void extractLinguisticComponents(String string, Map tagsToWords);
	public void translateToSoarSpeak(Identifier id);
}
