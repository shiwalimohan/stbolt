package com.soartech.bolt.evaluation;

public class ThreeByThreeConfig {
	private BoardLocation referenceObjLoc;
	private BoardLocation primaryObjLoc;
	
	public ThreeByThreeConfig(BoardLocation primary, BoardLocation reference) {
		primaryObjLoc = primary;
		referenceObjLoc = reference;
	}
	
	public ThreeByThreeConfig(int pr, int pc, int rr, int rc) {
		primaryObjLoc = new BoardLocation(pr, pc);
		referenceObjLoc = new BoardLocation(rr, rc);
	}

	public BoardLocation getReferenceObjLoc() {
		return referenceObjLoc;
	}

	public void setReferenceObjLoc(BoardLocation referenceObjLoc) {
		this.referenceObjLoc = referenceObjLoc;
	}

	public BoardLocation getPrimaryObjLoc() {
		return primaryObjLoc;
	}

	public void setPrimaryObjLoc(BoardLocation primaryObjLoc) {
		this.primaryObjLoc = primaryObjLoc;
	}
}
